# Диаграммы

Диаграммы можно использовать в тексте диплома и презентации. Они написаны в формате Mermaid, который поддерживается многими Markdown viewer'ами.

## Общая архитектура

```mermaid
flowchart LR
    API[demo-shop-api<br/>API-контракт]
    Cart[demo-shop-cart<br/>consumer service]
    Product[demo-shop-product<br/>provider service]
    Integration[spring-boot-integration<br/>API metadata]
    Orchestrator[orchestrator-service<br/>compatibility + routing]
    K8s[Kubernetes<br/>VirtualDeployment / Deployment]
    Istio[Istio<br/>VirtualService / DestinationRule]

    API --> Cart
    API --> Product
    Cart --> Integration
    Product --> Integration
    Integration --> Orchestrator
    Orchestrator --> K8s
    Orchestrator --> Istio
    Istio --> Product
    Cart --> Istio
```

## Последовательность проверки совместимости

```mermaid
sequenceDiagram
    participant C as Consumer service
    participant P as Provider service
    participant O as Orchestrator
    participant I as Istio

    O->>C: GET /meta/api/client/{api}
    C-->>O: OpenAPI requirements
    O->>P: GET /meta/api/server/{api}
    P-->>O: OpenAPI implementation
    O->>O: Compare OpenAPI contracts
    alt API compatible
        O->>I: Update VirtualService route
    else API incompatible
        O->>I: Keep existing route
    end
```

## Развертывание через VirtualDeployment

```mermaid
flowchart TD
    VD[VirtualDeployment YAML]
    Processor[VirtualDeploymentProcessor]
    D1[Deployment service-v1]
    D2[Deployment service-v2]
    D3[Deployment service-v3]
    DR[DestinationRule subsets]
    VS[VirtualService routes]

    VD --> Processor
    Processor --> D1
    Processor --> D2
    Processor --> D3
    Processor --> DR
    DR --> VS
```

## Граф совместимости

```mermaid
flowchart LR
    CartV1[cart:v1]
    CartV2[cart:v2]
    ProductV1[product:v1]
    ProductV2[product:v2]
    ProductV3[product:v3]

    CartV1 -->|compatible| ProductV1
    CartV1 -->|compatible| ProductV2
    CartV2 -->|compatible| ProductV3
```

## Контур дипломного решения

```mermaid
flowchart LR
    Code[Код приложения]
    Contract[API-контракт]
    Metadata[OpenAPI metadata]
    Compare[Compatibility check]
    Decision[Routing decision]
    Mesh[Сервисная сеть]

    Code --> Contract
    Contract --> Metadata
    Metadata --> Compare
    Compare --> Decision
    Decision --> Mesh
```
