# wa-task-configuration-api

[![Build Status](https://travis-ci.org/hmcts/wa-task-configuration-api.svg?branch=master)](https://travis-ci.org/hmcts/wa-task-configuration-api)

#### What does this app do?

- Receives an unconfigured Task upon its creation
- Retrieves CCD case data for given caseId
- Evaluates DMN configuration and gets output variables
- Updates the Task
- Auto-assigns the Task
- Responds with a Configured Camunda Task.

<!--
    Sequence Diagram Source:
    http://www.plantuml.com/plantuml/uml/fLFFR-is33vNVmNnTjaEVZwlXuUN_jXGe4CBf3qNZ4JRQcJ9fQYaGTl_VPRZf4sG9HlcaqH-_FY9_Fmp2hAargd66e8g19c5LwNYtFeE6Lk83PgmjBu6uKGx6Nf9v3N1cpgRC8VbPN21uOgecyvN5ovH7zSI-2Am8Lvd3V9yDgHkRKMtrjE9BEfvpI5viqRyCldzwlBg3tMcpkwHnMhRePUHOx__3cQVQTbO8GNZznV5Y3LDdAqzQF92h3wK4YkEu07Z72w3hsoT6CK63rFYXTMa52O9gCKkK0Y2LzrdH_YciUtxOJ7hsg9pA5NWzjikH7hkGd_bBkAQUm-ejIaC2XPH0bE1dTq3S71zE4PR-vQyR6a08p16fTHXFSMFOoBW7ExlfW_dK6PGkGCQfkU343YgP9obiArhOZAGkk0XLy6OqaoVTUJDK9jr7Ub_KfsjVjNQt9ZhLnib_dQ23eeVXqQPYVwypg_K66aPU4uSoxTapKVvdDX6OtNcwMFFEPPHXQmwwWDQDf5ywSy7np9THQ4M7YCnf4Wc-qytcyaihNFGu8803xbj0yih_huxlI5CajX3bl2KtwMKeOqdZpjigvHjtMoatdctEgHYJ6_RW8rZQp8xeavjzB0FHwT1QKZRc2iCLTRRN5o-vHdJ0br28JK-c2ac9qgzlx-VQWcCSTi0stBlPudbIo9UgOmgUlGnidF8nKzVYl-dWp59c0niDzLzw0ypHuStSrE1DCIK9u_PHjpVKI0s8Ja3CnfnQsraCdhOP3xuCGnsk-XCjhIH-b2cp7Q5Hx-f_luVbZfCxH_tkgaXyvGv1jv_Qz6Vv4rgtTy0

    See: https://plantuml.com/ docs for reference
-->

![task-configuration-service](task-configuration.png)


## Notes

Since Spring Boot 2.1 bean overriding is disabled. If you want to enable it you will need to set `spring.main.allow-bean-definition-overriding` to `true`.

JUnit 5 is now enabled by default in the project. Please refrain from using JUnit4 and use the next generation

## Building and deploying the application

### Building the application

The project uses [Gradle](https://gradle.org) as a build tool. It already contains
`./gradlew` wrapper script, so there's no need to install gradle.

To build the project execute the following command:

```bash
  ./gradlew build
```

### Running the application

Create the image of the application by executing the following command:

```bash
  ./gradlew assemble
```

Create docker image:

```bash
  docker-compose build
```

Run the distribution (created in `build/install/wa-task-configuration-api` directory)
by executing the following command:

```bash
  docker-compose up
```

This will start the API container exposing the application's port
(set to `8091` in this template app).

In order to test if the application is up, you can call its health endpoint:

```bash
  curl http://localhost:8091/health
```

You should get a response similar to this:

```
  {"status":"UP","diskSpace":{"status":"UP","total":249644974080,"free":137188298752,"threshold":10485760}}
```

### Alternative script to run application

To skip all the setting up and building, just execute the following command:

```bash
./bin/run-in-docker.sh
```

For more information:

```bash
./bin/run-in-docker.sh -h
```

Script includes bare minimum environment variables necessary to start api instance. Whenever any variable is changed or any other script regarding docker image/container build, the suggested way to ensure all is cleaned up properly is by this command:

```bash
docker-compose rm
```

It clears stopped containers correctly. Might consider removing clutter of images too, especially the ones fiddled with:

```bash
docker images

docker image rm <image-id>
```

There is no need to remove postgres and java or similar core images.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
