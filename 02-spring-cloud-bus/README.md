# Spring Cloud Bus Hands On

## Introduction 

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

