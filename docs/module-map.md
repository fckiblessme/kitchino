# Модульная карта проекта Kitchino

Версия: 1.1.
Статус: актуально для веток dishbatch и warehouse.

## Обозначения

- **Модуль** — часть программы с определенной ответственностью и публичной границей.
- **Контракт** — публичный интерфейс модуля, через который его использует потребитель.
- **Зависимость** — направление «потребитель → модуль» (стрелка). Потребитель зависит от контракта, а не от конкретной реализации; реализация выбирается в точке сборки.
- **Точка сборки** — место, где выбирается и связывается конкретная реализация контракта.

## Модули

| Модуль | Пакет | Ответственность | Контракт |
|---|---|---|---|
| dishbatch/decision | `com.kitchino.app.dishbatch.domain` | Рекомендация для партии блюда (без действия / скидка / списание) | `DiscountDecisionProvider.recommendAction(batch, currentTime)` |
| dishbatch/sales | `com.kitchino.app.dishbatch.domain` | Темп продаж в порциях в час | `SalesVelocity.calculatePortionsPerHour(...)` |
| dishbatch/data | `com.kitchino.app.dishbatch.data` | Хранение партий, решений и журнала продаж (Room) | `DishBatchDao`, `SalesLogDao`, `DiscountDecisionDao` |
| warehouse | `com.kitchino.app.warehouse` | Учет ингредиентов, рецептов, списание по рецепту | `WarehouseApi`, `ConsumeIngredientsUseCase` |
| warehouse/consume | `com.kitchino.app.core.contracts`, `com.kitchino.app.warehouse.domain` | Списание ингредиентов по рецепту с проверкой достаточности и дефицитным анализом | `WarehouseApi.consumeIngredients(recipeId, batchSize): ConsumeResult` |
| warehouse/forecast | `com.kitchino.app.warehouse.domain` | Прогноз расхода и срока годности ингредиента | `Forecast` (чистые функции), `ForecastUseCase.execute(id)` |
| warehouse/data | `com.kitchino.app.warehouse.data` | Хранение ингредиентов, партий, рецептов, журнала списаний (Room) | `IngredientDao`, `IngredientBatchDao`, `RecipeDao`, `ConsumptionLogDao` |
| employee | `com.kitchino.app.employee` | Роли, вход по PIN, журнал действий, обучение | `EmployeeDao`, `TrainingDao`, `AuditLogDao` |
| ui | `com.kitchino.app.dishbatch.ui` и др. | Экраны Compose | ViewModel + StateFlow |

## Реализации контрактов

| Контракт | Реализация | Назначение | Статус |
|---|---|---|---|
| `WarehouseApi` | `WarehouseApiImpl` | Списание через базу Room | работает |
| `WarehouseApi` | `FakeWarehouseApi` | Списание в памяти без базы (демо и тест потребителя) | работает |
| `DiscountDecisionProvider` | `ExpiryDiscountDecision` | Решение по доле остатка срока годности | работает |
| `DiscountDecisionProvider` | `VelocityDiscountDecision` | Решение с учетом темпа продаж | работает |

## Зависимости
```
ui/RecipesScreen → ui/MakeDishViewModel ui/MakeDishViewModel → warehouse/consume (WarehouseApi) ui/IngredientsScreen → ui/IngredientsViewModel → warehouse/forecast (ForecastUseCase)
warehouse/consume: WarehouseApiImpl → ConsumeIngredientsUseCase → warehouse/data warehouse/consume: FakeWarehouseApi → (без БД, in-memory) warehouse/forecast: ForecastUseCase → Forecast (чистые функции), warehouse/data warehouse/data → AppDatabase (единая схема Room)
ui/DishBatchScreen → ui/DishBatchViewModel ui/DishBatchViewModel → dishbatch/decision (DiscountDecisionProvider) dishbatch/decision → dishbatch/sales (SalesVelocity), dishbatch/data (DishBatchEntity) dishbatch/sales → dishbatch/data (SalesLogEntity) dishbatch/data → AppDatabase (единая схема Room)
```
## Точки сборки

`WarehouseDependencies` создает `WarehouseApiImpl` и предоставляет его как `WarehouseApi`. `MainActivity` передает реализацию в `MakeDishViewModelFactory(warehouseApi = ...)`. Для подмены достаточно передать `FakeWarehouseApi` в ту же фабрику — код `MakeDishViewModel` не изменяется, так как потребитель зависит от контракта, а не от конкретной реализации. `DishBatchViewModel` получает `DiscountDecisionProvider` через фабрику в `MainActivity` тем же способом.

## Границы

- `dishbatch/decision` **не зависит** от UI и напрямую от БД: вход — сущность и время, выход — решение. Проверяется тестами на JVM без эмулятора.
- `warehouse/consume` (контракт `WarehouseApi`) не зависит от UI. Потребитель `MakeDishViewModel` зависит только от контракта; реализация `WarehouseApiImpl` скрыта за границей и обращается к Room. `FakeWarehouseApi` работает без базы.
- `warehouse/forecast` (`Forecast`) — чистые функции без обращений к БД и системным часам; `ForecastUseCase` читает данные из репозитория и передает их в `Forecast`.
- Все модули данных сходятся в единой `AppDatabase` (версия 3), миграции 1→2→3 в `AppDatabase.kt`.

## Соответствие коду

Перечень модулей и зависимостей соответствует структуре пакетов `app/src/main/java/com/kitchino/app/...` и импортам в коде. Расхождений не выявлено.