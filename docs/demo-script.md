# Сценарий демонстрации

Документ описывает последовательность показа проекта на защите. Сценарий построен так, чтобы раскрыть весь проект с нуля: от идеи и модулей до сборки, Kubernetes-ресурсов и решений о маршрутизации.

## 1. Постановка задачи

Начать можно с вопроса:

> Как система развертывания может понять, какая версия сервиса-поставщика безопасна для сервиса-потребителя?

Далее объяснить:

- Kubernetes умеет запускать pod'ы и deployments;
- Istio умеет направлять трафик между версиями;
- но обычная инфраструктура не знает, совместимы ли API двух версий сервисов;
- проект добавляет этот слой через API metadata и OpenAPI compatibility.

## 2. Общая идея проекта

Короткая формулировка:

> Проект связывает API-контракты из кода приложения с решениями о маршрутизации в Kubernetes/Istio.

Показать общий поток:

```text
API contract
    -> Spring Boot metadata endpoint
    -> OpenAPI compatibility check
    -> VirtualDeployment
    -> Istio route
```

## 3. Структура репозитория

Показать модули:

- `api-markup`: базовые API markers.
- `spring-boot-integration`: генерация clients и metadata endpoints.
- `orchestrator-service`: Kubernetes/Istio orchestration.
- `demo/demo-shop-api`: общий API contract.
- `demo/demo-shop-cart`: consumer service.
- `demo/demo-shop-product`: provider service.
- `demo/demo-shop`: namespace и gateway manifests.

## 4. API-контракт

Открыть:

```text
demo/demo-shop-api/src/main/kotlin/com/dekogen/gdeploy/demo/api/mapping/ProductMapping.kt
demo/demo-shop-api/src/main/kotlin/com/dekogen/gdeploy/demo/api/coupling/CartProductApi.kt
demo/demo-shop-api/src/main/kotlin/com/dekogen/gdeploy/demo/api/model/ProductDto.kt
```

Объяснить:

- API описан как обычный Kotlin/Spring contract;
- consumer зависит от contract, а не от конкретного deployment;
- provider реализует тот же contract.

## 5. Интеграционная библиотека

Открыть:

```text
spring-boot-integration/src/main/kotlin/com/dekogen/gdeploy/integration/RemoteClientFactory.kt
spring-boot-integration/src/main/kotlin/com/dekogen/gdeploy/integration/metaapi/ApiMetaController.kt
spring-boot-integration/src/main/kotlin/com/dekogen/gdeploy/integration/metaapi/MetaFactory.kt
```

Показать endpoints:

```text
/meta/api/client
/meta/api/client/{name}
/meta/api/server
/meta/api/server/{name}
```

Объяснить, что это машинно-читаемое описание зависимостей сервиса.

## 6. Сервис-оркестратор

Открыть:

```text
orchestrator-service/src/main/kotlin/com/dekogen/gdeploy/orchestrator/VirtualDeploymentProcessor.kt
orchestrator-service/src/main/kotlin/com/dekogen/gdeploy/orchestrator/apiobserver/ApiVersionService.kt
orchestrator-service/src/main/kotlin/com/dekogen/gdeploy/orchestrator/apiobserver/ApiCompatibilityService.kt
orchestrator-service/src/main/kotlin/com/dekogen/gdeploy/orchestrator/outrouting/ClientRoutingService.kt
```

Объяснить последовательность:

1. Оркестратор читает Kubernetes-ресурсы.
2. Получает API metadata от сервисов.
3. Проверяет OpenAPI compatibility.
4. Создает или обновляет версионированные развертывания.
5. Настраивает Istio routing.

## 7. Пользовательский ресурс

Открыть:

```text
orchestrator-service/deploy/virtual-deployment-crd.yaml
demo/demo-shop-product/deploy/demo-shop-product.yaml
demo/demo-shop-cart/deploy/demo-shop-cart.yaml
```

Объяснить поля:

- `boundService`: логический Kubernetes service;
- `versions`: список версий;
- `deployment`: шаблон реального Kubernetes Deployment.

## 8. Сборка

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

Ожидаемый результат: каждая команда завершается строкой `BUILD SUCCESSFUL`.

## 9. Развертывание в Kubernetes

Этот этап выполняется при наличии demo cluster с Istio.

```bash
kubectl apply -f demo/demo-shop/deploy/demo-shop.yaml
kubectl apply -f orchestrator-service/deploy/virtual-deployment-crd.yaml
kubectl apply -f orchestrator-service/deploy/filter.yaml
kubectl apply -f orchestrator-service/deploy/granular-deployment-orchestrator.yaml
kubectl apply -f demo/demo-shop-product/deploy/demo-shop-product.yaml
kubectl apply -f demo/demo-shop-cart/deploy/demo-shop-cart.yaml
```

Проверка ресурсов:

```bash
kubectl get virtualdeployments -n gd-demo-shop
kubectl get deployments -n gd-demo-shop
kubectl get destinationrules -n gd-demo-shop
kubectl get virtualservices -n gd-demo-shop
```

## 10. Финальная формулировка

> Проект «Контролируемое развертывание микросервисов с использованием сервисной сети» показывает, как использовать API-контракты при развертывании и принятии решений о маршрутизации. Система получает метаданные из кода приложения, проверяет совместимость и применяет результат на уровне Kubernetes/Istio.
