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

import org.apache.syncope.common.lib.SyncopeClientException;
import org.apache.syncope.common.lib.to.RegisteredClientAppTO;
import org.apache.syncope.common.lib.to.client.ClientAppTO;
import org.apache.syncope.common.lib.types.ClientExceptionType;
import org.apache.syncope.core.persistence.api.dao.PolicyDAO;
import org.apache.syncope.core.persistence.api.entity.policy.AccessPolicy;
import org.apache.syncope.core.persistence.api.entity.policy.AttrReleasePolicy;
import org.apache.syncope.core.persistence.api.entity.policy.AuthPolicy;
import org.apache.syncope.core.persistence.api.entity.policy.Policy;
import org.apache.syncope.core.provisioning.api.data.PolicyDataBinder;
import org.apache.syncope.core.provisioning.api.data.RegisteredClientAppBinder;
import org.apache.syncope.core.spring.ImplementationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RegisteredClientAppBinderImpl implements RegisteredClientAppBinder {

    private static final Logger LOG = LoggerFactory.getLogger(RegisteredClientAppBinder.class);

    @Autowired
    private PolicyDAO policyDAO;

    @Autowired
    private PolicyDataBinder policyDataBinder;

    @Override
    public RegisteredClientAppTO getRegisteredClientAppTO(final ClientAppTO clientAppTO) {
        RegisteredClientAppTO registeredClientAppTO = new RegisteredClientAppTO();
        registeredClientAppTO.setClientAppTO(clientAppTO);

        if (clientAppTO.getAuthPolicy() == null) {
            clientAppTO.setAuthPolicy(null);
        } else {
            Policy policy = policyDAO.find(clientAppTO.getAuthPolicy());
            if (policy instanceof AuthPolicy) {
                registeredClientAppTO.setAuthPolicyTO(policyDataBinder.getPolicyTO(policy));
            } else {
                SyncopeClientException sce = SyncopeClientException.build(ClientExceptionType.InvalidPolicy);
                sce.getElements().add("Expected " + AuthPolicy.class.getSimpleName()
                        + ", found " + policy.getClass().getSimpleName());
                throw sce;
            }
        }

        if (clientAppTO.getAccessPolicy() == null) {
            clientAppTO.setAccessPolicy(null);
        } else {
            Policy policy = policyDAO.find(clientAppTO.getAccessPolicy());
            if (policy instanceof AccessPolicy) {
                try {
                    registeredClientAppTO.setAccessPolicyConf(ImplementationManager.build(((AccessPolicy) policy).
                            getConfigurations().get(0)));
                } catch (Exception e) {
                    LOG.error("While building {}", ((AccessPolicy) policy).getConfigurations().get(0), e);
                }
            } else {
                SyncopeClientException sce = SyncopeClientException.build(ClientExceptionType.InvalidPolicy);
                sce.getElements().add("Expected " + AccessPolicy.class.getSimpleName()
                        + ", found " + policy.getClass().getSimpleName());
                throw sce;
            }
        }

        if (clientAppTO.getAttrReleasePolicy() == null) {
            clientAppTO.setAttrReleasePolicy(null);
        } else {
            Policy policy = policyDAO.find(clientAppTO.getAttrReleasePolicy());
            if (policy instanceof AttrReleasePolicy) {
                try {
                    registeredClientAppTO.setAttrReleasePolicyConf(ImplementationManager.build(
                            ((AttrReleasePolicy) policy).getConfigurations().get(0)));
                } catch (Exception e) {
                    LOG.error("While building {}", ((AttrReleasePolicy) policy).getConfigurations().get(0), e);
                }

            } else {
                SyncopeClientException sce = SyncopeClientException.build(ClientExceptionType.InvalidPolicy);
                sce.getElements().add("Expected " + AttrReleasePolicy.class.getSimpleName()
                        + ", found " + policy.getClass().getSimpleName());
                throw sce;
            }
        }

        return registeredClientAppTO;
    }

}
