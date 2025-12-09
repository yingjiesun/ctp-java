package catchthepattern.com.repositories;

import java.util.Set;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import catchthepattern.com.models.CtpPayment;

@Repository
public interface CtpPaymentRepository  extends CrudRepository<CtpPayment, Long>  {

    Set<CtpPayment> findByUserId(Long userId);
}
