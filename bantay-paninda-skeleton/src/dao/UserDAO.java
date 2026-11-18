package dao;

import db.DBConnection;
import model.User;
import util.PinUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * UserDAO.java
 * -----------
 * This class does the database work for the "users" table.
 * It only needs one job right now: check a login.
 *
 * Rules followed here:
 * - Only PreparedStatement (never build SQL by joining text).
 * - Every connection comes from DBConnection.getConnection().
 * - try-with-resources closes everything automatically.
 *
 * The PIN is never saved and never compared as plain text.
 * We hash the typed PIN with PinUtil and compare it with the hash
 * that is already in users.pin_hash.
 */
public class UserDAO {

    /**
     * Checks a login.
     *
     * Returns the User object when the username exists AND the PIN
     * matches that account (so the caller can read the role).
     * Returns null when the username does not exist, or when the PIN
     * is wrong, or when the database fails.
     *
     * We do NOT tell the caller which of the two was wrong, because
     * that would help someone guess usernames.
     */
    public User login(String username, String pin) {
        // ---------- STEP 1: basic input check ----------
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        if (!PinUtil.isValidPin(pin)) {
            return null;        // a PIN must be exactly 4 digits
        }

        String sql = "SELECT user_id, full_name, username, pin_hash, role "
                   + "FROM users WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username.trim());

            try (ResultSet rs = ps.executeQuery()) {

                // ---------- STEP 2: does the username exist? ----------
                if (!rs.next()) {
                    return null;            // unknown username
                }

                String savedHash = rs.getString("pin_hash");
                String dbUsername = rs.getString("username");

                // ---------- STEP 3: does the PIN match the saved hash? ----------
                // The username from the database row is used for the hash,
                // not the text the user typed, so a small difference in
                // upper/lower case cannot break a correct login.
                if (!PinUtil.matches(dbUsername, pin, savedHash)) {
                    return null;            // wrong PIN
                }

                // ---------- STEP 4: build the User object ----------
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setFullName(rs.getString("full_name"));
                user.setUsername(dbUsername);
                user.setRole(rs.getString("role"));
                return user;
            }

        } catch (SQLException e) {
            System.out.println("UserDAO.login error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Returns how many accounts are in the users table.
     * The test uses this to warn when the sample accounts are missing.
     */
    public int countUsers() {
        String sql = "SELECT COUNT(*) FROM users";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.out.println("UserDAO.countUsers error: " + e.getMessage());
        }
        return 0;
    }
}
