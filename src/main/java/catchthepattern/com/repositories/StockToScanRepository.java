package catchthepattern.com.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import catchthepattern.com.models.StockToScan;

@Repository
public interface StockToScanRepository extends CrudRepository<StockToScan, Long>  {

}
