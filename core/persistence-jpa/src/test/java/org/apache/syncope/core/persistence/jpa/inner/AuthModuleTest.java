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
package org.apache.syncope.core.persistence.jpa.inner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.syncope.common.lib.authentication.module.GoogleMfaAuthModuleConf;
import org.apache.syncope.common.lib.authentication.module.JaasAuthModuleConf;
import org.apache.syncope.common.lib.authentication.module.LDAPAuthModuleConf;
import org.apache.syncope.common.lib.authentication.module.StaticAuthModuleConf;
import org.apache.syncope.common.lib.types.AMImplementationType;
import org.apache.syncope.common.lib.types.ImplementationEngine;
import org.apache.syncope.core.persistence.api.dao.ImplementationDAO;
import org.apache.syncope.core.persistence.api.entity.Implementation;
import org.apache.syncope.core.persistence.jpa.AbstractTest;
import org.apache.syncope.core.provisioning.api.serialization.POJOHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.syncope.core.persistence.api.entity.authentication.AuthModule;
import org.apache.syncope.common.lib.authentication.module.AuthModuleConf;
import org.apache.syncope.common.lib.authentication.module.OIDCAuthModuleConf;
import org.apache.syncope.common.lib.to.ItemTO;
import org.apache.syncope.core.persistence.api.dao.authentication.AuthModuleDAO;

@Transactional("Master")
public class AuthModuleTest extends AbstractTest {

    @Autowired
    private AuthModuleDAO authModuleDAO;

    @Autowired
    private ImplementationDAO implementationDAO;

    @Test
    public void find() {
        AuthModule module = authModuleDAO.find(
                "be456831-593d-4003-b273-4c3fb61700df");
        assertNotNull(module);

        module = authModuleDAO.find(UUID.randomUUID().toString());
        assertNull(module);
    }

    @Test
    public void findAll() {
        List<AuthModule> modules = authModuleDAO.findAll();
        assertNotNull(modules);
        assertEquals(1, modules.size());
    }

    @Test
    public void saveWithPredefinedModule() {
        StaticAuthModuleConf conf =
                new StaticAuthModuleConf(Map.of("user", UUID.randomUUID().toString()));

        Implementation config = getImplementation(conf);

        config = implementationDAO.save(config);

        assertNotNull(config);
        assertNotNull(config.getKey());

        saveAuthModule(config, "AuthModulePredefinedTest");
    }

    @Test
    public void saveWithJaasModule() {
        JaasAuthModuleConf conf = new JaasAuthModuleConf();
        conf.setKerberosKdcSystemProperty("sample-value");
        conf.setKerberosRealmSystemProperty("sample-value");
        conf.setLoginConfigType("JavaLoginConfig");
        conf.setRealm("SYNCOPE");
        conf.setLoginConfigurationFile("/opt/jaas/login.conf");
        Implementation config = getImplementation(conf);

        config = implementationDAO.save(config);

        assertNotNull(config);
        assertNotNull(config.getKey());

        saveAuthModule(config, "AuthModuleJaasTest");
    }

    @Test
    public void saveWithLdapModule() {
        LDAPAuthModuleConf conf = new LDAPAuthModuleConf();
        conf.setBaseDn("dc=example,dc=org");
        conf.setSearchFilter("cn={user}");
        conf.setSubtreeSearch(true);
        conf.setLdapUrl("ldap://localhost:1389");
        conf.setUserIdAttribute("uid");
        conf.setBaseDn("cn=Directory Manager,dc=example,dc=org");
        conf.setBindCredential("Password");
        ItemTO keyMapping = new ItemTO();
        keyMapping.setIntAttrName("uid");
        keyMapping.setExtAttrName("username");
        ItemTO fullnameMapping = new ItemTO();
        fullnameMapping.setIntAttrName("cn");
        fullnameMapping.setExtAttrName("fullname");
        conf.getProfileItems().addAll(List.of(fullnameMapping, keyMapping));
        Implementation config = getImplementation(conf);

        config = implementationDAO.save(config);

        assertNotNull(config);
        assertNotNull(config.getKey());

        saveAuthModule(config, "AuthModuleLdapTest");
    }

    @Test
    public void saveWithGoogleAuthenticatorModule() {
        GoogleMfaAuthModuleConf conf = new GoogleMfaAuthModuleConf();
        conf.setCodeDigits(6);
        conf.setIssuer("SyncopeTest");
        conf.setLabel("Syncope");
        conf.setTimeStepSize(30);
        conf.setWindowSize(3);
        Implementation config = getImplementation(conf);

        config = implementationDAO.save(config);

        assertNotNull(config);
        assertNotNull(config.getKey());

        saveAuthModule(config, "AuthModuleGoogleTest");
    }

    @Test
    public void saveWithOIDCAuthModule() {
        OIDCAuthModuleConf conf = new OIDCAuthModuleConf();
        conf.setId("OIDCTestId");
        conf.setDiscoveryUri("www.testurl.com");
        conf.setUserIdAttribute("username");
        conf.setResponseType("code");
        conf.setScope("openid email profile");
        ItemTO keyMapping = new ItemTO();
        keyMapping.setIntAttrName("uid");
        keyMapping.setExtAttrName("username");
        conf.getProfileItems().add(keyMapping);
        Implementation config = getImplementation(conf);

        config = implementationDAO.save(config);

        assertNotNull(config);
        assertNotNull(config.getKey());

        saveAuthModule(config, "AuthModuleOIDCTest");
    }

    private void saveAuthModule(final Implementation config, final String name) {
        AuthModule module = entityFactory.newEntity(AuthModule.class);
        module.setName(name);
        module.add(config);
        authModuleDAO.save(module);

        assertNotNull(module);
        assertNotNull(module.getKey());

        assertNotNull(authModuleDAO.find(module.getKey()));
    }

    private Implementation getImplementation(final AuthModuleConf conf) {
        Implementation config = entityFactory.newEntity(Implementation.class);
        config.setKey(UUID.randomUUID().toString());
        config.setEngine(ImplementationEngine.JAVA);
        config.setType(AMImplementationType.AUTH_MODULE_CONFIGURATIONS);
        config.setBody(POJOHelper.serialize(conf));
        return config;
    }

    @Test
    public void delete() {
        AuthModule authModule = authModuleDAO.find("be456831-593d-4003-b273-4c3fb61700df");
        assertNotNull(authModule);

        authModuleDAO.delete("be456831-593d-4003-b273-4c3fb61700df");

        authModule = authModuleDAO.find("be456831-593d-4003-b273-4c3fb61700df");
        assertNull(authModule);
    }

}
