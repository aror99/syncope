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
package org.apache.syncope.common.lib.authentication.module;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import org.apache.syncope.common.lib.to.ItemTO;

@XmlType
@XmlSeeAlso({ JaasAuthModuleConf.class, StaticAuthModuleConf.class, LDAPAuthModuleConf.class, OIDCAuthModuleConf.class,
    GoogleMfaAuthModuleConf.class, SAML2IdPAuthModuleConf.class })
public abstract class AbstractAuthModuleConf implements Serializable, AuthModuleConf {

    private static final long serialVersionUID = 4153200197344709778L;

    private String name;

    private int order;

    private List<ItemTO> profileItems = new ArrayList<>();

    public AbstractAuthModuleConf() {
        setName(getClass().getName());
    }

    @Override
    public final String getName() {
        return name;
    }

    public final void setName(final String name) {
        this.name = name;
    }

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(final int order) {
        this.order = order;
    }

    @XmlElementWrapper(name = "profileItems")
    @XmlElement(name = "profileItem")
    @JsonProperty("profileItems")
    @Override
    public List<ItemTO> getProfileItems() {
        return profileItems;
    }

}
