package goorm.athena.domain.project;

import goorm.athena.util.IntegrationServiceTestSupport;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import goorm.athena.domain.project.repository.ProjectRepository;
import goorm.athena.domain.project.service.ProjectService;

public abstract class ProjectIntegrationTestSupport extends IntegrationServiceTestSupport {
  @Mock
  protected ProjectRepository projectRepository;

  @InjectMocks
  protected ProjectService projectService;
}
