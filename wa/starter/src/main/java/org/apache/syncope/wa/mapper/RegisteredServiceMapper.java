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
package org.apache.syncope.wa.mapper;

import java.util.HashSet;
import org.apache.syncope.common.lib.policy.AllowedAttrReleasePolicyConf;
import org.apache.syncope.common.lib.to.RegisteredClientAppTO;
import org.apache.syncope.common.lib.to.client.ClientAppTO;
import org.apache.syncope.common.lib.to.client.OIDCRPTO;
import org.apache.syncope.common.lib.to.client.SAML2SPTO;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceAuthenticationPolicy;
import org.apereo.cas.services.DenyAllAttributeReleasePolicy;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.services.RegisteredServiceAuthenticationPolicy;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.apereo.cas.support.saml.services.SamlRegisteredService;

public class RegisteredServiceMapper {

    public RegisteredService toRegisteredService(final RegisteredClientAppTO clientApp) {

        RegisteredServiceAuthenticationPolicy authenticationPolicy = new DefaultRegisteredServiceAuthenticationPolicy();

        RegisteredServiceAccessStrategy accessStrategy = new DefaultRegisteredServiceAccessStrategy(
                clientApp.getAccessPolicyConf().isEnabled(), clientApp.getAccessPolicyConf().isSsoEnabled());
        accessStrategy.getRequiredAttributes().putAll(clientApp.getAccessPolicyConf().getRequiredAttributes());

        RegisteredServiceAttributeReleasePolicy attributeReleasePolicy;
        if (clientApp.getAttrReleasePolicyConf() != null
                && clientApp.getAttrReleasePolicyConf() instanceof AllowedAttrReleasePolicyConf) {
            attributeReleasePolicy = new ReturnAllowedAttributeReleasePolicy();
            ((AllowedAttrReleasePolicyConf) clientApp.getAttrReleasePolicyConf()).getAllowedAttributes();
            ((ReturnAllowedAttributeReleasePolicy) attributeReleasePolicy).getAllowedAttributes().addAll(
                    ((AllowedAttrReleasePolicyConf) clientApp.getAttrReleasePolicyConf()).getAllowedAttributes());
        } else {
            attributeReleasePolicy = new DenyAllAttributeReleasePolicy();
        }

        if (clientApp.getClientAppTO() instanceof OIDCRPTO) {
            OIDCRPTO rp = (OIDCRPTO) clientApp.getClientAppTO();
            OidcRegisteredService registeredService = new OidcRegisteredService();
            registeredService.setServiceId(rp.getKey());
            registeredService.setName(rp.getName());
            registeredService.setDescription(rp.getDescription());
            registeredService.setAccessStrategy(accessStrategy);
            registeredService.setAuthenticationPolicy(authenticationPolicy);
            registeredService.setAttributeReleasePolicy(attributeReleasePolicy);

            registeredService.setClientId(rp.getClientId());
            registeredService.setClientSecret(rp.getClientSecret());
            registeredService.setSignIdToken(rp.isSignIdToken());
            registeredService.setJwks(rp.getJwks());
            registeredService.setSubjectType(rp.getSubjectType().name());
            registeredService.setSupportedGrantTypes((HashSet<String>) rp.getSupportedGrantTypes());
            registeredService.setSupportedResponseTypes((HashSet<String>) rp.getSupportedResponseTypes());

            return registeredService;
        } else if (clientApp.getClientAppTO() instanceof SAML2SPTO) {
            SAML2SPTO sp = (SAML2SPTO) clientApp.getClientAppTO();
            SamlRegisteredService registeredService = new SamlRegisteredService();
            registeredService.setServiceId(sp.getKey());
            registeredService.setName(sp.getName());
            registeredService.setDescription(sp.getDescription());
            registeredService.setAccessStrategy(accessStrategy);
            registeredService.setAuthenticationPolicy(authenticationPolicy);
            registeredService.setAttributeReleasePolicy(attributeReleasePolicy);

            registeredService.setIssuerEntityId(sp.getEntityId());
            registeredService.setMetadataLocation(sp.getMetadataLocation());
            registeredService.setMetadataSignatureLocation(sp.getMetadataSignatureLocation());
            registeredService.setSignAssertions(sp.isSignAssertions());
            registeredService.setSignResponses(sp.isSignResponses());
            registeredService.setEncryptionOptional(sp.isEncryptionOptional());
            registeredService.setEncryptAssertions(sp.isEncryptAssertions());
            registeredService.setRequiredAuthenticationContextClass(sp.getRequiredAuthenticationContextClass());
            registeredService.setRequiredNameIdFormat(sp.getRequiredNameIdFormat().getNameId());
            registeredService.setSkewAllowance(sp.getSkewAllowance());
            registeredService.setNameIdQualifier(sp.getNameIdQualifier());
            registeredService.setAssertionAudiences(sp.getAssertionAudiences());
            registeredService.setServiceProviderNameIdQualifier(sp.getServiceProviderNameIdQualifier());
            return registeredService;
        }
        return null;
    }

    public ClientAppTO fromRegisteredService(final RegisteredService registeredService) {
        return null;
    }
}
