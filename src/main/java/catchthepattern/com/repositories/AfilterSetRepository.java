package catchthepattern.com.repositories;

import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import catchthepattern.com.afilters.AfilterSet;

public interface AfilterSetRepository extends CrudRepository<AfilterSet, Long> {
    
    Set<AfilterSet> findByUserId(Long userId);
    
    @Query(value = "SELECT * FROM afiltersets ORDER BY closeAvg30  / occurrence DESC LIMIT 10", nativeQuery = true)
    Set<AfilterSet> findTop10();    
    
    @Query(value = "SELECT * FROM afiltersets WHERE occurrence > 100 AND (close_avg30 / occurrence) > 0.03 ORDER BY occurrence DESC", nativeQuery = true)
    Set<AfilterSet> findTopFilterSets();
    
}
