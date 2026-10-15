# Эксперимент с искусственным дефектом, ПЗ № 2

## Мутация 01 – сравнение на границе срока поставки

| Поле | Значение |
|---|---|
| Участок | `app/src/main/java/com/kitchino/app/warehouse/domain/Forecast.kt` |
| Нарушаемое требование | FR-04: признак «пора заказывать» выставляется только когда запас меньше срока поставки; при равенстве заказ не требуется |
| Тип изменения | замена строгого сравнения на нестрогое в граничной ветви |
| Было | `daysUntilEmpty < leadTimeDays` |
| Стало | `daysUntilEmpty <= leadTimeDays` |
| Patch | `docs/experiments/pz02-warehouse-mutation-01.patch` |
| Исходный commit | `0956c61` (ветка `warehouse`) |
| Данные | значения из набора `ForecastTest`: `boundaryDaysEqualToLeadTime_doesNotReorder` использует `daysUntilEmpty = 3.0` при `leadTimeDays = 3` |
| Команда | `.\gradlew.bat :app:testDebugUnitTest --rerun` |

## Наблюдения

| Состояние | Наблюдаемый результат | Вывод |
|---|---|---|
| Исходный код | 19 тестов, все PASSED | корректная базовая линия |
| Код с patch | 19 тестов, 1 FAILED: `boundaryDaysEqualToLeadTime_doesNotReorder`, `expected:<false> but was:<true>` | дефект обнаружен |
| Восстановленный код | 19 тестов, все PASSED | дефект удалён, набор снова зелёный |

Дефект изменяет поведение только на самой границе `daysUntilEmpty = leadTimeDays`.
При равенстве запаса и срока поставки приложение начинает рекомендовать заказ,
хотя по требованию FR-04 признак должен выставляться только при строгом
неравенстве. Проверки, которые не работают с границей, результат не меняют,
поэтому падает ровно один тест - это и подтверждает, что набор чувствителен
к нарушению требования, а не к любому изменению кода.

## Воспроизведение

```bash
git apply docs/experiments/pz02-warehouse-mutation-01.patch
.\gradlew.bat :app:testDebugUnitTest --rerun
git checkout -- app/src/main/java/com/kitchino/app/warehouse/domain/Forecast.kt
.\gradlew.bat :app:testDebugUnitTest --rerun
```

В сдаваемой версии искусственный дефект отсутствует.