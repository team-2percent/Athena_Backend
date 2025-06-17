package goorm.athena.domain.payment;

import goorm.athena.domain.project.repository.ProjectRepository;
import goorm.athena.domain.user.repository.UserRepository;
import goorm.athena.util.IntegrationServiceTestSupport;
import org.springframework.beans.factory.annotation.Autowired;


public class PaymentIntergrationTestSupport extends IntegrationServiceTestSupport {

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ProjectRepository projectRepository;


}
