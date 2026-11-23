package com.nkduy.restaurant_application.config.util;

import java.util.Set;

public final class PaymentStatusStateMachine {

    private PaymentStatusStateMachine(){}


    public static Set<String> allowedNextStatus(String currentStatus){
        return switch (currentStatus) {
            case "PENDING" -> Set.of("CANCELLED", "FAILED", "COMPLETED");
            case "CANCELLED", "FAILED", "COMPLETED" -> Set.of();
            default -> Set.of();
        };
    }

    public static boolean canTransition(String currentStatus, String nextStatus){
        return allowedNextStatus(currentStatus).contains(nextStatus);
    }
}
