# Эксперимент с искусственным дефектом, ПЗ № 2

## Мутация 01 – сравнение на границе 50 %

| Поле | Значение |
|---|---|
| Участок | `app/src/main/java/com/kitchino/app/dishbatch/domain/DiscountDecisionEngine.kt` |
| Нарушаемое требование | FR-08: при доле остатка срока f ≥ 0,5 решение «без действия», скидка не назначается |
| Тип изменения | замена строгого сравнения на нестрогое в граничной ветви |
| Было | `partOfRemainingTime < LIGHT_DISCOUNT_THRESHOLD` |
| Стало | `partOfRemainingTime <= LIGHT_DISCOUNT_THRESHOLD` |
| Patch | `docs/experiments/pz02-mutation-01.patch` |
| Исходный commit | `04fbcac` (ветка `pz02-checks`) |
| Данные | те же, что в наборе: срок жизни партии 1000 мс, остаток срока задается в тесте |
| Команда | `./gradlew :app:testDebugUnitTest` |

## Наблюдения

| Состояние | Наблюдаемый результат | Вывод |
|---|---|---|
| Исходный код | 22 теста, все PASSED | корректная базовая линия |
| Код с patch | 22 теста, 1 FAILED: `boundaryExactly50_isNoAction`, `expected:<NO_ACTION> but was:<DISCOUNT>` | дефект обнаружен |
| Восстановленный код | 22 теста, все PASSED | дефект удален, набор снова зеленый |

Дефект изменяет поведение только на самой границе f = 0,5: партия с половиной срока
получает скидку 20 % вместо «без действия». Проверки, которые не работают с границей,
результат не меняют, поэтому падает ровно один тест – это и подтверждает, что набор
чувствителен к нарушению требования, а не к любому изменению кода.

## Воспроизведение

```bash
git apply docs/experiments/pz02-mutation-01.patch
./gradlew :app:testDebugUnitTest
git checkout -- app/src/main/java/com/kitchino/app/dishbatch/domain/DiscountDecisionEngine.kt
./gradlew :app:testDebugUnitTest
```

В сдаваемой версии искусственный дефект отсутствует.
