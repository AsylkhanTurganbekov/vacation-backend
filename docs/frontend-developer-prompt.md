# Prompt for Frontend Developer

Ты senior frontend developer. Твоя задача — реализовать production-ready web/mobile-friendly frontend для системы контроля командировок, интегрированный с существующим Spring Boot backend.

Работай по контракту из файла `docs/frontend-backend-handoff.md`. Перед началом открой Swagger на `<API_BASE_URL>/swagger-ui.html` и сверяй типы с `<API_BASE_URL>/api-docs`. Не придумывай новые backend endpoint'ы и не хардкодь storage URLs, роли или статусы.

## Цель продукта

Система имеет две роли:

- `ADMIN` управляет сотрудниками, командировками, отчётами, monitoring map и документами.
- `EMPLOYEE` видит только свои командировки, отмечает departure/arrival/return, загружает фото событий, получает уведомления и своё удостоверение.

Сделай понятный, адаптивный интерфейс с desktop-first admin dashboard и удобным mobile flow для сотрудника.

## Обязательные экраны

### Общие

- Login.
- Public registration: не показывай выбор роли; регистрация создаёт только `EMPLOYEE`.
- Профиль пользователя и logout.
- Глобальная обработка loading, empty, error и offline состояний.

### Admin

- Dashboard: summary-карточки, фильтры, последние/активные командировки, ссылка на карту.
- Users: поиск, фильтры `role/department/active`, создание, редактирование, включение/отключение, карточка со статистикой, загрузка аватара.
- Trips: таблица/список с фильтрами `q/status/employeeId/department/dateFrom/dateTo`, создание, редактирование, approve, cancel, detail с timeline событий.
- Monitoring map: отображай `withCoordinates` и отдельный список `withoutCoordinates`.
- Reports и выгрузка/печать доступных backend документов.
- Notifications center: read/read-all и test push для диагностики.

### Employee

- Список и детальная страница только собственных поездок.
- Пошаговый action flow: `DEPARTURE → ARRIVAL → RETURN`; показывай только разрешённое текущим статусом действие.
- Форма события: геолокация, адрес, время, комментарий, фото. Перед отправкой проверяй, что фото JPEG/PNG/WebP и до 5 MB.
- Просмотр истории событий, event photo, статуса биометрии и удостоверения (JSON/HTML/PDF).
- Уведомления и регистрация FCM device token после login.

## API и безопасность

- Добавляй `Authorization: Bearer <accessToken>` ко всем защищённым запросам.
- Храни access token безопасно (предпочтительно in-memory); refresh token используй только для `POST /api/v1/auth/refresh`.
- На `401` сделай только одну попытку refresh, затем очисти сессию и отправь на login.
- На `403` покажи «Недостаточно прав» и не пытайся обходить ограничение на клиенте.
- На `400` покажи `message` из backend в контексте формы.
- На `404` покажи empty/not-found state.
- На `409` сообщи о параллельном изменении командировки, перезагрузи detail и предложи повторить действие.
- Никогда не помещай в frontend Bitrix API key, JWT secret, Firebase service account или ML secret.
- Bitrix endpoints — только для server-to-server integration, не вызывай их из браузера.

## Состояния и UX

- Не вычисляй статусы командировок самостоятельно: используй поле `status` из API.
- Не строй путь к файлу вручную: используй `avatarUrl`/`imageUrl` из ответов или endpoint получения изображения.
- Блокируй кнопку во время submit, чтобы снизить вероятность повторной отправки event.
- После approve/cancel/event обновляй trip detail, timeline, dashboard summary, map и notifications cache.
- Отображай даты в локали продукта, но отправляй в API ISO-8601 (`YYYY-MM-DDTHH:mm:ss`).
- Предусмотри UI для пустой карты, отсутствующего аватара, отсутствующего фото и недоступной push-конфигурации.

## Технические требования

- Используй TypeScript, строгие типы и API client, сгенерированный из OpenAPI или синхронизированный со Swagger.
- Раздели UI, API client, auth state, server-state cache и form validation.
- Добавь route guards для ролей, но считай backend источником истины.
- Сделай адаптивную вёрстку и доступные формы: labels, keyboard navigation, readable validation errors.
- Добавь unit/component tests минимум для auth interceptor, role guards, trip action visibility, form validation и обработки API errors.
- Не меняй backend контракт без согласования. Если endpoint или поле действительно отсутствуют, зафиксируй потребность в отдельном API backlog, а не симулируй данные в production-коде.

## Acceptance criteria

1. ADMIN проходит login → users → create/edit trip → approve → sees dashboard/map/report/document.
2. EMPLOYEE проходит login → sees only own trips → sends allowed event with valid image → sees timeline and certificate.
3. EMPLOYEE получает `403` при ручном переходе на чужой trip/photo/certificate URL и видит понятный экран.
4. Session корректно обновляется по refresh token и завершается при неуспехе refresh.
5. Все перечисленные состояния `400/401/403/404/409`, loading и empty имеют UI.
6. Линтер, typecheck, frontend tests и production build проходят до handoff.
