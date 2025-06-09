package marius.server.repo;

/**
 * Interface used for query with the table approvedUsers
 */
import marius.server.data.ApprovedUsers;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
public interface ApprovedUsersRepo extends JpaRepository<ApprovedUsers,Integer> {
    Optional<ApprovedUsers> findByEmail(String email);
    Boolean existsByEmail(String email);

    Integer deleteByEmail(String email);
}
