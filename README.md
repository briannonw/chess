# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGckCh0AcCGAnUBjEbAO2DnBElIEZVs8RCSzYKrgAmO3AorU6AGVIOAG4jUAEyzAsAIyxIYAERnzFkdKgrFIuaKlaUa0ALQA+ISPE4AXNABWAexDFoAcywBbTcLEizS1VZBSVbbVc9HGgnADNYiN19QzZSDkCrfztHFzdPH1Q-Gwzg9TDEqJj4iuSjdmoMopF7LywAaxgvJ3FC6wCLaFLQyHCdSriEseSm6NMBurT7AFcMaWAYOSdcSRTjTka+7NaO6C6emZK1YdHI-Qma6N6ss3nU4Gpl1ZkNrZwdhfeByy9hwyBA7mIT2KAyGGhuSWi9wuc0sAI49nyMG6ElQQA)

## Modules

The application has three modules.

- **Client**: The command line program used to play a game of chess over the network.
- **Server**: The command line program that listens for network requests from the client and manages users and games.
- **Shared**: Code that is used by both the client and the server. This includes the rules of chess and tracking the state of a game.

## Starter Code

As you create your chess application you will move through specific phases of development. This starts with implementing the moves of chess and finishes with sending game moves over the network between your client and server. You will start each phase by copying course provided [starter-code](starter-code/) for that phase into the source code of the project. Do not copy a phases' starter code before you are ready to begin work on that phase.

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

## Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```

## Sequence Diagram Link

[Link](https://sequencediagram.org/index.html?presentationMode=readOnly&shrinkToFit=true#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWDABLBoAmCtu+hx7ZhWqEUdPo0EwAIsDDAAgiBAoAzqswc5wAEbBVKGBx2ZM6MFACeq3ETQBzGAAYAdAE5M9qBACu2AMQALADMABwATG4gMP7I9gAWYDoIPoYASij2SKoWckgQaJiIqKQAtAB85JQ0UABcMADaAAoA8mQAKgC6MAD0PgZQADpoAN4ARP2UaMAAtihjtWMwYwA0y7jqAO7QHAtLq8soM8BICHvLAL6YwjUwFazsXJT145NQ03PnB2MbqttQu0WyzWYyOJzOQLGVzYnG4sHuN1E9SgmWyYEoAAoMlkcpQMgBHVI5ACU12qojulVk8iUKnU9XsKDAAFUBhi3h8UKTqYplGpVJSjDpagAxJCcGCsyg8mA6SwwDmzMQ6FHAADWkoGME2SDA8QVA05MGACFVHHlKAAHmiNDzafy7gjySp6lKoDyySIVI7KjdnjAFKaUMBze11egAKKWlTYAgFT23Ur3YrmeqBJzBYbjObqYCMhbLCNQbx1A1TJXGoMh+XyNXoKFmTiYO189Q+qpelD1NA+BAIBMU+4tumqWogVXot3sgY87nae1t+7GWoKDgcTXS7QD71D+et0fj4PohQ+PUY4Cn+Kz5t7keC5er9cnvUexE7+4wp6l7FovFqXtYJ+cLtn6pavIaSpLPU+wgheertBAdZoFByyXAmlDtimGD1OEThOFmEwQZ8MDQcCyxwfECFISh+xXOgHCmF4vgBNA7CMjEIpwBG0hwAoMAADIQFkhRYcwTrUP6zRtF0vQGOo+RoFmipzGsvz-BwVygYKQH+uB5afJCIJqTsXzQo8wHiVQSIwAgQnihignCQSRJgKSb6GLuNL7gyTJTspXI3l5d5LsKYoSm6MpymW7xKpgKrBhqbpGhwEBqDAaAQMwVpooFvLBZZ1k9n224eZZ-oAHKZc+8TQEgABeKAcFGMZxoUWlJpUolpk4ACMBE5qoebzNBRYlvU-lGKlAoZVl1o5A29G5QuAodR2VkutFr7Oh5VK3vyY4Tig1Xnpe17Dg6IUrmuAanVu7ntjppa2U5-4IIB5kYatoEvIR+nzCRqHfBRVH1gDtHofCybIKmMC4fhoy-TFxGkUDl4g8hYNoYtnjeH4-heCg6AxHEiQE0Tjm+FgomCt9jTSBG-ERu0EbdD0cmqApwzA4h6AQ9pH2ltzSGYI9NPbfUz2Uw5QmU85aiuSVgrnfSMCMmAx1C+gc5BRdlTLmF64UZF8qa4UysrQVG1G3d21K3tKscCg3DHpeJ0vto2t5brQq1NITtMoY1vyIrq2PRLMunhkqgASLAsgdUumaQnkOddD2Gw3hWZ0U2OPMf4KLrv42Dihq-FojAADiSoaNTZWlg0FdM6z9hKlzaM821yf87C-qm7HPfwr64s2WiVc5g5o-V3LJIh7tOsq2rGvt0hnvLfeoXiobt3yLKJvL7z5vx529RB8As8yPbo6q0yZc5GPagYqv+7r7UFdMqrSoeofX3Dy3cxbZ2B6AsJaTxzFHGOosf4STAssP+agCwNHGHAgAktIAsPVwjBECCCTY8RdQoCSpBRY3xkigDVIQgyYxvhwPKkqYhFwYCdCTtAzCacwA4UzqMWB1cEFIKVKg9BmDsHLFwfgihw0qEglISAchREJHUKVLQuY9DGHZwYrnPGHAADsbgnAoCcDECMwQ4BcQAGzwEOpXCsRQ2Fi2gfUKSHRm6t2mPvRSowaFKmYYmSoYdjRuKzJ4uYZkB52PWl2GAh45AoHvhiOAh177TwVvdTyXsF5MiXvBDuT9gp6w3hKU+u9-FZOFt-S2ETT7n3NvUKJ6JYlBICt-PJJ9rpwIAeEoBA8akJKVOAt6-cvxHxqC8bhcwBH1AwVgmA3jPpQxKOwjO8MRijJQOMmAkzAjTMbOopieNLBO1spsYmSAEhgH2X2CARyABSEBxRWLmDEaRaobHzLCZJJozIZI9DgW3Ep6AszYAQMAfZUA4AQFslANYKDpAzMHg8LpxTKIdwBUCkFYKIVQv4TCgZFkh7HxgAAK1uWgWJNzxSJJQISeWblbapOWj5dWrtTY5O9vrTeiLjaIvRktZ+UDwkn23mfFJc80lXzVvUrFLLFzNLfswNp2ginQqqZfca2AtB1KVBiQFwLKDougJisZ0gpUrWacyNV0SP7-wVXIS1qzpAhw-MAwlxKKXR36ZAuuLxYWsPmRwpZajGK4wCF4YFxzTkhvlIgYMsBgDYEBYQPIBQYAvPMG8+u9NGbM1ZsYPmjquk4s+ni-lkTuB4AxCAUtUAkk0sAXS7y18wA8kfjy3JPs-bO0DoKg1KAeRrDeB6UwKS81fhqZWvp71Ql8sTrmuZMM4ZZ22ZgIAA)
