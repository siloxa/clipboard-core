package tech.siloxa.clipboard.service;

import org.springframework.stereotype.Service;
import tech.siloxa.clipboard.domain.User;
import tech.siloxa.clipboard.domain.WorkSpace;
import tech.siloxa.clipboard.repository.WorkSpaceRepository;

import javax.annotation.Resource;

@Service
public class WorkSpaceService {

    @Resource
    private WorkSpaceRepository workSpaceRepository;

    public void initWorkSpaceForUser(final User user) {
        final WorkSpace workSpace = new WorkSpace();
        workSpace.setName("Home");
        workSpace.setUser(user);
        workSpaceRepository.save(workSpace);
    }
}
