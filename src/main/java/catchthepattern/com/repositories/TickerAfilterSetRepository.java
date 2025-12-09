package catchthepattern.com.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import catchthepattern.com.afilters.TickerAfilterSet;
import jakarta.transaction.Transactional;

public interface TickerAfilterSetRepository extends CrudRepository<TickerAfilterSet, Long> {
    
    @Query("SELECT t FROM tickerAfiltersets t WHERE t.ticker = ?1")
    TickerAfilterSet findByTicker(String ticker);
    
    @Modifying
    @Transactional
    @Query(value = "delete from ticker_afilterset t where t.afilterset_id = :id", nativeQuery = true)
    void deleteTickerByAfilterSetId(@Param("id") long id);
}
