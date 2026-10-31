import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Holds secrets by name and checks guesses against them in two ways:
 * one insecure, one safe. Compare the two methods to see the difference.
 */
public class SecretStore {
    private final Map<String, String> secrets = new HashMap<>();
    private final Map<String, byte[]> secretBytes = new HashMap<>();

    public void put(String name, String secret) {
        secrets.put(name, secret);
        secretBytes.put(name, secret.getBytes(UTF_8));
    }

    /**
     * THE PROBLEM: String.equals() stops at the first character that differs.
     * A guess that is wrong early is rejected faster than a guess that is wrong late,
     * so an attacker who can time this check learns how much of their guess is right.
     */
    public boolean insecureEquals(String name, String guess) {
        String secret = secrets.get(name);
        return secret != null && secret.equals(guess);
    }

    /**
     * THE SOLUTION: MessageDigest.isEqual() compares every byte, wherever the first
     * difference is, so the time taken reveals nothing about how close the guess was.
     *
     * The guess is passed as bytes so the demo times only the comparison itself.
     * Converting a String to bytes on every call would add cost unrelated to the lesson.
     */
    public boolean constantTimeEquals(String name, byte[] guess) {
        byte[] secret = secretBytes.get(name);
        return secret != null && MessageDigest.isEqual(secret, guess);
    }
}
