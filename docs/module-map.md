# Модульная карта проекта Kitchino

Версия: 1.0. 
Статус: актуально для ветки dishbatch.

## Обозначения

- **Модуль** — часть программы с определённой ответственностью и публичной границей.
- **Контракт** — публичный интерфейс модуля, через который его использует потребитель.
- **Зависимость** — направление «потребитель → модуль» (стрелка).

## Модули

| Модуль | Пакет | Ответственность | Контракт |
|---|---|---|---|
| dishbatch/decision | `com.kitchino.app.dishbatch.domain` | Рекомендация для партии блюда (без действия / скидка / списание) | `DiscountDecisionProvider.recommendAction(batch, currentTime)` |
| dishbatch/sales | `com.kitchino.app.dishbatch.domain` | Темп продаж в порциях в час | `SalesVelocity.calculatePortionsPerHour(...)` |
| dishbatch/data | `com.kitchino.app.dishbatch.data` | Хранение партий, решений и журнала продаж (Room) | `DishBatchDao`, `SalesLogDao`, `DiscountDecisionDao` |
| warehouse | `com.kitchino.app.warehouse` | Учёт ингредиентов, рецептов, списание по рецепту | `WarehouseApi`, `ConsumeIngredientsUseCase` |
| employee | `com.kitchino.app.employee` | Роли, вход по PIN, журнал действий, обучение | `EmployeeDao`, `TrainingDao`, `AuditLogDao` |
| ui | `com.kitchino.app.dishbatch.ui` и др. | Экраны Compose | ViewModel + StateFlow |

## Зависимости

```
ui/DishBatchScreen → ui/DishBatchViewModel
ui/DishBatchViewModel → dishbatch/decision (DiscountDecisionProvider)
dishbatch/decision → dishbatch/sales (SalesVelocity), dishbatch/data (DishBatchEntity)
dishbatch/sales → dishbatch/data (SalesLogEntity)
dishbatch/data → AppDatabase (единая схема Room)
warehouse/ui → warehouse (WarehouseApi)
warehouse → AppDatabase
employee → AppDatabase
```

## Границы

- `dishbatch/decision` **не зависит** от UI и напрямую от БД: вход — сущность и время, выход — решение. Проверяется тестами на JVM без эмулятора.
- Все модули данных сходятся в единой `AppDatabase` (версия 3), миграции 1→2→3 в `AppDatabase.kt`.

## Соответствие коду

Перечень модулей и зависимостей соответствует структуре пакетов `app/src/main/java/com/kitchino/app/...` и импортам в коде. Расхождений не выявлено.
