# Hands-on-Lab.Config-Center

Hands-on examples for various **Config Centers** and their integration with Spring Boot / Spring Cloud applications. 

This repo demostrates **Spring Cloud Config**, **Spring Cloud Bus**, **Consul**, **Spring Cloud Kubernentes Config**, and **AWS Config integration** in a cloud-native, dynamic configuration environment. 

--- 

## Table of Contents 
- [Overview](#overview)
- [Purpose](#purpose)
- [Hands-on Projects](#hands-on-projects)
    - [01: Spring Cloud Config](#01-spring-cloud-config)
    - [02: Spring Cloud Bus](#02-spring-cloud-bus)
    - [03: Consul Config](#03-consul-config)
    - [04: Spring Cloud Kubernetes Config](#04-spring-cloud-k8s-config)
    - [05: AWS Config](#05-aws-config)

- [Usage](#usage)
- [Verifying Configuration](#verifying-configuration)
- [License](#license)

---

## Overview 
Configuration management is critical in cloud-native microservices. Mismanaged thread pools, feature flags, or environment-specific settings can lead to **runtime issues, KPI problems, and production instability**.

This repository provides **hands-on examples** to explore: 

- How to dynamically refresh configurations.
- How configuraiton changes propagate across multiple instances. 
- How to integrate different config centers with Spring Boot / Spring Cloud apps. 

---

## Purpose 

- Centralize configuration for Spring Boot microservices. 
- Enable runtime refresh of configurations without restarting apps. 
- Illustrate differences between config approaches (lightweight vs scalable).
- Provide quick verification methods (logs, actuator endpoints, REST queries).

---
## Hands-on Projects

### 01: Spring Cloud Config 

- Spring Boot + Spring Cloud Config **client** and **server**. 
- `spring-cloud-config-repo` holds configurations files (dev/prod YAML).
- Features: 
    - `/actuator/refresh` for runtime refresh.
    - `EnvironmentChangeEvent` logs updated properties. 
    - Fetch latest config using `/acuator/env`.

#### Refresh Flow with Spring Cloud Config 
**Configuration Storage**
- Application configuration files are stored in a dedicated Git repository (e.g., `spring-cloud-config-app-prod.yml`)

**Startup**
- The **Config Server** reads the Git repo and serves configuration to client applications. 
- The **Config Client App** fetches its configuration at startup through `spring.config.import=configserver:`.

**Update Configuration**
- A change is committed and pushed to the Git repo (for example, toggling `custom.featureToggle.enableAwesomeFeature` from true to false). 

**Trigger Refresh**
- A refresh is manually triggered on the client by calling: 
```
curl -X POST http://localhost:8080/actuator/refresh
```

**Refresh Process**
- The client contacts the **Config Server(port:8888)**
- The **Config Server** pulls the latest configuration from the Git repo.
- If differences are detected, the new configuration is sent back to the client. 
- If differences are detected, the new configuration is sent back to the client. 
- The client udpates its in-memory **Environment** and publishes an **EnvironmentChangeEvent**.

**Event Handling**
- A custom listener in the client captures this event. 
- Changed keys and values are logged, showing exactly which properties were updated.

### 02: Spring Cloud Bus 
- Extends Spring Cloud Config to **broadcast refresh events** automatically.
- Integrates with **Kafka/RabbitMQ** for multi-instance config propagation. 
- Log **which key were updated** during refresh. 

### 03: Consul Config 
- Spring Cloud Consul integration.
- Combines **services discovery** + **dynamic configuration**.
- Supports `/actuator/refresh` and event listener logging. 


### 04: Spring Cloud Kubernentes Config 
- Integrates ConfigMaps and Secrets with Spring Boot Apps.
- Auto-refresh when ConfigMap/Secrets updates. 
- Logs events triggered on configuration change. 


### 05: AWS Config 
- Use AWS SSM Parameters Store or Secrets Manager. 
- Dynamically fetches remote config into Spring Environment.
- Supports actuator endpoints to verify refresh.
---

## Usage 
- Clone the repo: 
```bash 
git clone https://github.com/Rurutia1027/Hands-on-Lab.Config-Center.git
cd Hands-on-Lab.Config-Center
```
- Navigate to the config center of interest (01-spring-cloud-config etc.) and follow the instructions in its README.
- Start servers and clients as per each example.

--- 

## Verifying Configuration 
- Actuator endpoints (/actuator/env, /actuator/refresh) provide a quick way to inspect configs.
- Event listeners log which keys/values were refreshed.

- For Spring Cloud Config, /actuator/env shows source, active profile, and property value origins.

- Changes in YAML or remote Git repo are reflected dynamically after invoking refresh.

--- 

## Microservices and Config Center: Same Pattern or Different ? 

When applying these practices to **microservices**, most of the **config management principles remain the same**:

### Shared Config Repository 
- Multiple microservices call pull their environment-specific configs from the same Git-based config repo. 
- Each service identifies its configs by `spring.application.name` (or equivalent identifier in other frameworks).

### Runtime Refresh 
- Config centers like **Spring Cloud Config**, **Consul**, or **K8S ConfigMap** can propagate updated configs without redeployment. 
- For microservices, this ensures **dynamic feature toggles** or **operational parameter tuning** without downtime. 

### Differences to Consider 
- **Service-Specific Configs** -- Unlike a single monolith, each microservices may require a **separate set of configs**(DB connections, API keys, sharding rules).
- **Config Granularity** -- Some configs are **global**(e.g., observability, thread pool policies), whiel others are **per-service scoped**. 

- **Cross-Service Consistency** -- In microservices, **config drift across services** become a bigger risk; GitOps helps reduce this, but config repo must be carefully organized (per-service, profiles, or branches). 

- **Secret Management** -- As the number of microservices grows, the need for secure, automated **secrets distribution** becomes critical (e.g. Vault, AWS Secrets Manager). 


### In Summary 
- The **pattern of Git-backed config management** scales naturally from monolithic apps to **microservice ecosystems**.
- However, microservices introduce **new challenges in config granularity, isolation, and secret distribution** that require stricter repo organization and possibly integration with dedicated secrets managers. 
- Combining **Config Centers (Spring Cloud Config, Consul, K8S ConfigMap)** with **GitOps** practices is the emerging best practice to balance **flexibility**, **observability**, and **security** in cloud-native environments.  


---

## LICENSE
- [LICENSE](./LICENSE)
