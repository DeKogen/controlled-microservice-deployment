# Архитектура

Проект «Контролируемое развертывание микросервисов с использованием сервисной сети» построен как полный цикл API-aware deployment: от объявления API-зависимости в коде до маршрутизации между совместимыми версиями сервисов в Kubernetes/Istio.

Проект состоит из трех логических уровней:

- уровень приложения: demo services и общий API-контракт;
- интеграционный уровень: Spring Boot library для генерации клиентов и API-метаданных;
- инфраструктурный уровень: Kubernetes orchestrator, custom resource и Istio routing.

## Компоненты

| Компонент | Назначение |
| --- | --- |
| `demo-shop-api` | Описывает общий контракт между cart и product services. |
| `demo-shop-cart` | Потребляет Product API и показывает сторону client dependency. |
| `demo-shop-product` | Реализует Product API и показывает сторону server implementation. |
| `spring-boot-integration` | Формирует remote client и публикует OpenAPI-метаданные для клиентских и серверных API. |
| `api-markup` | Содержит общие API-маркеры. |
| `orchestrator-service` | Проверяет версии сервисов, сравнивает OpenAPI-контракты, управляет Kubernetes/Istio-ресурсами. |
| `VirtualDeployment` | Пользовательский Kubernetes resource для описания логического сервиса и его версий. |
| Istio | Выполняет фактическую маршрутизацию трафика между версиями в сервисной сети. |

## Поток API-метаданных

```text
1. API-контракт описывается в demo-shop-api.
2. Provider service реализует этот контракт.
3. Consumer service объявляет зависимость от этого контракта.
4. spring-boot-integration публикует:
   /meta/api/client
   /meta/api/client/{name}
   /meta/api/server
   /meta/api/server/{name}
5. orchestrator-service получает OpenAPI descriptions.
6. ApiCompatibilityService сравнивает требования consumer и возможности provider.
```

Этот поток нужен для того, чтобы информация о совместимости была получена из кода, а не из ручной документации.

## Поток развертывания

```text
VirtualDeployment YAML
        |
        v
VirtualDeploymentProcessor
        |
        v
Versioned Kubernetes Deployments
        |
        v
DestinationRule subsets
        |
        v
VirtualService routes
```

`VirtualDeployment` содержит:

- `boundService`: имя логического Kubernetes service;
- `versions`: список версий сервиса;
- `deployment`: базовый Kubernetes Deployment template.

Оркестратор читает этот resource и создает отдельные развертывания для версий, сохраняя связь между логическим сервисом и его конкретными версиями.

## Поток маршрутизации

```text
Consumer version
        |
        | requires API
        v
OpenAPI compatibility check
        |
        | compatible with
        v
Provider version
        |
        v
Istio route update
```

Маршрутизация строится не только по факту существования pod'а, а по совместимости API-контрактов. Это позволяет связать deployment logic и application compatibility.

## Роль оркестратора

`orchestrator-service` выполняет несколько задач:

- работает с Kubernetes API через Fabric8 client;
- регистрирует custom resource mapping для `VirtualDeployment`;
- создает и обновляет Kubernetes-развертывания;
- читает и обновляет Istio resources;
- получает OpenAPI metadata от сервисов;
- строит граф совместимости API;
- применяет маршруты для совместимых версий.

## Роль интеграционной библиотеки

`spring-boot-integration` встраивается в обычное Spring Boot application. Она решает две задачи:

- создает remote client для обращения к другому сервису через интерфейс API;
- публикует machine-readable metadata о клиентских и серверных API.

Так приложение остается обычным Spring Boot service, но становится понятным для системы развертывания.

## Итоговая схема

```text
Code contracts
    -> OpenAPI metadata
    -> Compatibility graph
    -> Versioned deployments
    -> Istio routing
```

Архитектура связывает API-контракты уровня приложения с инфраструктурными решениями о маршрутизации.
