package catchthepattern.com.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import catchthepattern.com.models.StockInfo;
import jakarta.transaction.Transactional;

@Repository
public interface StockInfoRepository extends CrudRepository<StockInfo, Long> {

    @Query("SELECT t FROM stockInfo t WHERE t.symbol = ?1")
    StockInfo findBySymbol(String ticker);     
    
    void deleteBySymbol(String symbol);
    
    @Query("SELECT DISTINCT sector FROM stockInfo")
    List<String> findDistinctSectors();
    
    @Query("SELECT DISTINCT industry FROM stockInfo")
    List<String> findDistinctIndustrys();
    
    @Query("SELECT DISTINCT country FROM stockInfo")
    List<String> findDistinctCountries();
    
    @Query("SELECT DISTINCT exchange FROM stockInfo")
    List<String> findDistinctExchanges();
    
    @Query("SELECT s FROM stockInfo s WHERE description LIKE %:str% OR industry LIKE %:str%")
    List<StockInfo> findStockInforsRelatedTo(String str);
    
    @Query("SELECT s.symbol FROM stockInfo s WHERE  s.description LIKE %:str% OR s.industry LIKE %:str%")
    List<String> findTickersRelatedTo(String str);
    
    @Query("SELECT s.symbol FROM stockInfo s WHERE s.sector = :str")
    List<String> findTickersInSector(String str);
    
    @Query("SELECT s.symbol FROM stockInfo s WHERE s.industry = :str")
    List<String> findTickersInIndustry(String str);
   
}
