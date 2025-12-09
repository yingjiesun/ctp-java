package catchthepattern.com.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import catchthepattern.com.models.DayRecord;

@Service
public class InMemoryService {
    
    public Map<String, DayRecord[]> stockDataMap; // for FE/BE search, check this map first
    public Map<String, DayRecord[]> stockDataMapSCH; // for scheduled updates, when updates are done, assign it to stockDataMap

    public InMemoryService(Map<String, DayRecord[]> stockDataMap) {
        this.stockDataMap = stockDataMap;
    }
    
    public InMemoryService() {
        stockDataMap = new HashMap<String, DayRecord[]>();
        stockDataMapSCH  = new HashMap<String, DayRecord[]>();
    }

    public Map<String, DayRecord[]> getStockDataMap() {
        return stockDataMap;
    }

    public void setStockDataMap(Map<String, DayRecord[]> stockDataMap) {
        this.stockDataMap = stockDataMap;
    }

    public Map<String, DayRecord[]> getStockDataMapSCH() {
        return stockDataMapSCH;
    }

    public void setStockDataMapSCH(Map<String, DayRecord[]> stockDataMapSCH) {
        this.stockDataMapSCH = stockDataMapSCH;
    }
    
    
    
}
