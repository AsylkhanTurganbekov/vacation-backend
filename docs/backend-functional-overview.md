# Backend Functional Overview

## Назначение системы

Backend реализует систему контроля командировок сотрудников.

Система позволяет:
- создавать командировки сотрудникам
- согласовывать командировки
- фиксировать ключевые этапы поездки
- хранить историю перемещений
- выполнять биометрическую проверку на событиях поездки
- строить отчеты и сводки для dashboard

## Аутентификация и роли

Используется JWT-авторизация.

Поддерживаемые роли:
- `ADMIN`
- `EMPLOYEE`

Что реализовано:
- логин
- регистрация
- refresh token
- logout
- получение текущего пользователя
- разграничение доступа по ролям

Логика:
- `ADMIN` управляет пользователями, командировками и отчетами
- `EMPLOYEE` видит только свои поездки и отправляет события по ним

Текущая auth-схема:
- `login` возвращает `accessToken` и `refreshToken`
- `refresh` перевыпускает новую пару токенов
- `logout` отзывает refresh token
- access token фронт удаляет локально

## Push-уведомления и notification center

Backend подготовлен для Firebase FCM.

Используемая библиотека:
- `com.google.firebase:firebase-admin`

Конфигурация:
- `FIREBASE_SERVICE_ACCOUNT_PATH`
- `FIREBASE_SERVICE_ACCOUNT_BASE64`

Рекомендуемый вариант для сервера:
- положить service account JSON вне репозитория
- прописать путь в `.env` через `FIREBASE_SERVICE_ACCOUNT_PATH`

### Хранение устройств

Есть отдельная сущность `UserDevice`.

Что хранится:
- пользователь
- `pushToken`
- `platform`
- `deviceId`
- `deviceName`
- `appVersion`
- `active`
- `lastSeenAt`

Один пользователь может иметь несколько активных устройств.

### API устройств

- `POST /api/v1/devices/push-token`
- `DELETE /api/v1/devices/push-token`

Логика:
- client после логина регистрирует FCM token
- на logout или при reset app token деактивируется
- если FCM вернул invalid/unregistered token, backend сам помечает его неактивным

### Notification center

Есть отдельная сущность `UserNotification`.

Что хранится:
- пользователь
- поездка
- тип уведомления
- title / body
- `eventKey` для dedupe
- `clickAction`
- `oldStatus`
- `newStatus`
- JSON payload
- `read`
- `readAt`

API:
- `GET /api/v1/notifications`
- `PATCH /api/v1/notifications/{id}/read`
- `PATCH /api/v1/notifications/read-all`
- `POST /api/v1/notifications/test-push`

### Test push endpoint

Есть отдельный ручной endpoint для frontend/mobile команды:
- `POST /api/v1/notifications/test-push`

Назначение:
- проверить FCM-доставку на текущего пользователя
- не менять статус командировки
- не запускать trip business logic

Что делает:
- берет текущего пользователя из access token
- находит все его активные устройства
- отправляет test push на все активные `pushToken`
- возвращает:
  - сколько устройств найдено
  - настроен ли Firebase на сервере
  - причину результата (`reason`)
  - `projectId`, с которым инициализировался Firebase Admin SDK
  - сколько отправок прошло успешно
  - сколько упало
  - результат по каждому токену в masked виде

Возможные `reason`:
- `ok`
- `missing_service_account`
- `missing_service_account_file`
- `firebase_not_initialized`
- `token_send_failed`

### Когда отправляются push

Сейчас реализован сценарий:
- push при смене статуса командировки

Источники смены статуса:
- `approveTrip()`
- `cancelTrip()`
- trip events:
  - `DEPARTURE`
  - `ARRIVAL`
  - `RETURN`

Push отправляется только если статус действительно изменился.

### Кто получает push

Текущая логика получателей:
- сотрудник поездки
- инициатор создания поездки

