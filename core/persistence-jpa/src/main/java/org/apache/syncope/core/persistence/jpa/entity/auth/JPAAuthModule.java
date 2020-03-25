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
package org.apache.syncope.core.persistence.jpa.entity.auth;

import org.apache.syncope.common.lib.types.AMImplementationType;
import org.apache.syncope.core.persistence.api.entity.Implementation;
import org.apache.syncope.core.persistence.jpa.entity.AbstractGeneratedKeyEntity;
import org.apache.syncope.core.persistence.jpa.entity.JPAImplementation;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import org.apache.syncope.core.persistence.api.entity.auth.AuthModule;

@Entity
@Table(name = JPAAuthModule.TABLE)
public class JPAAuthModule extends AbstractGeneratedKeyEntity implements AuthModule {

    public static final String TABLE = "AuthModule";

    private static final long serialVersionUID = 7422422526695279794L;

    @Column(unique = true, nullable = false)
    private String name;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = TABLE + "Conf",
            joinColumns =
            @JoinColumn(name = "authentication_module_id"),
            inverseJoinColumns =
            @JoinColumn(name = "implementation_id"))
    private List<JPAImplementation> configurations = new ArrayList<>();

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public List<? extends Implementation> getConfigurations() {
        return configurations;
    }

    @Override
    public boolean add(final Implementation configuration) {
        checkType(configuration, JPAImplementation.class);
        checkImplementationType(configuration, AMImplementationType.AUTH_MODULE_CONFIGURATIONS);
        return configurations.contains((JPAImplementation) configuration)
                || configurations.add((JPAImplementation) configuration);
    }
}
