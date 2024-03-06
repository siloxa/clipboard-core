package tech.siloxa.clipboard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.siloxa.clipboard.domain.Authority;

/**
 * Spring Data JPA repository for the {@link Authority} entity.
 */
public interface AuthorityRepository extends JpaRepository<Authority, String> {}
