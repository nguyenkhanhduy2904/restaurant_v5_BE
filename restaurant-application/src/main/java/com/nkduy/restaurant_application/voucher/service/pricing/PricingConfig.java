package com.nkduy.restaurant_application.voucher.service.pricing;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;
@Configuration
public class PricingConfig {
    @Bean
    Clock clock() { return Clock.system(ZoneId.of("Asia/Ho_Chi_Minh")); }
}
