package util;

import model.User;

/**
 * Session.java
 * -----------
 * Remembers WHO is using the program right now.
 *
 * The program has two business types:
 *  - TINDAHAN (sari-sari store) : no login, so currentUser is null.
 *  - MALL                      : the employee logs in with a username and PIN.
 *
 * All fields are static because there is only ONE session while the
 * program is running. Any screen can ask Session.getRole() without
 * passing the session around.
 */
public class Session {

    /** The two kinds of business the program can run. */
    public enum BusinessType {
        MALL,
        TINDAHAN
    }

    // ---------- STATIC FIELDS (the whole session) ----------
    private static BusinessType businessType;   // MALL or TINDAHAN
    private static User currentUser;            // null in Tindahan mode

    // Private constructor: this class is only used through its
    // static methods, so nobody should create a Session object.
    private Session() {
    }

    /**
     * Starts a MALL session for a user who just logged in.
     * The user object must already be checked by UserDAO.login().
     */
    public static void startMall(User user) {
        businessType = BusinessType.MALL;
        currentUser = user;
    }

    /**
     * Starts a TINDAHAN session.
     * There is no login here, so currentUser is set back to null
     * and getUserId() will return null.
     */
    public static void startTindahan() {
        businessType = BusinessType.TINDAHAN;
        currentUser = null;
    }

    /**
     * Ends the session. After this, isLoggedIn() is false and
     * getRole() / getUserId() return null.
     * This is what the "Logout" button calls.
     */
    public static void logout() {
        businessType = null;
        currentUser = null;
    }

    /**
     * Returns true when somebody can use the program:
     *  - Tindahan mode: always true (no login needed).
     *  - Mall mode: true only after a successful login.
     */
    public static boolean isLoggedIn() {
        if (businessType == BusinessType.TINDAHAN) {
            return true;
        }
        return businessType == BusinessType.MALL && currentUser != null;
    }

    /**
     * Returns the role ("MANAGER", "INVENTORY", "CASHIER")
     * or null in Tindahan mode.
     */
    public static String getRole() {
        if (currentUser == null) {
            return null;
        }
        return currentUser.getRole();
    }

    /**
     * Returns the user_id of the logged in employee,
     * or null in Tindahan mode (no login).
     * The DAO uses this value for stock_movements.user_id.
     */
    public static Integer getUserId() {
        if (currentUser == null) {
            return null;
        }
        return currentUser.getUserId();
    }

    /** Returns the logged in User object, or null in Tindahan mode. */
    public static User getCurrentUser() {
        return currentUser;
    }

    /** Returns MALL or TINDAHAN, or null when nobody has started. */
    public static BusinessType getBusinessType() {
        return businessType;
    }

    /** True when the program is running in Tindahan mode. */
    public static boolean isTindahan() {
        return businessType == BusinessType.TINDAHAN;
    }

    /** True when the program is running in Mall mode. */
    public static boolean isMall() {
        return businessType == BusinessType.MALL;
    }
}
