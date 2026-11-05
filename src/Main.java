import java.util.function.Function;
import java.util.function.Predicate;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Main {
    private static final String LOGIN_CODE = "483920";
    private static final int STRETCH = 10_000;
    private static final int REPEATS = 200;
    private static final int ROUNDS = 5;
    private static int accepted;

    public static void main(String[] args) {
        SecretStore server = new SecretStore(stretch(LOGIN_CODE));

        System.out.println("You log in to an app and it asks for the 6-digit code from your authenticator app.");
        System.out.println("The server checks what you type against the code it expects (" + LOGIN_CODE + ").");
        System.out.println();
        System.out.println("An attacker who doesn't know the code would need up to 1,000,000 blind guesses.");
        System.out.println("But if the check stops at the first wrong digit, a guess with more correct digits");
        System.out.println("at the start takes a little longer to reject. By timing each guess, the attacker");
        System.out.println("can find the code one digit at a time.");
        System.out.println();
        System.out.println("(In real life the gap is billionths of a second, so attackers repeat each guess");
        System.out.println(" thousands of times. This demo makes each digit bigger so your machine can show it.)");
        System.out.println();

        System.out.println("1. UNSAFE CHECK: String.equals() stops at the first wrong digit");
        attack(Main::stretch, server::insecureEquals);
        System.out.println();

        System.out.println("2. SAFE CHECK: MessageDigest.isEqual() always checks every digit");
        attack(guess -> stretch(guess).getBytes(UTF_8), server::constantTimeEquals);
        System.out.println();

        System.out.println("The same applies to any secret a server checks: login codes, API keys,");
        System.out.println("password-reset links. See the README for more.");
    }

    private static <T> void attack(Function<String, T> prepare, Predicate<T> check) {
        String known = "";
        for (int digit = 0; digit < 10; digit++) {
            time(check, prepare.apply(guess(known, digit)));
        }
        for (int position = 0; position < LOGIN_CODE.length() - 1; position++) {
            long[] times = new long[10];
            for (int round = 0; round < ROUNDS; round++) {
                for (int digit = 0; digit < 10; digit++) {
                    long time = time(check, prepare.apply(guess(known, digit)));
                    if (round == 0 || time < times[digit]) {
                        times[digit] = time;
                    }
                }
            }
            int slowest = 0;
            for (int digit = 1; digit < 10; digit++) {
                if (times[digit] > times[slowest]) {
                    slowest = digit;
                }
            }
            known += slowest;
            System.out.printf("   digit %d: slowest guess was %d, so the attacker keeps it -> %s%n",
                    position + 1, slowest, pad(known));
        }
        for (int digit = 0; digit < 10; digit++) {
            if (check.test(prepare.apply(known + digit))) {
                System.out.println("   digit 6: tried all 10 options, " + digit + " logged in -> " + known + digit);
                System.out.println("   -> HACKED: the attacker found the code in at most 60 guesses instead of 1,000,000.");
                return;
            }
        }
        System.out.println("   digit 6: tried all 10 options, none logged in.");
        System.out.println("   -> SAFE: the timings gave nothing away, so the attacker is back to 1,000,000 blind guesses.");
    }

    private static String guess(String known, int digit) {
        return (known + digit + "000000").substring(0, LOGIN_CODE.length());
    }

    private static String pad(String known) {
        return (known + "______").substring(0, LOGIN_CODE.length());
    }

    private static String stretch(String code) {
        StringBuilder stretched = new StringBuilder();
        for (char c : code.toCharArray()) {
            stretched.append(String.valueOf(c).repeat(STRETCH));
        }
        return stretched.toString();
    }

    private static <T> long time(Predicate<T> check, T guess) {
        long start = System.nanoTime();
        for (int i = 0; i < REPEATS; i++) {
            if (check.test(guess)) {
                accepted++;
            }
        }
        return System.nanoTime() - start;
    }
}
