![Coverage](.github/badges/jacoco.svg)

# Market Sales Service

## Overview
The Market Sales Service is a reactive, serverless REST API built with Java, Spring WebFlux, and AWS DynamoDB for managing market sales.
This service allows users to create, view, update, and delete sales.
With a reactive stack using Spring WebFlux, it’s optimized for high throughput, non-blocking requests, making it scalable and suitable for cloud-native applications.

## Features
* Reactive API: Fully non-blocking API using Spring WebFlux.
* AWS DynamoDB Integration: Leverages DynamoDB as the persistent data store.
* CRUD Operations: Create and retrieve market sales.
* Sales Status Tracking: Track sales statuses.
* Scalable and Cloud-ready: Serverless and ready to deploy on AWS infrastructure.

## Building and Run locally from Source
Market Sales Service uses a Gradle-based build system.
In the instructions below, ./gradlew is invoked from the root of the source tree and serves as a cross-platform, self-contained bootstrap mechanism for the build.

### Prerequisites
Git and the JDK21.
Local docker DynamoDB

### Check out sources
```
$ git clone git@github.com:ZiedEcheikh/market-sales-service.git
```

### Compile and test
Gradle 8.10.2

```
$ ./gradlew build
```
```
$ ./gradlew test
```

### Building blocks
| Component                                  | Version   |
|:-------------------------------------------|:---------:|
| spring-core                                |   6.2.5   |
| spring-boot                                |   3.4.4   |
| spring-boot-starter-webflux                |   3.4.4   |
| spring-boot-starter-oauth2-resource-server |   3.4.4   |
| dynamodb-enhanced                          |  2.31.21  |
### Run
```
$  ./gradlew bootRun --args='--spring.profiles.active=local'
```
To active profile for run in the IntelliJ IDEA, add to VM options : -Dspring.profiles.active=local
