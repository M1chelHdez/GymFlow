# GymFlow — diagramas de arquitectura y diseño

Los diagramas representan el esquema SQL de las migraciones Flyway `V1` a `V4` y los flujos implementados en la demo. No representan funcionalidades futuras como JWT, cobros reales o multi-tenancy completo.

## 1. Arquitectura lógica

```mermaid
flowchart LR
    U[Administrador demo] --> WEB[Navegador]
    WEB --> ANG[Angular 20]
    ANG -->|HTTP / JSON| API[Spring Boot REST]
    API --> SEC[Spring Security<br/>HTTP Basic demo]
    SEC --> CTRL[Controllers]
    CTRL --> SVC[Services / reglas de negocio]
    SVC --> REPO[Spring Data JPA]
    REPO --> DB[(PostgreSQL 17)]
    MIG[Flyway] --> DB
    API --> SW[OpenAPI / Swagger UI]
```

## 2. Diagrama entidad–relación (ERD)

```mermaid
erDiagram
    GYMS ||--o{ MEMBERS : registers
    GYMS ||--o{ MEMBERSHIP_PLANS : defines
    GYMS ||--o{ PRODUCT_CATEGORIES : groups
    GYMS ||--o{ SUPPLIERS : catalogs
    GYMS ||--o{ PRODUCTS : owns
    GYMS ||--o{ STOCK_BALANCES : tracks
    GYMS ||--o{ STOCK_MOVEMENTS : records
    GYMS ||--o{ MEMBERSHIPS : manages
    GYMS ||--o{ SALES : records
    GYMS ||--o{ PRODUCT_CHANGE_HISTORY : audits
    MEMBERS ||--o{ MEMBERSHIPS : receives
    MEMBERSHIP_PLANS ||--o{ MEMBERSHIPS : applies
    PRODUCT_CATEGORIES o|--o{ PRODUCTS : classifies
    PRODUCTS ||--o| STOCK_BALANCES : balance
    PRODUCTS ||--o{ STOCK_MOVEMENTS : movement
    PRODUCTS ||--o{ SALE_ITEMS : sold_as
    PRODUCTS ||--o{ PRODUCT_CHANGE_HISTORY : changes
    MEMBERS o|--o{ SALES : purchases
    SALES ||--|{ SALE_ITEMS : contains

    GYMS {
      uuid id PK
      varchar name
      varchar timezone
      varchar currency
      varchar status
    }
    MEMBERS {
      uuid id PK
      uuid gym_id FK
      varchar member_number
      varchar full_name
      varchar email
      varchar phone
      varchar status
    }
    MEMBERSHIP_PLANS {
      uuid id PK
      uuid gym_id FK
      varchar name
      int duration_value
      varchar duration_unit
      numeric price
      varchar status
    }
    MEMBERSHIPS {
      uuid id PK
      uuid gym_id FK
      uuid member_id FK
      uuid plan_id FK
      date start_date
      date end_date
      numeric agreed_price
      varchar status
    }
    PRODUCT_CATEGORIES {
      uuid id PK
      uuid gym_id FK
      varchar name
      boolean active
    }
    SUPPLIERS {
      uuid id PK
      uuid gym_id FK
      varchar name
      varchar email
      varchar phone
    }
    PRODUCTS {
      uuid id PK
      uuid gym_id FK
      uuid category_id FK
      varchar sku
      varchar name
      numeric cost_price
      numeric sale_price
      int minimum_stock
      varchar status
    }
    STOCK_BALANCES {
      uuid id PK
      uuid gym_id FK
      uuid product_id FK
      int quantity
      bigint version
    }
    STOCK_MOVEMENTS {
      uuid id PK
      uuid gym_id FK
      uuid product_id FK
      varchar type
      int quantity
      varchar reason
    }
    SALES {
      uuid id PK
      uuid gym_id FK
      uuid member_id FK
      varchar folio
      numeric subtotal
      numeric discount_total
      numeric total
      varchar status
    }
    SALE_ITEMS {
      uuid id PK
      uuid sale_id FK
      uuid product_id FK
      int quantity
      numeric unit_price
      numeric unit_cost
      numeric line_total
    }
    PRODUCT_CHANGE_HISTORY {
      uuid id PK
      uuid gym_id FK
      uuid product_id FK
      varchar field_name
      text old_value
      text new_value
      timestamptz changed_at
    }
```

