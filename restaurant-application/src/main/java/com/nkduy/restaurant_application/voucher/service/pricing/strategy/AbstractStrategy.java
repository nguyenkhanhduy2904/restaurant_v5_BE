package com.nkduy.restaurant_application.voucher.service.pricing.strategy;


import com.nkduy.restaurant_application.voucher.service.pricing.model.Cart;
import com.nkduy.restaurant_application.voucher.service.pricing.model.DiscountResult;
import jakarta.validation.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.stream.Collectors;


//base class for all discount strategy

public abstract class AbstractStrategy<P> implements DiscountStrategy {

    private final ObjectMapper strictMapper;
    private final Validator validator;
    private final Class<P> paramsType;


    protected AbstractStrategy(ObjectMapper strictMapper, Validator validator, Class<P> paramsType) {
        this.strictMapper = strictMapper.rebuild().enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
        this.validator = validator;
        this.paramsType = paramsType;
    }

    @Override
    public void validateParams(Map<String, Object> params) {
        parseAndValidateParams(params);
    }

    @Override
    public DiscountResult calculate(Cart cart, Map<String, Object> params) {
        return compute(cart, parseAndValidateParams(params));
    }



    private P parseAndValidateParams(Map<String, Object> params) {
        P paramsObject;// return a object of type P( from json to record)
        try {
            paramsObject = strictMapper.convertValue(params, paramsType);

        } catch (JacksonException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid voucher parameters" + e.getMessage());
        }
        if (paramsObject == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Voucher params are required");
        }

        var violations = validator.validate(paramsObject);// validate the object

        if (!violations.isEmpty()) {// if there is any violation, concatenate all violations into one message
            String msg = violations.stream()
                    .map(v -> v.getPropertyPath() + " " + v.getMessage())
                    .collect(Collectors.joining("; "));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid voucher params: " + msg);
        }
        return paramsObject;

    }
    // amount * percent / 100, rounded down to whole dong.
    //example: percentOf(800, 50) -> return 400 (50% of 800 is 400)
    protected static BigDecimal percentOf(BigDecimal amount, BigDecimal percent) {
        return amount.multiply(percent).divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN);
    }

    protected abstract DiscountResult compute(Cart cart, P params);
}
