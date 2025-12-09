package catchthepattern.com.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.common.util.concurrent.RateLimiter;

import catchthepattern.com.models.StockInfo;
import catchthepattern.com.models.User;
import catchthepattern.com.models.UserRole;
import catchthepattern.com.repositories.StockInfoRepository;
import catchthepattern.com.repositories.UserRepository;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
public class AlphaService {   
    
    private Authentication auth;
    private User activeUser;    
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private StockInfoRepository stockInfoRepository;
     
    private double PERMITS_PER_SECOND = 1.25; // 72 per minute
    private int PERMITS_REQUIRE = 1;
    
    private String ALPHA_URL = "https://www.alphavantage.co/query?function=";    
    private String LIST_URL = "https://www.alphavantage.co/query?function=LISTING_STATUS&apikey=";
    
    // Crypto
    // https://www.alphavantage.co/query?function=DIGITAL_CURRENCY_DAILY&symbol=ETH&market=USD&apikey=37SNAFOHXTCOO2E3&datatype=csv
   // private String URL_CRYPTO = "https://www.alphavantage.co/query?function=";
    private String DIGITAL_CURRENCY_DAILY = "DIGITAL_CURRENCY_DAILY";
    private String MARKET_CRYPTO = "&market=USD";
    // List of Coins: https://www.alphavantage.co/digital_currency_list/
    
    //Stock
    //private String DAILY_URL = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=";
    //private String STOCK_URL = "https://www.alphavantage.co/query?function=";
    private String TIME_SERIES_DAILY_ADJUSTED = "TIME_SERIES_DAILY_ADJUSTED";
    private String SIZE_FULL = "&outputsize=full";
    private String SIZE_COMPACT = "&outputsize=compact";   
    private String CSV = "&datatype=csv"; 
    
    // @Value("${alphavantage.api}")
    private String APIKEY = "ZYWW4DXQ8K38TMU8";
    
    final RateLimiter rateLimiter = RateLimiter.create(PERMITS_PER_SECOND); 
    
    private boolean isValidUser() {
        
        try {
          auth = SecurityContextHolder.getContext().getAuthentication();
          activeUser = userRepository.findByUsername(auth.getName());
          if (activeUser == null) return false;
          if (activeUser.isEnabled() && !activeUser.getLocked()) return true;
          return false;
         } catch (Error e) {
          return false;
         }         
    }
    
    // one instance, reuse
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();    
    
