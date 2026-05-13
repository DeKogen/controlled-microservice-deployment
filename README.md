# Контролируемое развертывание микросервисов с использованием сервисной сети

> Что, если микросервисную систему можно обновлять так же осознанно, как набор библиотек?

«Контролируемое развертывание микросервисов с использованием сервисной сети» - учебно-исследовательский проект, разработанный с нуля для автоматизации развертывания микросервисов с учетом совместимости API. Проект объединяет Spring Boot, Kubernetes, Istio, OpenAPI и собственный оркестратор, который анализирует зависимости между сервисами и управляет маршрутизацией между версиями.

Главная идея проекта: сервисы должны не только принимать HTTP-запросы, но и явно публиковать информацию о том, какие API они предоставляют и какие API потребляют. На основе этих метаданных система может проверить совместимость версий и направить потребителя к подходящему поставщику.

## Научная новизна и практическая ценность

В обычном Kubernetes rollout учитывается состояние pod'ов, но не учитывается совместимость API между версиями сервисов. В обычной сервисной сети можно настроить маршруты, но решение о том, какие версии совместимы, остается ручным.

В этом проекте routing decision связывается с API-контрактами:

- API-зависимости извлекаются из кода приложения;
- клиентские и серверные контракты публикуются как OpenAPI metadata;
- совместимость проверяется автоматически;
- результат проверки применяется к Kubernetes/Istio routing;
- deployment process получает знание о сервисных зависимостях.

Практическая ценность проекта - снижение ручных действий при обновлении микросервисов и уменьшение риска направить потребителя на несовместимую версию поставщика.

## Назначение

Проект решает задачу безопасного обновления API в микросервисной архитектуре:

- фиксирует зависимости между сервисами на уровне кода;
- генерирует OpenAPI-описания для клиентских и серверных API;
- сравнивает API-контракты потребителя и поставщика;
- создает версионированные Kubernetes-развертывания;
- обновляет маршрутизацию в сервисной сети Istio между совместимыми версиями сервисов;
- показывает полный демонстрационный сценарий на примере магазина.

## Состав проекта

| Модуль | Назначение |
| --- | --- |
| `api-markup` | Минимальный общий модуль с API-маркерами. |
| `spring-boot-integration` | Интеграция со Spring Boot: создает remote client и публикует API-метаданные в `/meta/api`. |
| `orchestrator-service` | Центральный оркестратор: работает с Kubernetes, custom resource `VirtualDeployment`, Istio и OpenAPI compatibility. |
| `demo/demo-shop-api` | Общий API-контракт демонстрационного магазина: DTO, mapping и coupling-интерфейсы. |
| `demo/demo-shop-cart` | Сервис-потребитель: вызывает Product API через интеграционный клиент. |
| `demo/demo-shop-product` | Сервис-поставщик: реализует Product API. |
| `demo/demo-shop` | Namespace и Istio gateway для демонстрационного приложения. |

## Как работает система

```text
demo-shop-cart
  объявляет зависимость от CartProductApi
        |
        v
spring-boot-integration
  публикует /meta/api/client/{api}
        |
        v
orchestrator-service
  получает метаданные, сравнивает OpenAPI, выбирает совместимые версии
        |
        v
Istio VirtualService / DestinationRule
        |
        v
demo-shop-product
  публикует /meta/api/server/{api}
```

Подробное описание архитектуры находится в [docs/architecture.md](docs/architecture.md).

## Документация

| Документ | Назначение |
| --- | --- |
| [docs/architecture.md](docs/architecture.md) | Архитектура системы и основные потоки данных. |
| [docs/algorithm.md](docs/algorithm.md) | Алгоритм работы оркестратора и псевдокод. |
| [docs/comparison.md](docs/comparison.md) | Сравнение проекта с Kubernetes rollout, Istio routing, Spring Cloud и contract testing. |
| [docs/diagrams.md](docs/diagrams.md) | Диаграммы для диплома и презентации. |
| [docs/demo-evidence.md](docs/demo-evidence.md) | Ожидаемые выводы команд и артефакты демонстрации. |
| [docs/demo-script.md](docs/demo-script.md) | Сценарий защиты и показа проекта. |
| [docs/evaluation.md](docs/evaluation.md) | Оценка результата и критерии завершенности. |
| [docs/thesis-outline.md](docs/thesis-outline.md) | Структура дипломной работы. |

