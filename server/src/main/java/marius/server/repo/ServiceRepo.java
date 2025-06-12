package marius.server.repo;

import marius.server.data.Service;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceRepo extends JpaRepository<Service,Integer> {
    /**
     *  Return all services with ip
     * @param ip
     * @return a list all services with ip
     */
   List<Service> findAllByIp(String ip);
   Optional<Service>  findByName(String name);
}
