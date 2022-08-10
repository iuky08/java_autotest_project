Проект `lib` - там условно "фреймворк"

Проект `demo` - пример с CPT.

`gradlew test allureReport`

У меня почему-то не работает `gradlew allureServe`

После создания репортов надо открывать отчёт в папке
`demo/build/reports/allure-report/index.html`

Общего отчёта для всех проектов он не делает почему-то.

Основной пример:
`demo/src/test/java/com/actitime/demo/api/CPTTest.java`

там и сеть задаётся, и тесты