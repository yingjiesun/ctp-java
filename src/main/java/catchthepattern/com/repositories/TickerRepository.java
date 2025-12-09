package catchthepattern.com.repositories;

import java.util.Set;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import catchthepattern.com.models.TickerFound;
import jakarta.transaction.Transactional;

@Repository
public interface TickerRepository extends CrudRepository<TickerFound, Long> {
    
    @Query("SELECT t FROM tickers t WHERE t.ticker = ?1")
    TickerFound findByTicker(String ticker);
    
    @Modifying
    @Transactional
    @Query(value = "delete from ticker_pattern t where t.pattern_id = :id", nativeQuery = true)
    void deleteTickerByPatternId(@Param("id") long id);
   
}