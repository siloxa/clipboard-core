package tech.siloxa.clipboard.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.siloxa.clipboard.domain.User;
import tech.siloxa.clipboard.domain.WorkSpace;

/**
 * Spring Data JPA repository for the WorkSpace entity.
 *
 * When extending this class, extend WorkSpaceRepositoryWithBagRelationships too.
 * For more information refer to https://github.com/jhipster/generator-jhipster/issues/17990.
 */
@Repository
public interface WorkSpaceRepository extends WorkSpaceRepositoryWithBagRelationships, JpaRepository<WorkSpace, Long> {
    default Optional<WorkSpace> findOneWithEagerRelationships(Long id) {
        return this.fetchBagRelationships(this.findById(id));
    }

    default List<WorkSpace> findAllWithEagerRelationships() {
        return this.fetchBagRelationships(this.findAll());
    }

    default List<WorkSpace> findAllWithEagerRelationshipsByUser(User user) {
        return this.fetchBagRelationships(this.findAllByUser(user));
    }

    default Page<WorkSpace> findAllWithEagerRelationships(Pageable pageable) {
        return this.fetchBagRelationships(this.findAll(pageable));
    }

    List<WorkSpace> findAllByUser(User user);
}
