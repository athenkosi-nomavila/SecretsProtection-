# SecretsProtection-

A small Java demo of comparing secrets in constant time so the check doesn't leak timing information.

## The problem and the solution

`String.equals()` stops at the first character that differs, so a guess that is wrong early is rejected faster than one that is wrong late. An attacker who can measure that difference can work out a secret one character at a time.

`MessageDigest.isEqual()` compares every byte wherever the first difference is, so its timing reveals nothing.

Both checks are in [`src/SecretStore.java`](src/SecretStore.java):

- `insecureEquals()` uses `String.equals()` (the problem)
- `constantTimeEquals()` uses `MessageDigest.isEqual()` (the solution)

## Run it

Requires JDK 11 or newer. From the repository root:

```bash
javac -d out src/*.java
java -cp out Main
```

In an IDE such as IntelliJ, mark `src` as the sources root and run `Main`.

## What you'll see

Your numbers will differ, but the pattern should look like this:

```
THE PROBLEM: insecureEquals() using String.equals()
  wrong at first char: 0 ms
  wrong at last char:  200 ms
  -> LEAKS: the closer guess takes longer to reject.

THE SOLUTION: constantTimeEquals() using MessageDigest.isEqual()
  wrong at first char: 1372 ms
  wrong at last char:  1276 ms
  -> SAFE: both guesses take about the same time.
```

Compare the two lines within each block. The constant-time check is slower overall only because the demo uses a huge 100,000-character secret to make the difference visible.
