# Spring Cloud Config Hands On

## Introduction 
Spring Cloud Config is a **lightweight**, **centralized configuration solution** for Spring Boot applications. It allows your applications to: 
- Store configuration in a central location (Git, filesystem, or other supported backends)
- Dynamically refresh configuration without restarting the application
- Integrate with Spring Boot's Environment and `/actual/refresh` endpoint

This demo focuses on understanding **how Spring Cloud Config works**, observing **property refresh**, and capturing configuration change events. 


## When to Use Config Server & Client 
- **Config Server**: Needed whenever you want a central repository for your application configurations. It exposes an HTTP endpoint that clients can query for their configuration files.
- **Config Client**: Needed for each Spring Boot application that consumes configuration from the Config Server. The client fetches its configuration on startup and can refresh dynamically at runtime. 

### Key Points
- If you only have a single Spring Boot app and don't need centralized config or runtime refresh, a Config Server is optional.
- For multi-instance or microservice setups, using both **server** + **client** is recommended. This ensures:
> Consistent configuration across instances.
> Ability to trigger runtime refresh via `/actuator/refresh`. 
> Integration with Spring Cloud Bus (optional) for automatic multi-instance propagation. 


## Usage Overview 
### Start the Config Server 
- The server exposes an HTTP endpoint for clients to fetch configuration files. 
- Configuration files are usually stored in Git, localfilesystem, or a shared repository. 


### Configure the Client Application 
- Set `spring.application.name` in your `application.yml`
- Point to the Config Server with `spring.cloud.config.uri`
- Enable runtiem refresh with Spring Boot Actuator (`/actuator/refresh`).

### Dynamic Configuration Updates 
- Modify the configuration file in the Config Server repository.
- Trigger a refresh via the client endpoint (POST `/actuator/refresh`)
- Observe changes in logs via Spring's `EnvironmentChangeEvent` handler. 



## What Happens When `/actuator/refresh` is Triggered ? 
When a client triggers the Spring Boot Actuator refresh endpoint, here's what happens under the hood:

```
         ┌───────────────┐
         │ Config Server │  <-- holds config files (Git / filesystem)
         └───────┬───────┘
                 │
      client fetches latest config
                 │
         ┌───────▼────────┐
         │ Config Client  │  <-- Spring Boot app
         │  Environment   │
         │  (memory)      │
         └───────┬────────┘
                 │
         POST /actuator/refresh
                 │
         ┌───────▼────────┐
         │ Environment    │  <-- in-memory updated
         │ ChangeEvent    │  <-- Spring publishes event
         └───────┬────────┘
                 │
 ┌───────────────▼───────────────┐
 │ Listeners / Handlers          │
 │ (e.g., Dynamic Thread Pool)   │
 │ Compare old vs new values      │
 │ Apply updates dynamically      │
 └───────────────────────────────┘
```

### Key Points: 
#### Config Server
- Holds authoritative config files
- Does **not** get modified by refresh -- it's read-only in this flow. 

#### Config Client
- Fetches the latest config from the server when `/actuator/refresh` is called. 
- Updates the Spring `Environment` in memory. 

#### EnvironmentChangeEvent 
- Published automatically after client updates environment.
- Any `ApplicationListener<EnvironmentChangeEvent>` can handle the changes (e.g., update thread pools or other configs dynamically).


#### Multiple Instances
- With **Spring Cloud Bus**, refresh events can be **broadcast** to all client instances automatically.
- Without Bus, each instance must receive `/actuator/refresh` individually. 

#### Server Files 
- Remain unchanged 
- Only client memory and runtime beans are updated. 