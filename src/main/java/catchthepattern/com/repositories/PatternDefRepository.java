package catchthepattern.com.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import catchthepattern.com.models.PatternDef;

@Repository
public interface PatternDefRepository extends CrudRepository<PatternDef, Long> {

}




