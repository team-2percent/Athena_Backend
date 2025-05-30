package goorm.athena.domain.project;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import goorm.athena.domain.project.repository.ProjectRepository;
import goorm.athena.domain.project.service.ProjectService;
import goorm.athena.util.IntegrationTestSupport;

public abstract class ProjectIntegrationTestSupport extends IntegrationTestSupport {
  @Mock
  protected ProjectRepository projectRepository;

  @InjectMocks
  protected ProjectService projectService;
}
