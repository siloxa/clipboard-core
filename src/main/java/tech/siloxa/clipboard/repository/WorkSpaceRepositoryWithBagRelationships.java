package tech.siloxa.clipboard.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import tech.siloxa.clipboard.domain.WorkSpace;

public interface WorkSpaceRepositoryWithBagRelationships {
    Optional<WorkSpace> fetchBagRelationships(Optional<WorkSpace> workSpace);

    List<WorkSpace> fetchBagRelationships(List<WorkSpace> workSpaces);

    Page<WorkSpace> fetchBagRelationships(Page<WorkSpace> workSpaces);
}
