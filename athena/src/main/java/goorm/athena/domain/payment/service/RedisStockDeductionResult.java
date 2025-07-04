package goorm.athena.domain.payment.service;

import lombok.Getter;

import java.util.Map;
@Getter
public class RedisStockDeductionResult {

    private final boolean success;
    private final Map<String, Integer> deductedStocks;

    public RedisStockDeductionResult(boolean success, Map<String, Integer> deductedStocks) {
        this.success = success;
        this.deductedStocks = deductedStocks;
    }
}
