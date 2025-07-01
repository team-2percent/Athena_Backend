//package goorm.athena.domain.payment.service;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.dao.DataAccessException;
//import org.springframework.data.redis.core.RedisOperations;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.SessionCallback;
//
//import java.util.function.Function;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Component;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class RedisTransactionUtil {
//    private final RedisTemplate<String, Integer> integerRedisTemplate;
//
//    @SuppressWarnings("unchecked")
//    public <T> T transaction(Function<RedisOperations<String, Integer>, T> commands) {
//        return integerRedisTemplate.execute(new SessionCallback<T>() {
//            @Override
//            public T execute(RedisOperations operations) throws DataAccessException {
//                operations.multi();
//                try {
//                    T result = (T) commands.apply(operations);
//                    operations.exec();
//                    return result;
//                } catch (Exception e) {
//                    log.error("Redis 트랜잭션 수행 중 오류 발생", e);
//                    operations.discard();
//                    throw e;
//                }
//            }
//        });
//    }
//
//    public RedisTemplate<String, Integer> getIntegerRedisTemplate() {
//        return integerRedisTemplate;
//    }
//}