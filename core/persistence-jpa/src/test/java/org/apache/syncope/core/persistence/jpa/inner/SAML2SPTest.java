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

import org.apache.syncope.core.persistence.api.entity.policy.AccessPolicy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.apache.syncope.common.lib.types.SAML2SPNameId;
import org.apache.syncope.core.persistence.api.entity.policy.AuthPolicy;
import org.apache.syncope.core.persistence.api.dao.authentication.SAML2SPDAO;
import org.apache.syncope.core.persistence.api.entity.authentication.SAML2SP;

@Transactional("Master")
public class SAML2SPTest extends AbstractClientAppTest {

    @Autowired
    private SAML2SPDAO saml2spDAO;

    @Test
    public void find() {
        int beforeCount = saml2spDAO.findAll().size();
        SAML2SP rp = entityFactory.newEntity(SAML2SP.class);
        rp.setName("SAML2");
        rp.setDescription("This is a sample SAML2 SP");
        rp.setEntityId("urn:example:saml2:sp");
        rp.setMetadataLocation("https://example.org/metadata.xml");
        rp.setRequiredNameIdFormat(SAML2SPNameId.EMAIL_ADDRESS);
        rp.setEncryptionOptional(true);
        rp.setEncryptAssertions(true);

        AccessPolicy accessPolicy = buildAndSaveAccessPolicy();
        rp.setAccessPolicy(accessPolicy);

        AuthPolicy authnPolicy = buildAndSaveAuthPolicy();
        rp.setAuthPolicy(authnPolicy);

        saml2spDAO.save(rp);

        assertNotNull(rp);
        assertNotNull(rp.getKey());

        int afterCount = saml2spDAO.findAll().size();
        assertEquals(afterCount, beforeCount + 1);

        rp = saml2spDAO.findByEntityId(rp.getEntityId());
        assertNotNull(rp);

        rp = saml2spDAO.findByName(rp.getName());
        assertNotNull(rp);

        saml2spDAO.deleteByEntityId(rp.getEntityId());
        assertNull(saml2spDAO.findByName(rp.getName()));
    }
}
