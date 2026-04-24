# API Docs

Документация для фронтенда и ручного тестирования.

## Файлы

- [overview.md](/Users/asylkhanturganbekov/IdeaProjects/vacation-backend/docs/api/overview.md) - базовая информация, URL, auth flow, пагинация, ошибки
- [enums.md](/Users/asylkhanturganbekov/IdeaProjects/vacation-backend/docs/api/enums.md) - все enum значения
- [auth.md](/Users/asylkhanturganbekov/IdeaProjects/vacation-backend/docs/api/auth.md) - авторизация и текущий пользователь
- [users.md](/Users/asylkhanturganbekov/IdeaProjects/vacation-backend/docs/api/users.md) - API пользователей
- [trips.md](/Users/asylkhanturganbekov/IdeaProjects/vacation-backend/docs/api/trips.md) - API командировок
- [trip-events.md](/Users/asylkhanturganbekov/IdeaProjects/vacation-backend/docs/api/trip-events.md) - события командировки
- [biometric.md](/Users/asylkhanturganbekov/IdeaProjects/vacation-backend/docs/api/biometric.md) - биометрическая верификация
- [reports.md](/Users/asylkhanturganbekov/IdeaProjects/vacation-backend/docs/api/reports.md) - отчетность

## Быстрый сценарий

1. Логин под admin.
2. Создание или получение employee.
3. Создание командировки.
4. Approve командировки.
5. Логин под employee.
6. Отправка `departure`.
7. Отправка `arrival`.
8. Отправка `return`.
9. Получение отчетов.
