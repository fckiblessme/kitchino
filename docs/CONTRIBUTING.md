# Правила работы с репозиторием

## 1. Основная ветка защищена

Прямой push в `main` запрещен, изменения попадают туда только через Pull Request. Защита включается в Settings → Branches (Require a pull request before merging, Require approvals = 1, Require status checks to pass)

## 2. Путь изменения

Issue с описанием задачи - ветка участника - коммиты - Push - Pull Request в `main` с `Closes #N` в описании - автоматические проверки - review - merge.

## 3. Автоматические проверки

GitHub Actions (`.github/workflows/build.yml`) на каждый push в `main` и каждый PR: `./gradlew build` и `./gradlew test`. PR с красной проверкой не сливается.

## 4. Независимое review

Автор не одобряет свой PR. Значимые изменения требуют минимум одного approve другого участника:

- логика и пороги `DiscountDecisionEngine`;
- контракты в `core/contracts`;
- схема БД и `AppDatabase`;
- файлы сборки (`build.gradle.kts`, `libs.versions.toml`, `gradle.properties`);
- `docs/SPEC.md` и эти правила.

Мелкие правки (опечатки, комментарии) тоже идут через PR, review для них по желанию.

## 5. Definition of Done

Задача считается выполненной, если:
- [ ] есть Issue, PR ссылается на неё через `Closes #N`
- [ ] затронутые требования в `docs/SPEC.md` обновлены, либо в PR указано, что они не затронуты
- [ ] проект собирается из чистого клона, CI зелёный
- [ ] контракты в `core/contracts` не изменены без согласования с владельцем зависимого модуля
- [ ] `docs/SETUP.md` актуален, если менялись сборка или конфигурация


