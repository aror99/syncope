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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.syncope.common.lib.SyncopeClientException;
import org.apache.syncope.fit.AbstractITCase;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.syncope.common.lib.auth.AuthModuleConf;
import org.apache.syncope.common.lib.auth.GoogleMfaAuthModuleConf;
import org.apache.syncope.common.lib.auth.JDBCAuthModuleConf;
import org.apache.syncope.common.lib.auth.JaasAuthModuleConf;
import org.apache.syncope.common.lib.auth.LDAPAuthModuleConf;
import org.apache.syncope.common.lib.auth.OIDCAuthModuleConf;
import org.apache.syncope.common.lib.auth.RadiusAuthModuleConf;
import org.apache.syncope.common.lib.auth.SAML2IdPAuthModuleConf;
import org.apache.syncope.common.lib.auth.StaticAuthModuleConf;
import org.apache.syncope.common.lib.auth.SyncopeAuthModuleConf;
import org.apache.syncope.common.lib.auth.U2FAuthModuleConf;
import org.apache.syncope.common.lib.to.AuthModuleTO;

public class AuthModuleITCase extends AbstractITCase {

    private enum AuthModuleSupportedType {
        GOOGLE_MFA,
        SAML2_IDP,
        STATIC,
        SYNCOPE,
        LDAP,
        JAAS,
        JDBC,
        U2F,
        RADIUS,
        OIDC;

    };

