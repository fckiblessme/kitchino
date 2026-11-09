package com.kitchino.app.employee.domain

enum class Action(val label: String) {
    MAKE_DISH("Приготовление блюда"),
    WRITE_OFF_PRODUCTS("Списание продуктов"),
    APPLICATION_DISCOUNT("Применение скидки"),
    EDIT_RECIPE("Редактирование рецепта"),
    VIEW_AUDIT("Просмотр аудита"),
    EMPLOYEE_MANAGEMENT("Управление сотрудниками")
}