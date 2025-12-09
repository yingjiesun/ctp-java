package catchthepattern.com.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import catchthepattern.com.models.Filterset;

@Repository
public interface FiltersetRepository  extends CrudRepository<Filterset, Long> {

}
