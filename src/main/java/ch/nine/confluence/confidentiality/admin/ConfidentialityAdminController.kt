package ch.nine.confluence.confidentiality.admin

import ch.nine.confluence.confidentiality.admin.model.AdministerConfidentialityRow
import ch.nine.confluence.confidentiality.api.model.NotFound
import ch.nine.confluence.confidentiality.service.PermissionService
import ch.nine.confluence.confidentiality.service.SpaceConfidentialityService
import com.atlassian.confluence.spaces.Space
import com.atlassian.confluence.spaces.SpaceManager
import org.apache.log4j.LogManager
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status.FORBIDDEN
import javax.ws.rs.core.Response.Status.NOT_FOUND

/**
 * Controller that's responsible for configuring confluence-confidentiality plugin
 */
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/administer/confidentiality")
class ConfidentialityAdminController constructor(private val service: SpaceConfidentialityService,
                                                 private val permissionService: PermissionService,
                                                 private val spaceManager: SpaceManager) {
    companion object {
        private val log = LogManager.getLogger(this::class.java.name.substringBefore("\$Companion"))
    }

    @GET
    @Path("/{spaceKey}")
    fun getConfidentialityOptions(@PathParam("spaceKey") spaceKey: String): Response {
        return try {
            val space = spaceManager.getSpace(spaceKey)
            when {
                (space == null) -> notFound()
                (!canViewSpace(space)) -> forbidden()
                else -> Response.ok(service.getConfidentialityOptions(space)).build()
            }
        } catch (e: Exception) {
            log.error("Exception occurred while trying to get page with id: $spaceKey", e)
            return serverError()
        }
    }

    @POST
    @Path("/{spaceKey}")
    fun setConfidentialityOptions(@PathParam("spaceKey") spaceKey: String,
                                  confidentialityRow: AdministerConfidentialityRow): Response {
        return try {
            val space = spaceManager.getSpace(spaceKey)
            when {
                (space == null) -> notFound()
                (!canViewSpace(space)) -> forbidden()
                (!canAdministerSpace(space)) -> forbidden()
                else -> Response.ok(service.saveConfidentiality(space, confidentialityRow)).build()
            }
        } catch (e: Exception) {
            log.error("Exception occurred while trying to save confidentialityList: $confidentialityRow, enabled: '' for space: $spaceKey", e)
            return serverError()
        }
    }

    @PUT
    @Path("/{spaceKey}/{id}")
    fun updateConfidentiality(@PathParam("spaceKey") spaceKey: String,
                              @PathParam("id") id: Int?,
                              confidentiality: AdministerConfidentialityRow): Response {
        return try {
            val space = spaceManager.getSpace(spaceKey)
            when {
                (id == null) -> notFound("Invalid id: $id")
                (space == null) -> notFound()
                (!canViewSpace(space)) -> forbidden()
                (!canAdministerSpace(space)) -> forbidden()
                else -> Response.ok(service.updateConfidentiality(space, id, confidentiality)).build()
            }
        } catch (e: Exception) {
            log.error("Exception occurred while trying to add new confidentiality: $confidentiality, for space: $spaceKey", e)
            return serverError()
        }
    }

    @DELETE
    @Path("/{spaceKey}/{id}")
    fun deleteConfidentialityOption(@PathParam("spaceKey") spaceKey: String,
                                    @PathParam("id") id: Int): Response {
        return try {
            val space = spaceManager.getSpace(spaceKey)
            when {
                (space == null) -> notFound("Invalid id: $id")
                (!canViewSpace(space)) -> forbidden()
                (!canAdministerSpace(space)) -> forbidden()
                else -> Response.ok(service.removeConfidentiality(space, id)).build()
            }
        } catch (e: Exception) {
            log.error("Exception occurred while trying to remove confidentiality with id: $id, for space: $spaceKey", e)
            return serverError()
        }
    }

    private fun canViewSpace(space: Space): Boolean {
        return permissionService.canUserView(space)
    }

    private fun canAdministerSpace(space: Space): Boolean {
        return permissionService.canUserAdministrerSpace(space)
    }

    private fun forbidden() = Response.status(FORBIDDEN).build()

    private fun notFound(msg: String): Response = Response.status(NOT_FOUND).entity(NotFound(msg)).build()

    private fun notFound(): Response = Response.status(NOT_FOUND).build()

    private fun serverError() = Response.serverError().entity(false).build()
}
