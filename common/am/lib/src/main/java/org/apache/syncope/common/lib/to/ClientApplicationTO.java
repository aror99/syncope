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

package org.apache.syncope.common.lib.to;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.syncope.common.lib.BaseBean;
import org.apache.syncope.common.lib.policy.AuthenticationPolicyTO;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlSeeAlso({OpenIdConnectRelyingPartyTO.class, SAML2ServiceProviderTO.class})
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "@class")
@JsonPropertyOrder(value = {"@class", "key", "name", "description", "authenticationPolicy"})
@Schema(subTypes = {OpenIdConnectRelyingPartyTO.class, SAML2ServiceProviderTO.class}, discriminatorProperty = "@class")
public abstract class ClientApplicationTO extends BaseBean implements EntityTO {

    private static final long serialVersionUID = 6577639976115661357L;

    private String key;

    private String name;

    private String description;

    private AuthenticationPolicyTO authenticationPolicy;

    public AuthenticationPolicyTO getAuthenticationPolicy() {
        return authenticationPolicy;
    }

    public void setAuthenticationPolicy(final AuthenticationPolicyTO authenticationPolicy) {
        this.authenticationPolicy = authenticationPolicy;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public void setKey(final String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Schema(name = "@class", required = true)
    public abstract String getDiscriminator();

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .appendSuper(super.hashCode())
            .append(key)
            .append(name)
            .append(description)
            .append(authenticationPolicy)
            .toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        ClientApplicationTO rhs = (ClientApplicationTO) obj;
        return new EqualsBuilder()
            .appendSuper(super.equals(obj))
            .append(this.key, rhs.key)
            .append(this.name, rhs.name)
            .append(this.description, rhs.description)
            .append(this.authenticationPolicy, rhs.authenticationPolicy)
            .isEquals();
    }
}
