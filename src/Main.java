import java.util.function.Predicate;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Main {
    private static final int SECRET_LENGTH = 100_000;
    private static final int ITERATIONS = 20_000;

    public static void main(String[] args) {
        String secret = "a".repeat(SECRET_LENGTH);
        SecretStore store = new SecretStore(secret);

        String wrongFirst = "b" + secret.substring(1);
        String wrongLast = secret.substring(0, SECRET_LENGTH - 1) + "b";

        System.out.println("Timing two wrong guesses against a secret:");
        System.out.println("one wrong at the first character, one wrong at the last.");
        System.out.println("If the times differ, an attacker can guess the secret one character at a time.");
        System.out.println();

        System.out.println("THE PROBLEM: insecureEquals() using String.equals()");
        report(store::insecureEquals, wrongFirst, wrongLast);
        System.out.println("  -> LEAKS: the closer guess takes longer to reject.");
        System.out.println();

        System.out.println("THE SOLUTION: constantTimeEquals() using MessageDigest.isEqual()");
        report(store::constantTimeEquals, wrongFirst.getBytes(UTF_8), wrongLast.getBytes(UTF_8));
        System.out.println("  -> SAFE: both guesses take about the same time.");
    }

    private static <T> void report(Predicate<T> check, T wrongFirst, T wrongLast) {
        time(check, wrongFirst);
        time(check, wrongLast);
        System.out.println("  wrong at first char: " + time(check, wrongFirst) + " ms");
        System.out.println("  wrong at last char:  " + time(check, wrongLast) + " ms");
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
}
