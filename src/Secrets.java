import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Secrets {
    private final Map<String, String> secrets = new HashMap<>();

    public void put(String key, String value) {
        secrets.put(key, value);
    }

    public String get(String key) {
        return secrets.get(key);
    }

    public int count() {
        return secrets.size();
    }

    // Leaky: String.equals() stops at the first mismatching character,
    // so the time taken reveals how much of the guess was correct.
    public boolean leakyMatches(String key, String provided) {
        String stored = secrets.get(key);
        return stored != null && stored.equals(provided);
    }

    // Safe: MessageDigest.isEqual() checks every byte regardless of where
    // the first mismatch is, so the time taken reveals nothing.
    public boolean safeMatches(String key, String provided) {
        String stored = secrets.get(key);
        return stored != null && MessageDigest.isEqual(
                provided.getBytes(UTF_8),
                stored.getBytes(UTF_8));
    }
}
