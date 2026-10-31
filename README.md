# SecretsProtection-

A small Java demo of one way to protect secrets: comparing them in constant time.

`Secrets.java` stores secrets in a map and checks a guess two ways:

- `leakyMatches` uses `String.equals()`, which stops at the first wrong character. How long it takes reveals how much of the guess was right (a timing attack).
- `safeMatches` uses `MessageDigest.isEqual()`, which checks every byte no matter where the mismatch is, so the timing reveals nothing.

`Main.java` times both methods against a 100,000-character token, using one guess that is wrong at the first character and one that is wrong at the last.

## Requirements

- JDK 11 or newer (the code uses `String.repeat`)

## Run it

From the repository root:

```bash
javac -d out src/*.java
java -cp out Main
```

The run takes a few seconds. You should see output like this (times vary by machine):

```
Correct token accepted (leaky): true
Correct token accepted (safe):  true

Leaky String.equals()
  guess wrong at first char: 0 ms
  guess wrong at last char:  66 ms

Safe MessageDigest.isEqual()
  guess wrong at first char: 2551 ms
  guess wrong at last char:  2622 ms
```

The leaky check is much faster when the guess fails early, which is the leak. The safe check takes about the same time for both guesses.

In an IDE such as IntelliJ, mark `src` as the sources root and run `Main`.
