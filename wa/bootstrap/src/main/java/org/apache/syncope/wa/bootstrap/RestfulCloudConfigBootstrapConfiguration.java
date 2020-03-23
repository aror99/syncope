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
package org.apache.syncope.wa.bootstrap;

import org.apache.syncope.common.keymaster.client.api.ServiceOps;
import org.apache.syncope.common.keymaster.client.zookeper.ZookeeperKeymasterClientContext;
import org.apache.syncope.wa.WARestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

@Configuration(proxyBeanMethods = false)
@ImportAutoConfiguration(ZookeeperKeymasterClientContext.class)
@PropertySource("classpath:wa.properties")
@PropertySource(value = "file:${conf.directory}/wa.properties", ignoreResourceNotFound = true)
public class RestfulCloudConfigBootstrapConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(RestfulCloudConfigBootstrapConfiguration.class);

    @Value("${anonymousUser}")
    private String anonymousUser;

    @Value("${anonymousKey}")
    private String anonymousKey;

    @Value("${useGZIPCompression}")
    private boolean useGZIPCompression;

    @Autowired
    @Bean
    public WARestClient waRestClient(final ServiceOps serviceOps) {
        return new WARestClient(serviceOps, anonymousUser, anonymousKey, useGZIPCompression);
    }

    @Autowired
    @Bean
    public PropertySourceLocator configPropertySourceLocator(final WARestClient waRestClient) {
        return new PropertySourceLocator() {
            @Override
            public org.springframework.core.env.PropertySource<?> locate(final Environment environment) {
                try {
                    LOG.info("Bootstrapping WA configuration");
                    Map<String, Object> payload = new HashMap<>();
                    return new MapPropertySource(getClass().getName(), payload);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Unable to fetch settings", e);
                }
            }
        };
    }
}