    private static AuthModuleTO buildAuthModuleTO(final AuthModuleSupportedType type) {
        AuthModuleTO authModuleTO = new AuthModuleTO();
        authModuleTO.setName("Test" + type + "AuthenticationModule" + getUUIDString());
        authModuleTO.setDescription("A test " + type + " Authentication Module");
        AuthModuleConf conf;

        switch (type) {
            case LDAP:
                conf = new LDAPAuthModuleConf();
                LDAPAuthModuleConf.class.cast(conf).setName("TestConf" + getUUIDString());
                LDAPAuthModuleConf.class.cast(conf).setBaseDn("dc=example,dc=org");
                LDAPAuthModuleConf.class.cast(conf).setSearchFilter("cn={user}");
                LDAPAuthModuleConf.class.cast(conf).setSubtreeSearch(true);
                LDAPAuthModuleConf.class.cast(conf).setLdapUrl("ldap://localhost:1389");
                LDAPAuthModuleConf.class.cast(conf).setUserIdAttribute("uid");
                LDAPAuthModuleConf.class.cast(conf).setBaseDn("cn=Directory Manager,dc=example,dc=org");
                LDAPAuthModuleConf.class.cast(conf).setBindCredential("Password");
                break;

            case GOOGLE_MFA:
                conf = new GoogleMfaAuthModuleConf();
                GoogleMfaAuthModuleConf.class.cast(conf).setName("TestConf" + getUUIDString());
                GoogleMfaAuthModuleConf.class.cast(conf).setCodeDigits(6);
                GoogleMfaAuthModuleConf.class.cast(conf).setIssuer("SyncopeTest");
                GoogleMfaAuthModuleConf.class.cast(conf).setLabel("Syncope");
                GoogleMfaAuthModuleConf.class.cast(conf).setTimeStepSize(30);
                GoogleMfaAuthModuleConf.class.cast(conf).setWindowSize(3);
                break;

            case JAAS:
                conf = new JaasAuthModuleConf();
                JaasAuthModuleConf.class.cast(conf).setName("TestConf" + getUUIDString());
                JaasAuthModuleConf.class.cast(conf).setKerberosKdcSystemProperty("sample-value");
                JaasAuthModuleConf.class.cast(conf).setKerberosRealmSystemProperty("sample-value");
                JaasAuthModuleConf.class.cast(conf).setLoginConfigType("JavaLoginConfig");
                JaasAuthModuleConf.class.cast(conf).setRealm("SYNCOPE");
                JaasAuthModuleConf.class.cast(conf).setLoginConfigurationFile("/opt/jaas/login.conf");
                break;

            case JDBC:
                conf = new JDBCAuthModuleConf();
                JDBCAuthModuleConf.class.cast(conf).setName("TestConf" + getUUIDString());
                JDBCAuthModuleConf.class.cast(conf).setSql("SELECT * FROM table WHERE name=?");
                JDBCAuthModuleConf.class.cast(conf).setFieldPassword("password");
                JDBCAuthModuleConf.class.cast(conf).getPrincipalAttributeList().addAll(
                        List.of("sn", "cn:commonName", "givenName"));
                break;

            case OIDC:
                conf = new OIDCAuthModuleConf();
                OIDCAuthModuleConf.class.cast(conf).setName("TestConf" + getUUIDString());
                OIDCAuthModuleConf.class.cast(conf).setId("OIDCTestId");
                OIDCAuthModuleConf.class.cast(conf).setDiscoveryUri("www.testurl.com");
                OIDCAuthModuleConf.class.cast(conf).setUserIdAttribute("username");
                OIDCAuthModuleConf.class.cast(conf).setResponseType("code");
                OIDCAuthModuleConf.class.cast(conf).setScope("openid email profile");
                break;

            case SAML2_IDP:
                conf = new SAML2IdPAuthModuleConf();
                SAML2IdPAuthModuleConf.class.cast(conf).setName("TestConf" + getUUIDString());
                SAML2IdPAuthModuleConf.class.cast(conf).setServiceProviderEntityId("testEntityId");
                SAML2IdPAuthModuleConf.class.cast(conf).setProviderName("testProviderName");
                SAML2IdPAuthModuleConf.class.cast(conf).setServiceProviderMetadataPath(
                        "file:/etc/metadata");
                break;

            case SYNCOPE:
                conf = new SyncopeAuthModuleConf();
                SyncopeAuthModuleConf.class.cast(conf).setName("TestConf" + getUUIDString());
                SyncopeAuthModuleConf.class.cast(conf).setDomain("Master");
                SyncopeAuthModuleConf.class.cast(conf).setUrl("http://mydomain.com/syncope/rest");
                break;

            case U2F:
                conf = new U2FAuthModuleConf();
                U2FAuthModuleConf.class.cast(conf).setName("TestConf" + getUUIDString());
                U2FAuthModuleConf.class.cast(conf).setExpireDevices(50);
                break;

            case RADIUS:
                conf = new RadiusAuthModuleConf();
                RadiusAuthModuleConf.class.cast(conf).setName("TestConf" + getUUIDString());
                RadiusAuthModuleConf.class.cast(conf).setProtocol("MSCHAPv2");
                RadiusAuthModuleConf.class.cast(conf).setInetAddress("1.2.3.4");
                RadiusAuthModuleConf.class.cast(conf).setSharedSecret("xyz");
                RadiusAuthModuleConf.class.cast(conf).setSocketTimeout(40);
                break;

            case STATIC:
            default:
                conf = new StaticAuthModuleConf();
                StaticAuthModuleConf.class.cast(conf).setName("TestConf" + getUUIDString());
                StaticAuthModuleConf.class.cast(conf).getUsers().put("user1", UUID.randomUUID().toString());
                StaticAuthModuleConf.class.cast(conf).getUsers().put("user2", "user2Password123");
                break;
        }
        authModuleTO.setConf(conf);

        return authModuleTO;
    }

    @Test
    public void findAll() {
        List<AuthModuleTO> authModuleTOs = authModuleService.list();
        assertNotNull(authModuleTOs);
        assertFalse(authModuleTOs.isEmpty());
        assertTrue(authModuleTOs.size() >= 10);
    }

