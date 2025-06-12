package marius.server.repo;

import marius.server.data.Server;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServerRepo  extends JpaRepository<Server,Integer> {
   public Optional<Server> findByIp(String username);
}
