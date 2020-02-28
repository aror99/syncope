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
package org.apache.syncope.common.lib.types;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public final class AMEntitlement {

    public static final String GATEWAY_ROUTE_CREATE = "GATEWAY_ROUTE_CREATE";

    public static final String GATEWAY_ROUTE_UPDATE = "GATEWAY_ROUTE_UPDATE";

    public static final String GATEWAY_ROUTE_DELETE = "GATEWAY_ROUTE_DELETE";

    public static final String GATEWAY_ROUTE_PUSH = "GATEWAY_ROUTE_PUSH";

    public static final String AUTHENTICATION_MODULE_READ = "AUTHENTICATION_MODULE_READ";

    public static final String AUTHENTICATION_MODULE_LIST = "AUTHENTICATION_MODULE_LIST";

    public static final String AUTHENTICATION_MODULE_CREATE = "AUTHENTICATION_MODULE_CREATE";

    public static final String AUTHENTICATION_MODULE_UPDATE = "AUTHENTICATION_MODULE_UPDATE";

    public static final String AUTHENTICATION_MODULE_DELETE = "AUTHENTICATION_MODULE_DELETE";

    private static final Set<String> VALUES;

    public static final String SAML2_SERVICE_PROVIDER_READ = "OIDC_RELYING_PARTY_READ";

    public static final String OIDC_RELYING_PARTY_DELETE = "OIDC_RELYING_PARTY_DELETE";

    public static final String OIDC_RELYING_PARTY_READ = "OIDC_RELYING_PARTY_READ";

    public static final String SAML2_SERVICE_PROVIDER_LIST = "SAML2_SERVICE_PROVIDER_LIST";

    public static final String OIDC_RELYING_PARTY_CREATE = "OIDC_RELYING_PARTY_CREATE";

    public static final String SAML2_SERVICE_PROVIDER_DELETE = "SAML2_SERVICE_PROVIDER_DELETE";

    public static final String OIDC_RELYING_PARTY_LIST = "OIDC_RELYING_PARTY_LIST";

    public static final String SAML2_SERVICE_PROVIDER_CREATE = "SAML2_SERVICE_PROVIDER_CREATE";

    static {
        Set<String> values = new TreeSet<>();
        for (Field field : AMEntitlement.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && String.class.equals(field.getType())) {
                values.add(field.getName());
            }
        }
        VALUES = Collections.unmodifiableSet(values);
    }

    public static Set<String> values() {
        return VALUES;
    }

    private AMEntitlement() {
        // private constructor for static utility class
    }
}
