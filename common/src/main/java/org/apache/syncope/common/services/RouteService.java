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
package org.apache.syncope.common.services;

import java.util.List;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.model.wadl.Description;
import org.apache.cxf.jaxrs.model.wadl.Descriptions;
import org.apache.cxf.jaxrs.model.wadl.DocTarget;
import org.apache.syncope.common.to.RouteTO;
import org.apache.syncope.common.types.SubjectType;

@Path("routes/{kind}")
public interface RouteService extends JAXRSService {

    /**
     * Checks whether Camel is choosed as default provisioning engine.
     *
     * @param kind user or role
     * @return <tt>Response</tt> contains special syncope HTTP header indicating if Camel is enabled for
     * users / roles provisioning
     * @see org.apache.syncope.common.types.RESTHeaders#CAMEL_USER_PROVISIONING_MANAGER
     * @see org.apache.syncope.common.types.RESTHeaders#CAMEL_ROLE_PROVISIONING_MANAGER
     *
     * @Descriptions({
     * @Description(target = DocTarget.RESPONSE,
     * value = "Contains special syncope HTTP header indicating if Camel is the default provisioning manager")
     * }) */
    @OPTIONS
    Response getOptions(@NotNull @PathParam("kind") SubjectType kind);

    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    List<RouteTO> getRoutes(@NotNull @PathParam("kind") SubjectType kind);

    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Path("{id}")
    public RouteTO getRoute(@NotNull @PathParam("kind") SubjectType kind, @PathParam("id") Long Id);

    @PUT
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Path("{id}")
    void importRoute(@NotNull @PathParam("kind") SubjectType kind,@PathParam("id") Long id, RouteTO route);

}
