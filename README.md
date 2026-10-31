# SecretsProtection-

A small Java demo of one way to protect secrets: comparing them in constant time so the check doesn't leak timing information.

## The problem and the solution

When a server checks a secret such as an API token or password, the obvious `String.equals()` check stops at the first character that differs. A guess that is wrong at the first character is rejected faster than one that is wrong at the last. An attacker who can measure that difference can work out the secret one character at a time.

`MessageDigest.isEqual()` compares every byte no matter where the first difference is, so the time taken reveals nothing about how close a guess was.

Both checks live in [`src/SecretStore.java`](src/SecretStore.java):

- `insecureEquals()` uses `String.equals()` (the problem)
- `constantTimeEquals()` uses `MessageDigest.isEqual()` (the solution)

[`src/Main.java`](src/Main.java) times both against two wrong guesses and prints the results.

## Requirements

- JDK 11 or newer

## Run it

From the repository root:

```bash
javac -d out src/*.java
java -cp out Main
```

In an IDE such as IntelliJ, mark `src` as the sources root and run `Main`.

## What you'll see

The run takes a few seconds. Your numbers will differ, but the pattern should look like this:

```
THE PROBLEM: insecureEquals() using String.equals()
  guess wrong at first char: 0 ms
  guess wrong at last char:  75 ms
  -> LEAKS: the guess that is right for longer takes longer to reject.
     An attacker can use this to guess the secret one character at a time.

THE SOLUTION: constantTimeEquals() using MessageDigest.isEqual()
  guess wrong at first char: 1502 ms
  guess wrong at last char:  1584 ms
  -> SAFE: both guesses take about the same time,
     so timing reveals nothing about how close a guess is.
```

Compare the two lines within each block, not the blocks with each other. What matters is that the insecure check's time depends on where the guess goes wrong and the constant-time check's doesn't. The constant-time check is slower overall for this deliberately huge 100,000-character secret, but for a real-sized secret both take well under a microsecond.
