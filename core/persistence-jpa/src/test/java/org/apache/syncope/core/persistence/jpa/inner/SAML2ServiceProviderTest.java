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

package org.apache.syncope.core.persistence.jpa.inner;

import org.apache.syncope.core.persistence.api.dao.authentication.SAML2ServiceProviderDAO;
import org.apache.syncope.core.persistence.api.entity.authentication.OpenIdConnectRelyingParty;
import org.apache.syncope.core.persistence.api.entity.authentication.SAML2ServiceProvider;
import org.apache.syncope.core.persistence.jpa.AbstractTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@Transactional("Master")
public class SAML2ServiceProviderTest extends AbstractTest {

    @Autowired
    private SAML2ServiceProviderDAO saml2ServiceProviderDAO;

    @Test
    public void find() {
        int beforeCount = saml2ServiceProviderDAO.findAll().size();
        SAML2ServiceProvider rp = entityFactory.newEntity(SAML2ServiceProvider.class);
        rp.setName("OIDC");
        rp.setDescription("This is a sample OIDC RP");
        rp.setEntityId("urn:example:saml2:sp");
        rp.setMetadataLocation("https://example.org/metadata.xml");
        saml2ServiceProviderDAO.save(rp);

        assertNotNull(rp);
        assertNotNull(rp.getKey());

        int afterCount = saml2ServiceProviderDAO.findAll().size();
        assertEquals(afterCount, beforeCount + 1);

        rp = saml2ServiceProviderDAO.findByEntityId(rp.getEntityId());
        assertNotNull(rp);

        rp = saml2ServiceProviderDAO.findByName("OIDC");
        assertNotNull(rp);

        saml2ServiceProviderDAO.deleteByEntityId(rp.getEntityId());
        assertNull(saml2ServiceProviderDAO.findByName("OIDC"));
    }

}