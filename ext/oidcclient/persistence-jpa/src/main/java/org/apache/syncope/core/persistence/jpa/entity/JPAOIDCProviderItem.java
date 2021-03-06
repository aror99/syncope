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
package org.apache.syncope.core.persistence.jpa.entity;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.apache.syncope.common.lib.types.IdMImplementationType;
import javax.persistence.UniqueConstraint;
import org.apache.syncope.core.persistence.api.entity.Implementation;
import org.apache.syncope.core.persistence.api.entity.OIDCProvider;
import org.apache.syncope.core.persistence.api.entity.OIDCProviderItem;
import org.apache.syncope.core.persistence.jpa.entity.resource.AbstractItem;

@Entity
@Table(name = JPAOIDCProviderItem.TABLE)
@Cacheable
public class JPAOIDCProviderItem extends AbstractItem implements OIDCProviderItem {

    public static final String TABLE = "OIDCProviderItem";

    private static final long serialVersionUID = -6903418265811089724L;

    @ManyToOne
    private JPAOIDCProvider op;

    @Override
    public OIDCProvider getOP() {
        return op;
    }

    @Override
    public void setOP(final OIDCProvider op) {
        checkType(op, JPAOIDCProvider.class);
        this.op = (JPAOIDCProvider) op;
    }

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = TABLE + "Transformer",
            joinColumns =
            @JoinColumn(name = "item_id"),
            inverseJoinColumns =
            @JoinColumn(name = "implementation_id"),
            uniqueConstraints =
            @UniqueConstraint(columnNames = { "item_id", "implementation_id" }))
    private List<JPAImplementation> transformers = new ArrayList<>();

    @Override
    public boolean add(final Implementation transformer) {
        checkType(transformer, JPAImplementation.class);
        checkImplementationType(transformer, IdMImplementationType.ITEM_TRANSFORMER);
        return transformers.contains((JPAImplementation) transformer)
                || this.transformers.add((JPAImplementation) transformer);
    }

    @Override
    public List<? extends Implementation> getTransformers() {
        return transformers;
    }
}
