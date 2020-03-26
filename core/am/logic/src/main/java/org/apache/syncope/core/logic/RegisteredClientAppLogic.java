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
package org.apache.syncope.core.logic;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.syncope.common.lib.to.RegisteredClientAppTO;
import org.apache.syncope.common.lib.to.client.ClientAppTO;
import org.apache.syncope.common.lib.types.AMEntitlement;
import org.apache.syncope.core.provisioning.api.data.RegisteredClientAppBinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RegisteredClientAppLogic {

    @Autowired
    private RegisteredClientAppBinder binder;

    @PreAuthorize("hasRole('" + AMEntitlement.REGISTERED_CLIENT_APP_LIST + "')")
    @Transactional(readOnly = true)
    public List<RegisteredClientAppTO> list(final List<ClientAppTO> clientApps) {
        return clientApps.stream().map(binder::getRegisteredClientAppTO).collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('" + AMEntitlement.REGISTERED_CLIENT_APP_READ + "')")
    @Transactional(readOnly = true)
    public RegisteredClientAppTO read(final ClientAppTO clientApp) {
        return binder.getRegisteredClientAppTO(clientApp);
    }

}
