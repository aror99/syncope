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
package org.apache.syncope.fit.core;

import static org.apache.syncope.fit.AbstractITCase.getObject;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.syncope.common.lib.SyncopeClientException;
import org.apache.syncope.common.lib.to.AccessPolicyTO;
import org.apache.syncope.common.lib.to.AuthPolicyTO;
import org.apache.syncope.common.lib.to.client.ClientAppTO;
import org.apache.syncope.common.lib.to.client.OIDCRPTO;
import org.apache.syncope.common.lib.to.client.SAML2SPTO;
import org.apache.syncope.common.lib.types.ClientAppType;
import org.apache.syncope.common.lib.types.OIDCSubjectType;
import org.apache.syncope.common.lib.types.PolicyType;
import org.apache.syncope.common.lib.types.SAML2SPNameId;
import org.apache.syncope.common.rest.api.service.ClientAppService;
import org.apache.syncope.fit.AbstractITCase;
import org.junit.jupiter.api.Test;

public class ClientAppITCase extends AbstractITCase {

    @Test
    public void createSAML2SP() {
        createClientApp(ClientAppType.SAML2SP, buildSAML2SP());
    }

    @Test
    public void readSAML2SP() {
        SAML2SPTO samlSpTO = buildSAML2SP();
        samlSpTO = createClientApp(ClientAppType.SAML2SP, samlSpTO);

        SAML2SPTO found = clientAppService.read(ClientAppType.SAML2SP, samlSpTO.getKey());
        assertNotNull(found);
        assertFalse(StringUtils.isBlank(found.getEntityId()));
        assertFalse(StringUtils.isBlank(found.getMetadataLocation()));
        assertTrue(found.isEncryptAssertions());
        assertTrue(found.isEncryptionOptional());
        assertNotNull(found.getRequiredNameIdFormat());
        assertNotNull(found.getAccessPolicy());
        assertNotNull(found.getAuthPolicy());
    }

    @Test
    public void updateSAML2SP() {
        SAML2SPTO samlSpTO = buildSAML2SP();
        samlSpTO = createClientApp(ClientAppType.SAML2SP, samlSpTO);

        AccessPolicyTO accessPolicyTO = new AccessPolicyTO();
        accessPolicyTO.setKey("NewAccessPolicyTest_" + getUUIDString());
        accessPolicyTO.setDescription("New Access policy");
        accessPolicyTO = createPolicy(PolicyType.ACCESS, accessPolicyTO);
        assertNotNull(accessPolicyTO);

        samlSpTO.setEntityId("newEntityId");
        samlSpTO.setAccessPolicy(accessPolicyTO.getKey());

        clientAppService.update(ClientAppType.SAML2SP, samlSpTO);
        SAML2SPTO updated = clientAppService.read(ClientAppType.SAML2SP, samlSpTO.getKey());

        assertNotNull(updated);
        assertEquals("newEntityId", updated.getEntityId());
        assertNotNull(updated.getAccessPolicy());
    }

    @Test
    public void deleteSAML2SP() {
        SAML2SPTO samlSpTO = buildSAML2SP();
        samlSpTO = createClientApp(ClientAppType.SAML2SP, samlSpTO);

        clientAppService.delete(ClientAppType.SAML2SP, samlSpTO.getKey());

        try {
            clientAppService.read(ClientAppType.SAML2SP, samlSpTO.getKey());
            fail("This should not happen");
        } catch (SyncopeClientException e) {
            assertNotNull(e);
        }
    }

    private SAML2SPTO buildSAML2SP() {
        AuthPolicyTO authPolicyTO = new AuthPolicyTO();
        authPolicyTO.setKey("AuthPolicyTest_" + getUUIDString());
        authPolicyTO.setDescription("Authentication Policy");
        authPolicyTO = createPolicy(PolicyType.AUTH, authPolicyTO);
        assertNotNull(authPolicyTO);

        AccessPolicyTO accessPolicyTO = new AccessPolicyTO();
        accessPolicyTO.setKey("AccessPolicyTest_" + getUUIDString());
        accessPolicyTO.setDescription("Access policy");
        accessPolicyTO = createPolicy(PolicyType.ACCESS, accessPolicyTO);
        assertNotNull(accessPolicyTO);

        SAML2SPTO saml2spto = new SAML2SPTO();
        saml2spto.setName("ExampleSAML2SP_" + getUUIDString());
        saml2spto.setDescription("Example SAML 2.0 service provider");
        saml2spto.setEntityId("SAML2SPEntityId_" + getUUIDString());
        saml2spto.setMetadataLocation("file:./test.xml");
        saml2spto.setRequiredNameIdFormat(SAML2SPNameId.EMAIL_ADDRESS);
        saml2spto.setEncryptionOptional(true);
        saml2spto.setEncryptAssertions(true);

        saml2spto.setAuthPolicy(authPolicyTO.getKey());
        saml2spto.setAccessPolicy(accessPolicyTO.getKey());

        return saml2spto;
    }

