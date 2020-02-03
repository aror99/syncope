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
package org.apache.syncope.client.console;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.security.AccessControlException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.ws.rs.BadRequestException;
import org.apache.commons.lang3.StringUtils;
import org.apache.syncope.client.console.pages.Dashboard;
import org.apache.syncope.client.console.pages.Login;
import org.apache.syncope.common.lib.SyncopeClientCompositeException;
import org.apache.syncope.common.lib.SyncopeClientException;
import org.apache.syncope.common.lib.types.ClientExceptionType;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.util.tester.FormTester;
import org.junit.jupiter.api.Test;

public class SyncopeConsoleApplicationTest extends AbstractTest {

    private Map<String, String> getConfiguredSecurityHeaders() throws IOException {
        Map<String, String> securityHeaders = new HashMap<>();

        @SuppressWarnings("unchecked")
        Enumeration<String> propNames = (Enumeration<String>) PROPS.propertyNames();
        while (propNames.hasMoreElements()) {
            String name = propNames.nextElement();
            if (name.startsWith("security.headers.")) {
                securityHeaders.put(StringUtils.substringAfter(name, "security.headers."), PROPS.getProperty(name));
            }
        }

        return securityHeaders;
    }

    @Test
    public void securityHeaders() throws IOException {
        Map<String, String> securityHeaders = getConfiguredSecurityHeaders();
        assertEquals(4, securityHeaders.size());

        // 1. anonymous
        TESTER.startPage(Login.class);
        TESTER.assertRenderedPage(Login.class);
        securityHeaders.forEach((key, value) -> assertEquals(value, TESTER.getLastResponse().getHeader(key)));

        // 2. authenticated
        FormTester formTester = TESTER.newFormTester("login");
        formTester.setValue("username", "username");
        formTester.setValue("password", "password");
        formTester.submit("submit");

        TESTER.assertRenderedPage(Dashboard.class);
        securityHeaders.forEach((key, value) -> assertEquals(value, TESTER.getLastResponse().getHeader(key)));
    }

    @Test
    public void errors() {
        SyncopeConsoleSession session = SyncopeConsoleSession.get();

        assertNull(session.getFeedbackMessages().first());

        session.onException(new AccessControlException("JWT Expired"));
        FeedbackMessage message = session.getFeedbackMessages().first();
        assertNotNull(message);
        assertTrue(message.isError());
        assertEquals(SyncopeConsoleSession.Error.SESSION_EXPIRED.fallback(), message.getMessage());
        session.getFeedbackMessages().clear();

        session.onException(new AccessControlException("Auth Exception"));
        message = session.getFeedbackMessages().first();
        assertNotNull(message);
        assertTrue(message.isError());
        assertEquals(SyncopeConsoleSession.Error.AUTHORIZATION.fallback(), message.getMessage());
        session.getFeedbackMessages().clear();

        session.onException(new BadRequestException());
        message = session.getFeedbackMessages().first();
        assertNotNull(message);
        assertTrue(message.isError());
        assertEquals(SyncopeConsoleSession.Error.REST.fallback(), message.getMessage());
        session.getFeedbackMessages().clear();

        SyncopeClientException sce = SyncopeClientException.build(ClientExceptionType.InvalidUser);
        sce.getElements().add("Error 1");
        session.onException(sce);
        message = session.getFeedbackMessages().first();
        assertNotNull(message);
        assertTrue(message.isError());
        assertEquals("Error 1", message.getMessage());
        session.getFeedbackMessages().clear();

        sce = SyncopeClientException.build(ClientExceptionType.InvalidUser);
        sce.getElements().add("Error 1");
        sce.getElements().add("Error 2");
        session.onException(sce);
        message = session.getFeedbackMessages().first();
        assertNotNull(message);
        assertTrue(message.isError());
        assertEquals("Error 1, Error 2", message.getMessage());
        session.getFeedbackMessages().clear();

        SyncopeClientCompositeException scce = SyncopeClientException.buildComposite();
        scce.addException(SyncopeClientException.build(ClientExceptionType.InvalidUser));
        scce.addException(SyncopeClientException.build(ClientExceptionType.InvalidExternalResource));
        session.onException(new ExecutionException(scce));
        message = session.getFeedbackMessages().first();
        assertNotNull(message);
        assertTrue(message.isError());
        assertEquals(scce.getMessage(), message.getMessage());
        session.getFeedbackMessages().clear();
    }
}
