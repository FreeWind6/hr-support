# hr-support

[![Docker Publish](https://github.com/FreeWind6/hr-support/actions/workflows/docker-publish.yml/badge.svg)](https://github.com/FreeWind6/hr-support/actions/workflows/docker-publish.yml)   [![Java CI with Gradle](https://github.com/FreeWind6/hr-support/actions/workflows/gradle.yml/badge.svg)](https://github.com/FreeWind6/hr-support/actions/workflows/gradle.yml)  [![Dependabot Updates](https://github.com/FreeWind6/hr-support/actions/workflows/dependabot/dependabot-updates/badge.svg)](https://github.com/FreeWind6/hr-support/actions/workflows/dependabot/dependabot-updates) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=FreeWind6_hr-support&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=FreeWind6_hr-support)

Telegram-бот для первичной обработки откликов на вакансии. Принимает сообщения от кандидатов и перенаправляет их в
группу поддержки, автоматически создавая отдельную тему для каждого кандидата.

## Стек

| Компонент   | Версия  |
|-------------|---------|
| Java        | 25      |
| Spring Boot | 4.\*.\* |

## Переменные окружения

| Переменная                  | По умолчанию       | Описание                                        |
|-----------------------------|--------------------|-------------------------------------------------|
| `POSTGRES_ADDRESS`          | —                  | `host:port` PostgreSQL                          |
| `POSTGRES_USER`             | —                  | Пользователь БД                                 |
| `POSTGRES_PASSWORD`         | —                  | Пароль БД                                       |
| `POSTGRES_DB`               | `hrsupport`        | Имя БД                                          |
| `POSTGRES_SCHEMA`           | `public`           | Схема БД                                        |
| `TELEGRAM_BOT_TOKEN`        | —                  | Токен бота от BotFather                         |
| `TELEGRAM_SUPPORT_GROUP_ID` | —                  | ID супергруппы поддержки (с включёнными темами) |
| `TELEGRAM_BOT_SCHEMA`       | `https`            | Схема подключения к Telegram API                |
| `TELEGRAM_BOT_HOST`         | `api.telegram.org` | Хост Telegram API                               |
| `TELEGRAM_BOT_PORT`         | `443`              | Порт Telegram API                               |

## Actuator

Порт: **8090**

| Эндпоинт            | Описание            |
|---------------------|---------------------|
| `/actuator/health`  | Статус приложения   |
| `/actuator/info`    | Версия и Git-коммит |
| `/actuator/metrics` | Метрики             |

## Поведение бота

| Сообщение                       | Действие                                            |
|---------------------------------|-----------------------------------------------------|
| `/start`                        | Приветственное сообщение с инструкцией              |
| Первое содержательное сообщение | Создание темы в группе + ссылка на кандидата в теме |
| Последующие сообщения           | Пересылка в существующую тему                       |

Пересылаются: текст, фото, видео, документы, аудио, голосовые, видео-кружки, стикеры.
