# SecretsProtection-

A small Java demo showing why secrets should be compared in constant time: `String.equals()` leaks timing information, `MessageDigest.isEqual()` doesn't.

## Requirements

- JDK 11 or newer

## Run it

```bash
javac -d out src/*.java
java -cp out Main
```

In an IDE such as IntelliJ, mark `src` as the sources root and run `Main`.

## Sample output

```
THE PROBLEM: insecureEquals() using String.equals()
  wrong at first char: 0 ms
  wrong at last char:  221 ms

THE SOLUTION: constantTimeEquals() using MessageDigest.isEqual()
  wrong at first char: 1447 ms
  wrong at last char:  1596 ms
```
