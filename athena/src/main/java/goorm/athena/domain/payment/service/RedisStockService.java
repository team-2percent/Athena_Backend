//package goorm.athena.domain.payment.service;
//
//import goorm.athena.domain.orderitem.entity.OrderItem;
//import goorm.athena.domain.payment.service.V4.RedisStockDeductionResult;
//import goorm.athena.domain.product.entity.Product;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.redis.core.script.DefaultRedisScript;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class RedisStockService {
//
//    private final RedisTransactionUtil transactionUtil;
//
//    private static final String REDIS_PRODUCT_STOCK_KEY_PREFIX = "product:stock:";
//
//    private static final String LUA_SCRIPT =
//            "local results = {} " +
//                    "for i, key in ipairs(KEYS) do " +
//                    "    local stock = redis.call('GET', key) " +
//                    "    if not stock then " +
//                    "        stock = ARGV[i * 2 - 1] " +
//                    "        redis.call('SET', key, stock) " +
//                    "    end " +
//                    "    stock = tonumber(stock) " +
//                    "    local quantity = tonumber(ARGV[i * 2]) " +
//                    "    if stock < quantity then " +
//                    "        return {-1} " +
//                    "    end " +
//                    "    redis.call('DECRBY', key, quantity) " +
//                    "    results[i] = stock - quantity " +
//                    "end " +
//                    "return results";
//
//
//    public RedisStockDeductionResult tryDeductStocks(List<OrderItem> items) {
//        Map<String, Integer> deducted = new HashMap<>();
//        try {
//            DefaultRedisScript<List> script = new DefaultRedisScript<>(LUA_SCRIPT, List.class);
//            List<String> keys = new ArrayList<>();
//            List<Object> args = new ArrayList<>();
//
//            for (OrderItem item : items) {
//                String key = REDIS_PRODUCT_STOCK_KEY_PREFIX + item.getProduct().getId();
//                keys.add(key);
//
//                // Redis 또는 DB에서 현재 재고 조회
//                Integer stock = transactionUtil.getIntegerRedisTemplate().opsForValue().get(key);
//                if (stock == null) {
//                    Product product = item.getProduct();  // 이미 가져온 엔티티라면
//                    Long stock = product.getStock();           // 혹은 DB에서 조회
//                }
//
//                args.add(stock);               // 초기 재고
//                args.add(item.getQuantity()); // 차감 수량
//            }
//
//            List<Long> results = transactionUtil.getIntegerRedisTemplate()
//                    .execute(script, keys, args.toArray());
//
//            if (results == null || results.contains(-1L)) {
//                log.warn("Lua 재고 차감 실패: 재고 부족");
//                return new RedisStockDeductionResult(false, Map.of());
//            }
//
//            for (int i = 0; i < items.size(); i++) {
//                deducted.put(keys.get(i), items.get(i).getQuantity());
//            }
//
//            return new RedisStockDeductionResult(true, deducted);
//        } catch (Exception e) {
//            log.error("Redis Lua 실행 오류: {}", e.getMessage(), e);
//            return new RedisStockDeductionResult(false, Map.of());
//        }
//    }
//
//    public void rollbackStocks(Map<String, Integer> deducted) {
//        if (deducted.isEmpty()) return;
//        int retries = 3;
//        while (retries-- > 0) {
//            try {
//                transactionUtil.transaction(operations -> {
//                    deducted.forEach((key, value) -> operations.opsForValue().increment(key, value));
//                    return null;
//                });
//                log.info("Rollback 성공: {}", deducted);
//                return;
//            } catch (Exception e) {
//                log.warn("Rollback 실패 (남은 시도: {}): {}", retries, e.getMessage());
//                if (retries == 0) throw new RuntimeException("재고 롤백 실패", e);
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException ignored) {
//                    Thread.currentThread().interrupt();
//                }
//            }
//        }
//    }
//}