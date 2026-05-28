package marius.server.repo;

import marius.server.data.Lynis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Interface user for interact with the Lynis table.
 */
public interface LynisRepo extends JpaRepository<Lynis, Integer> {

    public Optional<Lynis> findByIp(String ip);
}
