package com.kitchino.app.employee.domain
import org.junit.Test

import com.kitchino.app.employee.domain.Action
import com.kitchino.app.employee.domain.PermissionChecker
import com.kitchino.app.employee.data.Role
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PermissionCheckerTest {
    private val checker = PermissionChecker()

    @Test
    fun cookMakeDish() {
        assertTrue(checker.can(Role.COOK, Action.MAKE_DISH))
    }

    @Test
    fun cashierCantMakeDish() {
        assertFalse(checker.can(Role.CASHIER, Action.MAKE_DISH))
    }

    @Test
    fun managerMakeDish() {
        assertTrue(checker.can(Role.MANAGER, Action.MAKE_DISH))
    }

    @Test
    fun cookWriteOff() {
        assertTrue(checker.can(Role.COOK, Action.WRITE_OFF_PRODUCTS))
    }

    @Test
    fun cashierCantWriteOff() {
        assertFalse(checker.can(Role.CASHIER, Action.WRITE_OFF_PRODUCTS))
    }

    @Test
    fun managerWriteOff() {
        assertTrue(checker.can(Role.MANAGER, Action.WRITE_OFF_PRODUCTS))
    }

    @Test
    fun cookCantApplyDiscount() {
        assertFalse(checker.can(Role.COOK, Action.APPLICATION_DISCOUNT))
    }

    @Test
    fun cashierApplyDiscount() {
        assertTrue(checker.can(Role.CASHIER, Action.APPLICATION_DISCOUNT))
    }

    @Test
    fun managerApplyDiscount() {
        assertTrue(checker.can(Role.MANAGER, Action.APPLICATION_DISCOUNT))
    }

    @Test
    fun cookCantEditRecipe() {
        assertFalse(checker.can(Role.COOK, Action.EDIT_RECIPE))
    }

    @Test
    fun cashierCantEditRecipe() {
        assertFalse(checker.can(Role.CASHIER, Action.EDIT_RECIPE))
    }

    @Test
    fun managerEditRecipe() {
        assertTrue(checker.can(Role.MANAGER, Action.EDIT_RECIPE))
    }

    @Test
    fun cookCantViewAudit() {
        assertFalse(checker.can(Role.COOK, Action.VIEW_AUDIT))
    }

    @Test
    fun cashierCantViewAudit() {
        assertFalse(checker.can(Role.CASHIER, Action.VIEW_AUDIT))
    }

    @Test
    fun managerViewAudit() {
        assertTrue(checker.can(Role.MANAGER, Action.VIEW_AUDIT))
    }

    @Test
    fun cookCantManage() {
        assertFalse(checker.can(Role.COOK, Action.EMPLOYEE_MANAGEMENT))
    }

    @Test
    fun cashierCantManage() {
        assertFalse(checker.can(Role.CASHIER, Action.EMPLOYEE_MANAGEMENT))
    }

    @Test
    fun managerManage() {
        assertTrue(checker.can(Role.MANAGER, Action.EMPLOYEE_MANAGEMENT))
    }
}