package catchthepattern.com.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import catchthepattern.com.models.Breakout;

@Repository
public interface BreakoutRepository extends CrudRepository<Breakout, Long> {

}
