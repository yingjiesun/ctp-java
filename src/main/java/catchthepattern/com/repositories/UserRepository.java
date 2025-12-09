package catchthepattern.com.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import catchthepattern.com.models.User;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

	// @Query("SELECT u FROM users u WHERE u.email = ?1")
	// Optional<User> findByEmail(String email);
	// User findByEmail(String email);
	User findByUsername(String email);
	
	User findById(long id);
	
	Boolean existsByUsername(String username);
}