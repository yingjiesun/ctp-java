package catchthepattern.com.repositories;

import java.util.Date;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import catchthepattern.com.models.TopRated;
import jakarta.transaction.Transactional;

@Repository
public interface TopRatedRepository extends CrudRepository<TopRated, Long> {

    TopRated findByTicker(String ticker);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM TopRated e WHERE e.foundDate < :cutoffDate")
    void deleteOlderThan(@Param("cutoffDate") Date cutoffDate);
}
