package goorm.athena.domain.search.service;

import goorm.athena.domain.search.repository.SearchRepository;
import goorm.athena.domain.product.entity.Product;
import goorm.athena.domain.search.entity.Search;
import goorm.athena.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Service;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.List;
import java.util.ArrayList;

@RequiredArgsConstructor
@Service
public class SearchService {
  private final SearchRepository searchRepository;

  private Specification<Search> searchWord(String searchWord) {
    return new Specification<Search>() {
      // ToDo 아래 구문의 역할 이해하기
      private static final long serialVersionUID = 1L;

      @Override
      public Predicate toPredicate(@NonNull Root<Search> root, @Nullable CriteriaQuery<?> query,
          @NonNull CriteriaBuilder criteriaBuilder) {
        if (query != null) {
          query.distinct(true);
        }

        // 검색 키워드와 상품 테이블을 조인해서 검색 키워드가 포함된 상품을 조회
        Join<Search, Product> product = root.join("product", JoinType.LEFT);
        // 그렇게 조회된 상품과 판매자 테이블을 조인해서 판매자 이름을 조회
        Join<Product, User> seller = product.join("seller", JoinType.LEFT);

        return criteriaBuilder.or(
            // 조인된 상품 product에서 title 컬럼을 비교
            criteriaBuilder.like(product.get("title"), "%" + searchWord + "%"),
            // 조인된 판매자 seller에서 name 컬럼을 비교
            criteriaBuilder.like(seller.get("name"), "%" + searchWord + "%"));
      }
    };
  }

  //
  public Page<Search> getList(Integer page, String searchWord) {
    List<Sort.Order> sorts = new ArrayList<>();
    sorts.add(Sort.Order.desc("createdDate"));
    Pageable pageable = PageRequest.of(page, 20, Sort.by(sorts));
    Specification<Search> spec = searchWord(searchWord);
    return this.searchRepository.findAll(spec, pageable);
  };
}