**Nota:** `suppliers` está modelada como catálogo independiente; las migraciones revisadas no definen una FK de proveedor en `products`. Las relaciones `GYMS` reflejan las FK declaradas en el esquema, no una afirmación de aislamiento multi-tenant completo.

## 3. Casos de uso (UML)

```mermaid
flowchart LR
    ADMIN((Administrador))
    subgraph GYMFLOW[GymFlow]
      UC1([Gestionar socios])
      UC2([Gestionar planes y membresías])
      UC3([Gestionar categorías y proveedores])
      UC4([Gestionar productos])
      UC5([Consultar inventario y movimientos])
      UC6([Registrar ventas])
      UC7([Consultar reportes])
      UC8([Consultar historial de cambios])
    end
    ADMIN --- UC1
    ADMIN --- UC2
    ADMIN --- UC3
    ADMIN --- UC4
    ADMIN --- UC5
    ADMIN --- UC6
    ADMIN --- UC7
    ADMIN --- UC8
```

## 4. Flujo de venta

```mermaid
flowchart TD
    A([Inicio]) --> B[Seleccionar socio opcional]
    B --> C[Agregar productos y cantidades]
    C --> D[Enviar solicitud de venta]
    D --> E[Validar datos y productos]
    E --> F{¿Stock suficiente?}
    F -- No --> X[Rechazar operación]
    X --> Z([Fin sin persistir venta])
    F -- Sí --> G[Calcular importes]
    G --> H[Registrar venta y partidas]
    H --> I[Descontar existencias y registrar movimientos]
    I --> J{¿Transacción exitosa?}
    J -- No --> K[Rollback]
    K --> Z
    J -- Sí --> L[Devolver folio VTA]
    L --> M[Mostrar confirmación]
    M --> N([Fin])
```

## 5. Flujo de inventario

```mermaid
flowchart TD
    A([Inicio]) --> B[Seleccionar producto]
    B --> C[Elegir tipo de movimiento]
    C --> D[Capturar cantidad y motivo]
    D --> E[Validar producto y cantidad]
    E --> F{¿Datos válidos?}
    F -- No --> X[Mostrar validación]
    X --> Z([Fin])
    F -- Sí --> G[Calcular saldo resultante]
    G --> H{¿Saldo no negativo?}
    H -- No --> Y[Rechazar movimiento]
    Y --> Z
    H -- Sí --> I[Actualizar saldo]
    I --> J[Registrar movimiento]
    J --> K[Confirmar operación]
    K --> Z
```

## 6. Flujo de gestión de membresías

```mermaid
flowchart TD
    A([Inicio]) --> B[Seleccionar socio]
    B --> C[Seleccionar plan]
    C --> D[Definir fecha de inicio]
    D --> E[Calcular fecha de vencimiento]
    E --> F[Registrar membresía]
    F --> G[Consultar vigencia y estado]
    G --> H([Fin])
```

## 7. Secuencia de venta (UML)

```mermaid
sequenceDiagram
    actor Admin as Administrador
    participant FE as Angular
    participant API as SaleController
    participant SVC as SaleService
    participant DB as PostgreSQL
    Admin->>FE: Confirma carrito
    FE->>API: POST /api/gyms/{gymId}/sales
    API->>SVC: createSale(request)
    SVC->>DB: Validar y bloquear existencias
    DB-->>SVC: Saldos disponibles
    alt Stock suficiente
      SVC->>DB: Guardar venta y partidas
      SVC->>DB: Descontar stock y registrar movimientos
      DB-->>SVC: Commit
      SVC-->>API: SaleResponse + folio
      API-->>FE: 201 Created
      FE-->>Admin: Mostrar confirmación
    else Stock insuficiente
      SVC-->>API: Excepción de negocio
      API-->>FE: Respuesta de error
      FE-->>Admin: Mostrar error
    end
```

## 8. Despliegue objetivo (pendiente de hardening)

```mermaid
flowchart TB
    B[Usuario / navegador] --> HOST[Hosting estático Angular]
    HOST -->|HTTPS API| APP[Servicio Spring Boot]
    APP --> DB[(PostgreSQL administrado)]
    APP --> MIG[Flyway migrations]
    MIG --> DB
```

Este diagrama es una arquitectura objetivo, no un despliegue existente. Antes de publicar la API se debe retirar el interceptor Angular que inyecta credenciales Basic fijas, configurar autenticación segura, HTTPS, secretos del proveedor y CORS restringido al dominio del frontend.
