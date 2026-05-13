# Сравнение с существующими подходами

Проект не заменяет Kubernetes, Istio или contract testing. Он объединяет их идеи в отдельный слой принятия решений, где маршрутизация зависит от совместимости API.

## Сравнительная таблица

| Подход | Что умеет | Чего не хватает | Как решает проект |
| --- | --- | --- | --- |
| Kubernetes rolling update | Обновляет pods и deployments. | Не понимает API-зависимости между сервисами. | Добавляет проверку API compatibility перед routing decision. |
| Istio traffic routing | Направляет трафик между subsets и versions. | Не решает, какие versions совместимы. | Генерирует маршруты на основе OpenAPI compatibility. |
| Manual deployment | Позволяет инженеру полностью контролировать rollout. | Медленно, ошибочно, плохо масштабируется. | Автоматизирует повторяемые решения по совместимости и маршрутам. |
| Spring Cloud / service discovery | Помогает находить сервисы и вызывать их. | Не управляет Kubernetes/Istio routes по API compatibility. | Связывает service metadata с инфраструктурной маршрутизацией. |
| Consumer-driven contract testing | Проверяет contract между consumer и provider. | Обычно не управляет runtime routing. | Использует contract information для deployment/runtime decisions. |
| OpenAPI documentation | Описывает HTTP API. | Само по себе не влияет на deployment. | Превращает OpenAPI descriptions в сигнал для routing. |

## Отличие проекта

Ключевое отличие:

> Проект использует API-контракты не только как документацию или тестовый артефакт, а как входные данные для развертывания и маршрутизации.

Это дает три свойства:

1. **Контролируемость**: маршрут выбирается не вручную, а по проверяемому правилу.
2. **Прослеживаемость**: можно объяснить, почему consumer направлен к конкретному provider.
3. **Автоматизация**: повторяемая логика вынесена в orchestrator service.

## Что остается за существующими инструментами

Kubernetes продолжает отвечать за:

- запуск pod'ов;
- services;
- deployments;
- custom resources;
- API server.

Istio продолжает отвечать за:

- traffic routing;
- VirtualService;
- DestinationRule;
- gateway;
- Envoy configuration.

Проект добавляет слой между ними:

```text
API compatibility -> routing decision -> Kubernetes/Istio resources
```

## Почему это важно для диплома

Для дипломной работы важно показать, что проект не является обычной настройкой Kubernetes. Его вклад находится на уровне принятия решений:

- извлечение API-зависимостей из кода;
- генерация machine-readable metadata;
- сравнение контрактов;
- построение compatibility graph;
- применение результата к сервисной сети.

Такой подход демонстрирует инженерную и исследовательскую часть работы.
