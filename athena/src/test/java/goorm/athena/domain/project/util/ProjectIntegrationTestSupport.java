package goorm.athena.domain.project.util;

import goorm.athena.util.IntegrationServiceTestSupport;
import jakarta.transaction.Transactional;

import goorm.athena.domain.project.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;

@Transactional
public abstract class ProjectIntegrationTestSupport extends IntegrationServiceTestSupport {
    @Autowired
    protected ProjectRepository projectRepository;


}
