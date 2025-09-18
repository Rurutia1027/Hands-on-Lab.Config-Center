# Spring Cloud Bus Hands On

## Introduction 
This demo shows how **Spring Cloud Bus** can be used to broadcast configuration changes multiple instances of a Spring Boot application. 

Without Spring Cloud Bus, when using **Spring Cloud Config**, refreshing configuration (via `/actuator/refresh`) only updates one instance. Other instances will not be aware of the update and may run with stale configuration. 

With **Spring Cloud Bus**, the refresh event is **propagated via a message broker (RabbitMQ/kafka)** to all registered application instances. This ensures **consistency** and avoid **config drift** across the distributed system. 

---

## Beginner's Guide to Spring Cloud Bus 
If you are new to **Spring Cloud Bus**, here are the key points you should know: 

### What is Spring Cloud Bus? 
- Spring Cloud Bus links **distributed Spring Boot applications** with a **lightweight message broker**(RabbitMQ or Kafka).
- It extends the idea of **Spring ApplicationEvents** across **a cluster of applications**.
- Instead of each instance working in isolation, Spring Cloud Bus provides a **communication channel** so events can be **broadcasted to all nodes**.

### Why was it created ? 
- In a **microservices architecture**, you usually have many instances of the same service. 
- If you only update one instance (using `/actuator/refresh`), other instances won't notice. 
- Spring Cloud Bus solves this problem by turning a **local refresh event into a distributed event**.

### Key Conceptos 
#### Message Broker Required 
- Spring Cloud Bus does not work by itself; it needs RabbitMQ or Kafka underneath. 
- It automatically wires itself to the broker and sends/receives events. 

#### Bus Events 
- A `RefreshRemoteApplicationEvent` is the most common event: it tells all instances to reload their configuration.
- You can also define **custom events** and broadcast them. 

#### Endpoints 
- `/actuator/refresh`: Refreshes **only the current instance**.
- `/actuator/bus-refresh`: Broadcasts a refresh event to **all instances via the message broker**. 

#### Server vs Client 
- There is **no special Bus Server**. Any instance connected to the bus can **send and receive events**. 
- You typically still need a **Config Server** (to store and serve configuraiton file). The bus just distributes events more effectively.


#### Best Practices 
- Always prefer `/actuator/bus-refresh` when running in distributed environments.
- Use Spring Cloud Bus when your system has **more than a few instances** -- it avoids configuraiton drift and reduces manual refresh operations.

---

## Core Idea 

- **Spring Cloud Config Server** fetches configuration from a store -- we use Git repository to store the config files. 
- Applications (clients) fetch configuration from the Config Server at startup. 
- Normally: refershing via POST `/actuator/refresh` only updates the instance that received the request. 
- With Spring Cloud Bus: 
> You trigger POST `/actuator/bus-refresh` on any instance. 
> Spring Cloud Bus publishes the event to a **message broker**.
> All application instances subscribed to the bus receive the event. 
> Each instance updates its environment and re-triggers `EnvironmentChangeEvent`. 


--- 

## Architecture 

### Without Spring Cloud Bus _ONLY Spring Cloud Conifg_

```
   Git Config Repo
          │
          ▼
   Config Server
          │
POST /actuator/refresh (app-0)
          │
          ▼
   app-0 updated
   app-1 ~ app-7 remain stale
```


### With Spring Cloud Bus 

```
   Git Config Repo
          │
          ▼
   Config Server
          │
POST /actuator/bus-refresh (any instance)
          │
          ▼
     Message Broker (RabbitMQ/Kafka)
          │
 ┌────────┼────────┐
 ▼        ▼        ▼
app-0    app-1    ... app-7
   │        │           │
   ▼        ▼           ▼
 All instances updated
```


### Features Demostrated 
- Config Server + Git Backend Store (centralized config management)
- Spring Cloud Bus with RabbitMQ (event broadcasting)
- EnvironmentChangeEvent Listner to verify that all instances receive updates. 


### Project Structure 

```
02-spring-cloud-bus/
├── config-server/        # Spring Cloud Config Server
├── config-client/        # Spring Boot client (multi-instance)
├── docker-compose.yml    # RabbitMQ setup
└── README.md             # This document
```

---

## How to Run 

To fully understand how **Spring Cloud Bus** works, you need to run **multiple instances** of the same application. 
We use **Jib** to build a lightweight Docker image, then configure multiple containers in `docker-compose.yml`. 

### Step-1: Start RabbitMQ 
```
docker-compose up -d rabbitmq
```


### Step-2: Run Multiple Instances with Jib + Docker Compse 
```bash 
./mvnw compile jib:dockerBuild
```

Then bring up multiple instances with different ports and app names: 
```yaml 
services:
  app1:
    image: spring-bus-demo:latest
    ports:
      - "8081:8080"
    environment:
      - SPRING_APPLICATION_NAME=app1

  app2:
    image: spring-bus-demo:latest
    ports:
      - "8082:8080"
    environment:
      - SPRING_APPLICATION_NAME=app2
```

```bash 
docker-compose up -d app1 app2
```


### Step-3: Run multiple client instances (different ports)
```bash 
cd config-client
./mvnw spring-boot:run -Dspring-boot.run.arguments="--server.port=8081 --spring.application.name=demo-client"
./mvnw spring-boot:run -Dspring-boot.run.arguments="--server.port=8082 --spring.application.name=demo-client"
```


### Step-4: Trigger refresh via Spring Cloud Bus 
```bash 
curl -X POST http://localhost:8081/actuator/bus-refresh
```

### Step-5: Observe Logs (expected results)
- All client instances (8081, 8082, ...) should log the `EnvironmentChangeEvent`. 

--- 

## Step Further: Custom Config Event Example
Up to this point, Spring Cloud Bus already gives us powerful features for propagating config changes across distributed services. For many systems, this is more than enough. 

But when systems grow more complex and the number of configuration key increases, treating all config updates the same way (via a blanket refresh) can become inefficient or too tightly coupled. 

A better approach is to define **custom events** -- so different types of coding changes can trigger different handlers, validations, or business logic. This keeps things **flexible** and **decoupled**. 