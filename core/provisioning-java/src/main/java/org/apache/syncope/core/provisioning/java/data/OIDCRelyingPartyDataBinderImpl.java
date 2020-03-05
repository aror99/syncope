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
package org.apache.syncope.core.provisioning.java.data;

import org.apache.syncope.common.lib.SyncopeClientException;
import org.apache.syncope.common.lib.access.to.AccessPolicyTO;
import org.apache.syncope.common.lib.authentication.policy.to.AuthenticationPolicyTO;
import org.apache.syncope.common.lib.to.client.OIDCRelyingPartyTO;
import org.apache.syncope.common.lib.types.ClientExceptionType;
import org.apache.syncope.core.persistence.api.dao.PolicyDAO;
import org.apache.syncope.core.persistence.api.dao.authentication.OIDCRelyingPartyDAO;
import org.apache.syncope.core.persistence.api.entity.EntityFactory;
import org.apache.syncope.core.persistence.api.entity.authentication.OIDCRelyingParty;
import org.apache.syncope.core.persistence.api.entity.policy.AccessPolicy;
import org.apache.syncope.core.persistence.api.entity.policy.AuthenticationPolicy;
import org.apache.syncope.core.provisioning.api.data.OIDCRelyingPartyDataBinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OIDCRelyingPartyDataBinderImpl implements OIDCRelyingPartyDataBinder {

    @Autowired
    private OIDCRelyingPartyDAO openIdConnectRelyingPartyDAO;

    @Autowired
    private EntityFactory entityFactory;

    @Autowired
    private PolicyDAO policyDAO;

    @Override
    public OIDCRelyingParty create(final OIDCRelyingPartyTO applicationTO) {
        return update(entityFactory.newEntity(OIDCRelyingParty.class), applicationTO);
    }

    @Override
    public OIDCRelyingParty update(
        final OIDCRelyingParty toBeUpdated,
        final OIDCRelyingPartyTO applicationTO) {

        OIDCRelyingParty application = openIdConnectRelyingPartyDAO.save(toBeUpdated);

        application.setDescription(applicationTO.getDescription());
        application.setName(applicationTO.getName());
        application.setClientSecret(applicationTO.getClientSecret());
        application.setClientId(applicationTO.getClientId());
        application.setRedirectUris(applicationTO.getRedirectUris());

        AuthenticationPolicy authenticationPolicy = policyDAO.
                find(applicationTO.getAuthenticationPolicy().getKey());
        if (authenticationPolicy == null) {
            SyncopeClientException sce = SyncopeClientException.build(ClientExceptionType.InvalidEntity);
            sce.getElements().add("Unable to locate authentication policy "
                    + applicationTO.getAuthenticationPolicy().getKey());
            throw sce;
        }
        application.setAuthenticationPolicy(authenticationPolicy);

        AccessPolicy accessPolicy = policyDAO.find(applicationTO.getAccessPolicy().getKey());
        application.setAccessPolicy(accessPolicy);

        return application;
    }

    @Override
    public OIDCRelyingPartyTO getClientApplicationTO(final OIDCRelyingParty rp) {
        OIDCRelyingPartyTO applicationTO = new OIDCRelyingPartyTO();

        applicationTO.setKey(rp.getKey());
        applicationTO.setDescription(rp.getDescription());
        applicationTO.setClientId(rp.getClientId());
        applicationTO.setClientSecret(rp.getClientSecret());
        applicationTO.setRedirectUris(rp.getRedirectUris());
        applicationTO.setName(rp.getName());

        AuthenticationPolicyTO authenticationPolicyTO = new AuthenticationPolicyTO();
        authenticationPolicyTO.setDescription(rp.getAuthenticationPolicy().getDescription());
        authenticationPolicyTO.setKey(rp.getAuthenticationPolicy().getKey());
        applicationTO.setAuthenticationPolicy(authenticationPolicyTO);

        if (rp.getAccessPolicy() != null) {
            AccessPolicyTO accessPolicyTO = new AccessPolicyTO();
            accessPolicyTO.setDescription(rp.getAccessPolicy().getDescription());
            accessPolicyTO.setKey(rp.getAccessPolicy().getKey());
            applicationTO.setAccessPolicy(accessPolicyTO);
        }
        return applicationTO;
    }
}