    @Test
    public void listByType() {
        List<AuthModuleTO> authModuleTOs = authModuleService.list();
        assertNotNull(authModuleTOs);
        assertFalse(authModuleTOs.isEmpty());

        assertTrue(authModuleTOs.stream().anyMatch(
                authModule -> isSpecificConf(authModule.getConf(), LDAPAuthModuleConf.class)
                && authModule.getName().equals("DefaultLDAPAuthModule")));
        assertTrue(authModuleTOs.stream().anyMatch(
                authModule -> isSpecificConf(authModule.getConf(), JDBCAuthModuleConf.class)
                && authModule.getName().equals("DefaultJDBCAuthModule")));
        assertTrue(authModuleTOs.stream().anyMatch(
                authModule -> isSpecificConf(authModule.getConf(), GoogleMfaAuthModuleConf.class)
                && authModule.getName().equals("DefaultGoogleMfaAuthModule")));
        assertTrue(authModuleTOs.stream().anyMatch(
                authModule -> isSpecificConf(authModule.getConf(), OIDCAuthModuleConf.class)
                && authModule.getName().equals("DefaultOIDCAuthModule")));
        assertTrue(authModuleTOs.stream().anyMatch(
                authModule -> isSpecificConf(authModule.getConf(), SAML2IdPAuthModuleConf.class)
                && authModule.getName().equals("DefaultSAML2IdPAuthModule")));
        assertTrue(authModuleTOs.stream().anyMatch(
                authModule -> isSpecificConf(authModule.getConf(), JaasAuthModuleConf.class)
                && authModule.getName().equals("DefaultJaasAuthModule")));
        assertTrue(authModuleTOs.stream().anyMatch(
                authModule -> isSpecificConf(authModule.getConf(), StaticAuthModuleConf.class)
                && authModule.getName().equals("DefaultStaticAuthModule")));
        assertTrue(authModuleTOs.stream().anyMatch(
                authModule -> isSpecificConf(authModule.getConf(), SyncopeAuthModuleConf.class)
                && authModule.getName().equals("DefaultSyncopeAuthModule")));
        assertTrue(authModuleTOs.stream().anyMatch(
                authModule -> isSpecificConf(authModule.getConf(), U2FAuthModuleConf.class)
                && authModule.getName().equals("DefaultU2FAuthModule")));
        assertTrue(authModuleTOs.stream().anyMatch(
                authModule -> isSpecificConf(authModule.getConf(), RadiusAuthModuleConf.class)
                && authModule.getName().equals("DefaultRadiusAuthModule")));
    }

    @Test
    public void getLDAPAuthModule() {
        AuthModuleTO authModuleTO = authModuleService.read("be456831-593d-4003-b273-4c3fb61700df");

        assertNotNull(authModuleTO);
        assertTrue(StringUtils.isNotBlank(authModuleTO.getName()));
        assertTrue(StringUtils.isNotBlank(authModuleTO.getDescription()));
        assertTrue(isSpecificConf(authModuleTO.getConf(), LDAPAuthModuleConf.class));
        assertFalse(isSpecificConf(authModuleTO.getConf(), JDBCAuthModuleConf.class));
    }

    @Test
    public void getJDBCAuthModule() {
        AuthModuleTO authModuleTO = authModuleService.read("4c3ed7e8-7008-11ea-bc55-0242ac130003");

        assertNotNull(authModuleTO);
        assertTrue(StringUtils.isNotBlank(authModuleTO.getName()));
        assertTrue(StringUtils.isNotBlank(authModuleTO.getDescription()));
        assertTrue(isSpecificConf(authModuleTO.getConf(), JDBCAuthModuleConf.class));
        assertFalse(isSpecificConf(authModuleTO.getConf(), GoogleMfaAuthModuleConf.class));
    }

    @Test
    public void getGoogleMfaAuthModule() {
        AuthModuleTO authModuleTO = authModuleService.read("4c3ed4e6-7008-11ea-bc55-0242ac130003");

        assertNotNull(authModuleTO);
        assertTrue(StringUtils.isNotBlank(authModuleTO.getName()));
        assertTrue(StringUtils.isNotBlank(authModuleTO.getDescription()));
        assertTrue(isSpecificConf(authModuleTO.getConf(), GoogleMfaAuthModuleConf.class));
        assertFalse(isSpecificConf(authModuleTO.getConf(), OIDCAuthModuleConf.class));
    }

