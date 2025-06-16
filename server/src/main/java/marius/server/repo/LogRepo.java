package marius.server.repo;

import marius.server.data.Log;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogRepo extends JpaRepository<Log,Integer> {
}
