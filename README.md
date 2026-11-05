# SecretsProtection-

A small Java demo of a **timing attack**: how an attacker can steal a 2FA login code just by measuring how long the server takes to say "wrong code", and the one-line fix.

## What this is about

When you log in and type the 6-digit code from your authenticator app, the server compares it with the code it expects. If it uses `String.equals()`, the comparison stops at the first wrong digit. That makes a guess like `480000` (two correct digits) take a tiny bit longer to reject than `100000` (none correct).

An attacker can time their guesses, keep the digit that was slowest to reject, and move on to the next one. That's at most 60 guesses instead of 1,000,000.

`MessageDigest.isEqual()` always checks every byte, so every wrong guess takes the same time and the timings reveal nothing.

## Where this matters

Anywhere your code checks a value someone sent you against a secret it holds:

- **2FA and SMS login codes**
- **API keys** sent in a request header
- **Password-reset and email-verification links** (the token in the link)
- **Session tokens and webhook signatures**

**What about passwords?** Don't store or compare plain-text passwords at all. Hash them with bcrypt or Argon2 and use the library's verify function, which already compares safely.

**What about environment variables?** An env var is often where the secret lives, for example `API_KEY=...`. Keeping it there is fine. The risk is the line of code that compares an incoming request against it.

Real systems also limit attempts and expire codes quickly. A constant-time comparison is one layer of protection, not the only one.

## Requirements

- JDK 11 or newer

## Run it

```bash
javac -d out src/*.java
java -cp out Main
```

In an IDE such as IntelliJ, mark `src` as the sources root and run `Main`.

## How to read the output

The program runs the same attack twice, once against each way of checking the code.

```
1. UNSAFE CHECK: String.equals() stops at the first wrong digit
   digit 1: slowest guess was 4, so the attacker keeps it -> 4_____
   digit 2: slowest guess was 8, so the attacker keeps it -> 48____
   ...
   digit 6: tried all 10 options, 0 logged in -> 483920
   -> HACKED: the attacker found the code in at most 60 guesses instead of 1,000,000.

2. SAFE CHECK: MessageDigest.isEqual() always checks every digit
   digit 1: slowest guess was 0, so the attacker keeps it -> 0_____
   ...
   digit 6: tried all 10 options, none logged in.
   -> SAFE: the timings gave nothing away, so the attacker is back to 1,000,000 blind guesses.
```

- **Each `digit` line** is the attacker trying 0 to 9 in one position and keeping the one the server was slowest to reject.
- **Against `String.equals()`** the slowest digit is the correct one, so the code is rebuilt step by step.
- **Against `MessageDigest.isEqual()`** the "slowest" digit is random, so the attacker ends up with a wrong code.

In real life the time difference is billionths of a second, so attackers repeat each guess thousands of times and average the results. To make the gap visible on a laptop, the demo makes each digit of the code much longer behind the scenes. Your exact digits for the fix will differ on each run.
