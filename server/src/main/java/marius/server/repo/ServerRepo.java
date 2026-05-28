package marius.server.repo;

import marius.server.data.Server;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Interface used for interact with the Server table
 */
public interface ServerRepo  extends JpaRepository<Server,Integer> {
   public Optional<Server> findByIp(String username);
}
