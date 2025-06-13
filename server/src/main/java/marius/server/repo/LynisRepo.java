package marius.server.repo;

import marius.server.data.Lynis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LynisRepo extends JpaRepository<Lynis, Integer> {

    public Optional<Lynis> findByIp(String ip);
}
