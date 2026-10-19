# Исходное состояние для проверки

## Откуда взять код

```bash
git clone https://github.com/fckiblessme/kitchino.git
cd kitchino
git checkout <тег или ветка, указанные в отчёте>
```

## Окружение

- Android Studio, JDK 17 или новее;
- эмулятор или устройство с Android API 24 и выше (`minSdk = 24`). Эмулятор должен быть запущен *до* выполнения команды установки.;
- установленный Android SDK 37 (`compileSdk = 37`);
- доступ в интернет только для первой сборки: Gradle скачивает зависимости.
  - Если Gradle выдаёт "requires JVM 17 or later... currently using JVM 11",
    укажите JDK 17 вручную:
  в Android Studio: File → Settings → Build, Execution, Deployment →
    Build Tools → Gradle → Gradle JDK = 17;
    в терминале PowerShell: $env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
  перед запуском .\gradlew.bat.

## Конфигурация сборки

В `gradle.properties` должны быть строки, без них проект не собирается с AGP 9:

```
android.disallowKotlinSourceSets=false
android.builtInKotlin=false
android.newDsl=false
```

Версии, на которых проект проверен: Kotlin 2.2.10, KSP 2.2.10-2.0.2, Room 2.7.1. Актуальные значения: [ПРОВЕРИТЬ в `libs.versions.toml` и `build.gradle.kts`].

## Данные

- База `app_database` (SQLite через Room) создаётся пустой при первом запуске. Источник истины по схеме — Room-сущности; SQL-дамп был эскизом.
- Тестовая партия блюда сейчас вставляется в `MainActivity` при каждом запуске, поэтому записи накапливаются. Сброс: удалить приложение с эмулятора.
- Посмотреть данные: View → Tool Windows → App Inspection → Database Inspector, таблицы `dish_batch` и `discount_decision`.

### Настройка перед сборкой

1. При первом открытии проекта в Android Studio дождитесь окончания Gradle Sync. Это автоматически создаст файл `local.properties` с путём к вашему Android SDK.
2. Если вы запускаете сборку из терминала (без Android Studio), создайте файл `local.properties` в корне проекта вручную и укажите в нем путь к SDK:
   `sdk.dir=C:\\Users\\Имя\\AppData\\Local\\Android\\Sdk` (путь зависит от вашей ОС).

### Кроссплатформенность команд
В инструкции приведены команды для Windows (`.\gradlew.bat`).
Для macOS и Linux используйте `./gradlew` (например, `./gradlew build`).

## Как проверить

```bash
./gradlew build
./gradlew test
```

Затем запустить приложение на эмуляторе: экран «Партии блюд» показывает карточки партий; при пустой базе — «Нет партий блюд».
Если на эмуляторе уже установлена прежняя версия Kitchino, удалите её
перед установкой, иначе Room может не открыть базу из-за несовпадения
схемы:
adb uninstall com.kitchino.app
или через Настройки эмулятора → Приложения → Kitchino → Удалить.

## Ограничения безопасности

- Секретов, паролей и ключей в проекте нет и быть не должно. В репозиторий не коммитятся `local.properties` и `.DS_Store`.
- Тестовые данные вымышленные. Схема сотрудников содержит поля паспорта, адреса и телефона: реальные персональные данные в них не вносить.
- Приложение не использует сеть, разрешения INTERNET в манифесте нет.
- Локальная база не шифруется. Это ограничение MVP, при краже устройства данные читаются.
