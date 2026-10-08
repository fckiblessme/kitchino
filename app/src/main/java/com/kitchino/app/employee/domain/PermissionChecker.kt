package com.kitchino.app.employee.domain

import com.kitchino.app.employee.data.Role

class PermissionChecker {
    fun can(role: Role, action: Action): Boolean {

        if (action == Action.MAKE_DISH) {
            return role == Role.COOK
        }

        if (action == Action.WRITE_OFF_PRODUCTS) {
            return role == Role.MANAGER || role == Role.COOK
        }

        if (action == Action.APPLICATION_DISCOUNT) {
            return role == Role.CASHIER || role == Role.MANAGER
        }

        if (action == Action.EDIT_RECIPE) {
            return role == Role.MANAGER
        }

        if (action == Action.VIEW_AUDIT) {
            return role == Role.MANAGER
        }

        if (action == Action.EMPLOYEE_MANAGEMENT) {
            return role == Role.MANAGER
        }
        return false
    }
}