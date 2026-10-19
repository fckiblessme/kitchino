package com.kitchino.app.core.contracts

/**
 * Публичный контракт модуля склада и рецептов.
 *
 * Назначение: списание ингредиентов со склада при приготовлении блюда по рецепту
 * и предоставление справочных данных о рецептах другим модулям (партии блюд,
 * сотрудники). За границей контракта скрыты работа с базой Room, расчёт дефицита
 * и распределение списания по партиям.
 *
 * Зависимости: реализация обращается к базе данных Room через репозиторий и DAO
 * склада. Сети, файлов и системных часов как источника бизнес-времени контракт
 * не использует.
 *
 * Потребитель зависит от контракта, а не от конкретной реализации, поэтому
 * реализацию можно подменить без изменения потребителя.
 */
interface WarehouseApi {

    /**
     * Списать ингредиенты со склада по рецепту.
     *
     * Проверяет существование рецепта и достаточность каждого ингредиента;
     * при успехе уменьшает остаток партий и записывает списание в журнал.
     *
     * @param recipeId идентификатор рецепта.
     * @param batchSize число порций, строго больше нуля.
     * @return результат списания:
     *   - [ConsumeResult.Success] — все ингредиенты списаны;
     *   - [ConsumeResult.InsufficientStock] — дефицит, склад не изменён;
     *   - [ConsumeResult.RecipeNotFound] — рецепт не найден, склад не изменён.
     * @throws IllegalArgumentException если [batchSize] <= 0.
     *
     * Порядок проверки нарушений: [batchSize] > 0 -> рецепт существует ->
     * достаточность ингредиентов -> списание. При любом отказе склад и журнал
     * остаются без изменений.
     *
     * Владение: входные аргументы не изменяются; возвращаемый результат (в том
     * числе список [ConsumeResult.InsufficientStock.missing]) принадлежит
     * вызывающему и не разделяется с внутренним состоянием.
     *
     * Побочные эффекты: изменение [remainingVolume] партий, вставка записей в
     * `consumption_log`. Списание по партиям ведётся в порядке возрастания
     * срока годности (FEFO).
     *
     * Пример:
     * ```
     * when (val r = api.consumeIngredients(recipeId, 2)) {
     *     ConsumeResult.Success -> show("Готово")
     *     is ConsumeResult.InsufficientStock -> show("Не хватает: ${r.missing}")
     *     ConsumeResult.RecipeNotFound -> show("Рецепт не найден")
     * }
     * ```
     */
    suspend fun consumeIngredients(recipeId: Long, batchSize: Int): ConsumeResult

    /**
     * Вернуть информацию о рецепте по идентификатору.
     *
     * Побочные эффекты отсутствуют: операция только читает базу.
     *
     * @param recipeId идентификатор рецепта.
     * @return [RecipeInfo] для существующего рецепта или `null`, если рецепта нет.
     */
    suspend fun getRecipeInfo(recipeId: Long): RecipeInfo?

    /**
     * Вернуть список всех рецептов.
     *
     * Побочные эффекты отсутствуют: операция только читает базу.
     *
     * @return список [RecipeInfo]; пустой список, если рецептов нет.
     */
    suspend fun listRecipes(): List<RecipeInfo>
}

/**
 * Результат списания ингредиентов.
 *
 * Категории ошибок различимы типом, а не текстом, поэтому потребитель
 * обрабатывает их через `when` без разбора строк.
 */
sealed interface ConsumeResult {

    /** Успешное списание: остатки партий уменьшены, записи в журнале добавлены. */
    data object Success : ConsumeResult

    /**
     * Нехватка хотя бы одного ингредиента. Склад и журнал не изменены.
     *
     * @property missing перечень дефицитов: идентификатор, название, недостающее
     * количество и единица измерения для каждого ингредиента.
     */
    data class InsufficientStock(
        val missing: List<MissingIngredient>
    ) : ConsumeResult

    /** Рецепт не найден. Склад и журнал не изменены. */
    data object RecipeNotFound : ConsumeResult
}

/**
 * Информация о рецепте для отображения и межмодульного обмена.
 *
 * @property id идентификатор рецепта.
 * @property dishName название блюда.
 */
data class RecipeInfo(
    val id: Long,
    val dishName: String
)

/**
 * Описание одного недостающего ингредиента.
 *
 * @property ingredientId идентификатор ингредиента.
 * @property name название ингредиента.
 * @property missingAmount недостающее количество.
 * @property unit единица измерения.
 */
data class MissingIngredient(
    val ingredientId: Long,
    val name: String,
    val missingAmount: Double,
    val unit: String
)