package ch.nine.confluence.confidentiality.api

import ch.nine.confluence.confidentiality.api.model.NotFound
import ch.nine.confluence.confidentiality.service.ConfidentialityService
import com.atlassian.confluence.pages.Page
import com.atlassian.confluence.pages.PageManager
import org.apache.log4j.LogManager
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.FormParam
import javax.ws.rs.PathParam
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status.BAD_REQUEST
import javax.ws.rs.core.Response.Status.FORBIDDEN
import javax.ws.rs.core.Response.Status.NOT_FOUND

/**
 * Controller that's responsible for validating permissions
 * and calls backend services that perform business logic.
 */
@Produces(MediaType.APPLICATION_JSON)
@Path("/confidentiality")
class ConfidentialityController constructor(private val service: ConfidentialityService,
                                            private val pageManager: PageManager) {
    companion object {
        private val log = LogManager.getLogger(this::class.java.name.substringBefore("\$Companion"))
    }

    @GET
    fun getConfidentialityWithParam(@QueryParam("pageId") pageId: Long): Response {
        // for backward compatibility
        return getConfidentiality(pageId)
    }

    @GET
    @Path("{pageId}")
    fun getConfidentiality(@PathParam("pageId") pageId: Long): Response {
        return try {
            val page = pageManager.getPage(pageId)
            when {
                (page == null) -> notFound(pageId)
                (!canUserView(page)) -> forbidden()
                else -> Response.ok(service.getConfidentiality(page)).build()
            }
        } catch (e: Exception) {
            log.error("Exception occurred while trying to get page with id: $pageId", e)
            return serverError()
        }
    }

    @GET
    @Path("{pageId}/enabled")
    fun isPluginEnabled(@PathParam("pageId") pageId: Long): Response {
        return try {
            val page = pageManager.getPage(pageId)
            when {
                (page == null) -> notFound(pageId)
                (page.space?.key == null) -> notFound("Not found space for pageId: $pageId")
                (!canUserView(page)) -> forbidden()
                else -> Response.ok(service.isConfidentialityEnabled(page)).build()
            }
        } catch (e: Exception) {
            log.error("Exception occurred while trying to check is confidentiality enabled in space, with page id: $pageId", e)
            return serverError()
        }
    }

    @POST
    @Path("{pageId}")
    fun setConfidentiality(@PathParam("pageId") pageId: Long,
                           @FormParam("confidentiality") newConfidentiality: String): Response {
        return try {
            val page = pageManager.getPage(pageId)
            when {
                page == null -> notFound(pageId)
                !(canUserView(page)) -> forbidden()
                !(canUserEdit(page)) -> forbidden()
                !(validSpaceConfidentiality(page, newConfidentiality)) -> badRequest()
                else -> Response.ok(service.saveConfidentiality(page, newConfidentiality)).build()
            }
        } catch (e: Exception) {
            log.error("Exception occurred while trying to save confidentiality: $newConfidentiality, for page id: $pageId", e)
            return serverError()
        }
    }

    private fun validSpaceConfidentiality(page: Page, newConf: String): Boolean {
        return service.validateConfidentiality(page, newConf)
    }

    private fun canUserEdit(page: Page?): Boolean {
        return service.canUserEdit(page)
    }

    private fun canUserView(page: Page?): Boolean {
        return service.canUserView(page)
    }

    private fun badRequest() = Response.status(BAD_REQUEST).build()

    private fun forbidden() = Response.status(FORBIDDEN).build()

    private fun notFound(pageId: Long?) = Response.status(NOT_FOUND).entity(NotFound("Page id: $pageId not found")).build()

    private fun notFound(desc: String) = Response.status(NOT_FOUND).entity(desc).build()

    private fun serverError() = Response.serverError().build()

}
