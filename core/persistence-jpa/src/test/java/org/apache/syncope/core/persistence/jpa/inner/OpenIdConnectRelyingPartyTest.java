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

import org.apache.syncope.core.persistence.api.dao.authentication.OpenIdConnectRelyingPartyDAO;
import org.apache.syncope.core.persistence.api.entity.authentication.OpenIdConnectRelyingParty;
import org.apache.syncope.core.persistence.jpa.AbstractTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@Transactional("Master")
public class OpenIdConnectRelyingPartyTest extends AbstractTest {

    @Autowired
    private OpenIdConnectRelyingPartyDAO openIdConnectRelyingPartyDAO;

    @Test
    public void find() {
        int beforeCount = openIdConnectRelyingPartyDAO.findAll().size();
        OpenIdConnectRelyingParty rp = entityFactory.newEntity(OpenIdConnectRelyingParty.class);
        rp.setName("OIDC");
        rp.setDescription("This is a sample OIDC RP");
        rp.setClientId("clientid");
        rp.setClientSecret("secret");
        openIdConnectRelyingPartyDAO.save(rp);

        assertNotNull(rp);
        assertNotNull(rp.getKey());

        int afterCount = openIdConnectRelyingPartyDAO.findAll().size();
        assertEquals(afterCount, beforeCount + 1);

        rp = openIdConnectRelyingPartyDAO.findByClientId("clientid");
        assertNotNull(rp);

        rp = openIdConnectRelyingPartyDAO.findByName("OIDC");
        assertNotNull(rp);

        openIdConnectRelyingPartyDAO.deleteByClientId("clientid");
        assertNull(openIdConnectRelyingPartyDAO.findByName("OIDC"));
    }

}