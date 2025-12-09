package catchthepattern.com.controllers;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import catchthepattern.com.models.BestBullish;
import catchthepattern.com.models.DayRecord;
import catchthepattern.com.models.StockData;
import catchthepattern.com.models.StockInfo;
import catchthepattern.com.models.TickerFound;
import catchthepattern.com.models.UserRole;
import catchthepattern.com.schedulers.StockScanTask;
import catchthepattern.com.services.AlphaService;
import catchthepattern.com.services.DataFormatService;
import catchthepattern.com.services.InMemoryService;
import catchthepattern.com.services.StockDataService;
import catchthepattern.com.services.Utils;

@CrossOrigin
@RestController
public class AlphaController {  
    
    @Autowired
    private AlphaService alphaService;  
    
    @Autowired
    StockDataService stockDataService;
    
    @Autowired
    private DataFormatService dataFormatService; 
    
    @Autowired
    private InMemoryService inMemoryService; 
    
    @Autowired
    private StockScanTask stockScanTask;
    
    @GetMapping(value = "/alpha/compact/{ticker}") 
    public String getStock(@PathVariable("ticker") String ticker) throws Exception {
        ticker = Utils.sanitize(ticker);
        return this.alphaService.getStock(ticker, true);
    }
    
    @GetMapping(value = "/alpha/ma10/{ticker}") 
    public String getMa10(@PathVariable("ticker") String ticker) throws Exception {
        return this.alphaService.getStockMa(ticker, "daily", "10");
    }
    
    @GetMapping(value = "/alpha/ma50/{ticker}") 
    public String getMa50(@PathVariable("ticker") String ticker) throws Exception {
        return this.alphaService.getStockMa(ticker, "daily", "50");
    }
    
    @GetMapping(value = "/alpha/ma250/{ticker}") 
    public String getMa250(@PathVariable("ticker") String ticker) throws Exception {
        return this.alphaService.getStockMa(ticker, "daily", "250");
    }    
    
    @GetMapping(value = "/pro/alpha/compact/{ticker}") 
    public String getStockPro(@PathVariable("ticker") String ticker) throws Exception {
       // Collection<? extends GrantedAuthority> auth = SecurityContextHolder.getContext().getAuthentication().getAuthorities();       
        return this.alphaService.getStock(ticker, true);
    }
    
    @GetMapping(value = "/alpha/full/{ticker}", produces = "text/csv") 
    String getFullStock(@PathVariable("ticker") String ticker) throws Exception {     
      return alphaService.getStock(ticker, false);
    }
    
    @GetMapping(value = "/alpha/arr/{ticker}") 
    DayRecord[] get5YeaStockArr(@PathVariable("ticker") String ticker) throws Exception {  
        
        ticker = Utils.sanitize(ticker);   
        
        /* DO NOT USE DB anymore
        
        String ticker_db = ticker; // for DB access, remove * DAILY etc
        
        if (ticker_db.startsWith("*")) {
            ticker_db = Utils.decodeTicker(ticker_db);
        }
        if (ticker_db.contains("-") && ticker_db.contains("DAILY")) {
            ticker_db = ticker_db.split("-")[0];
        } 
        
        StockData sd = stockDataService.getStockData(ticker_db);
        
        boolean dataInDBisCurrent = false;
        
        if (sd != null && sd.getDayRecords() != null && sd.getDayRecords().length > 0) {            
            try {
                LocalDate stockLastDay = LocalDate.parse(sd.getDayRecords()[0].getDate());
                LocalDate yesterday = LocalDate.now().minus(Period.ofDays(1));
                
                if (stockLastDay.isBefore(yesterday)) {
                    dataInDBisCurrent = false;                    
                } else if( stockLastDay.isEqual(yesterday)) {
                    ZoneId centralZone = ZoneId.of("America/Chicago");
                    ZonedDateTime now = ZonedDateTime.now(centralZone);
                    ZonedDateTime fivePM = now.withHour(17).withMinute(0).withSecond(0).withNano(0);
                    if (now.isBefore(fivePM)) {
                        dataInDBisCurrent = true;
                    } else {
                        dataInDBisCurrent = false;  
                    }
                }else {
                    dataInDBisCurrent = true;
                }
                
            } catch(Exception e) {
                System.out.println("EXCEPTION : DataFormatService - getDayRecordsFromRawArrs()");
                System.out.println(e);
            }
            
            if (dataInDBisCurrent) {
                // System.out.println("/alpha/arr/{ticker} get5YeaStockArr called, return data from DB");
                return sd.getDayRecords();
            }
        } 
        
        */
        // not found in DB or dataInDBisCurrent = false
        // get stock data from http and save/update to DB        
        
        DayRecord[] drs = dataFormatService.getStockWithMa(ticker);
        
        
        /*
        if (sd != null ) { // update
           // System.out.println("/alpha/arr/{ticker} get5YeaStockArr called, data found in DB but too old, updating");
            sd.setDayRecords(drs);
            stockDataService.saveStockData(sd);
        } else { // save new
          //  System.out.println("/alpha/arr/{ticker} get5YeaStockArr called, data NOT found in DB, saving as new");
            StockData thisSD = new StockData(ticker_db, drs);
            stockDataService.saveStockData(thisSD);
        }
        
        */
        
      //  System.out.println("/alpha/arr/{ticker} get5YeaStockArr called, return data from alpha http");
        
        return drs;        
      
    }   
    
