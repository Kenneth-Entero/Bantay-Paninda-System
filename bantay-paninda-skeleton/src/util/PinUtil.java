package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * PinUtil.java
 * -----------
 * Helper for the employee PIN. We NEVER save the plain PIN in the
 * database. Instead we save a "hash" - a long text that is made from
 * the username and the PIN. The same username + PIN always gives the
 * same hash, so we can compare hashes to check a login.
 *
 * Format of the hash:
 * sha256Hex( username + ":" + pin )
 *
 * Example: username "manager" + PIN "1111"
 *          -> sha256Hex("manager:1111")
 */
public class PinUtil {

    // A PIN must be exactly this many digits.
    public static final int PIN_LENGTH = 4;

    // Private constructor: nobody needs to create a PinUtil object,
    // every method here is static.
    private PinUtil() {
    }

    /**
     * Builds the hash we store in users.pin_hash.
     *
     * @param username the account name
     * @param pin      the 4 digit PIN
     * @return the SHA-256 hash as 64 lowercase hex characters,
     *         or null when the username or the PIN is not valid.
     */
    public static String hash(String username, String pin) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        if (!isValidPin(pin)) {
            return null;
        }

        String text = username.trim() + ":" + pin;
        return sha256Hex(text);
    }

    /**
     * Returns true when the PIN typed by the user matches the hash
     * that is saved in the database.
     *
     * @param username   the account name
     * @param pin        the PIN typed on the login screen
     * @param storedHash the value of users.pin_hash
     */
    public static boolean matches(String username, String pin, String storedHash) {
        if (storedHash == null) {
            return false;
        }

        String computed = hash(username, pin);
        if (computed == null) {
            return false;               // bad username or bad PIN format
        }

        // equalsIgnoreCase ignores uppercase/lowercase differences.
        return computed.equalsIgnoreCase(storedHash.trim());
    }

    /**
     * Returns true when the text is exactly 4 characters and all of
     * them are digits 0-9. A PIN like "12a4" or "111" is not valid.
     */
    public static boolean isValidPin(String pin) {
        if (pin == null || pin.length() != PIN_LENGTH) {
            return false;
        }
        for (int i = 0; i < pin.length(); i++) {
            char c = pin.charAt(i);
            if (c < '0' || c > '9') {
                return false;           // not a digit
            }
        }
        return true;
    }

    /**
     * Helper: turns any text into its SHA-256 hash written as
     * 64 hex characters (0-9, a-f).
     * Returns null if the computer cannot find SHA-256.
     */
    private static String sha256Hex(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                // %02x writes the byte as two hex characters, e.g. 255 -> "ff"
                hex.append(String.format("%02x", b));
            }
            return hex.toString();

        } catch (NoSuchAlgorithmException e) {
            System.out.println("PinUtil error: " + e.getMessage());
            return null;
        }
    }
}
