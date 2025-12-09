package catchthepattern.com.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import catchthepattern.com.models.StockData;

public interface StockDataRepository extends CrudRepository<StockData, Long> {
    
    @Query("SELECT t FROM StockData t WHERE t.ticker = ?1")
    StockData findByTicker(String ticker);
    
    @Query("SELECT ticker FROM StockData")
    List<String> findAllTickers();
    
    void deleteByTicker(String ticker);
    
    /*
     * @Modifying
     * 
     * @Query("DELETE FROM StockData s WHERE s.ticker=:ticker")
     * void deleteByTicker(@Param("ticker") String ticker);
     */
}
