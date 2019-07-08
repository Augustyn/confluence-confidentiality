package ch.nine.confluence.confidentiality.service

import com.atlassian.confluence.pages.Page
import com.atlassian.confluence.security.Permission
import com.atlassian.confluence.security.PermissionManager
import com.atlassian.confluence.security.SpacePermission.ADMINISTER_SPACE_PERMISSION
import com.atlassian.confluence.security.SpacePermissionManager
import com.atlassian.confluence.spaces.Space
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal
import com.atlassian.user.User

class PermissionService constructor(private val permissionManager: PermissionManager,
                                    private val spaceManager: SpacePermissionManager) {

    fun canUserEdit(page: Page?): Boolean {
        return canUserDo(Permission.EDIT, page)
    }

    fun canUserView(page: Page?): Boolean {
        return canUserDo(Permission.VIEW, page)
    }

    fun canUserView(space: Space): Boolean {
        return canUserDo(Permission.VIEW, space)
    }

    fun canUserEdit(space: Space): Boolean {
        return canUserDo(Permission.EDIT, space)
    }

    fun isSystemAdministrator(user: User?): Boolean {
        return permissionManager.isSystemAdministrator(user)
    }

    fun canUserAdministrerSpace(space: Space): Boolean {
        return canUserDo(AuthenticatedUserThreadLocal.get(), ADMINISTER_SPACE_PERMISSION, space)
    }

    private fun canUserDo(permission: Permission?, page: Page?): Boolean {
        return canUserDo(AuthenticatedUserThreadLocal.get(), permission, page)
    }

    private fun canUserDo(permission: Permission?, space: Space?): Boolean {
        return canUserDo(AuthenticatedUserThreadLocal.get(), permission, space)
    }

    private fun canUserDo(user: User, permission: Permission?, target: Any?): Boolean {
        return permissionManager.hasPermission(user, permission, target)
    }
    private fun canUserDo(user: User?, permission: String, space: Space): Boolean {
        return spaceManager.hasPermission(permission, space, user)
    }

}