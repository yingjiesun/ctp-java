package catchthepattern.com.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import catchthepattern.com.models.StockData;
import catchthepattern.com.models.StockInfo;
import catchthepattern.com.repositories.StockDataRepository;
import catchthepattern.com.repositories.StockInfoRepository;

@Service
public class StockDataService {
    
    @Autowired
    StockDataRepository stockDataRepository;
    @Autowired
    StockInfoRepository stockInfoRepository;
    
    public StockDataService() {}

    public StockData getStockData(String ticker) {
        return stockDataRepository.findByTicker(ticker);
    }
    
    public void saveStockData(StockData sd) {
        StockData sd_db = getStockData(sd.getTicker());
        if (sd_db != null) {
            sd_db.setDayRecords(sd.getDayRecords());
            stockDataRepository.save(sd_db);
        } else {
            stockDataRepository.save(sd);
        }
    }
    
    public void deleteStockDataByTicker(String ticker) {
        stockDataRepository.deleteByTicker(ticker);
    }
    
    public List<String> getAllTickersFromDB() {
        return stockDataRepository.findAllTickers();
    }
    
    public List<String> getRelatedTickersFromDB(String str) {
        return stockInfoRepository.findTickersRelatedTo(str);
    }
    
    public List<StockInfo> getRelatedStockInfoFromDB(String str) {
        return stockInfoRepository.findStockInforsRelatedTo(str);
    }
    
    
    /*
     * public List<StockData> getAllStockData() {
     * return stockDataRepository.findAll();
     * }
     */
}
