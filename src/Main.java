import java.util.function.BiPredicate;

public class Main {
    // A long secret and many repetitions make the timing difference easy to see.
    private static final int SECRET_LENGTH = 100_000;
    private static final int ITERATIONS = 20_000;

    public static void main(String[] args) {
        Secrets secrets = new Secrets();
        String token = "a".repeat(SECRET_LENGTH);
        secrets.put("apiToken", token);

        // Same length as the real token, but wrong at the very start or the very end.
        String wrongFirstChar = "b" + token.substring(1);
        String wrongLastChar = token.substring(0, SECRET_LENGTH - 1) + "b";

        System.out.println("Correct token accepted (leaky): " + secrets.leakyMatches("apiToken", token));
        System.out.println("Correct token accepted (safe):  " + secrets.safeMatches("apiToken", token));
        System.out.println();

        compare("Leaky String.equals()", secrets::leakyMatches, wrongFirstChar, wrongLastChar);
        compare("Safe MessageDigest.isEqual()", secrets::safeMatches, wrongFirstChar, wrongLastChar);
    }

    private static void compare(String label, BiPredicate<String, String> check,
                                String wrongFirstChar, String wrongLastChar) {
        // Warm up so the JIT compiler doesn't distort the first measurement.
        time(check, wrongFirstChar);
        time(check, wrongLastChar);

        long firstMs = time(check, wrongFirstChar);
        long lastMs = time(check, wrongLastChar);

        System.out.println(label);
        System.out.println("  guess wrong at first char: " + firstMs + " ms");
        System.out.println("  guess wrong at last char:  " + lastMs + " ms");
        System.out.println();
    }

    private static long time(BiPredicate<String, String> check, String guess) {
        int accepted = 0;
        long start = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            if (check.test("apiToken", guess)) {
                accepted++;
            }
        }
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        if (accepted != 0) {
            throw new IllegalStateException("A wrong guess was accepted");
        }
        return elapsedMs;
    }
}
