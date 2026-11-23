package com.nkduy.restaurant_application.config.util;

import java.util.Set;

public final class RefundStatusStateMachine {
    private RefundStatusStateMachine(){}

    public static Set<String> allowedNextStatus(String currentStatus){
        return switch (currentStatus) {
            case "NOT_REQUIRED" -> Set.of("PENDING");
            case "PENDING" -> Set.of("FAILED", "REFUNDED");
            case "FAILED", "REFUNDED" -> Set.of();
            default -> Set.of();
        };
    }

    public static boolean canTransition(String currentStatus, String nextStatus){
        return allowedNextStatus(currentStatus).contains(nextStatus);
    }
}
