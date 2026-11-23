package com.nkduy.restaurant_application.config.util;

import java.util.Set;

public final class OrderStatusStateMachine {
    private OrderStatusStateMachine(){}

    public static Set<String> allowedNextStatus(String currentStatus){
        return switch (currentStatus) {
            case "PENDING" -> Set.of("REJECTED", "CANCELLED", "PREPARING");
            case "PREPARING" -> Set.of("DELIVERING");
            case "DELIVERING" -> Set.of("COMPLETED", "DELIVERY_FAILED");
            case "COMPLETED", "DELIVERY_FAILED", "CANCELLED", "REJECTED" -> Set.of();
            default -> Set.of();
        };
    }

    public static boolean canTransition(String currentStatus, String nextStatus){
        return allowedNextStatus(currentStatus).contains(nextStatus);
    }



}
