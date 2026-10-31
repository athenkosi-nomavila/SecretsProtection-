import java.util.function.Predicate;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Shows why secrets must be compared in constant time.
 *
 * It times two wrong guesses against a stored secret: one wrong at the first
 * character, one wrong at the last. If the check takes longer for the second guess,
 * the timing reveals how much of the guess was right.
 */
public class Main {
    private static final String SECRET_NAME = "apiToken";
    // A long secret and many repetitions make the timing difference easy to see.
    private static final int SECRET_LENGTH = 100_000;
    private static final int ITERATIONS = 20_000;

    public static void main(String[] args) {
        SecretStore store = new SecretStore();
        String secret = "a".repeat(SECRET_LENGTH);
        store.put(SECRET_NAME, secret);

        // Same length as the real secret, but wrong at the very start or the very end.
        String wrongAtFirstChar = "b" + secret.substring(1);
        String wrongAtLastChar = secret.substring(0, SECRET_LENGTH - 1) + "b";

        printIntro();

        System.out.println("Sanity check: both methods accept the correct secret");
        System.out.println("  insecureEquals():     " + store.insecureEquals(SECRET_NAME, secret));
        System.out.println("  constantTimeEquals(): " + store.constantTimeEquals(SECRET_NAME, secret.getBytes(UTF_8)));
        System.out.println();

        Result insecure = measure(
                guess -> store.insecureEquals(SECRET_NAME, guess),
                wrongAtFirstChar, wrongAtLastChar);
        Result constantTime = measure(
                guess -> store.constantTimeEquals(SECRET_NAME, guess),
                wrongAtFirstChar.getBytes(UTF_8), wrongAtLastChar.getBytes(UTF_8));

        System.out.println("THE PROBLEM: insecureEquals() using String.equals()");
        insecure.print();
        System.out.println("  -> LEAKS: the guess that is right for longer takes longer to reject.");
        System.out.println("     An attacker can use this to guess the secret one character at a time.");
        System.out.println();

        System.out.println("THE SOLUTION: constantTimeEquals() using MessageDigest.isEqual()");
        constantTime.print();
        System.out.println("  -> SAFE: both guesses take about the same time,");
        System.out.println("     so timing reveals nothing about how close a guess is.");
        System.out.println();

        System.out.println("Note: compare the two lines within each block, not the blocks with each other.");
        System.out.println("constantTimeEquals() is slower overall: it always checks every byte, while");
        System.out.println("String.equals() is heavily optimised by the JVM. With a real-sized secret");
        System.out.println("(32-64 characters) both take well under a microsecond, so the cost is negligible.");
    }

    private static void printIntro() {
        System.out.println("=== Timing attack demo: comparing a secret (e.g. an API token) ===");
        System.out.println();
        System.out.println("A server checks a guessed secret " + ITERATIONS + " times per guess.");
        System.out.println("Two wrong guesses are tried, both " + SECRET_LENGTH + " characters long:");
        System.out.println("  - one that is wrong at the FIRST character");
        System.out.println("  - one that is wrong at the LAST character (almost entirely correct)");
        System.out.println("If rejecting them takes different times, the check leaks information.");
        System.out.println();
    }

    private static <T> Result measure(Predicate<T> check, T wrongAtFirstChar, T wrongAtLastChar) {
        // Warm up so the JIT compiler doesn't distort the first measurement.
        time(check, wrongAtFirstChar);
        time(check, wrongAtLastChar);
        return new Result(time(check, wrongAtFirstChar), time(check, wrongAtLastChar));
    }

    private static <T> long time(Predicate<T> check, T guess) {
        long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            if (check.test(guess)) {
                throw new IllegalStateException("A wrong guess was accepted");
            }
        }
        return (System.nanoTime() - start) / 1_000_000;
    }

    private static final class Result {
        final long wrongAtFirstCharMs;
        final long wrongAtLastCharMs;

        Result(long wrongAtFirstCharMs, long wrongAtLastCharMs) {
            this.wrongAtFirstCharMs = wrongAtFirstCharMs;
            this.wrongAtLastCharMs = wrongAtLastCharMs;
        }

        void print() {
            System.out.println("  guess wrong at first char: " + wrongAtFirstCharMs + " ms");
            System.out.println("  guess wrong at last char:  " + wrongAtLastCharMs + " ms");
        }
    }
}
