package marius.server.repo;

import marius.server.data.Server;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServerRepo  extends JpaRepository<Server,Integer> {}
