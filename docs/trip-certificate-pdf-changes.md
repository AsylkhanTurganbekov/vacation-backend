# Trip Certificate PDF Changes

## Что изменено

Для командировочного удостоверения добавлен отдельный PDF endpoint:

- `GET /api/v1/trips/{tripId}/certificate/pdf`

Текущие endpoints сохранены без изменений:

- `GET /api/v1/trips/{tripId}/certificate`
- `GET /api/v1/trips/{tripId}/certificate/html`

## Как теперь работает

### JSON

`GET /api/v1/trips/{tripId}/certificate`

Возвращает `TripCertificateResponse` с данными документа.

### HTML

`GET /api/v1/trips/{tripId}/certificate/html`

Возвращает готовый `text/html`, отрендеренный через Thymeleaf.

### PDF

`GET /api/v1/trips/{tripId}/certificate/pdf`

Возвращает готовый PDF-документ.

## Что было исправлено

Изначально PDF строился из вебового HTML-шаблона сертификата.  
Этот шаблон использовал современную CSS-верстку, которая нестабильно обрабатывалась библиотекой PDF-рендера.

Из-за этого endpoint мог возвращать ошибку:

```json
{
  "status": 400,
  "message": "Failed to generate trip certificate PDF"
}
```

После исправления:

- HTML для веба остался отдельным
- для PDF используется отдельный PDF-friendly шаблон
- PDF теперь строится из упрощенной табличной верстки

## Технические изменения

Добавлено:

- отдельный шаблон:
  - `src/main/resources/templates/trip-certificate-pdf.html`
- отдельный метод рендера PDF HTML:
  - `TripCertificateService.renderCertificatePdfHtml(Long tripId)`
- `TripCertificatePdfServiceImpl` теперь берет не вебовый HTML, а PDF-шаблон

Измененные файлы:

- `src/main/java/com/company/vacation/service/TripCertificateService.java`
- `src/main/java/com/company/vacation/service/impl/TripCertificateServiceImpl.java`
- `src/main/java/com/company/vacation/service/impl/TripCertificatePdfServiceImpl.java`
- `src/main/resources/templates/trip-certificate-pdf.html`

## Используемая библиотека

Для генерации PDF используется:

- `com.openhtmltopdf:openhtmltopdf-pdfbox:1.0.10`

## Response headers

PDF endpoint должен отдавать:

```http
Content-Type: application/pdf
Content-Disposition: inline; filename="trip-certificate-{tripId}.pdf"
```

Пример:

```http
Content-Type: application/pdf
Content-Disposition: inline; filename="trip-certificate-2.pdf"
```

## Доступ

Права доступа остались прежними:

- `ADMIN` может получать сертификат любой поездки
- `EMPLOYEE` может получать сертификат только своей поездки

## curl для проверки

### Логин

```bash
curl -X POST 'http://92.38.49.156:8090/api/v1/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "admin@vacation.local",
    "password": "Admin123!"
  }'
```

### Получить PDF

```bash
curl 'http://92.38.49.156:8090/api/v1/trips/2/certificate/pdf' \
  -H 'Authorization: Bearer ACCESS_TOKEN' \
  -o trip-certificate-2.pdf
```

### Проверить headers

```bash
curl -I 'http://92.38.49.156:8090/api/v1/trips/2/certificate/pdf' \
  -H 'Authorization: Bearer ACCESS_TOKEN'
```

## Проверка сборки

Локальная проверка проходит:

```bash
./mvn-local -q -DskipTests compile
```

## Когда что использовать

- если нужен свой UI документа:
  - `GET /api/v1/trips/{tripId}/certificate`
- если нужен готовый HTML для web preview:
  - `GET /api/v1/trips/{tripId}/certificate/html`
- если нужен готовый файл для скачивания, печати или mobile:
  - `GET /api/v1/trips/{tripId}/certificate/pdf`
