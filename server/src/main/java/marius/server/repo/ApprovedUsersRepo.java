package marius.server.repo;

/*
 * Interface used for query with the table approvedUsers
 */
import marius.server.data.ApprovedUsers;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
public interface ApprovedUsersRepo extends JpaRepository<ApprovedUsers,Integer> {
    /**
     * Return the approved user with email
     * @param email the email of the user
     * @return the {@code ApprovedUsesrs}
     */
    Optional<ApprovedUsers> findByEmail(String email);

    /**
     * Delete the user with email from the approved user table
     * @param email the email of the user that will be deleted
     * @return the number of deletions
     */
    Integer deleteByEmail(String email);

}
