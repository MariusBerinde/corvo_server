package marius.server.repo;

import marius.server.data.Rules;
import marius.server.data.Server;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RulesRepo extends JpaRepository<Rules,Integer> {
    public Optional<Rules> findByIp(String ip);
}
