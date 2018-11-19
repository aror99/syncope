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
package org.apache.syncope.core.persistence.jpa.entity.anyobject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.apache.syncope.core.persistence.api.entity.Membership;
import org.apache.syncope.core.persistence.api.entity.anyobject.AMembership;
import org.apache.syncope.core.persistence.api.entity.anyobject.APlainAttr;
import org.apache.syncope.core.persistence.api.entity.anyobject.AnyObject;
import org.apache.syncope.core.persistence.jpa.entity.PGPlainAttr;
import org.apache.syncope.core.persistence.jpa.entity.PGJPAAny;
import org.apache.syncope.core.persistence.jpa.validation.entity.PGJPAAnyCheck;

@Entity
@Table(name = JPAAnyObject.TABLE)
@EntityListeners({ PGJPAAnyObjectListener.class })
@PGJPAAnyCheck
public class PGJPAAnyObject extends JPAAnyObject implements PGJPAAny<AnyObject>, AnyObject {

    private static final long serialVersionUID = -8543654943709531885L;

    @Lob
    @Column(columnDefinition = "jsonb")
    private String plainAttrs;

    @Transient
    private final List<PGAPlainAttr> plainAttrList = new ArrayList<>();

    @Override
    public String getPlainAttrsJSON() {
        return plainAttrs;
    }

    @Override
    public void setPlainAttrsJSON(final String plainAttrs) {
        this.plainAttrs = plainAttrs;
    }

    @Override
    public List<PGAPlainAttr> getPlainAttrList() {
        return plainAttrList;
    }

    @Override
    public boolean add(final PGPlainAttr<AnyObject> attr) {
        return add((APlainAttr) attr);
    }

    @Override
    public boolean add(final APlainAttr attr) {
        checkType(attr, PGAPlainAttr.class);
        return plainAttrList.add((PGAPlainAttr) attr);
    }

    @Override
    public boolean remove(final APlainAttr attr) {
        return plainAttrList.removeIf(pgattr
                -> pgattr.getSchemaKey().equals(attr.getSchema().getKey())
                && Objects.equals(pgattr.getMembershipKey(), ((PGAPlainAttr) attr).getMembershipKey()));
    }

    @Override
    protected List<? extends APlainAttr> internalGetPlainAttrs() {
        return plainAttrList;
    }

    @Override
    public List<? extends APlainAttr> getPlainAttrs() {
        return plainAttrList.stream().
                filter(pgattr -> pgattr.getMembershipKey() == null).
                collect(Collectors.toList());
    }

    @Override
    public Optional<? extends APlainAttr> getPlainAttr(final String plainSchema) {
        return plainAttrList.stream().
                filter(pgattr -> pgattr.getSchemaKey() != null && pgattr.getSchemaKey().equals(plainSchema)
                && pgattr.getMembershipKey() == null).
                findFirst();
    }

    @Override
    public Optional<? extends APlainAttr> getPlainAttr(final String plainSchema, final Membership<?> membership) {
        return plainAttrList.stream().
                filter(pgattr -> pgattr.getSchemaKey() != null && pgattr.getSchemaKey().equals(plainSchema)
                && pgattr.getMembershipKey() != null && pgattr.getMembershipKey().equals(membership.getKey())).
                findFirst();
    }

    @Override
    public boolean remove(final AMembership membership) {
        plainAttrList.removeIf(pgattr
                -> pgattr.getMembershipKey() != null && pgattr.getMembershipKey().equals(membership.getKey()));
        return super.remove(membership);
    }
}
