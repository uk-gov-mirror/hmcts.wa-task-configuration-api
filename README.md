# wa-task-configuration-api

[![Build Status](https://travis-ci.org/hmcts/wa-task-configuration-api.svg?branch=master)](https://travis-ci.org/hmcts/wa-task-configuration-api)

#### What does this app do?

- Receives an unconfigured Task upon its creation
- Retrieves CCD case data for given ccdId
- Evaluates DMN configuration and gets output variables
- Updates the Task
- Auto-assigns the Task
- Responds with a Configured Camunda Task.

<!--
    Sequence Diagram Source:
    http://www.plantuml.com/plantuml/uml/hLDDJzj043spls94B-LWvLmEY93GgX9LaU2EXjsnCyJUTMTd2H7mt-jDRAL6QT93VR2yy_7UpDldowWecfhQBDWHb27eOtWn9YxPjoZOG5oW2ols5QWa6cNeLo9l2LxDoLGah6QX3W9pelPkkRwg4tLngq6kWZYIRTQXB1ytgHikwOOz7ScYNLOIadVRYa_JB_FP_AivCIUtACgMM_IwmnZnZse-8gqMh6IWVtvGZ5ZHjERAWoMl9ERT66Mj2UunBc4MVCbL4bGE7kv8djcICPWqe5L-HYK8thGxHZYrs7HyM3osJT7Mg6MGvdHSeXrsOV_4Bk8M-qEXjQvmg5X43K85jdngZDbFKLmSmeLpkFrvTtyEarmq6HLj1F512MegjPT7XQkAX1oaDdZ8NT2dhD2kZxpRz1fZ3l4VdOxBpzQwx-xwXQF6ioDmK5piKoS3VRlEMrgCj0go98cJryJkVVAKXADZct6ws5EEvQf2rorr0Qj391-wxxson7LKQY15alnoWoOQWBE1q2giM1T3V3QRZsNA1o5DuY5pFUG5Z74q-EcWEtOZ2LUBOQRWw_NsWaPT_GWx6k3OKvE3yLH7xcw_utfFmSIHvPWx79NiEJTFNhEQz8nrGYNJRomKKwt6lBsz_GUxe9yc3TCOkVBvTpqxvfbichzjClIGUyWO6td-zLOkoRlKrBy1

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
