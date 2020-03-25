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

import org.apache.syncope.common.lib.types.OIDCSubjectType;
import org.apache.syncope.core.persistence.api.dao.auth.OIDCRPDAO;
import org.apache.syncope.core.persistence.api.entity.auth.OIDCRP;
import org.apache.syncope.core.persistence.api.entity.policy.AccessPolicy;
import org.apache.syncope.core.persistence.api.entity.policy.AuthPolicy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional("Master")
public class OIDCRPTest extends AbstractClientAppTest {

    @Autowired
    private OIDCRPDAO oidcrpDAO;

    @Test
    public void find() {
        int beforeCount = oidcrpDAO.findAll().size();

        OIDCRP rp = entityFactory.newEntity(OIDCRP.class);
        rp.setName("OIDC");
        rp.setDescription("This is a sample OIDC RP");
        rp.setClientId("clientid");
        rp.setClientSecret("secret");
        rp.setSubjectType(OIDCSubjectType.PUBLIC);
        rp.getSupportedGrantTypes().add("something");
        rp.getSupportedResponseTypes().add("something");

        AccessPolicy accessPolicy = buildAndSaveAccessPolicy();
        rp.setAccessPolicy(accessPolicy);

        AuthPolicy authPolicy = buildAndSaveAuthPolicy();
        rp.setAuthPolicy(authPolicy);

        oidcrpDAO.save(rp);

        assertNotNull(rp);
        assertNotNull(rp.getKey());

        int afterCount = oidcrpDAO.findAll().size();
        assertEquals(afterCount, beforeCount + 1);

        rp = oidcrpDAO.findByClientId("clientid");
        assertNotNull(rp);
        assertNotNull(rp.getAuthPolicy());

        rp = oidcrpDAO.findByName("OIDC");
        assertNotNull(rp);

        oidcrpDAO.deleteByClientId("clientid");
        assertNull(oidcrpDAO.findByName("OIDC"));
    }
}
