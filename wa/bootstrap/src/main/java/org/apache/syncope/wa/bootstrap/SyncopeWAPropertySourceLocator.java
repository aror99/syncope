/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.syncope.wa.bootstrap;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.CasCoreConfigurationUtils;

import org.apache.syncope.common.lib.authentication.module.StaticAuthModuleConf;
import org.apache.syncope.common.lib.authentication.policy.AuthPolicyConf;
import org.apache.syncope.common.lib.policy.PolicyTO;
import org.apache.syncope.common.lib.to.AuthPolicyTO;
import org.apache.syncope.common.lib.to.ImplementationTO;
import org.apache.syncope.common.lib.types.AMImplementationType;
import org.apache.syncope.common.lib.types.PolicyType;
import org.apache.syncope.common.rest.api.service.ImplementationService;
import org.apache.syncope.common.rest.api.service.PolicyService;
import org.apache.syncope.core.provisioning.api.serialization.POJOHelper;
import org.apache.syncope.wa.WARestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import java.util.List;
import java.util.Map;

@Order
public class SyncopeWAPropertySourceLocator implements PropertySourceLocator {
    private static final Logger LOG = LoggerFactory.getLogger(SyncopeWABootstrapConfiguration.class);

    private final PolicyService policyService;

    private final ImplementationService implementationService;

    public SyncopeWAPropertySourceLocator(final WARestClient waRestClient) {
        this.policyService = waRestClient.getSyncopeClient().getService(PolicyService.class);
        this.implementationService = waRestClient.getSyncopeClient().getService(ImplementationService.class);
    }

    @Override
    public PropertySource<?> locate(final Environment environment) {
        try {
            LOG.info("Bootstrapping WA configuration");
            List<PolicyTO> policies = policyService.list(PolicyType.AUTHENTICATION);
            CasConfigurationProperties cas = new CasConfigurationProperties();
            policies.
                stream().
                map(AuthPolicyTO.class::cast).
                forEach(policy -> translatePolicy(policy, cas));
            Map<String, Object> payload = CasCoreConfigurationUtils.asMap(cas.withHolder());
            return new MapPropertySource(getClass().getName(), payload);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to fetch settings", e);
        }
    }

    private void translatePolicy(final AuthPolicyTO policy, final CasConfigurationProperties cas) {
        ImplementationTO implTO = implementationService.
            read(AMImplementationType.AUTH_POLICY_CONFIGURATIONS, policy.getKey());
        AuthPolicyConf policyConf = POJOHelper.deserialize(implTO.getBody(), AuthPolicyConf.class);
        if (policyConf instanceof StaticAuthModuleConf) {
            StaticAuthModuleConf staticConf = (StaticAuthModuleConf) policyConf;
            StringBuilder users = new StringBuilder();
            staticConf.getUsers().forEach((key, value) -> users.append(key).append("::").append(value));
            cas.getAuthn().getAccept().setName(policyConf.getName());
            cas.getAuthn().getAccept().setUsers(users.toString());
        }
    }
}
