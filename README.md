# Kafka Most Basic SBT Test
This Scala project examples a very basic Kafka Test using a consumer and producer.
The code in this project is based on [benstopford/KafkaMostBasicTest](https://gist.github.com/benstopford/49555b2962f93f6d50e3).

This repo has two tests:
- HelloTest: A simple hello world test, to test the Hello World application
- KafkaMostBasicTest: A simple test that produces an event and consumes it

# Prerequisites

- Java 8
- SBT 1.2.6
- Make sure there is no kafka docker container running locally on ports 9092/2181

# Running

```
sbt test
```

# License
MIT License