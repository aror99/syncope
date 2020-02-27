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

package org.apache.syncope.core.persistence.jpa.entity.authentication;

import org.apache.syncope.core.persistence.api.entity.authentication.OpenIdConnectRelyingParty;
import org.apache.syncope.core.persistence.api.entity.policy.AuthenticationPolicy;
import org.apache.syncope.core.persistence.jpa.entity.AbstractGeneratedKeyEntity;
import org.apache.syncope.core.persistence.jpa.entity.policy.JPAAuthenticationPolicy;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = JPAOpenIdConnectRelyingParty.TABLE)
public class JPAOpenIdConnectRelyingParty extends AbstractGeneratedKeyEntity implements OpenIdConnectRelyingParty {

    public static final String TABLE = "OpenIdConnectRelyingParty";

    private static final long serialVersionUID = 7422422526695279794L;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String description;

    @Column(unique = true, nullable = false)
    private String clientId;

    @Column
    private String clientSecret;

    @Column(nullable = false)
    @OneToOne(fetch = FetchType.EAGER)
    private JPAAuthenticationPolicy authenticationPolicy;

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "redirectUris")
    @CollectionTable(name = "OpenIdConnectRelyingParty_RedirectUris", joinColumns = @JoinColumn(name = "clientId"))
    private List<String> redirectUris = new ArrayList<>();

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public JPAAuthenticationPolicy getAuthenticationPolicy() {
        return authenticationPolicy;
    }

    @Override
    public void setAuthenticationPolicy(final AuthenticationPolicy authenticationPolicy) {
        checkType(authenticationPolicy, JPAAuthenticationPolicy.class);
        this.authenticationPolicy = (JPAAuthenticationPolicy) authenticationPolicy;
    }

    @Override
    public List<String> getRedirectUris() {
        return redirectUris;
    }
    
    @Override
    public void setRedirectUris(final List<String> redirectUris) {
        this.redirectUris = redirectUris;
    }


    @Override
    public String getClientId() {
        return clientId;
    }


    @Override
    public void setClientId(final String clientId) {
        this.clientId = clientId;
    }

    @Override
    public String getClientSecret() {
        return clientSecret;
    }

    @Override
    public void setClientSecret(final String clientSecret) {
        this.clientSecret = clientSecret;
    }


}
