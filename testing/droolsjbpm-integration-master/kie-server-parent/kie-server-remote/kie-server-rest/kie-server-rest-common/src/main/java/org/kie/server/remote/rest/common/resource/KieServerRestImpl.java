/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.remote.rest.common.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieScannerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.impl.KieServerLocator;
import org.kie.server.services.impl.marshal.MarshallerHelper;

import static org.kie.server.remote.rest.common.util.RestUtils.*;

@Path("server")
public class KieServerRestImpl {

    private KieServerImpl server;
    private MarshallerHelper marshallerHelper;

    public KieServerRestImpl() {
        // for now, if no server impl is passed as parameter, create one
        this.server = KieServerLocator.getInstance();
        this.marshallerHelper = new MarshallerHelper(this.server.getServerRegistry());
    }

    public KieServerRestImpl(KieServerImpl server) {
        this.server = server;
        this.marshallerHelper = new MarshallerHelper(server.getServerRegistry());
    }

    public KieServerImpl getServer() {
        return server;
    }

    public void setServer(KieServerImpl server) {
        this.server = server;
    }


    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getInfo(@Context HttpHeaders headers) { 
        return createCorrectVariant(server.getInfo(), headers);
    }

    @GET
    @Path("containers")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response listContainers(@Context HttpHeaders headers) { 
        return createCorrectVariant(server.listContainers(), headers);
    }

    @PUT
    @Path("containers/{id}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response createContainer( @Context HttpHeaders headers, @PathParam("id") String id, String containerPayload ) {
        String contentType = getContentType(headers);

        KieContainerResource container = marshallerHelper.unmarshal(containerPayload, contentType, KieContainerResource.class);

        ServiceResponse<KieContainerResource> response = server.createContainer(id, container);
        if( response.getType() == ServiceResponse.ResponseType.SUCCESS ) {
            return createCorrectVariant(response, headers, Status.CREATED);
        }
        return createCorrectVariant(response, headers, Status.BAD_REQUEST);
    }

    @GET
    @Path("containers/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getContainerInfo( @Context HttpHeaders headers, @PathParam("id") String id ) {
        return createCorrectVariant(server.getContainerInfo(id), headers);
    }

    @DELETE
    @Path("containers/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response disposeContainer( @Context HttpHeaders headers, @PathParam("id") String id ) {
       return createCorrectVariant(server.disposeContainer(id), headers);
    }

    @GET
    @Path("containers/{id}/scanner")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getScannerInfo( @Context HttpHeaders headers, @PathParam("id") String id ) {
        return createCorrectVariant(server.getScannerInfo(id), headers);
    }
    
    @POST
    @Path("containers/{id}/scanner")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response updateScanner( @Context HttpHeaders headers, @PathParam("id") String id, String resourcePayload ) {
        String contentType = getContentType(headers);

        KieScannerResource resource = marshallerHelper.unmarshal(resourcePayload, contentType, KieScannerResource.class);

        return createCorrectVariant(server.updateScanner(id, resource), headers);
    };

    @GET
    @Path("containers/{id}/release-id")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getReleaseId( @Context HttpHeaders headers, @PathParam("id") String id) {
        return createCorrectVariant(server.getContainerReleaseId(id), headers);
    }

    @POST
    @Path("containers/{id}/release-id")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response updateReleaseId( @Context HttpHeaders headers, @PathParam("id") String id, String releaseIdPayload) {

        String contentType = getContentType(headers);

        ReleaseId releaseId = marshallerHelper.unmarshal(releaseIdPayload, contentType, ReleaseId.class);

        return createCorrectVariant(server.updateContainerReleaseId(id, releaseId), headers);
    }



}
