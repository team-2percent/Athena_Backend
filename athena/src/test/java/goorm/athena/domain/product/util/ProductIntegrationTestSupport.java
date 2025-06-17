package goorm.athena.domain.product.util;

import goorm.athena.domain.product.entity.Product;
import goorm.athena.domain.product.service.ProductCommandService;
import goorm.athena.domain.product.service.ProductQueryService;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.project.repository.ProjectRepository;
import goorm.athena.util.IntegrationServiceTestSupport;
import goorm.athena.util.TestEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class ProductIntegrationTestSupport extends IntegrationServiceTestSupport {

    @Autowired
    protected ProductQueryService productQueryService;

    @Autowired
    protected ProductCommandService productCommandService;

    @Autowired
    protected ProjectRepository projectRepository;

    protected Product setupProduct(Project project, String name, String description, Long price, Long stock){
        return TestEntityFactory.createProduct(project, name, description, price, stock);
    }
}