    public String getAllTickers() throws Exception {
        String url = LIST_URL + APIKEY;
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .setHeader("Source", "Alpha")
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
    
    public List<String> getAllTickersArr() throws Exception {
        String allTickersStr = getAllTickers();
        String[] allTickersArrOri =  allTickersStr.split("\\r?\\n");
        String[] allTickersArr = Arrays.copyOfRange(allTickersArrOri, 1, allTickersArrOri.length);
        List<String> tickers = new ArrayList<String>();   
        for (String str : allTickersArr ) {
            tickers.add(str.split(",")[0]);
        } 
        
       // System.out.print(tickers.size());        
 
        tickers = Utils.removeStringsContainingChar(tickers, '-');
        //System.out.print(tickers.size());
        tickers = Utils.filterShortStrings(tickers); // remove 5 letter tickers       
        //System.out.print(tickers.size());
        return tickers;        
    }
    
    public String getStock(String tickerTimeStr, boolean compact) throws Exception {
        
        String[] tickerTimeArr = tickerTimeStr.split("-");
        String timeSeries = tickerTimeArr.length > 1? tickerTimeArr[1] : TIME_SERIES_DAILY_ADJUSTED;    
        String ticker = tickerTimeArr[0];
        if (ticker.startsWith("*")) {
            ticker = Utils.decodeTicker(ticker);
        }        
        
        String dataSize = compact ? SIZE_COMPACT : SIZE_FULL;
        String url = ALPHA_URL + 
                timeSeries + 
                "&symbol=" + ticker + 
                dataSize + 
                "&apikey=" + APIKEY + 
                CSV;
        
       // System.out.println("getStock url: " + url);
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .setHeader("Source", "Alpha")
                .build();
        
        rateLimiter.acquire(PERMITS_REQUIRE);
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
    
    // https://www.alphavantage.co/query?function=SMA&symbol=IBM&interval=daily&time_period=10&series_type=open&apikey=demo
    public String getStockMa(String tickerTimeStr, String interval, String time_period) throws Exception {   
        
        String[] tickerTimeArr = tickerTimeStr.split("-");
       // String timeSeries = tickerTimeArr.length > 1? tickerTimeArr[1] : TIME_SERIES_DAILY_ADJUSTED;    
        String ticker = tickerTimeArr[0];
       
        if (ticker.startsWith("*")) {
            ticker = Utils.decodeTicker(ticker);
        }        
        
       
        String url = "https://www.alphavantage.co/query?function=SMA&symbol=" + 
                ticker + 
                "&interval=" + interval +
                "&time_period=" + time_period +
                "&series_type=close&apikey=" + APIKEY + 
                CSV;
        
       // System.out.println("getStock url: " + url);
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .setHeader("Source", "Alpha")
                .build();
        
        rateLimiter.acquire(PERMITS_REQUIRE);
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
    
    public String getCrypto(String tickerTimeStr) throws Exception {   
        
        String[] tickerTimeArr = tickerTimeStr.split("-");
        String timeSeries = tickerTimeArr.length > 1? tickerTimeArr[1] : DIGITAL_CURRENCY_DAILY;       
       
        String url = ALPHA_URL + 
                timeSeries + 
                "&symbol=" + tickerTimeArr[0] +
                MARKET_CRYPTO +
                "&apikey=" + APIKEY + 
                CSV;
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .setHeader("Source", "Alpha")
                .build();
        
        rateLimiter.acquire(PERMITS_REQUIRE);
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
    
    
    
    
    public String getEconimic(String ticker) throws Exception {  
        
        //https://www.alphavantage.co/query?function=REAL_GDP&interval=annual&apikey=demo
            
        Map ecnomics = new HashMap<String, String>();  
        ecnomics.put("GDP", "REAL_GDP&interval=annual");
        ecnomics.put("GDP_PER_CAPITA", "REAL_GDP_PER_CAPITA");
        ecnomics.put("TREASURY_YIELD_10_YEAR", "TREASURY_YIELD&interval=monthly&maturity=10year");
        ecnomics.put("FEDERAL_FUNDS_INTEREST_RATE_MONTHLY", "FEDERAL_FUNDS_RATE&interval=monthly");
        ecnomics.put("CPI", "CPI&interval=monthly");
        ecnomics.put("INFLATION", "INFLATION");
        ecnomics.put("INFLATION_EXPECTATION", "INFLATION_EXPECTATION");
        ecnomics.put("CONSUMER_SENTIMENT", "CONSUMER_SENTIMENT");
        ecnomics.put("RETAIL", "RETAIL_SALES");
        ecnomics.put("DURABLE_GOODS", "DURABLES");
        ecnomics.put("UNEMPLOYMENT", "UNEMPLOYMENT");
        ecnomics.put("NONFARM", "NONFARM_PAYROLL");        
       
        String url = ALPHA_URL + 
                ecnomics.get(ticker.trim()) + 
                "&apikey=" + APIKEY  + CSV;
       
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .setHeader("Source", "Alpha")
                .build();
        
        rateLimiter.acquire(PERMITS_REQUIRE);
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
    
    public String getCommodity(String ticker) throws Exception {  
        
        //https://www.alphavantage.co/query?function=ALL_COMMODITIES&interval=monthly&apikey=demo  
       
        String url = ALPHA_URL + 
                ticker + 
                "&interval=monthly" +
                "&apikey=" + APIKEY  + CSV;
        
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .setHeader("Source", "Alpha")
                .build();
        
        rateLimiter.acquire(PERMITS_REQUIRE);
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
    
    public StockInfo getStockInfo(String ticker) throws Exception {  
        
        // https://www.alphavantage.co/query?function=OVERVIEW&symbol=IBM&apikey=demo 
       
        String url = ALPHA_URL + 
                "OVERVIEW" +
                "&symbol=" +
                ticker + 
                "&apikey=" + APIKEY;
        
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .setHeader("Source", "Alpha")
                .build();
        
        rateLimiter.acquire(PERMITS_REQUIRE);
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        ObjectMapper objectMapper = new ObjectMapper();  
       // objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        StockInfo stockInfo = new StockInfo();
        try {
            stockInfo = objectMapper.readValue(response.body(), StockInfo.class);
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
       
        // System.out.println(stockInfo.toString());
        return stockInfo;
    }
    
    public void saveStockInfo(StockInfo si) {
        //deleteStockInfoBySymbol(si.getSymbol());
        StockInfo siInDb = stockInfoRepository.findBySymbol(si.getSymbol());
        if (siInDb != null) {
            siInDb.setSymbol(si.getSymbol());
            siInDb.setBeta(si.getBeta());
            siInDb.setCap(si.getCap());
            siInDb.setCountry(si.getCountry());
            siInDb.setCurrency(si.getCurrency());
            siInDb.setDesc(si.getDesc());
            siInDb.setDividendPerShare(si.getDividendPerShare());
            siInDb.setDividendYield(si.getDividendYield());
            siInDb.setEPS(si.getEPS());
            siInDb.setExchange(si.getExchange());
            siInDb.setIndustry(si.getIndustry());
            siInDb.setName(si.getName());
            siInDb.setPe(si.getPe());
            siInDb.setPeg(si.getPeg());
            siInDb.setSector(si.getSector());
            stockInfoRepository.save(siInDb);
        } else {
            if (si != null && si.getSymbol() != null) stockInfoRepository.save(si);
        }
    }
    
    public StockInfo getStockInfoFromDB(String ticker) {
        return stockInfoRepository.findBySymbol(ticker);
    }
    
    public List<StockInfo> getAllStockInfoFromDB() {
        return (List<StockInfo>) stockInfoRepository.findAll();
    }
    
    public List<String> getStockSectorsFromDB() {
        return stockInfoRepository.findDistinctSectors();
    }
    
    public List<String>  getStockIndustriesFromDB() {
        return stockInfoRepository.findDistinctIndustrys();
    }
    
    public List<String>  getStockCountriesFromDB() {
        return stockInfoRepository.findDistinctCountries();
    }
    
    public List<String>  getStockExchangesFromDB() {
        return stockInfoRepository.findDistinctExchanges();
    }
    
    // searches info in description and industry
    public List<StockInfo> getRelatedStockInfoFromDB(String info) {
        return stockInfoRepository.findStockInforsRelatedTo(info);
    }
    
    // searches info in description and industry
    public List<String> getRelatedTickersFromDB(String info) {        
        List<String> list = stockInfoRepository.findTickersRelatedTo(info);
        if (!isValidUser() || activeUser.getUserRole() != UserRole.PRO) {
            int count = 0;
            List<String> encryptedList = new ArrayList<String>();
            for (String ticker : list) {
                if (count > 9 ) encryptedList.add(Utils.encodeTicker(ticker));
                else encryptedList.add(ticker);
                count++;
            }
            return encryptedList;
        } else {
            return list;
        }
    }
    
    public List<String> getTickersInSector(String sector) {        
        List<String> list = stockInfoRepository.findTickersInSector(sector);
        if (!isValidUser() || activeUser.getUserRole() != UserRole.PRO) {
            int count = 0;
            List<String> encryptedList = new ArrayList<String>();
            for (String ticker : list) {
                if (count > 9 ) encryptedList.add(Utils.encodeTicker(ticker));
                else encryptedList.add(ticker);
                count++;
            }
            return encryptedList;
        } else {
            return list;
        } 
    }
    
    public List<String> getTickersInIndustry(String industry) {        
        List<String> list = stockInfoRepository.findTickersInIndustry(industry);
        if (!isValidUser() || activeUser.getUserRole() != UserRole.PRO) {
            int count = 0;
            List<String> encryptedList = new ArrayList<String>();
            for (String ticker : list) {
                if (count > 9 ) encryptedList.add(Utils.encodeTicker(ticker));
                else encryptedList.add(ticker);
                count++;
            }
            return encryptedList;
        } else {
            return list;
        } 
    }
    
    public void deleteStockInfoBySymbol(String symbol) {
        stockInfoRepository.deleteBySymbol(symbol);
    }

}
