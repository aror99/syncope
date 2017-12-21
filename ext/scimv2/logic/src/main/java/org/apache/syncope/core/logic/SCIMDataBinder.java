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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.apache.syncope.common.lib.EntityTOUtils;
import org.apache.syncope.common.lib.SyncopeConstants;
import org.apache.syncope.common.lib.scim.SCIMComplexConf;
import org.apache.syncope.common.lib.scim.SCIMConf;
import org.apache.syncope.common.lib.scim.SCIMUserAddressConf;
import org.apache.syncope.common.lib.to.AttrTO;
import org.apache.syncope.common.lib.to.GroupTO;
import org.apache.syncope.common.lib.to.MembershipTO;
import org.apache.syncope.common.lib.to.UserTO;
import org.apache.syncope.core.logic.scim.SCIMConfManager;
import org.apache.syncope.core.persistence.api.dao.AnyDAO;
import org.apache.syncope.core.persistence.api.dao.search.MembershipCond;
import org.apache.syncope.core.persistence.api.dao.search.OrderByClause;
import org.apache.syncope.core.persistence.api.dao.search.SearchCond;
import org.apache.syncope.core.spring.security.AuthDataAccessor;
import org.apache.syncope.core.spring.security.SyncopeGrantedAuthority;
import org.apache.syncope.ext.scimv2.api.BadRequestException;
import org.apache.syncope.ext.scimv2.api.data.Value;
import org.apache.syncope.ext.scimv2.api.data.Group;
import org.apache.syncope.ext.scimv2.api.data.Member;
import org.apache.syncope.ext.scimv2.api.data.Meta;
import org.apache.syncope.ext.scimv2.api.data.SCIMComplexValue;
import org.apache.syncope.ext.scimv2.api.data.SCIMEnterpriseInfo;
import org.apache.syncope.ext.scimv2.api.data.SCIMGroup;
import org.apache.syncope.ext.scimv2.api.data.SCIMUser;
import org.apache.syncope.ext.scimv2.api.data.SCIMUserAddress;
import org.apache.syncope.ext.scimv2.api.data.SCIMUserManager;
import org.apache.syncope.ext.scimv2.api.data.SCIMUserName;
import org.apache.syncope.ext.scimv2.api.type.ErrorType;
import org.apache.syncope.ext.scimv2.api.type.Function;
import org.apache.syncope.ext.scimv2.api.type.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SCIMDataBinder {

    protected static final Logger LOG = LoggerFactory.getLogger(SCIMDataBinder.class);

    private static final List<String> USER_SCHEMAS = Collections.singletonList(Resource.User.schema());

    private static final List<String> ENTERPRISE_USER_SCHEMAS =
            Arrays.asList(Resource.User.schema(), Resource.EnterpriseUser.schema());

    private static final List<String> GROUP_SCHEMAS = Collections.singletonList(Resource.Group.schema());

    @Autowired
    private SCIMConfManager confManager;

    @Autowired
    private UserLogic userLogic;

    @Autowired
    private AuthDataAccessor authDataAccessor;

    private <E extends Enum<?>> void fill(
            final Map<String, AttrTO> attrs,
            final List<SCIMComplexConf<E>> confs,
            final List<SCIMComplexValue> values) {

        for (SCIMComplexConf<?> conf : confs) {
            SCIMComplexValue value = new SCIMComplexValue();

            if (conf.getValue() != null && attrs.containsKey(conf.getValue())) {
                value.setValue(attrs.get(conf.getValue()).getValues().get(0));
            }
            if (conf.getDisplay() != null && attrs.containsKey(conf.getDisplay())) {
                value.setDisplay(attrs.get(conf.getDisplay()).getValues().get(0));
            }
            if (conf.getType() != null) {
                value.setType(conf.getType().name());
            }
            if (conf.isPrimary()) {
                value.setPrimary(true);
            }

            if (!value.isEmpty()) {
                values.add(value);
            }
        }
    }

    private boolean output(
            final List<String> attributes,
            final List<String> excludedAttributes,
            final String schema) {

        return (attributes.isEmpty() || attributes.contains(schema))
                && (excludedAttributes.isEmpty() || !excludedAttributes.contains(schema));
    }

    private <T> T output(
            final List<String> attributes,
            final List<String> excludedAttributes,
            final String schema,
            final T value) {

        return output(attributes, excludedAttributes, schema)
                ? value
                : null;
    }

    public SCIMUser toSCIMUser(
            final UserTO userTO,
            final String location,
            final List<String> attributes,
            final List<String> excludedAttributes) {

        SCIMConf conf = confManager.get();

        List<String> schemas = new ArrayList<>();
        schemas.add(Resource.User.schema());
        if (conf.getEnterpriseUserConf() != null) {
            schemas.add(Resource.EnterpriseUser.schema());
        }

        SCIMUser user = new SCIMUser(
                userTO.getKey(),
                schemas,
                new Meta(
                        Resource.User,
                        userTO.getCreationDate(),
                        userTO.getLastChangeDate() == null
                        ? userTO.getCreationDate() : userTO.getLastChangeDate(),
                        userTO.getETagValue(),
                        location),
                output(attributes, excludedAttributes, "userName", userTO.getUsername()),
                !userTO.isSuspended());

        Map<String, AttrTO> attrs = new HashMap<>();
        attrs.putAll(EntityTOUtils.buildAttrMap(userTO.getPlainAttrs()));
        attrs.putAll(EntityTOUtils.buildAttrMap(userTO.getDerAttrs()));
        attrs.putAll(EntityTOUtils.buildAttrMap(userTO.getVirAttrs()));

        if (conf.getUserConf() != null) {
            if (output(attributes, excludedAttributes, "name") && conf.getUserConf().getName() != null) {
                SCIMUserName name = new SCIMUserName();

                if (conf.getUserConf().getName().getFamilyName() != null
                        && attrs.containsKey(conf.getUserConf().getName().getFamilyName())) {

                    name.setFamilyName(attrs.get(conf.getUserConf().getName().getFamilyName()).getValues().get(0));
                }
                if (conf.getUserConf().getName().getFormatted() != null
                        && attrs.containsKey(conf.getUserConf().getName().getFormatted())) {

                    name.setFormatted(attrs.get(conf.getUserConf().getName().getFormatted()).getValues().get(0));
                }
                if (conf.getUserConf().getName().getGivenName() != null
                        && attrs.containsKey(conf.getUserConf().getName().getGivenName())) {

                    name.setGivenName(attrs.get(conf.getUserConf().getName().getGivenName()).getValues().get(0));
                }
                if (conf.getUserConf().getName().getHonorificPrefix() != null
                        && attrs.containsKey(conf.getUserConf().getName().getHonorificPrefix())) {

                    name.setHonorificPrefix(
                            attrs.get(conf.getUserConf().getName().getHonorificPrefix()).getValues().get(0));
                }
                if (conf.getUserConf().getName().getHonorificSuffix() != null
                        && attrs.containsKey(conf.getUserConf().getName().getHonorificSuffix())) {

                    name.setHonorificSuffix(
                            attrs.get(conf.getUserConf().getName().getHonorificSuffix()).getValues().get(0));
                }
                if (conf.getUserConf().getName().getMiddleName() != null
                        && attrs.containsKey(conf.getUserConf().getName().getMiddleName())) {

                    name.setMiddleName(attrs.get(conf.getUserConf().getName().getMiddleName()).getValues().get(0));
                }

                if (!name.isEmpty()) {
                    user.setName(name);
                }
            }

            if (output(attributes, excludedAttributes, "displayName")
                    && conf.getUserConf().getDisplayName() != null
                    && attrs.containsKey(conf.getUserConf().getDisplayName())) {

                user.setDisplayName(attrs.get(conf.getUserConf().getDisplayName()).getValues().get(0));
            }
            if (output(attributes, excludedAttributes, "nickName")
                    && conf.getUserConf().getNickName() != null
                    && attrs.containsKey(conf.getUserConf().getNickName())) {

                user.setNickName(attrs.get(conf.getUserConf().getNickName()).getValues().get(0));
            }
            if (output(attributes, excludedAttributes, "profileUrl")
                    && conf.getUserConf().getProfileUrl() != null
                    && attrs.containsKey(conf.getUserConf().getProfileUrl())) {

                user.setProfileUrl(attrs.get(conf.getUserConf().getProfileUrl()).getValues().get(0));
            }
            if (output(attributes, excludedAttributes, "title")
                    && conf.getUserConf().getTitle() != null
                    && attrs.containsKey(conf.getUserConf().getTitle())) {

                user.setTitle(attrs.get(conf.getUserConf().getTitle()).getValues().get(0));
            }
            if (output(attributes, excludedAttributes, "userType")
                    && conf.getUserConf().getUserType() != null
                    && attrs.containsKey(conf.getUserConf().getUserType())) {

                user.setUserType(attrs.get(conf.getUserConf().getUserType()).getValues().get(0));
            }
            if (output(attributes, excludedAttributes, "preferredLanguage")
                    && conf.getUserConf().getPreferredLanguage() != null
                    && attrs.containsKey(conf.getUserConf().getPreferredLanguage())) {

                user.setPreferredLanguage(attrs.get(conf.getUserConf().getPreferredLanguage()).getValues().get(0));
            }
            if (output(attributes, excludedAttributes, "locale")
                    && conf.getUserConf().getLocale() != null
                    && attrs.containsKey(conf.getUserConf().getLocale())) {

                user.setLocale(attrs.get(conf.getUserConf().getLocale()).getValues().get(0));
            }
            if (output(attributes, excludedAttributes, "timezone")
                    && conf.getUserConf().getTimezone() != null
                    && attrs.containsKey(conf.getUserConf().getTimezone())) {

                user.setTimezone(attrs.get(conf.getUserConf().getTimezone()).getValues().get(0));
            }

            if (output(attributes, excludedAttributes, "emails")) {
                fill(attrs, conf.getUserConf().getEmails(), user.getEmails());
            }
            if (output(attributes, excludedAttributes, "phoneNumbers")) {
                fill(attrs, conf.getUserConf().getPhoneNumbers(), user.getPhoneNumbers());
            }
            if (output(attributes, excludedAttributes, "ims")) {
                fill(attrs, conf.getUserConf().getIms(), user.getIms());
            }
            if (output(attributes, excludedAttributes, "photos")) {
                fill(attrs, conf.getUserConf().getPhotos(), user.getPhotos());
            }
            if (output(attributes, excludedAttributes, "addresses")) {
                for (SCIMUserAddressConf addressConf : conf.getUserConf().getAddresses()) {
                    SCIMUserAddress address = new SCIMUserAddress();

                    if (addressConf.getFormatted() != null && attrs.containsKey(addressConf.getFormatted())) {
                        address.setFormatted(attrs.get(addressConf.getFormatted()).getValues().get(0));
                    }
                    if (addressConf.getStreetAddress() != null && attrs.containsKey(addressConf.getStreetAddress())) {
                        address.setStreetAddress(attrs.get(addressConf.getStreetAddress()).getValues().get(0));
                    }
                    if (addressConf.getLocality() != null && attrs.containsKey(addressConf.getLocality())) {
                        address.setLocality(attrs.get(addressConf.getLocality()).getValues().get(0));
                    }
                    if (addressConf.getRegion() != null && attrs.containsKey(addressConf.getRegion())) {
                        address.setRegion(attrs.get(addressConf.getRegion()).getValues().get(0));
                    }
                    if (addressConf.getCountry() != null && attrs.containsKey(addressConf.getCountry())) {
                        address.setCountry(attrs.get(addressConf.getCountry()).getValues().get(0));
                    }
                    if (addressConf.getType() != null) {
                        address.setType(addressConf.getType().name());
                    }
                    if (addressConf.isPrimary()) {
                        address.setPrimary(true);
                    }

                    if (!address.isEmpty()) {
                        user.getAddresses().add(address);
                    }
                }
            }
            if (output(attributes, excludedAttributes, "x509Certificates")) {
                for (String certificate : conf.getUserConf().getX509Certificates()) {
                    if (attrs.containsKey(certificate)) {
                        user.getX509Certificates().add(new Value(attrs.get(certificate).getValues().get(0)));
                    }
                }
            }
        }

        if (conf.getEnterpriseUserConf() != null) {
            SCIMEnterpriseInfo enterpriseInfo = new SCIMEnterpriseInfo();

            if (output(attributes, excludedAttributes, "employeeNumber")
                    && conf.getEnterpriseUserConf().getEmployeeNumber() != null
                    && attrs.containsKey(conf.getEnterpriseUserConf().getEmployeeNumber())) {

                enterpriseInfo.setEmployeeNumber(
                        attrs.get(conf.getEnterpriseUserConf().getEmployeeNumber()).getValues().get(0));
            }
            if (output(attributes, excludedAttributes, "costCenter")
                    && conf.getEnterpriseUserConf().getCostCenter() != null
                    && attrs.containsKey(conf.getEnterpriseUserConf().getCostCenter())) {

                enterpriseInfo.setCostCenter(
                        attrs.get(conf.getEnterpriseUserConf().getCostCenter()).getValues().get(0));
            }
            if (output(attributes, excludedAttributes, "organization")
                    && conf.getEnterpriseUserConf().getOrganization() != null
                    && attrs.containsKey(conf.getEnterpriseUserConf().getOrganization())) {

                enterpriseInfo.setOrganization(
                        attrs.get(conf.getEnterpriseUserConf().getOrganization()).getValues().get(0));
            }
            if (output(attributes, excludedAttributes, "division")
                    && conf.getEnterpriseUserConf().getDivision() != null
                    && attrs.containsKey(conf.getEnterpriseUserConf().getDivision())) {

                enterpriseInfo.setDivision(
                        attrs.get(conf.getEnterpriseUserConf().getDivision()).getValues().get(0));
            }
            if (output(attributes, excludedAttributes, "department")
                    && conf.getEnterpriseUserConf().getDepartment() != null
                    && attrs.containsKey(conf.getEnterpriseUserConf().getDepartment())) {

                enterpriseInfo.setDepartment(
                        attrs.get(conf.getEnterpriseUserConf().getDepartment()).getValues().get(0));
            }
            if (output(attributes, excludedAttributes, "manager")
                    && conf.getEnterpriseUserConf().getManager() != null) {

                SCIMUserManager manager = new SCIMUserManager();

                if (conf.getEnterpriseUserConf().getManager().getKey() != null
                        && attrs.containsKey(conf.getEnterpriseUserConf().getManager().getKey())) {

                    try {
                        UserTO userManager = userLogic.read(
                                attrs.get(conf.getEnterpriseUserConf().getManager().getKey()).getValues().get(0));
                        manager.setValue(userManager.getKey());
                        manager.setRef(
                                StringUtils.substringBefore(location, "/Users") + "/Users/" + userManager.getKey());

                        if (conf.getEnterpriseUserConf().getManager().getDisplayName() != null) {
                            AttrTO displayName = userManager.getPlainAttr(
                                    conf.getEnterpriseUserConf().getManager().getDisplayName());
                            if (displayName == null) {
                                displayName = userManager.getDerAttr(
                                        conf.getEnterpriseUserConf().getManager().getDisplayName());
                            }
                            if (displayName == null) {
                                displayName = userManager.getVirAttr(
                                        conf.getEnterpriseUserConf().getManager().getDisplayName());
                            }
                            if (displayName != null) {
                                manager.setDisplayName(displayName.getValues().get(0));
                            }
                        }
                    } catch (Exception e) {
                        LOG.error("Could not read user {}", conf.getEnterpriseUserConf().getManager().getKey(), e);
                    }
                }

                if (!manager.isEmpty()) {
                    enterpriseInfo.setManager(manager);
                }
            }

            if (!enterpriseInfo.isEmpty()) {
                user.setEnterpriseInfo(enterpriseInfo);
            }
        }

        if (output(attributes, excludedAttributes, "groups")) {
            for (MembershipTO membership : userTO.getMemberships()) {
                user.getGroups().add(new Group(
                        membership.getGroupKey(),
                        StringUtils.substringBefore(location, "/Users") + "/Groups/" + membership.getGroupKey(),
                        membership.getGroupName(),
                        Function.direct));
            }
            for (MembershipTO membership : userTO.getDynMemberships()) {
                user.getGroups().add(new Group(
                        membership.getGroupKey(),
                        StringUtils.substringBefore(location, "/Users") + "/Groups/" + membership.getGroupKey(),
                        membership.getGroupName(),
                        Function.indirect));
            }
        }

        if (output(attributes, excludedAttributes, "entitlements")) {
            for (SyncopeGrantedAuthority authority : authDataAccessor.getAuthorities(userTO.getUsername())) {
                user.getEntitlements().add(
                        new Value(authority.getAuthority() + " on Realm(s) " + authority.getRealms()));
            }
        }

        if (output(attributes, excludedAttributes, "roles")) {
            for (String role : userTO.getRoles()) {
                user.getRoles().add(new Value(role));
            }
        }

        return user;
    }

    private <E extends Enum<?>> void fill(
            final Set<AttrTO> attrs,
            final List<SCIMComplexConf<E>> confs,
            final List<SCIMComplexValue> values) {

        for (final SCIMComplexValue value : values) {
            if (value.getType() != null) {
                SCIMComplexConf<E> conf = IterableUtils.find(confs, new Predicate<SCIMComplexConf<E>>() {

                    @Override
                    public boolean evaluate(final SCIMComplexConf<E> object) {
                        return value.getType().equals(object.getType().name());
                    }
                });
                if (conf != null) {
                    attrs.add(new AttrTO.Builder().schema(conf.getValue()).value(value.getValue()).build());
                }
            }
        }
    }

    public UserTO toUserTO(final SCIMUser user) {
        if (!USER_SCHEMAS.equals(user.getSchemas()) && !ENTERPRISE_USER_SCHEMAS.equals(user.getSchemas())) {
            throw new BadRequestException(ErrorType.invalidValue);
        }

        UserTO userTO = new UserTO();
        userTO.setRealm(SyncopeConstants.ROOT_REALM);
        userTO.setKey(user.getId());
        userTO.setPassword(user.getPassword());
        userTO.setUsername(user.getUserName());

        SCIMConf conf = confManager.get();

        if (conf.getUserConf() != null) {
            if (conf.getUserConf().getName() != null && user.getName() != null) {
                if (conf.getUserConf().getName().getFamilyName() != null
                        && user.getName().getFamilyName() != null) {

                    userTO.getPlainAttrs().add(new AttrTO.Builder().
                            schema(conf.getUserConf().getName().getFamilyName()).
                            value(user.getName().getFamilyName()).build());
                }
                if (conf.getUserConf().getName().getFormatted() != null
                        && user.getName().getFormatted() != null) {

                    userTO.getPlainAttrs().add(new AttrTO.Builder().
                            schema(conf.getUserConf().getName().getFormatted()).
                            value(user.getName().getFormatted()).build());
                }
                if (conf.getUserConf().getName().getGivenName() != null
                        && user.getName().getGivenName() != null) {

                    userTO.getPlainAttrs().add(new AttrTO.Builder().
                            schema(conf.getUserConf().getName().getGivenName()).
                            value(user.getName().getGivenName()).build());
                }
                if (conf.getUserConf().getName().getHonorificPrefix() != null
                        && user.getName().getHonorificPrefix() != null) {

                    userTO.getPlainAttrs().add(new AttrTO.Builder().
                            schema(conf.getUserConf().getName().getHonorificPrefix()).
                            value(user.getName().getHonorificPrefix()).build());
                }
                if (conf.getUserConf().getName().getHonorificSuffix() != null
                        && user.getName().getHonorificSuffix() != null) {

                    userTO.getPlainAttrs().add(new AttrTO.Builder().
                            schema(conf.getUserConf().getName().getHonorificSuffix()).
                            value(user.getName().getHonorificSuffix()).build());
                }
                if (conf.getUserConf().getName().getMiddleName() != null
                        && user.getName().getMiddleName() != null) {

                    userTO.getPlainAttrs().add(new AttrTO.Builder().
                            schema(conf.getUserConf().getName().getMiddleName()).
                            value(user.getName().getMiddleName()).build());
                }
            }

            if (conf.getUserConf().getDisplayName() != null && user.getDisplayName() != null) {
                userTO.getPlainAttrs().add(new AttrTO.Builder().
                        schema(conf.getUserConf().getDisplayName()).value(user.getDisplayName()).build());
            }
            if (conf.getUserConf().getNickName() != null && user.getNickName() != null) {
                userTO.getPlainAttrs().add(new AttrTO.Builder().
                        schema(conf.getUserConf().getNickName()).value(user.getNickName()).build());
            }
            if (conf.getUserConf().getProfileUrl() != null && user.getProfileUrl() != null) {
                userTO.getPlainAttrs().add(new AttrTO.Builder().
                        schema(conf.getUserConf().getProfileUrl()).value(user.getProfileUrl()).build());
            }
            if (conf.getUserConf().getTitle() != null && user.getTitle() != null) {
                userTO.getPlainAttrs().add(new AttrTO.Builder().
                        schema(conf.getUserConf().getTitle()).value(user.getTitle()).build());
            }
            if (conf.getUserConf().getUserType() != null && user.getUserType() != null) {
                userTO.getPlainAttrs().add(new AttrTO.Builder().
                        schema(conf.getUserConf().getUserType()).value(user.getUserType()).build());
            }
            if (conf.getUserConf().getPreferredLanguage() != null && user.getPreferredLanguage() != null) {
                userTO.getPlainAttrs().add(new AttrTO.Builder().
                        schema(conf.getUserConf().getPreferredLanguage()).value(user.getPreferredLanguage()).build());
            }
            if (conf.getUserConf().getLocale() != null && user.getLocale() != null) {
                userTO.getPlainAttrs().add(new AttrTO.Builder().
                        schema(conf.getUserConf().getLocale()).value(user.getLocale()).build());
            }
            if (conf.getUserConf().getTimezone() != null && user.getTimezone() != null) {
                userTO.getPlainAttrs().add(new AttrTO.Builder().
                        schema(conf.getUserConf().getTimezone()).value(user.getTimezone()).build());
            }

            fill(userTO.getPlainAttrs(), conf.getUserConf().getEmails(), user.getEmails());
            fill(userTO.getPlainAttrs(), conf.getUserConf().getPhoneNumbers(), user.getPhoneNumbers());
            fill(userTO.getPlainAttrs(), conf.getUserConf().getIms(), user.getIms());
            fill(userTO.getPlainAttrs(), conf.getUserConf().getPhotos(), user.getPhotos());

            for (final SCIMUserAddress address : user.getAddresses()) {
                if (address.getType() != null) {
                    SCIMUserAddressConf addressConf = IterableUtils.find(conf.getUserConf().getAddresses(),
                            new Predicate<SCIMUserAddressConf>() {

                        @Override
                        public boolean evaluate(final SCIMUserAddressConf object) {
                            return address.getType().equals(object.getType().name());
                        }
                    });
                    if (addressConf != null) {
                        if (addressConf.getFormatted() != null && address.getFormatted() != null) {
                            userTO.getPlainAttrs().add(new AttrTO.Builder().
                                    schema(addressConf.getFormatted()).value(address.getFormatted()).build());
                        }
                        if (addressConf.getStreetAddress() != null && address.getStreetAddress() != null) {
                            userTO.getPlainAttrs().add(new AttrTO.Builder().
                                    schema(addressConf.getStreetAddress()).value(address.getStreetAddress()).build());
                        }
                        if (addressConf.getLocality() != null && address.getLocality() != null) {
                            userTO.getPlainAttrs().add(new AttrTO.Builder().
                                    schema(addressConf.getLocality()).value(address.getLocality()).build());
                        }
                        if (addressConf.getRegion() != null && address.getFormatted() != null) {
                            userTO.getPlainAttrs().add(new AttrTO.Builder().
                                    schema(addressConf.getFormatted()).value(address.getFormatted()).build());
                        }
                        if (addressConf.getPostalCode() != null && address.getPostalCode() != null) {
                            userTO.getPlainAttrs().add(new AttrTO.Builder().
                                    schema(addressConf.getPostalCode()).value(address.getPostalCode()).build());
                        }
                        if (addressConf.getCountry() != null && address.getCountry() != null) {
                            userTO.getPlainAttrs().add(new AttrTO.Builder().
                                    schema(addressConf.getCountry()).value(address.getCountry()).build());
                        }
                    }
                }
            }

            for (int i = 0; i < user.getX509Certificates().size(); i++) {
                Value certificate = user.getX509Certificates().get(i);
                if (conf.getUserConf().getX509Certificates().size() > i) {
                    userTO.getPlainAttrs().add(new AttrTO.Builder().
                            schema(conf.getUserConf().getX509Certificates().get(i)).
                            value(certificate.getValue()).build());
                }
            }
        }

        if (conf.getEnterpriseUserConf() != null) {
            if (conf.getEnterpriseUserConf().getEmployeeNumber() != null
                    && user.getEnterpriseInfo().getEmployeeNumber() != null) {

                userTO.getPlainAttrs().add(new AttrTO.Builder().
                        schema(conf.getEnterpriseUserConf().getEmployeeNumber()).
                        value(user.getEnterpriseInfo().getEmployeeNumber()).build());
            }
            if (conf.getEnterpriseUserConf().getCostCenter() != null
                    && user.getEnterpriseInfo().getCostCenter() != null) {

                userTO.getPlainAttrs().add(new AttrTO.Builder().
                        schema(conf.getEnterpriseUserConf().getCostCenter()).
                        value(user.getEnterpriseInfo().getCostCenter()).build());
            }
            if (conf.getEnterpriseUserConf().getOrganization() != null
                    && user.getEnterpriseInfo().getOrganization() != null) {

                userTO.getPlainAttrs().add(new AttrTO.Builder().
                        schema(conf.getEnterpriseUserConf().getOrganization()).
                        value(user.getEnterpriseInfo().getOrganization()).build());
            }
            if (conf.getEnterpriseUserConf().getDivision() != null
                    && user.getEnterpriseInfo().getDivision() != null) {

                userTO.getPlainAttrs().add(new AttrTO.Builder().
                        schema(conf.getEnterpriseUserConf().getDivision()).
                        value(user.getEnterpriseInfo().getDivision()).build());
            }
            if (conf.getEnterpriseUserConf().getDepartment() != null
                    && user.getEnterpriseInfo().getDepartment() != null) {

                userTO.getPlainAttrs().add(new AttrTO.Builder().
                        schema(conf.getEnterpriseUserConf().getDepartment()).
                        value(user.getEnterpriseInfo().getDepartment()).build());
            }
            if (conf.getEnterpriseUserConf().getManager() != null
                    && conf.getEnterpriseUserConf().getManager().getKey() != null
                    && user.getEnterpriseInfo().getManager() != null
                    && user.getEnterpriseInfo().getManager().getValue() != null) {

                userTO.getPlainAttrs().add(new AttrTO.Builder().
                        schema(conf.getEnterpriseUserConf().getManager().getKey()).
                        value(user.getEnterpriseInfo().getManager().getValue()).build());
            }
        }

        for (Group group : user.getGroups()) {
            userTO.getMemberships().add(new MembershipTO.Builder().group(group.getValue()).build());
        }

        for (Value role : user.getRoles()) {
            userTO.getRoles().add(role.getValue());
        }

        return userTO;
    }

    public SCIMGroup toSCIMGroup(
            final GroupTO groupTO,
            final String location,
            final List<String> attributes,
            final List<String> excludedAttributes) {

        SCIMGroup group = new SCIMGroup(
                groupTO.getKey(),
                new Meta(
                        Resource.Group,
                        groupTO.getCreationDate(),
                        groupTO.getLastChangeDate() == null
                        ? groupTO.getCreationDate() : groupTO.getLastChangeDate(),
                        groupTO.getETagValue(),
                        location),
                output(attributes, excludedAttributes, "displayName", groupTO.getName()));

        if (output(attributes, excludedAttributes, "members")) {
            MembershipCond membCond = new MembershipCond();
            membCond.setGroup(groupTO.getKey());
            SearchCond searchCond = SearchCond.getLeafCond(membCond);

            int count = userLogic.search(searchCond,
                    1, 1, Collections.<OrderByClause>emptyList(),
                    SyncopeConstants.ROOT_REALM, false).getLeft();

            for (int page = 1; page <= (count / AnyDAO.DEFAULT_PAGE_SIZE) + 1; page++) {
                List<UserTO> users = userLogic.search(
                        searchCond,
                        page,
                        AnyDAO.DEFAULT_PAGE_SIZE,
                        Collections.<OrderByClause>emptyList(),
                        SyncopeConstants.ROOT_REALM,
                        false).
                        getRight();
                for (UserTO userTO : users) {
                    group.getMembers().add(new Member(
                            userTO.getKey(),
                            StringUtils.substringBefore(location, "/Groups") + "/Users/" + userTO.getKey(),
                            userTO.getUsername()));
                }
            }
        }

        return group;
    }

    public GroupTO toGroupTO(final SCIMGroup group) {
        if (!GROUP_SCHEMAS.equals(group.getSchemas())) {
            throw new BadRequestException(ErrorType.invalidValue);
        }

        GroupTO groupTO = new GroupTO();
        groupTO.setRealm(SyncopeConstants.ROOT_REALM);
        groupTO.setKey(group.getId());
        groupTO.setName(group.getDisplayName());
        return groupTO;
    }

}