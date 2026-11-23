package com.nkduy.restaurant_application.voucher.service.pricing.helper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

public class DiscountAllocator {
    private DiscountAllocator(){}

    public static Map<String, BigDecimal> split(BigDecimal amountToSplit, Map<String, BigDecimal> pricePerLine ){
        BigDecimal totalPrice = pricePerLine.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);// get the total price of all items, so we can calculate the percentage

        Map<String, BigDecimal> discountPerLine = new LinkedHashMap<>();// create a new map to store the discount amount for each item (id -> discount amount)
        if(totalPrice.signum() == 0 || amountToSplit.signum() == 0){
            return discountPerLine;// if the total price is 0 or the total amount to split is 0, return an empty map
        }
        BigDecimal leftover = amountToSplit;

        for(var entry : pricePerLine.entrySet()){// loop through the entire map, calculate and return <id -> discount amount> for each item
            // share = linePrice * amountToSplit / totalPrice
            // Multiply first and divide last, so round only once.
            // Round DOWN to whole dong, so we never hand out more than amountToSplit.
            BigDecimal share = entry.getValue()
                    .multiply(amountToSplit)
                    .divide(totalPrice, 0, RoundingMode.DOWN);

            discountPerLine.put(entry.getKey(), share);
            leftover = leftover.subtract(share);

        }

        if(leftover.signum() > 0){

            String mostExpensiveItemLineId = pricePerLine.entrySet().stream().max(Map.Entry.comparingByValue()).orElseThrow().getKey();
            discountPerLine.merge(mostExpensiveItemLineId, leftover, BigDecimal::add);

        }
        return discountPerLine;

    }
}
