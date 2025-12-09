package catchthepattern.com.repositories;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import catchthepattern.com.models.Pattern;
import catchthepattern.com.models.TickerFound;

@Repository
public interface PatternRepository extends CrudRepository<Pattern, Long> {
	
	Set<Pattern> findByUserId(Long userId);
	
    @Query(value = "SELECT * FROM patterns ORDER BY closeAvg30  / occurrence DESC LIMIT 10", nativeQuery = true)
    Set<Pattern> findTop10();
    
    @Query(value = "SELECT * FROM patterns WHERE occurrence > 100 AND (close_avg30 / occurrence) > 0.03 ORDER BY occurrence DESC", nativeQuery = true)
    Set<Pattern> findTopPatterns();
	
}