    @Test
    public void getOIDCAuthModule() {
        AuthModuleTO authModuleTO = authModuleService.read("4c3ed8f6-7008-11ea-bc55-0242ac130003");

        assertNotNull(authModuleTO);
        assertTrue(StringUtils.isNotBlank(authModuleTO.getName()));
        assertTrue(StringUtils.isNotBlank(authModuleTO.getDescription()));
        assertTrue(isSpecificConf(authModuleTO.getConf(), OIDCAuthModuleConf.class));
        assertFalse(isSpecificConf(authModuleTO.getConf(), SAML2IdPAuthModuleConf.class));
    }

    @Test
    public void getSAML2IdPAuthModule() {
        AuthModuleTO authModuleTO = authModuleService.read("4c3ed9d2-7008-11ea-bc55-0242ac130003");

        assertNotNull(authModuleTO);
        assertTrue(StringUtils.isNotBlank(authModuleTO.getName()));
        assertTrue(StringUtils.isNotBlank(authModuleTO.getDescription()));
        assertTrue(isSpecificConf(authModuleTO.getConf(), SAML2IdPAuthModuleConf.class));
        assertFalse(isSpecificConf(authModuleTO.getConf(), JaasAuthModuleConf.class));
    }

    @Test
    public void getJaasAuthModule() {
        AuthModuleTO authModuleTO = authModuleService.read("4c3edbbc-7008-11ea-bc55-0242ac130003");

        assertNotNull(authModuleTO);
        assertTrue(StringUtils.isNotBlank(authModuleTO.getName()));
        assertTrue(StringUtils.isNotBlank(authModuleTO.getDescription()));
        assertTrue(isSpecificConf(authModuleTO.getConf(), JaasAuthModuleConf.class));
        assertFalse(isSpecificConf(authModuleTO.getConf(), StaticAuthModuleConf.class));
    }

    @Test
    public void getStaticAuthModule() {
        AuthModuleTO authModuleTO = authModuleService.read("4c3edc98-7008-11ea-bc55-0242ac130003");

        assertNotNull(authModuleTO);
        assertTrue(StringUtils.isNotBlank(authModuleTO.getName()));
        assertTrue(StringUtils.isNotBlank(authModuleTO.getDescription()));
        assertTrue(isSpecificConf(authModuleTO.getConf(), StaticAuthModuleConf.class));
        assertFalse(isSpecificConf(authModuleTO.getConf(), SyncopeAuthModuleConf.class));
    }

    @Test
    public void getSyncopeAuthModule() {
        AuthModuleTO authModuleTO = authModuleService.read("4c3edd60-7008-11ea-bc55-0242ac130003");

        assertNotNull(authModuleTO);
        assertTrue(StringUtils.isNotBlank(authModuleTO.getName()));
        assertTrue(StringUtils.isNotBlank(authModuleTO.getDescription()));
        assertTrue(isSpecificConf(authModuleTO.getConf(), SyncopeAuthModuleConf.class));
        assertFalse(isSpecificConf(authModuleTO.getConf(), RadiusAuthModuleConf.class));
    }

    @Test
    public void getRadiusAuthModule() {
        AuthModuleTO authModuleTO = authModuleService.read("07c528f3-63b4-4dc1-a4da-87f35b8bdec8");

        assertNotNull(authModuleTO);
        assertTrue(StringUtils.isNotBlank(authModuleTO.getName()));
        assertTrue(StringUtils.isNotBlank(authModuleTO.getDescription()));
        assertTrue(isSpecificConf(authModuleTO.getConf(), RadiusAuthModuleConf.class));
        assertFalse(isSpecificConf(authModuleTO.getConf(), U2FAuthModuleConf.class));
    }

    @Test
    public void getU2FAuthModule() {
        AuthModuleTO authModuleTO = authModuleService.read("f6e1288d-50d9-45fe-82ee-597c42242205");

        assertNotNull(authModuleTO);
        assertTrue(StringUtils.isNotBlank(authModuleTO.getName()));
        assertTrue(StringUtils.isNotBlank(authModuleTO.getDescription()));
        assertTrue(isSpecificConf(authModuleTO.getConf(), U2FAuthModuleConf.class));
        assertFalse(isSpecificConf(authModuleTO.getConf(), LDAPAuthModuleConf.class));
    }

