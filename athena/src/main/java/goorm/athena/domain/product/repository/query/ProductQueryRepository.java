package goorm.athena.domain.product.repository.query;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import goorm.athena.domain.option.entity.Option;
import goorm.athena.domain.option.entity.QOption;
import goorm.athena.domain.product.dto.res.ProductResponse;
import goorm.athena.domain.product.entity.Product;
import goorm.athena.domain.product.entity.QProduct;
import goorm.athena.domain.project.entity.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ProductQueryRepository {
    private final JPAQueryFactory queryFactory;

    public List<ProductResponse> getAllProducts(Project project){
        QProduct product = QProduct.product;
        QOption option = QOption.option;

        List<Tuple> results = queryFactory
                .select(product, option)
                .from(product)
                .leftJoin(option).on(option.product.eq(product))
                .where(product.project.eq(project))
                .fetch();

        Map<Long, ProductResponse> responseMap = new LinkedHashMap<>();

        for (Tuple tuple : results) {
            Product p = tuple.get(product);
            Option o = tuple.get(option);

            ProductResponse existing = responseMap.get(p.getId());
            if (existing == null) {
                List<String> options = new ArrayList<>();
                if (o != null) options.add(o.getOptionName());

                ProductResponse newResponse = new ProductResponse(
                        p.getId(),
                        p.getName(),
                        p.getDescription(),
                        p.getPrice(),
                        p.getStock(),
                        options
                );
                responseMap.put(p.getId(), newResponse);
            } else {
                if (o != null) {
                    existing.options().add(o.getOptionName());
                }
            }
        }

        return new ArrayList<>(responseMap.values());
    }
}
