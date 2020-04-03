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
package org.apache.syncope.wa.starter.rest;

import java.util.Collection;
import java.util.stream.Collectors;
import org.apache.syncope.common.rest.api.service.RegisteredClientAppService;
import org.apache.syncope.wa.WARestClient;
import org.apache.syncope.wa.mapper.RegisteredServiceMapper;
import org.apereo.cas.services.AbstractServiceRegistry;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServiceRegistryListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;

public class SyncopeServiceRegistry extends AbstractServiceRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(SyncopeServiceRegistry.class);

    private final WARestClient restClient;

    private final RegisteredServiceMapper mapper;

    public SyncopeServiceRegistry(final WARestClient restClient,
            final ConfigurableApplicationContext applicationContext,
            final Collection<ServiceRegistryListener> serviceRegistryListeners) {

        super(applicationContext, serviceRegistryListeners);
        this.restClient = restClient;
        this.mapper = new RegisteredServiceMapper();
    }

    @Override
    public RegisteredService save(final RegisteredService registeredService) {
        return null;
    }

    @Override
    public boolean delete(final RegisteredService registeredService) {
        return false;
    }

    @Override
    public Collection<RegisteredService> load() {
        LOG.info("Loading application definitions");

        return restClient.getSyncopeClient().getService(RegisteredClientAppService.class).list().stream().
                map(clientApp -> mapper.toRegisteredService(clientApp)).collect(Collectors.toList());
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        LOG.info("Searching for application definition by id {}", id);
        return null;
    }
}