    @Test
    public void create() throws IOException {
        EnumSet.allOf(AuthModuleSupportedType.class).forEach(type -> testCreate(type));
    }

    @Test
    public void updateGoogleMfaAuthModule() {
        AuthModuleTO googleMfaAuthModuleTO = authModuleService.read("4c3ed4e6-7008-11ea-bc55-0242ac130003");
        assertNotNull(googleMfaAuthModuleTO);

        final String authModuleName = "TestGoogleMfaAuthModule" + getUUIDString();
        AuthModuleTO newGoogleMfaAuthModuleTO = buildAuthModuleTO(AuthModuleSupportedType.GOOGLE_MFA);
        newGoogleMfaAuthModuleTO = createAuthModule(newGoogleMfaAuthModuleTO);
        assertNotNull(newGoogleMfaAuthModuleTO);

        AuthModuleConf conf = googleMfaAuthModuleTO.getConf();
        assertNotNull(conf);
        GoogleMfaAuthModuleConf.class.cast(conf).setLabel("newLabel");
        newGoogleMfaAuthModuleTO.setConf(conf);

        // update new auth module
        authModuleService.update(newGoogleMfaAuthModuleTO);
        newGoogleMfaAuthModuleTO = authModuleService.read(newGoogleMfaAuthModuleTO.getKey());
        assertNotNull(newGoogleMfaAuthModuleTO);

        conf = newGoogleMfaAuthModuleTO.getConf();
        assertEquals("newLabel", GoogleMfaAuthModuleConf.class.cast(conf).getLabel());
    }

    @Test
    public void updateLDAPAuthModule() {
        AuthModuleTO ldapAuthModuleTO = authModuleService.read("be456831-593d-4003-b273-4c3fb61700df");
        assertNotNull(ldapAuthModuleTO);

        final String authModuleName = "TestGoogleMfaAuthModule" + getUUIDString();
        AuthModuleTO newLdapAuthModuleTO = buildAuthModuleTO(AuthModuleSupportedType.LDAP);
        newLdapAuthModuleTO = createAuthModule(newLdapAuthModuleTO);
        assertNotNull(newLdapAuthModuleTO);

        AuthModuleConf conf = ldapAuthModuleTO.getConf();
        assertNotNull(conf);
        LDAPAuthModuleConf.class.cast(conf).setSubtreeSearch(false);
        newLdapAuthModuleTO.setConf(conf);

        // update new auth module
        authModuleService.update(newLdapAuthModuleTO);
        newLdapAuthModuleTO = authModuleService.read(newLdapAuthModuleTO.getKey());
        assertNotNull(newLdapAuthModuleTO);

        conf = newLdapAuthModuleTO.getConf();
        assertFalse(LDAPAuthModuleConf.class.cast(conf).isSubtreeSearch());
    }

    @Test
    public void delete() throws IOException {
        EnumSet.allOf(AuthModuleSupportedType.class).forEach(type -> testDelete(type));
    }

    private void testCreate(final AuthModuleSupportedType type) {
        AuthModuleTO authModuleTO = createAuthModule(buildAuthModuleTO(type));
        assertNotNull(authModuleTO);
        assertTrue(authModuleTO.getName().contains(
                "Test" + type + "AuthenticationModule"));
        assertTrue(authModuleTO.getDescription().contains(
                "A test " + type + " Authentication Module"));
    }

    private void testDelete(final AuthModuleSupportedType type) {
        AuthModuleTO authModuleTO = buildAuthModuleTO(type);
        AuthModuleTO read = createAuthModule(authModuleTO);
        assertNotNull(read);
        authModuleService.delete(read.getKey());
        try {
            authModuleService.read(read.getKey());
            fail("This should not happen");
        } catch (SyncopeClientException e) {
            assertNotNull(e);
        }
    }

    private boolean isSpecificConf(final AuthModuleConf conf, final Class<? extends AuthModuleConf> clazz) {
        return ClassUtils.isAssignable(clazz, conf.getClass());
    }
}