## Основные механизмы

### API-метаданные

`spring-boot-integration` анализирует Spring MVC mappings и публикует служебные endpoints:

```text
/meta/api/client
/meta/api/client/{name}
/meta/api/server
/meta/api/server/{name}
```

Так каждый сервис может сообщить, какие API он потребляет и какие предоставляет.

### Совместимость API

`orchestrator-service` получает OpenAPI-описания сервисов и сравнивает их через `ApiCompatibilityService`. Если API поставщика совместим с требованиями потребителя, эта версия может использоваться в маршрутизации.

### Версионированное развертывание

Пользовательский ресурс `VirtualDeployment` описывает логический сервис, список его версий и базовый Kubernetes `Deployment`. Оркестратор превращает это описание в реальные версионированные развертывания.

### Маршрутизация

Istio используется как сервисная сеть. После проверки совместимости оркестратор обновляет `VirtualService`, `DestinationRule` и Envoy routing logic, чтобы трафик шел к подходящей версии сервиса.

## Сборка

Репозиторий состоит из нескольких независимых Gradle-проектов. Сначала публикуются локальные библиотеки, затем собираются сервисы.

Windows PowerShell:

```powershell
cd api-markup
.\gradlew.bat publishToMavenLocal

cd ..\spring-boot-integration
.\gradlew.bat publishToMavenLocal

cd ..\demo\demo-shop-api
.\gradlew.bat publishToMavenLocal

cd ..\demo-shop-cart
.\gradlew.bat test

cd ..\demo-shop-product
.\gradlew.bat test

cd ..\..\orchestrator-service
.\gradlew.bat test
```

Linux/macOS:

```bash
chmod +x api-markup/gradlew spring-boot-integration/gradlew demo/demo-shop-api/gradlew demo/demo-shop-cart/gradlew demo/demo-shop-product/gradlew orchestrator-service/gradlew

(cd api-markup && ./gradlew publishToMavenLocal)
(cd spring-boot-integration && ./gradlew publishToMavenLocal)
(cd demo/demo-shop-api && ./gradlew publishToMavenLocal)
(cd demo/demo-shop-cart && ./gradlew test)
(cd demo/demo-shop-product && ./gradlew test)
(cd orchestrator-service && ./gradlew test)
```

## Демонстрационное развертывание

Для демонстрации в Kubernetes нужны:

- Kubernetes cluster с доступом из оркестратора;
- установленный Istio с ingress gateway;
- реестр образов, доступный по адресу `10.0.10.1:5000`, либо измененные image names в манифестах;
- демонстрационные host names, например `gd-shop.example.local`, направленные на ingress gateway.

Порядок применения манифестов:

```bash
kubectl apply -f demo/demo-shop/deploy/demo-shop.yaml
kubectl apply -f orchestrator-service/deploy/virtual-deployment-crd.yaml
kubectl apply -f orchestrator-service/deploy/filter.yaml
kubectl apply -f orchestrator-service/deploy/granular-deployment-orchestrator.yaml
kubectl apply -f demo/demo-shop-product/deploy/demo-shop-product.yaml
kubectl apply -f demo/demo-shop-cart/deploy/demo-shop-cart.yaml
```

Сценарий защиты и показа проекта описан в [docs/demo-script.md](docs/demo-script.md).

## Проверка результата

Проект включает unit и integration-style тесты для ключевых частей:

- совместимость API;
- генерация маршрутов между версиями;
- работа custom Kubernetes client;
- генерация Envoy/Lua routing script;
- Spring Boot context tests для demo services.

Подробная оценка результата находится в [docs/evaluation.md](docs/evaluation.md).

## Технологии

- Java 21
- Kotlin 1.9.24
- Gradle 8.9
- Spring Boot 3.5.14
- Springdoc OpenAPI 2.x
- OpenAPI Diff
- Fabric8 Kubernetes client
- Istio / Snowdrop Istio client

## Ограничения

- Демо-манифесты содержат конкретные namespace, host names и адрес registry.
- Тесты, требующие реального Kubernetes/Istio cluster, отключены для обычного локального запуска.
- В демонстрационном манифесте оркестратор использует широкие Kubernetes-права для упрощения показа.
- Репозиторий разделен на несколько Gradle-проектов, чтобы явно показать роли библиотек, интеграции, оркестратора и demo services.