    @Test
    public void createOIDCRP() {
        createClientApp(ClientAppType.OIDCRP, buildOIDCRP());
    }

    @Test
    public void readOIDCRP() {
        OIDCRPTO oidcrpTO = buildOIDCRP();
        oidcrpTO = createClientApp(ClientAppType.OIDCRP, oidcrpTO);

        OIDCRPTO found = clientAppService.read(ClientAppType.OIDCRP, oidcrpTO.getKey());
        assertNotNull(found);
        assertFalse(StringUtils.isBlank(found.getClientId()));
        assertFalse(StringUtils.isBlank(found.getClientSecret()));
        assertNotNull(found.getSubjectType());
        assertFalse(found.getSupportedGrantTypes().isEmpty());
        assertFalse(found.getSupportedResponseTypes().isEmpty());
        assertNotNull(found.getAccessPolicy());
        assertNotNull(found.getAuthPolicy());
    }

    @Test
    public void updateOIDCRP() {
        OIDCRPTO oidcrpTO = buildOIDCRP();
        oidcrpTO = createClientApp(ClientAppType.OIDCRP, oidcrpTO);

        AccessPolicyTO accessPolicyTO = new AccessPolicyTO();
        accessPolicyTO.setKey("NewAccessPolicyTest_" + getUUIDString());
        accessPolicyTO.setDescription("New Access policy");
        accessPolicyTO = createPolicy(PolicyType.ACCESS, accessPolicyTO);
        assertNotNull(accessPolicyTO);

        oidcrpTO.setClientId("newClientId");
        oidcrpTO.setAccessPolicy(accessPolicyTO.getKey());

        clientAppService.update(ClientAppType.OIDCRP, oidcrpTO);
        OIDCRPTO updated = clientAppService.read(ClientAppType.OIDCRP, oidcrpTO.getKey());

        assertNotNull(updated);
        assertEquals("newClientId", updated.getClientId());
        assertNotNull(updated.getAccessPolicy());
    }

    @Test
    public void delete() {
        OIDCRPTO oidcrpTO = buildOIDCRP();
        oidcrpTO = createClientApp(ClientAppType.OIDCRP, oidcrpTO);

        clientAppService.delete(ClientAppType.OIDCRP, oidcrpTO.getKey());

        try {
            clientAppService.read(ClientAppType.OIDCRP, oidcrpTO.getKey());
            fail("This should not happen");
        } catch (SyncopeClientException e) {
            assertNotNull(e);
        }
    }

    private OIDCRPTO buildOIDCRP() {
        AuthPolicyTO authPolicyTO = new AuthPolicyTO();
        authPolicyTO.setKey("AuthPolicyTest_" + getUUIDString());
        authPolicyTO.setDescription("Authentication Policy");
        authPolicyTO = createPolicy(PolicyType.AUTH, authPolicyTO);
        assertNotNull(authPolicyTO);

        AccessPolicyTO accessPolicyTO = new AccessPolicyTO();
        accessPolicyTO.setKey("AccessPolicyTest_" + getUUIDString());
        accessPolicyTO.setDescription("Access policy");
        accessPolicyTO = createPolicy(PolicyType.ACCESS, accessPolicyTO);
        assertNotNull(accessPolicyTO);

        OIDCRPTO oidcrpTO = new OIDCRPTO();
        oidcrpTO.setName("ExampleRP_" + getUUIDString());
        oidcrpTO.setDescription("Example OIDC RP application");
        oidcrpTO.setClientId("clientId_" + getUUIDString());
        oidcrpTO.setClientSecret("secret");
        oidcrpTO.setSubjectType(OIDCSubjectType.PUBLIC);
        oidcrpTO.getSupportedGrantTypes().add("something");
        oidcrpTO.getSupportedResponseTypes().add("something");

        oidcrpTO.setAuthPolicy(authPolicyTO.getKey());
        oidcrpTO.setAccessPolicy(accessPolicyTO.getKey());

        return oidcrpTO;
    }

    @SuppressWarnings("unchecked")
    private <T extends ClientAppTO> T createClientApp(final ClientAppType type, final T clientAppTO) {
        Response response = clientAppService.create(type, clientAppTO);
        if (response.getStatusInfo().getStatusCode() != Response.Status.CREATED.getStatusCode()) {
            Exception ex = clientFactory.getExceptionMapper().fromResponse(response);
            if (ex != null) {
                throw (RuntimeException) ex;
            }
        }
        return (T) getObject(response.getLocation(), ClientAppService.class, clientAppTO.getClass());
    }
}
