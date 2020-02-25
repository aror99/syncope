/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.syncope.core.logic;

import org.apache.syncope.common.lib.to.ClientApplicationTO;
import org.apache.syncope.common.lib.types.IdRepoEntitlement;
import org.apache.syncope.core.persistence.api.dao.NotFoundException;
import org.apache.syncope.core.persistence.api.dao.authentication.SAML2ServiceProviderDAO;
import org.apache.syncope.core.persistence.api.entity.authentication.SAML2ServiceProvider;
import org.apache.syncope.core.provisioning.api.data.SAML2ServiceProviderDataBinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SAML2ServiceProviderLogic extends AbstractClientApplicationLogic {

    @Autowired
    private SAML2ServiceProviderDAO saml2ServiceProviderDAO;

    @Autowired
    private SAML2ServiceProviderDataBinder binder;

    @Override
    @PreAuthorize("hasRole('" + IdRepoEntitlement.SAML2_SERVICE_PROVIDER_DELETE + "')")
    public ClientApplicationTO delete(final String key) {
        SAML2ServiceProvider application = saml2ServiceProviderDAO.find(key);
        if (application == null) {
            LOG.error("Could not find application '" + key + '\'');

            throw new NotFoundException(key);
        }

        ClientApplicationTO deleted = binder.getClientApplicationTO(application);
        saml2ServiceProviderDAO.delete(key);
        return deleted;
    }

    @Override
    @PreAuthorize("hasRole('" + IdRepoEntitlement.SAML2_SERVICE_PROVIDER_LIST + "')")
    @Transactional(readOnly = true)
    public List<ClientApplicationTO> list() {
        return saml2ServiceProviderDAO.findAll().stream().map(binder::getClientApplicationTO).collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('" + IdRepoEntitlement.SAML2_SERVICE_PROVIDER_READ + "')")
    @Transactional(readOnly = true)
    @Override
    public ClientApplicationTO read(final String key) {
        SAML2ServiceProvider application = saml2ServiceProviderDAO.find(key);
        if (application == null) {
            LOG.error("Could not find application '" + key + '\'');

            throw new NotFoundException(key);
        }

        return binder.getClientApplicationTO(application);
    }

    @Override
    @PreAuthorize("hasRole('" + IdRepoEntitlement.SAML2_SERVICE_PROVIDER_CREATE + "')")
    public ClientApplicationTO create(final ClientApplicationTO applicationTO) {
        return binder.getClientApplicationTO(saml2ServiceProviderDAO.save(binder.create(applicationTO)));
    }

    @Override
    @PreAuthorize("hasRole('" + IdRepoEntitlement.APPLICATION_UPDATE + "')")
    public ClientApplicationTO update(final ClientApplicationTO applicationTO) {
        SAML2ServiceProvider application = saml2ServiceProviderDAO.find(applicationTO.getKey());
        if (application == null) {
            LOG.error("Could not find application '" + applicationTO.getKey() + '\'');
            throw new NotFoundException(applicationTO.getKey());
        }

        return binder.getClientApplicationTO(saml2ServiceProviderDAO.save(binder.update(application, applicationTO)));
    }
}