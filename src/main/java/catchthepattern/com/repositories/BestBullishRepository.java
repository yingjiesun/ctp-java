package catchthepattern.com.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import catchthepattern.com.models.BestBullish;

@Repository
public interface BestBullishRepository extends CrudRepository<BestBullish, Long> {

}
