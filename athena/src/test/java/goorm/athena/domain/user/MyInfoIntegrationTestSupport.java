package goorm.athena.domain.user;

import goorm.athena.domain.user.dto.response.MyProjectScrollResponse;
import goorm.athena.domain.user.mapper.MyProjectScrollResponseMapper;
import goorm.athena.domain.user.repository.MyInfoQueryRepository;
import goorm.athena.domain.user.service.MyInfoService;
import goorm.athena.util.IntegrationTestSupport;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public abstract class MyInfoIntegrationTestSupport extends IntegrationTestSupport {

    @Mock
    protected MyInfoQueryRepository myInfoQueryRepository;

    @Mock
    protected MyProjectScrollResponseMapper myProjectScrollResponseMapper;

    @InjectMocks
    protected MyInfoService myInfoService;
}
