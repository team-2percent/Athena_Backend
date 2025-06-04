package goorm.athena.domain.project.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import goorm.athena.domain.project.repository.ProjectRepository;
import goorm.athena.domain.project.service.ProjectService;
import goorm.athena.util.IntegrationTestSupport;

@Transactional
public abstract class ProjectIntegrationTestSupport extends IntegrationTestSupport {
  @Autowired
  protected ProjectRepository projectRepository;

  @Autowired
  protected ProjectService projectService;
}
