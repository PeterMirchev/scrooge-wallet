package com.scrooge.config.client.exchange;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Currency;

@FeignClient(name = "exchangeRage", url = "https://v6.exchangerate-api.com/v6")
public interface RateExchange {

    @GetMapping("{key}/latest/{currency}")
    ExchangeRateResponse getExchangeRate(@PathVariable(name = "key") String key, @PathVariable(name = "currency") Currency currency);
}
