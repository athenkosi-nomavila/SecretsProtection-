# SecretsProtection-

A small repo to learn one of the ways to protect secrets: comparing them in constant time so the check doesn't leak timing information.

## Requirements

- JDK 11 or newer

## Run it

From the repository root:

```bash
javac -d out src/*.java
java -cp out Main
```

The run takes a few seconds and prints timing results for each check.

In an IDE such as IntelliJ, mark `src` as the sources root and run `Main`.
