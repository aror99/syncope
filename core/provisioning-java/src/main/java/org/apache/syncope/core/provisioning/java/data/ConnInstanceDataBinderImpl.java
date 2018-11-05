/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.syncope.core.provisioning.java.data;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.syncope.common.lib.SyncopeClientException;
import org.apache.syncope.common.lib.to.ConnInstanceHistoryConfTO;
import org.apache.syncope.common.lib.to.ConnInstanceTO;
import org.apache.syncope.common.lib.to.ConnPoolConfTO;
import org.apache.syncope.common.lib.types.ClientExceptionType;
import org.apache.syncope.common.lib.types.ConnConfPropSchema;
import org.apache.syncope.common.lib.types.ConnConfProperty;
import org.apache.syncope.core.persistence.api.dao.ConfDAO;
import org.apache.syncope.core.persistence.api.dao.ConnInstanceDAO;
import org.apache.syncope.core.persistence.api.dao.ConnInstanceHistoryConfDAO;
import org.apache.syncope.core.persistence.api.dao.NotFoundException;
import org.apache.syncope.core.persistence.api.dao.RealmDAO;
import org.apache.syncope.core.persistence.api.entity.ConnInstance;
import org.apache.syncope.core.persistence.api.entity.ConnInstanceHistoryConf;
import org.apache.syncope.core.persistence.api.entity.EntityFactory;
import org.apache.syncope.core.persistence.api.entity.Realm;
import org.apache.syncope.core.provisioning.api.ConnIdBundleManager;
import org.apache.syncope.core.provisioning.api.data.ConnInstanceDataBinder;
import org.apache.syncope.core.provisioning.api.utils.ConnPoolConfUtils;
import org.identityconnectors.framework.api.ConfigurationProperties;
import org.identityconnectors.framework.api.ConfigurationProperty;
import org.identityconnectors.framework.impl.api.ConfigurationPropertyImpl;
import org.apache.syncope.core.spring.BeanUtils;
import org.apache.syncope.core.spring.security.AuthContextUtils;
import org.identityconnectors.framework.api.ConnectorInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConnInstanceDataBinderImpl implements ConnInstanceDataBinder {

    private static final String[] IGNORE_PROPERTIES = { "key", "poolConf", "location", "adminRealm", "conf" };

    @Autowired
    private ConnIdBundleManager connIdBundleManager;

    @Autowired
    private ConnInstanceDAO connInstanceDAO;

    @Autowired
    private ConnInstanceHistoryConfDAO connInstanceHistoryConfDAO;

    @Autowired
    private RealmDAO realmDAO;

    @Autowired
    private ConfDAO confDAO;

    @Autowired
    private EntityFactory entityFactory;

    @Override
    public ConnInstance getConnInstance(final ConnInstanceTO connInstanceTO) {
        SyncopeClientException sce = SyncopeClientException.build(ClientExceptionType.RequiredValuesMissing);

        if (connInstanceTO.getLocation() == null) {
            sce.getElements().add("location");
        }

        if (connInstanceTO.getBundleName() == null) {
            sce.getElements().add("bundlename");
        }

        if (connInstanceTO.getVersion() == null) {
            sce.getElements().add("bundleversion");
        }

        if (connInstanceTO.getConnectorName() == null) {
            sce.getElements().add("connectorname");
        }

        if (connInstanceTO.getConf().isEmpty()) {
            sce.getElements().add("configuration");
        }

        ConnInstance connInstance = entityFactory.newEntity(ConnInstance.class);

        BeanUtils.copyProperties(connInstanceTO, connInstance, IGNORE_PROPERTIES);
        if (connInstanceTO.getAdminRealm() != null) {
            connInstance.setAdminRealm(realmDAO.findByFullPath(connInstanceTO.getAdminRealm()));
        }
        if (connInstance.getAdminRealm() == null) {
            sce.getElements().add("Invalid or null realm specified: " + connInstanceTO.getAdminRealm());
        }
        if (connInstanceTO.getLocation() != null) {
            connInstance.setLocation(connInstanceTO.getLocation());
        }
        connInstance.setConf(connInstanceTO.getConf());
        if (connInstanceTO.getPoolConf() != null) {
            connInstance.setPoolConf(
                    ConnPoolConfUtils.getConnPoolConf(connInstanceTO.getPoolConf(), entityFactory.newConnPoolConf()));
        }

        // Throw exception if there is at least one element set
        if (!sce.isEmpty()) {
            throw sce;
        }

        return connInstance;
    }

    @Override
    public ConnInstance update(final ConnInstanceTO connInstanceTO) {
        ConnInstance connInstance = connInstanceDAO.authFind(connInstanceTO.getKey());
        if (connInstance == null) {
            throw new NotFoundException("Connector '" + connInstanceTO.getKey() + "'");
        }

        ConnInstanceTO current = getConnInstanceTO(connInstance);
        if (!current.equals(connInstanceTO)) {
            // 1. save the current configuration, before update
            ConnInstanceHistoryConf connInstanceHistoryConf = entityFactory.newEntity(ConnInstanceHistoryConf.class);
            connInstanceHistoryConf.setCreator(AuthContextUtils.getUsername());
            connInstanceHistoryConf.setCreation(new Date());
            connInstanceHistoryConf.setEntity(connInstance);
            connInstanceHistoryConf.setConf(current);
            connInstanceHistoryConfDAO.save(connInstanceHistoryConf);

            // 2. ensure the maximum history size is not exceeded
            List<ConnInstanceHistoryConf> history = connInstanceHistoryConfDAO.findByEntity(connInstance);
            long maxHistorySize = confDAO.find("connector.conf.history.size", 10L);
            if (maxHistorySize < history.size()) {
                // always remove the last item since history was obtained  by a query with ORDER BY creation DESC
                for (int i = 0; i < history.size() - maxHistorySize; i++) {
                    connInstanceHistoryConfDAO.delete(history.get(history.size() - 1).getKey());
                }
            }
        }

        // 3. actual update
        connInstance.getCapabilities().clear();
        connInstance.getCapabilities().addAll(connInstanceTO.getCapabilities());

        if (connInstanceTO.getAdminRealm() != null) {
            Realm realm = realmDAO.findByFullPath(connInstanceTO.getAdminRealm());
            if (realm == null) {
                SyncopeClientException sce = SyncopeClientException.build(ClientExceptionType.InvalidRealm);
                sce.getElements().add("Invalid or null realm specified: " + connInstanceTO.getAdminRealm());
                throw sce;
            }
            connInstance.setAdminRealm(realm);
        }

        if (connInstanceTO.getLocation() != null) {
            connInstance.setLocation(connInstanceTO.getLocation());
        }

        if (connInstanceTO.getBundleName() != null) {
            connInstance.setBundleName(connInstanceTO.getBundleName());
        }

        if (connInstanceTO.getVersion() != null) {
            connInstance.setVersion(connInstanceTO.getVersion());
        }

        if (connInstanceTO.getConnectorName() != null) {
            connInstance.setConnectorName(connInstanceTO.getConnectorName());
        }

        if (connInstanceTO.getConf() != null && !connInstanceTO.getConf().isEmpty()) {
            connInstance.setConf(connInstanceTO.getConf());
        }

        if (connInstanceTO.getDisplayName() != null) {
            connInstance.setDisplayName(connInstanceTO.getDisplayName());
        }

        if (connInstanceTO.getConnRequestTimeout() != null) {
            connInstance.setConnRequestTimeout(connInstanceTO.getConnRequestTimeout());
        }

        if (connInstanceTO.getPoolConf() == null) {
            connInstance.setPoolConf(null);
        } else {
            connInstance.setPoolConf(
                    ConnPoolConfUtils.getConnPoolConf(connInstanceTO.getPoolConf(), entityFactory.newConnPoolConf()));
        }

        try {
            connInstance = connInstanceDAO.save(connInstance);
        } catch (Exception e) {
            SyncopeClientException sce = SyncopeClientException.build(ClientExceptionType.InvalidConnInstance);
            sce.getElements().add(e.getMessage());
            throw sce;
        }

        return connInstance;
    }

    @Override
    public ConnConfPropSchema build(final ConfigurationProperty property) {
        ConnConfPropSchema connConfPropSchema = new ConnConfPropSchema();

        connConfPropSchema.setName(property.getName());
        connConfPropSchema.setDisplayName(property.getDisplayName(property.getName()));
        connConfPropSchema.setHelpMessage(property.getHelpMessage(property.getName()));
        connConfPropSchema.setRequired(property.isRequired());
        connConfPropSchema.setType(property.getType().getName());
        connConfPropSchema.setOrder(((ConfigurationPropertyImpl) property).getOrder());
        connConfPropSchema.setConfidential(property.isConfidential());

        if (property.getValue() != null) {
            if (property.getValue().getClass().isArray()) {
                connConfPropSchema.getDefaultValues().addAll(Arrays.asList((Object[]) property.getValue()));
            } else if (property.getValue() instanceof Collection<?>) {
                connConfPropSchema.getDefaultValues().addAll((Collection<?>) property.getValue());
            } else {
                connConfPropSchema.getDefaultValues().add(property.getValue());
            }
        }

        return connConfPropSchema;
    }

    @Override
    public ConnInstanceTO getConnInstanceTO(final ConnInstance connInstance) {
        ConnInstanceTO connInstanceTO = new ConnInstanceTO();

        Pair<URI, ConnectorInfo> info = connIdBundleManager.getConnectorInfo(connInstance);
        BeanUtils.copyProperties(connInstance, connInstanceTO, IGNORE_PROPERTIES);
        connInstanceTO.setKey(connInstance.getKey());
        connInstanceTO.setAdminRealm(connInstance.getAdminRealm().getFullPath());
        connInstanceTO.setLocation(info.getLeft().toASCIIString());
        connInstanceTO.getConf().addAll(connInstance.getConf());
        // refresh stored properties in the given connInstance with direct information from underlying connector
        ConfigurationProperties properties = connIdBundleManager.getConfigurationProperties(info.getRight());
        properties.getPropertyNames().forEach(propName -> {
            ConnConfPropSchema schema = build(properties.getProperty(propName));

            Optional<ConnConfProperty> property = connInstanceTO.getConf(propName);
            if (!property.isPresent()) {
                property = Optional.of(new ConnConfProperty());
                connInstanceTO.getConf().add(property.get());
            }
            property.get().setSchema(schema);
        });
        Collections.sort(connInstanceTO.getConf());

        // pool configuration
        if (connInstance.getPoolConf() != null
                && (connInstance.getPoolConf().getMaxIdle() != null
                || connInstance.getPoolConf().getMaxObjects() != null
                || connInstance.getPoolConf().getMaxWait() != null
                || connInstance.getPoolConf().getMinEvictableIdleTimeMillis() != null
                || connInstance.getPoolConf().getMinIdle() != null)) {

            ConnPoolConfTO poolConf = new ConnPoolConfTO();
            BeanUtils.copyProperties(connInstance.getPoolConf(), poolConf);
            connInstanceTO.setPoolConf(poolConf);
        }

        return connInstanceTO;
    }

    @Override
    public ConnInstanceHistoryConfTO getConnInstanceHistoryConfTO(final ConnInstanceHistoryConf history) {
        ConnInstanceHistoryConfTO historyTO = new ConnInstanceHistoryConfTO();
        historyTO.setKey(history.getKey());
        historyTO.setCreator(history.getCreator());
        historyTO.setCreation(history.getCreation());
        historyTO.setConnInstanceTO(history.getConf());

        return historyTO;
    }
}
