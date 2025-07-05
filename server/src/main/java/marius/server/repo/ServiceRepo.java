package marius.server.repo;

import marius.server.data.AgentService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceRepo extends JpaRepository<AgentService,Integer> {
    /**
     *  Return all services with ip
     * @param ip
     * @return a list all services with ip
     */
   List<AgentService> findAllByIp(String ip);
   Optional<AgentService>  findByName(String name);
   List<AgentService> findByIp(String ip);
}