Особенности:
- без дублей
- пользователь, который сам изменил статус, не уведомляется
- поля `approver` в текущей модели нет, поэтому approver как отдельный recipient пока не используется
- инициатор определяется по первому audit log `BUSINESS_TRIP` с action `CREATED`

### Формат push payload

Backend отправляет в FCM:
- `notification` block
- `data` block

Текущий `data` payload:
- `type=trip_status_changed`
- `tripId`
- `oldStatus`
- `newStatus`
- `clickAction=trip_details`

### Асинхронность

Отправка push вынесена из основного request flow:
- публикуется `TripStatusChangedEvent`
- после commit транзакции срабатывает async listener
- listener создает записи в notification center и отправляет FCM

Это позволяет:
- не блокировать основной business request
- не отправлять push, если транзакция откатилась

## Пользователи

Пользователь содержит:
- `fullName`
- `email`
- `role`
- `department`
- `position`
- `avatarFileName`
- `active`

Что умеет backend:
- получить список пользователей
- получить одного пользователя
- создать пользователя
- обновить пользователя
- включить/отключить пользователя
- загрузить аватар
- удалить аватар
- отдать аватар-файл

Поддерживаемые фильтры по пользователям:
- `q`
- `role`
- `department`
- `active`
- `page`
- `size`
- `sort`

Это используется для:
- dropdown "Все сотрудники"
- фильтрации сотрудников
- привязки команд к `department`

Аватары:
- `GET /api/v1/users/{id}/avatar`
- `POST /api/v1/users/{id}/avatar`
- `DELETE /api/v1/users/{id}/avatar`

Логика доступа:
- `ADMIN` может менять аватар любого пользователя
- `EMPLOYEE` может менять только свой аватар
- смотреть аватар можно по URL из `avatarUrl`

## Командировки

Главная сущность: `BusinessTrip`.

Поля командировки:
- сотрудник
- цель поездки
- адрес назначения
- плановое время старта
- плановое время завершения
- фактическое время старта
- фактическое время прибытия
- фактическое время возврата
- статус

Статусы:
- `DRAFT`
- `APPROVED`
- `IN_PROGRESS`
- `ARRIVED`
- `COMPLETED`
- `CANCELLED`

Что реализовано:
- создать поездку
- получить список поездок
- получить одну поездку
- обновить поездку
- approve
- cancel
- получить данные командировочного удостоверения
- получить printable HTML командировочного удостоверения
- получить PDF командировочного удостоверения

## Фильтрация командировок

Под dashboard и списки trips backend поддерживает:
- `q`
- `employeeId`
- `department`
- `status`
- `dateFrom`
- `dateTo`
- `page`
- `size`
- `sort`

`q` ищет по:
- ФИО сотрудника
- `purpose`
- `destinationAddress`

Это покрывает UI фильтров вида:
- поиск
- все сотрудники
- все команды
- все статусы
- все периоды

## Employee view

Есть отдельные endpoint'ы для сотрудника:
- список только своих поездок
- одна своя поездка

Это позволяет фронту сотрудника работать отдельно от admin flow.

## Командировочное удостоверение

Есть отдельная логика для документа по командировке:
- `GET /api/v1/trips/{tripId}/certificate`
- `GET /api/v1/trips/{tripId}/certificate/html`
- `GET /api/v1/trips/{tripId}/certificate/pdf`

Что отдает backend:
- номер документа
- дату составления
- ФИО сотрудника
- подразделение
- должность
- табельный номер
- адрес назначения
- цель поездки
- даты поездки
- количество календарных дней
- отметки по поездке

Отметки документа строятся из trip events:
- `DEPARTURE`
- `ARRIVAL`
- `RETURN`

Доступ:
- `ADMIN` может получать удостоверение по любой поездке
- `EMPLOYEE` только по своей поездке

Форматы выдачи:
- `certificate` -> JSON DTO для собственного UI
- `certificate/html` -> готовый HTML, рендеренный через Thymeleaf
- `certificate/pdf` -> готовый PDF

