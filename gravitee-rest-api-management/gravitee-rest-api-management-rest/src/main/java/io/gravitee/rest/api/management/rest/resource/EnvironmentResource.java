/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.rest.api.management.rest.resource;

import io.gravitee.common.http.MediaType;
import io.gravitee.rest.api.management.rest.security.Permission;
import io.gravitee.rest.api.management.rest.security.Permissions;
import io.gravitee.rest.api.model.UpdateEnvironmentEntity;
import io.gravitee.rest.api.model.configuration.identity.IdentityProviderActivationEntity;
import io.gravitee.rest.api.model.configuration.identity.IdentityProviderActivationReferenceType;
import io.gravitee.rest.api.model.configuration.identity.IdentityProviderEntity;
import io.gravitee.rest.api.model.permissions.RolePermission;
import io.gravitee.rest.api.model.permissions.RolePermissionAction;
import io.gravitee.rest.api.service.EnvironmentService;
import io.gravitee.rest.api.service.common.GraviteeContext;
import io.gravitee.rest.api.service.configuration.identity.IdentityProviderActivationService;
import io.gravitee.rest.api.service.configuration.identity.IdentityProviderService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Florent CHAMFROY (florent.chamfroy at graviteesource.com)
 * @author GraviteeSource Team
 */
@Api
public class EnvironmentResource extends AbstractResource {

    @Context
    private ResourceContext resourceContext;

    @Inject
    private EnvironmentService environmentService;

    @Autowired
    private IdentityProviderService identityProviderService;

    @Inject
    private IdentityProviderActivationService identityProviderActivationService;

    /**
     * Create a new Environment.
     * @param environmentEntity
     * @return
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create an Environment", tags = {"Environment"})
    @ApiResponses({
            @ApiResponse(code = 201, message = "Environment successfully created"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public Response createEnvironment(
            @ApiParam(name = "environmentId", required = true) @PathParam("envId") String environmentId,
            @ApiParam(name = "environmentEntity", required = true) @Valid @NotNull final UpdateEnvironmentEntity environmentEntity) {
        environmentEntity.setId(environmentId);
        return Response
                .status(Status.CREATED)
                .entity(environmentService.createOrUpdate(environmentEntity))
                .build();
    }

    /**
     * Delete an existing Environment.
     * @param environmentId
     * @return
     */
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Delete an Environment", tags = {"Environment"})
    @ApiResponses({
            @ApiResponse(code = 204, message = "Environment successfully deleted"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public Response deleteEnvironment(
            @ApiParam(name = "environmentId", required = true)
            @PathParam("envId") String environmentId) {
        environmentService.delete(environmentId);
        //TODO: should delete all items that refers to this environment
        return Response
                .status(Status.NO_CONTENT)
                .build();
    }

    @GET
    @Path("/identities")
    @Permissions(@Permission(value = RolePermission.ENVIRONMENT_IDENTITY_PROVIDER_ACTIVATION, acls = RolePermissionAction.READ))
    @ApiOperation(value = "Get the list of identity provider activations for current environment",
            notes = "User must have the ENVIRONMENT_IDENTITY_PROVIDER_ACTIVATION[READ] permission to use this service")
    @ApiResponses({
            @ApiResponse(code = 200, message = "List identity provider activations for current environment", response = IdentityProviderActivationEntity.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public Set<IdentityProviderActivationEntity> listIdentityProviderActivations() {
        return identityProviderActivationService.findAllByTarget(new IdentityProviderActivationService.ActivationTarget(GraviteeContext.getCurrentEnvironment(), IdentityProviderActivationReferenceType.ENVIRONMENT));
    }

    @PUT
    @Path("/identities")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Permissions(@Permission(value = RolePermission.ENVIRONMENT_IDENTITY_PROVIDER_ACTIVATION, acls = {RolePermissionAction.CREATE, RolePermissionAction.DELETE, RolePermissionAction.UPDATE}))
    @ApiOperation(value = "Update available environment identities", tags = {"Environment"})
    @ApiResponses({
            @ApiResponse(code = 204, message = "Environment successfully updated"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public Response updateEnvironmentIdentities(List<IdentityProviderActivationEntity> identityProviderActivations) {
        this.identityProviderActivationService.updateTargetIdp(
                new IdentityProviderActivationService.ActivationTarget(GraviteeContext.getCurrentEnvironment(), IdentityProviderActivationReferenceType.ENVIRONMENT),
                identityProviderActivations.stream()
                        .filter(ipa -> {
                            final IdentityProviderEntity idp = this.identityProviderService.findById(ipa.getIdentityProvider());
                            return GraviteeContext.getCurrentOrganization().equals(idp.getOrganization());
                        })
                        .map(IdentityProviderActivationEntity::getIdentityProvider)
                        .collect(Collectors.toList()));
        return Response.noContent().build();
    }

    @Path("alerts")
    public AlertsResource getAlertsResource() {
        return resourceContext.getResource(AlertsResource.class);
    }

    @Path("apis")
    public ApisResource getApisResource() {
        return resourceContext.getResource(ApisResource.class);
    }

    @Path("applications")
    public ApplicationsResource getApplicationsResource() {
        return resourceContext.getResource(ApplicationsResource.class);
    }

    @Path("configuration")
    public EnvironmentConfigurationResource getConfigurationResource() {
        return resourceContext.getResource(EnvironmentConfigurationResource.class);
    }

    @Path("subscriptions")
    public SubscriptionsResource getSubscriptionsResource() {
        return resourceContext.getResource(SubscriptionsResource.class);
    }

    @Path("audit")
    public AuditResource getAuditResource() {
        return resourceContext.getResource(AuditResource.class);
    }

    @Path("portal")
    public PortalResource getPortalResource() {
        return resourceContext.getResource(PortalResource.class);
    }

    @Path("fetchers")
    public FetchersResource getFetchersResource() {
        return resourceContext.getResource(FetchersResource.class);
    }

    @Path("policies")
    public PoliciesResource getPoliciesResource() {
        return resourceContext.getResource(PoliciesResource.class);
    }

    @Path("resources")
    public ResourcesResource getResourcesResource() {
        return resourceContext.getResource(ResourcesResource.class);
    }

    @Path("services-discovery")
    public ServicesDiscoveryResource getServicesDiscoveryResource() {
        return resourceContext.getResource(ServicesDiscoveryResource.class);
    }

    @Path("instances")
    public InstancesResource getInstancesResource() {
        return resourceContext.getResource(InstancesResource.class);
    }

    @Path("platform")
    public PlatformResource getPlatformResource() {
        return resourceContext.getResource(PlatformResource.class);
    }

    @Path("messages")
    public MessagesResource getMessagesResource() {
        return resourceContext.getResource(MessagesResource.class);
    }

    @Path("tickets")
    public PlatformTicketsResource getPlatformTicketsResource() {
        return resourceContext.getResource(PlatformTicketsResource.class);
    }

    @Path("entrypoints")
    public PortalEntryPointsResource getPortalEntryPointsResource() {
        return resourceContext.getResource(PortalEntryPointsResource.class);
    }

    @Path("notifiers")
    public NotifiersResource getNotifiersResource() {
        return resourceContext.getResource(NotifiersResource.class);
    }
}