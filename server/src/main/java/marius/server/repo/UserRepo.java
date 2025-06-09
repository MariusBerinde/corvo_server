package marius.server.repo;

import marius.server.data.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<User, String>{
	/**
	 * @param username : the name of the user
	 * @return true if exist a user with the username , false otherwise
	 */
	Boolean existsByUsername(String username);

}