PDF-генерация:
- используется `openhtmltopdf-pdfbox`
- PDF строится из отдельного PDF-friendly шаблона
- response headers:
  - `Content-Type: application/pdf`
  - `Content-Disposition: inline; filename="trip-certificate-{tripId}.pdf"`

## События поездки

По поездке можно отправлять события:
- `DEPARTURE`
- `ARRIVAL`
- `RETURN`

Что хранится в событии:
- координаты
- адрес
- время события
- комментарий
- статус биометрической проверки
- фото события

Бизнес-ограничения:
- `DEPARTURE` можно только из `APPROVED`
- `ARRIVAL` можно только после `DEPARTURE`
- `RETURN` можно только после `ARRIVAL`
- нельзя создавать события для `CANCELLED` и `COMPLETED`
- `EMPLOYEE` может работать только со своей поездкой

Автоматические переходы статусов:
- после `DEPARTURE` -> `IN_PROGRESS`
- после `ARRIVAL` -> `ARRIVED`
- после `RETURN` -> `COMPLETED`

Фото событий:
- frontend/mobile может отправлять `imageBase64`
- backend сохраняет фото как файл, связанный с `TripEvent`
- в `TripEventResponse` возвращается только `imageUrl`
- отдельный endpoint для фото:
  - `GET /api/v1/trip-events/{eventId}/image`
- `ADMIN` видит фото всех событий
- `EMPLOYEE` видит фото только своих поездок

## Биометрия

Используется mock biometric verification.

Как работает:
- при создании trip event вызывается biometric provider
- создается запись биометрической проверки
- событию присваивается `verificationStatus`

Статусы проверки:
- `PENDING`
- `VERIFIED`
- `FAILED`

## Отчеты

Есть отчетные API:
- отчет по поездкам
- summary по поездкам
- отчет по поездкам конкретного сотрудника

Summary сейчас отдает:
- `totalTrips`
- `tripsByStatus`
- `verifiedEvents`
- `failedEvents`
- `pendingEvents`

Это можно использовать для dashboard-карточек:
- всего поездок
- в пути
- на месте
- завершено
- частично для аномалий

## Что уже готово для dashboard

Готово:
- фильтр-панель по trips
- сотрудники dropdown
- summary-карточки
- отдельный map endpoint для monitoring dashboard
- список поездок
- история событий по trip

Частично готово:
- команды dropdown через `department`
- аномалии можно временно строить через `failedEvents`

Пока нет отдельных специализированных endpoint'ов для:
- live monitoring feed
- блока "что требует внимания"
- средней длительности / среднего пути как отдельной аналитики
- списка уникальных departments отдельным endpoint'ом

Для карты есть отдельный endpoint:
- `GET /api/v1/monitoring/map`

Он:
- возвращает trips в формате `withCoordinates / withoutCoordinates`
- берет координаты из последнего `TripEvent`
- возвращает детальный текущий адрес (`currentAddress`)
- возвращает `employeeAvatarUrl`
- не перегружает обычный `GET /api/v1/trips`

## Инфраструктура и деплой

Что уже есть:
- backend задеплоен на сервер
- работает через Docker
- GitHub Actions CI/CD настроен
- деплой с GitHub на сервер работает

Рабочий цикл:
- изменить код
- сделать `git push`
- GitHub Actions собирает и деплоит backend на сервер

## Что уже проверено

Проверено вручную:
- login
- `me`
- `users`
- `trips`
- `employee/trips`
- `reports`
- `summary`
- `trip events`
- role restrictions (`403` where expected)

Исправлено по ходу работ:
- lazy loading ошибка на trips
- фильтры для dashboard trips/users
- стабильный deploy workflow

## Итог

Сейчас backend уже покрывает рабочее ядро продукта:
- auth
- роли
- пользователи
- командировки
- статусы
- события поездки
- mock биометрия
- отчеты
- dashboard filters
- CI/CD и серверный деплой

Это уже рабочая основа для:
- admin dashboard
- employee travel tracking flow
