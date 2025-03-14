package com.scrooge.service;

import com.scrooge.config.client.exchange.ExchangeRateResponse;
import com.scrooge.config.client.exchange.RateExchange;
import com.scrooge.exception.InsufficientAmountException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Currency;

@Service
public class ExchangeRateService {

    @Value("${exchange-rate.apiKey}")
    private String key;

    private final RateExchange rateExchange;

    public ExchangeRateService(RateExchange rateExchange) {
        this.rateExchange = rateExchange;
    }


    public ExchangeRateResponse getExchangeRate(Currency currency) {

        ExchangeRateResponse exchangeRate = rateExchange.getExchangeRate(key, currency);

        if (!exchangeRate.getResult().equals("success")) {

            throw new InsufficientAmountException("Currency convert exception.");
        }

        return exchangeRate;
    }
}