    @GetMapping(value = "/alpha/stockInfo/{ticker}") 
    public StockInfo getStockInfo(@PathVariable("ticker") String ticker) throws Exception {
        ticker = Utils.sanitize(ticker);
        StockInfo si = alphaService.getStockInfoFromDB(ticker);
        if (si == null) {
            si = alphaService.getStockInfo(ticker);
            if (si != null) alphaService.saveStockInfo(si);
        }        
        return si;   
    }
    
    @GetMapping(value = "/alpha/stockInfo/sectors") 
    public List<String> getSectors() throws Exception {
        return alphaService.getStockSectorsFromDB();
    }
    
    @GetMapping(value = "/alpha/stockInfo/industries") 
    public List<String> getIndustries() throws Exception {
        return alphaService.getStockIndustriesFromDB();
    }
    
    @GetMapping(value = "/alpha/stockInfo/all") 
    public List<StockInfo> getAllStockInfo() throws Exception {
        return alphaService.getAllStockInfoFromDB();
    }
    
    @GetMapping(value = "/alpha/stockInfo/countries") 
    public List<String> getCountries() throws Exception {
        return alphaService.getStockCountriesFromDB();
    }
    
    @GetMapping(value = "/alpha/stockInfo/exchanges") 
    public List<String> getExchanges() throws Exception {
        return alphaService.getStockExchangesFromDB();
    }
    
    @GetMapping(value = "/alpha/stockInfo/search/{info}") 
    public List<StockInfo> getRelatedStockInfo(@PathVariable("info") String info) throws Exception {
        info = Utils.sanitize(info);
        return alphaService.getRelatedStockInfoFromDB(info);
    }
    
    @GetMapping(value = "/alpha/stockInfo/sector/{sector}") 
    public List<String> getTickersInSector(@PathVariable("sector") String sector) throws Exception {
        sector = Utils.sanitize(sector);
        return alphaService.getTickersInSector(sector);
    }
    
    @GetMapping(value = "/alpha/stockInfo/industry/{industry}") 
    public List<String> getTickersInIndustry(@PathVariable("industry") String industry) throws Exception {
        industry = Utils.sanitize(industry);
        return alphaService.getTickersInIndustry(industry);
    }
    
    @GetMapping(value = "/alpha/ticker/search/{info}") 
    public List<String> getRelatedTickers(@PathVariable("info") String info) throws Exception {
        info = Utils.sanitize(info);
        return alphaService.getRelatedTickersFromDB(info);
    }
    
    @GetMapping(value = "/alpha/crypto/{ticker}") 
    public String getCrypto(@PathVariable("ticker") String ticker) throws Exception {
        ticker = Utils.sanitize(ticker);
        return this.alphaService.getCrypto(ticker);
    }
    
    @GetMapping(value = "/alpha/economic/{ticker}") 
    public String getEconimic(@PathVariable("ticker") String ticker) throws Exception {
        return this.alphaService.getEconimic(ticker);
    }
    
    @GetMapping(value = "/alpha/commodity/{ticker}") 
    public String getCommodity(@PathVariable("ticker") String ticker) throws Exception {
        return this.alphaService.getCommodity(ticker);
    }
    
    @GetMapping(value = "/alpha/conv/{ticker}") 
    public String getDecodedTicker(@PathVariable("ticker") String ticker) throws Exception {
        if (ticker.startsWith("*")) {
            ticker = Utils.decodeTicker(ticker);
        }  
        return ticker;        
    }
    
    @PostMapping(value = "/alpha/triggerScan/{date}")
    public void triggerManually(@PathVariable("date") String date) {
    	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate inputDate = LocalDate.parse(date, formatter);
        if (inputDate.equals(LocalDate.now())) {
	    	try {
				stockScanTask.scanStocks_schedule();
				System.out.println("Triggered stockScan async");
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
    }

}
