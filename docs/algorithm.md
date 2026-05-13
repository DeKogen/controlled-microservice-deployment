# Алгоритм работы оркестратора

Документ описывает центральную логику проекта: как система получает информацию о сервисах, проверяет совместимость API и применяет результат к маршрутизации.

## Входные данные

Оркестратор использует несколько источников данных:

- `VirtualDeployment` resources;
- Kubernetes `Service`, `Deployment`, `Pod`;
- Istio `VirtualService` и `DestinationRule`;
- OpenAPI metadata из `/meta/api/client/{name}`;
- OpenAPI metadata из `/meta/api/server/{name}`.

## Выходные данные

Результатом работы являются:

- версионированные Kubernetes deployments;
- subsets в `DestinationRule`;
- routes в `VirtualService`;
- маршруты между consumer version и provider version;
- обновленная конфигурация сервисной сети.

## Общий алгоритм

```text
для каждого VirtualDeployment:
    прочитать logical service name
    прочитать список версий
    для каждой версии:
        создать или обновить Kubernetes Deployment
        назначить labels для версии
    обновить DestinationRule subsets

периодически или по событию:
    найти consumer services
    найти provider services
    получить client API metadata
    получить server API metadata
    сравнить OpenAPI contracts
    построить compatibility graph
    для каждой совместимой пары:
        обновить route в Istio
```

## Псевдокод

```text
function reconcileVirtualDeployments():
    virtualDeployments = kubernetes.list(VirtualDeployment)

    for vd in virtualDeployments:
        serviceName = vd.spec.boundService
        versions = vd.spec.versions
        template = vd.spec.deployment

        for version in versions:
            deployment = buildVersionedDeployment(template, serviceName, version)
            kubernetes.apply(deployment)

        destinationRule = buildDestinationRule(serviceName, versions)
        istio.apply(destinationRule)


function reconcileApiRoutes():
    consumers = discoverConsumerVersions()
    providers = discoverProviderVersions()

    for consumer in consumers:
        clientApis = fetchClientMetadata(consumer)

        for provider in providers:
            serverApis = fetchServerMetadata(provider)

            for apiName in intersection(clientApis, serverApis):
                if isCompatible(clientApis[apiName], serverApis[apiName]):
                    route(consumer, provider)
```

## Построение графа совместимости

Совместимость можно представить как ориентированный граф:

```text
consumer version  ->  provider version
```

Ребро существует, если:

- consumer объявляет зависимость от API;
- provider публикует этот API;
- OpenAPI description provider совместим с требованиями consumer.

## Принятие routing decision

Routing decision строится по правилам:

1. Если совместимая версия provider найдена, consumer направляется к ней.
2. Если совместимых версий несколько, можно выбрать последнюю или заданную политикой.
3. Если совместимых версий нет, маршрут не должен обновляться на несовместимую версию.

В текущем проекте реализована базовая логика построения маршрутов, достаточная для демонстрационного сценария.

## Сложность алгоритма

Пусть:

- `C` - количество consumer versions;
- `P` - количество provider versions;
- `A` - количество API contracts.

Наивная проверка совместимости имеет порядок:

```text
O(C * P * A)
```

Для демонстрационного проекта этого достаточно. В промышленной версии можно добавить caching metadata, incremental reconciliation и индексацию API names.

## Отказоустойчивость

Для production-ready версии важны дополнительные механизмы:

- retry при недоступности service metadata;
- timeout для HTTP-запросов к `/meta/api`;
- сохранение последнего корректного route;
- метрики reconciliation loop;
- события Kubernetes для ошибок совместимости.
