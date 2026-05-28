package marius.server.repo;

import marius.server.data.Log;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Interface used interact with the table Log
 */
public interface LogRepo extends JpaRepository<Log,Integer> {

    List<Log> findByUserEmail(String ip);
}
