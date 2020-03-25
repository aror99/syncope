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
package org.apache.syncope.wa;

import org.apache.syncope.client.lib.AnonymousAuthenticationHandler;
import org.apache.syncope.client.lib.SyncopeClient;
import org.apache.syncope.client.lib.SyncopeClientFactoryBean;
import org.apache.syncope.common.keymaster.client.api.ServiceOps;
import org.apache.syncope.common.keymaster.client.api.model.NetworkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WARestClient {

    private static final Logger LOG = LoggerFactory.getLogger(WARestClient.class);

    private final ServiceOps serviceOps;

    private final String anonymousUser;

    private final String anonymousKey;

    private final boolean useGZIPCompression;

    private SyncopeClient client;

    public WARestClient(
            final ServiceOps serviceOps,
            final String anonymousUser,
            final String anonymousKey,
            final boolean useGZIPCompression) {

        this.serviceOps = serviceOps;
        this.anonymousUser = anonymousUser;
        this.anonymousKey = anonymousKey;
        this.useGZIPCompression = useGZIPCompression;
    }

    public SyncopeClient getSyncopeClient() {
        synchronized (this) {
            if (client == null) {
                try {
                    client = new SyncopeClientFactoryBean().
                            setAddress(serviceOps.get(NetworkService.Type.CORE).getAddress()).
                            setUseCompression(useGZIPCompression).
                            create(new AnonymousAuthenticationHandler(anonymousUser, anonymousKey));
                } catch (Exception e) {
                    LOG.error("Could not init SyncopeClient", e);
                }
            }
        }

        return client;
    }
}
