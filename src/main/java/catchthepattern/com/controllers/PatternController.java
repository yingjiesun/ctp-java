package catchthepattern.com.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import catchthepattern.com.afilters.TickerAfilterSet;
import catchthepattern.com.models.BestBearish;
import catchthepattern.com.models.BestBullish;
import catchthepattern.com.models.Breakout;
import catchthepattern.com.models.DoubleBottom;
import catchthepattern.com.models.Gainer;
import catchthepattern.com.models.Pattern;
import catchthepattern.com.models.StockInfo;
import catchthepattern.com.models.TickerFound;
import catchthepattern.com.models.TopRated;
import catchthepattern.com.models.VolSpike;
import catchthepattern.com.services.PatternService;
import catchthepattern.com.services.SearchPatternService;
import catchthepattern.com.services.StockDataService;

@CrossOrigin
@RestController
public class PatternController {

    @Autowired
    private PatternService patternService;
    
    @Autowired
    private StockDataService stockDataService;

    @Autowired
    private SearchPatternService searchPatternService;

    @PostMapping("/users/pattern/save")
    @ResponseBody
    public ResponseEntity<String> savePattern(@RequestBody Pattern pattern) {
        try {
            patternService.savePattern(pattern);
            return ResponseEntity.status(HttpStatus.OK).body("Success");
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Pattern Save Error");
        }  
    }

    @GetMapping("/users/patterns")
    @ResponseBody
    List<Pattern> getPatterns() {
        return patternService.getUserPatterns();
    }

    @GetMapping("/open/allPatterns")
    @ResponseBody
    List<Pattern> getAllPatterns() {
        return patternService.getAllPatterns();
    }

    @GetMapping("/users/pattern/{id}")
    @ResponseBody
    Optional<Pattern> getPatternById(@PathVariable long id) {
        return patternService.getPatternById(id);
    }

    @DeleteMapping("/users/patterns/delete/{id}")
    public ResponseEntity<String> deletePattern(@PathVariable int id) {
        try {
            patternService.deletePatternById(id);
            return ResponseEntity.status(HttpStatus.OK).body("Success");
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Pattern Delete Error");
        }        
    }   
    
    
    @GetMapping("/open/allGainers")
    @ResponseBody
    List<Gainer> getAllGainers() {
        return patternService.getAllGainers();
    }
    
    @GetMapping("/open/breakouts")
    @ResponseBody
    List<Breakout> getBreakouts() {
        return patternService.getBreakouts();
    }
    
    @GetMapping("/open/doubleBottoms")
    @ResponseBody
    List<DoubleBottom> getDoubleBottoms() {
        return patternService.getDoubleBottoms();
    }

    @PostMapping("/users/ticker/save")
    @ResponseBody
    public ResponseEntity<String> saveTicker(@RequestBody TickerFound tickerFound) {
        try {
            patternService.saveTicker(tickerFound);
            return ResponseEntity.status(HttpStatus.OK).body("Success");
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Pattern Ticker Save Error");
        }
    }

    @GetMapping("/open/allTickers")
    @ResponseBody
    List<TickerFound> getAllTickers() {
        return patternService.getAllTickers(false);
    } 
    
    @GetMapping("/open/sp500")
    String[] getSp500() {
        return patternService.getSp500();
    } 
    
    @GetMapping("/open/topRated")
    @ResponseBody
    List<TopRated> getTopRated() {
        return patternService.getTopRated(false);
    } 
    
    @GetMapping("/open/topRatedPerformance")
    @ResponseBody
    List<String> getTopRatedPerformance() {
    	List<String> performance = new ArrayList<String>();
    	
    	performance.add("Performance From " + patternService.getFirstTopRatedDate());
    	performance.add(patternService.getHighestPossibleUnrealizedGain());
    	performance.add(patternService.getHighestRealizedGain());
    	performance.add(patternService.getHighestUnrealizedGain());
    	performance.add(patternService.getAverageRealizedGain());
    	performance.add(patternService.getAverageUnRealizedGain());

    	performance.add("Strategy:\nStop-Gain: +23%, Trailing Stop-Loss: -13%");
    	// performance.add(patternService.getAverageNotSellingGain());
        return performance;
    } 
    
    @GetMapping("/open/topTrailResearch")
    @ResponseBody
    List<String> gettopTrailResearch() {
    	try {
			return patternService.getRealizedGains();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new ArrayList<String>();
		}
    } 
    
    @GetMapping("/open/bestBullish")
    @ResponseBody
    List<BestBullish> getBestBullish() {
        return patternService.getBestBullish(false);
    } 
    
    @GetMapping("/open/bestBearish")
    @ResponseBody
    List<BestBearish> getBestBearish() {
        return patternService.getBestBearish();
    } 
    
    @GetMapping("/open/volSpike")
    @ResponseBody
    List<VolSpike> getVolSpike() {
        return patternService.getVolSpike(false);
    } 
    
    @GetMapping("/user/tickers")
    @ResponseBody
    List<TickerFound> getUserTickers() {
        return patternService.getUserTickers();
    }     
    
    @GetMapping("/open/samplePatternTickers")
    @ResponseBody
    List<TickerFound> getSamplePatternTickers() {
        return patternService.getSampleUserTickers();
    } 
    
    @GetMapping("/open/relatedTickers/{str}")
    @ResponseBody
    List<String> getRelatedTickers(@PathVariable String str) {
        return stockDataService.getRelatedTickersFromDB(str);
    }
    
    @GetMapping("/open/relatedStockInfos/{str}")
    @ResponseBody
    List<StockInfo> getRelatedStockInfos(@PathVariable String str) {
        return stockDataService.getRelatedStockInfoFromDB(str);
    } 
}
