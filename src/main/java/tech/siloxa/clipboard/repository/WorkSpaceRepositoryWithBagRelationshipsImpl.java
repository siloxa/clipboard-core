package tech.siloxa.clipboard.repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.hibernate.annotations.QueryHints;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import tech.siloxa.clipboard.domain.WorkSpace;

/**
 * Utility repository to load bag relationships based on https://vladmihalcea.com/hibernate-multiplebagfetchexception/
 */
public class WorkSpaceRepositoryWithBagRelationshipsImpl implements WorkSpaceRepositoryWithBagRelationships {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<WorkSpace> fetchBagRelationships(Optional<WorkSpace> workSpace) {
        return workSpace.map(this::fetchUsers);
    }

    @Override
    public Page<WorkSpace> fetchBagRelationships(Page<WorkSpace> workSpaces) {
        return new PageImpl<>(fetchBagRelationships(workSpaces.getContent()), workSpaces.getPageable(), workSpaces.getTotalElements());
    }

    @Override
    public List<WorkSpace> fetchBagRelationships(List<WorkSpace> workSpaces) {
        return Optional.of(workSpaces).map(this::fetchUsers).orElse(Collections.emptyList());
    }

    WorkSpace fetchUsers(WorkSpace result) {
        return entityManager
            .createQuery(
                "select workSpace from WorkSpace workSpace left join fetch workSpace.users where workSpace is :workSpace",
                WorkSpace.class
            )
            .setParameter("workSpace", result)
            .setHint(QueryHints.PASS_DISTINCT_THROUGH, false)
            .getSingleResult();
    }

    List<WorkSpace> fetchUsers(List<WorkSpace> workSpaces) {
        HashMap<Object, Integer> order = new HashMap<>();
        IntStream.range(0, workSpaces.size()).forEach(index -> order.put(workSpaces.get(index).getId(), index));
        List<WorkSpace> result = entityManager
            .createQuery(
                "select distinct workSpace from WorkSpace workSpace left join fetch workSpace.users where workSpace in :workSpaces",
                WorkSpace.class
            )
            .setParameter("workSpaces", workSpaces)
            .setHint(QueryHints.PASS_DISTINCT_THROUGH, false)
            .getResultList();
        Collections.sort(result, (o1, o2) -> Integer.compare(order.get(o1.getId()), order.get(o2.getId())));
        return result;
    }
}
