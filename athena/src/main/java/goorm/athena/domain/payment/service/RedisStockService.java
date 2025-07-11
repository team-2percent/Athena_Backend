package goorm.athena.domain.payment.service;

import goorm.athena.domain.orderitem.entity.OrderItem;
import goorm.athena.domain.product.entity.Product;
import goorm.athena.domain.product.service.ProductQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class RedisStockService {

    private final RedisTransactionUtil transactionUtil;
    private final ProductQueryService productQueryService;

    private static final String REDIS_PRODUCT_STOCK_KEY_PREFIX = "product:stock:";
    private static final int stockTtl = 60;

    // 각 상품 별 재고를 계산 후 배열로 리턴
    private static final String LUA_SCRIPT =
            "local results = {} " +
                    "local ttl = tonumber(ARGV[#ARGV]) " +
                    "for i, key in ipairs(KEYS) do " +
                    "    local stock = redis.call('GET', key) " +
                    "    if not stock then " +
                    "        stock = ARGV[i * 2 - 1] " +
                    "        redis.call('SETEX', key, ttl, stock) " +
                    "    end " +
                    "    stock = tonumber(stock) " +
                    "    local quantity = tonumber(ARGV[i * 2]) " +
                    "    if stock < quantity then " +
                    "        return {-1} " +
                    "    end " +
                    "    redis.call('DECRBY', key, quantity) " +
                    "    results[i] = stock - quantity " +
                    "end " +
                    "return results";


    public RedisStockDeductionResult tryDeductStocks(List<OrderItem> items) {
        Map<String, Integer> deductedStocks = new HashMap<>();
        try {
            DefaultRedisScript<List> script = new DefaultRedisScript<>(LUA_SCRIPT, List.class);
            List<String> keys = new ArrayList<>();
            List<Object> args = new ArrayList<>();

            for (OrderItem item : items) {
                String key = REDIS_PRODUCT_STOCK_KEY_PREFIX + item.getProduct().getId();
                keys.add(key);

                // 캐시 미스
                Integer stock = transactionUtil.getIntegerRedisTemplate().opsForValue().get(key);
                if (stock == null) {
                    // 결제 로직에 프록시 객체를 신뢰하는게 맞을까?
//                    Product product = item.getProduct();
//                    stock = product.getStock();
                    Product product = productQueryService.getById(item.getProduct().getId());
                    stock = product.getStock().intValue();;
                }

                // 짝수 인덱스 방식 : ARGV[1] = stock , ARGV[2] = Quantity
                args.add(stock);
                args.add(item.getQuantity());
            }
            args.add(stockTtl);


            List<Long> results = transactionUtil.getIntegerRedisTemplate().execute(script, keys, args.toArray());

            if (results == null || results.contains(-1L)) {
                log.warn("Lua 재고 차감 실패: 재고 부족");
                return new RedisStockDeductionResult(false, Map.of());
            }

            for (int i = 0; i < items.size(); i++) {
                int deducted = items.get(i).getQuantity();

                Long remainingStock = results.get(i);
                deductedStocks.put(keys.get(i), items.get(i).getQuantity());

                log.info("상품 ID: {}, 차감된 수량: {}, 차감 후 Redis 재고: {}",
                        items.get(i).getProduct().getId(),
                        deducted,
                        remainingStock);
            }
            log.info("deductedStocks 결과: {}", deductedStocks);


            return new RedisStockDeductionResult(true, deductedStocks);
        } catch (Exception e) {
            log.error("Redis Lua 실행 오류: {}", e.getMessage(), e);
            return new RedisStockDeductionResult(false, Map.of());
        }
    }


    // lua는 성공 but 서비스 로직에서 예외 발생경우 롤백
    public void rollbackStocks(Map<String, Integer> deductedStocks) {
        if (deductedStocks.isEmpty()) return;
        int retries = 3;
        while (retries > 0) {
            try {
                transactionUtil.transaction(operations -> {
                    deductedStocks.forEach((key, value) -> operations.opsForValue().increment(key, value));
                    return null;
                });
                log.info("Rollback 성공: {}", deductedStocks);
                return;
            } catch (Exception e) {
                retries--;
                log.warn("Rollback 실패 (남은 시도: {}): {}", retries, e.getMessage());
                if (retries == 0) throw new RuntimeException("재고 롤백 실패", e);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}