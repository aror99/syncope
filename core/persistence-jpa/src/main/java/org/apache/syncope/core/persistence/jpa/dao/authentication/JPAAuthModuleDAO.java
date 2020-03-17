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
package org.apache.syncope.core.persistence.jpa.dao.authentication;

import org.apache.syncope.core.persistence.api.entity.Implementation;
import org.apache.syncope.core.persistence.jpa.dao.AbstractDAO;
import org.apache.syncope.core.persistence.jpa.entity.authentication.JPAAuthModule;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.TypedQuery;
import java.util.List;
import org.apache.syncope.core.persistence.api.entity.authentication.AuthModule;
import org.apache.syncope.core.persistence.api.dao.authentication.AuthModuleDAO;

@Repository
public class JPAAuthModuleDAO extends AbstractDAO<AuthModule> implements AuthModuleDAO {

    @Override
    public AuthModule find(final String key) {
        return entityManager().find(JPAAuthModule.class, key);
    }

    @Transactional(readOnly = true)
    @Override
    public List<AuthModule> findAll() {
        TypedQuery<AuthModule> query = entityManager().createQuery("SELECT e FROM " + JPAAuthModule.class.
                getSimpleName() + " e", AuthModule.class);

        return query.getResultList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<AuthModule> findByConfiguration(final Implementation configuration) {
        TypedQuery<AuthModule> query = entityManager().createQuery("SELECT e FROM " + JPAAuthModule.class.
                getSimpleName() + " e "
                + "WHERE :configuration MEMBER OF e.configurations", AuthModule.class);
        query.setParameter("configuration", configuration);
        return query.getResultList();
    }

    @Override
    public AuthModule save(final AuthModule authModule) {
        return entityManager().merge(authModule);
    }

    @Override
    public void delete(final String key) {
        AuthModule authModule = find(key);
        if (authModule == null) {
            return;
        }

        delete(authModule);
    }

    @Override
    public void delete(final AuthModule authModule) {
        entityManager().remove(authModule);
    }

}
