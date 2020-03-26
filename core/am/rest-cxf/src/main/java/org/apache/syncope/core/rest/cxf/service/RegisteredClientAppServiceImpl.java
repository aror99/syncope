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
package org.apache.syncope.core.rest.cxf.service;

import org.apache.syncope.common.lib.to.client.ClientAppTO;
import org.apache.syncope.core.logic.ClientAppLogic;
import org.apache.syncope.core.persistence.api.dao.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.syncope.common.lib.to.RegisteredClientAppTO;
import org.apache.syncope.common.lib.types.ClientAppType;
import org.apache.syncope.common.rest.api.service.RegisteredClientAppService;
import org.apache.syncope.core.logic.RegisteredClientAppLogic;

@Service
public class RegisteredClientAppServiceImpl extends AbstractServiceImpl implements RegisteredClientAppService {

    @Autowired
    private ClientAppLogic clientAppLogic;

    @Autowired
    private RegisteredClientAppLogic logic;

    @Override
    public List<RegisteredClientAppTO> list() {
        List<ClientAppTO> applications = new ArrayList<>();
        Arrays.asList(ClientAppType.values()).forEach(type -> applications.addAll(clientAppLogic.list(type)));
        return logic.list(applications);
    }

    @Override
    public RegisteredClientAppTO read(final String key) {
        try {
            return logic.read(clientAppLogic.read(ClientAppType.SAML2SP, key));
        } catch (NotFoundException e) {
            return logic.read(clientAppLogic.read(ClientAppType.OIDCRP, key));
        }
    }

}
