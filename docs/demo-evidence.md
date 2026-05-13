# Ожидаемые выводы команд и артефакты демонстрации

Этот документ помогает подготовить защиту даже в случае, если live cluster недоступен. Он фиксирует, какие команды показывают работоспособность проекта и какой смысл имеет каждый вывод.

## Сборка библиотек

Команды:

```powershell
cd api-markup
.\gradlew.bat publishToMavenLocal

cd ..\spring-boot-integration
.\gradlew.bat publishToMavenLocal

cd ..\demo\demo-shop-api
.\gradlew.bat publishToMavenLocal
```

Ожидаемый результат:

```text
BUILD SUCCESSFUL
```

Что это доказывает:

- общий API-модуль собирается;
- интеграционная библиотека компилируется;
- demo API contract доступен зависимым сервисам.

## Тесты сервисов

Команды:

```powershell
cd demo\demo-shop-cart
.\gradlew.bat test

cd ..\demo-shop-product
.\gradlew.bat test

cd ..\..\orchestrator-service
.\gradlew.bat test
```

Ожидаемый результат:

```text
BUILD SUCCESSFUL
```

Что это доказывает:

- Spring Boot context demo services запускается;
- основная логика orchestrator-service компилируется и тестируется;
- отключенные cluster-dependent tests не мешают локальной проверке.

## Проверка Kubernetes resources

Команды для demo cluster:

```bash
kubectl get virtualdeployments -n gd-demo-shop
kubectl get deployments -n gd-demo-shop
kubectl get destinationrules -n gd-demo-shop
kubectl get virtualservices -n gd-demo-shop
```

Ожидаемый смысл вывода:

- `virtualdeployments` показывает logical services и их versions;
- `deployments` показывает реальные развертывания версий;
- `destinationrules` показывает subsets для маршрутизации;
- `virtualservices` показывает active routes.

## Проверка metadata endpoints

Примеры endpoints:

```text
http://gd-shop.example.local/cart/meta/api/client/CartProductApi
http://gd-shop.example.local/product/meta/api/server/CartProductApi
```

Ожидаемый смысл:

- первый endpoint показывает требования consumer service;
- второй endpoint показывает API provider service;
- оба ответа представлены в формате OpenAPI.

## Проверка routing endpoint оркестратора

Endpoint:

```text
/api/v1/routing
```

Ожидаемый смысл:

- показывает рассчитанные маршруты между сервисами;
- демонстрирует compatibility graph;
- подтверждает, что маршруты строятся на основе API metadata.

## Что сохранить для диплома

Для текста диплома и презентации полезно сохранить:

- скриншот успешной сборки;
- скриншот `kubectl get virtualdeployments`;
- скриншот `kubectl get virtualservices`;
- пример JSON/OpenAPI ответа из `/meta/api`;
- пример ответа `/api/v1/routing`;
- диаграмму из [diagrams.md](diagrams.md).
