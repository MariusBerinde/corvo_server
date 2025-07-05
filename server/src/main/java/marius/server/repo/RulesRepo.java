package marius.server.repo;

import marius.server.data.Rules;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RulesRepo extends JpaRepository<Rules,Integer> {
    public Optional<List<Rules>> findByIp(String ip);
}
