package goorm.athena.domain.project.spec;

import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.user.entity.User;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

public class ProjectSpecification {
    public static Specification<Project> searchByTitleOrSeller(String searchWord) {
        return (root, query, cb) -> {
            Join<Project, User> seller = root.join("seller", JoinType.LEFT);
            String pattern = "%" + searchWord.replaceAll("[%_]", "\\$0") + "%";
            return cb.or(
                    cb.like(root.get("title"), pattern),
                    cb.like(seller.get("name"), pattern));
        };
    }
}