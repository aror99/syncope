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

package org.apache.syncope.core.logic;

import org.apache.syncope.common.lib.to.ClientApplicationTO;

import java.lang.reflect.Method;
import java.util.List;

public abstract class AbstractClientApplicationLogic extends AbstractTransactionalLogic<ClientApplicationTO> {

    @Override
    protected ClientApplicationTO resolveReference(final Method method, final Object... args)
        throws UnresolvedReferenceException {
        throw new UnresolvedReferenceException();
    }

    public abstract ClientApplicationTO delete(String key);

    public abstract List<ClientApplicationTO> list();

    public abstract ClientApplicationTO read(String key);

    public abstract ClientApplicationTO create(ClientApplicationTO applicationTO);

    public abstract ClientApplicationTO update(ClientApplicationTO applicationTO);

}