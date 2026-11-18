package util;

import java.util.ArrayList;
import java.util.List;

/**
 * Permissions.java
 * ----------------
 * Decides which business functions the current user is allowed to do.
 *
 * Rules:
 *   - TINDAHAN (role is null)          -> all 5 functions
 *   - MALL + MANAGER                   -> all 5 functions
 *   - MALL + INVENTORY                 -> ADD_NEW_PRODUCT, RESTOCK, VIEW_INVENTORY
 *   - MALL + CASHIER                   -> DEDUCT_STOCK, VIEW_INVENTORY
 *   - anything else (null type, unknown role) -> no function allowed
 */
public class Permissions {

    public enum BusinessFunction {
        ADD_NEW_PRODUCT,
        DEDUCT_STOCK,
        RESTOCK,
        REMOVE_PRODUCT,
        VIEW_INVENTORY
    }

    // Static-only class: nobody should create a Permissions object.
    private Permissions() {
    }

    /**
     * Checks a permission using the current Session (MALL or Tindahan).
     *
     * @param f the function to check
     * @return true when the logged-in user (or Tindahan) may do this function
     */
    public static boolean canDo(BusinessFunction f) {
        return canDo(Session.getBusinessType(), Session.getRole(), f);
    }

    /**
     * Same rules, but takes business type and role as arguments.
     * This version does not need a Session, so it can be used in tests.
     *
     * @param type MALL or TINDAHAN (or null when nobody has started)
     * @param role MANAGER / INVENTORY / CASHIER, or null in Tindahan mode
     * @param f    the function to check
     * @return true when this type+role may do the function
     */
    public static boolean canDo(Session.BusinessType type, String role, BusinessFunction f) {
        return allowedFor(type, role).contains(f);
    }

    /**
     * Returns the list of allowed functions for a business type + role.
     *
     * @param type MALL or TINDAHAN (or null)
     * @param role MANAGER / INVENTORY / CASHIER, or null in Tindahan mode
     * @return a list with all allowed functions (empty when nothing is allowed)
     */
    public static List<BusinessFunction> allowedFor(Session.BusinessType type, String role) {
        // Build the full list once so we can reuse it.
        List<BusinessFunction> all = new ArrayList<>();
        all.add(BusinessFunction.ADD_NEW_PRODUCT);
        all.add(BusinessFunction.DEDUCT_STOCK);
        all.add(BusinessFunction.RESTOCK);
        all.add(BusinessFunction.REMOVE_PRODUCT);
        all.add(BusinessFunction.VIEW_INVENTORY);

        // Empty list = nothing allowed.
        List<BusinessFunction> none = new ArrayList<>();

        if (type == Session.BusinessType.TINDAHAN) {
            // Tindahan has no login, so the role is ignored.
            return all;
        }
        if (type == Session.BusinessType.MALL) {
            if ("MANAGER".equals(role)) {
                return all;
            }
            if ("INVENTORY".equals(role)) {
                List<BusinessFunction> list = new ArrayList<>();
                list.add(BusinessFunction.ADD_NEW_PRODUCT);
                list.add(BusinessFunction.RESTOCK);
                list.add(BusinessFunction.VIEW_INVENTORY);
                return list;
            }
            if ("CASHIER".equals(role)) {
                List<BusinessFunction> list = new ArrayList<>();
                list.add(BusinessFunction.DEDUCT_STOCK);
                list.add(BusinessFunction.VIEW_INVENTORY);
                return list;
            }
            // Unknown mall role -> nothing allowed.
            return none;
        }
        // Null type or unknown -> nothing allowed.
        return none;
    }
}
