import java.security.MessageDigest;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SecretStore {
    private final String secret;
    private final byte[] secretBytes;

    public SecretStore(String secret) {
        this.secret = secret;
        this.secretBytes = secret.getBytes(UTF_8);
    }

    public boolean insecureEquals(String guess) {
        return secret.equals(guess);
    }

    public boolean constantTimeEquals(byte[] guess) {
        return MessageDigest.isEqual(secretBytes, guess);
    }
}
