package catchthepattern.com.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import catchthepattern.com.registration.ConfirmationToken;
import catchthepattern.com.registration.PasswordResetToken;

@Repository
public interface PasswordTokenRepository extends CrudRepository<PasswordResetToken, Long> {

}
