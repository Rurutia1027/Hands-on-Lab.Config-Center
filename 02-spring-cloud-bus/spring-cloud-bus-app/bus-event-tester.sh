#!/bin/sh
# docker compose up -d -> this setup one rabbitMQ instances, a config server listens to 8888,
# and 4 instances of spring cloud bus app listens to 8081 -> 8084

# then send curl command to any {8081,..., 8084} fetch health status
# expected response: {"status":"UP"}
curl http://localhost:8081/actuator/health

# then go your Git Store of the config center repo, modify the config options
# then execute the command below -> only port associated app instances, in this case 8081 -> app1, go the terminal of this app instances
# Config modify log will be shown on the console
curl -X POST http://localhost:8081/actuator/refresh

# then modify the Git config file repo modify other options,
# execute the command below -> all associated bus app instances' docker internal console will print the config change detected log info
curl -X POST http://localhost:8081/actuator/busrefresh
