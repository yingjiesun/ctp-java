package catchthepattern.com.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import catchthepattern.com.models.DayRecord;
import catchthepattern.com.models.Pattern;
import catchthepattern.com.models.PatternDef;
import catchthepattern.com.models.TickerFound;

@Service
public class SearchPatternService {
    
    private double MIN_SCORE = 11.0; 
    
    @Autowired
    private AlphaService alphaService;
    
    @Autowired
    private DataFormatService dataFormatService;
    
    @Autowired
    private FilterService filterService;
    
    @Autowired
    private Utils utils;
    
    // Search one stock multiple patterns
    public TickerFound findPatternsAndCalcPerformance(String ticker, DayRecord[] drs, List<Pattern> patterns) throws Exception {
        
        //System.out.println("findPatternsAndCalcPerformance called: " + ticker);
        
        TickerFound tf = new TickerFound();
        Set<Pattern> patternsFound = new HashSet<Pattern>(); 
        
        if (drs == null || drs.length < 10) return null;
        
        tf.setBigStock(filterService.isBigStock(drs));
        
        for (int i=0; i< patterns.size(); i++) { 
            if (findPattern(drs, patterns.get(i))) patternsFound.add(patterns.get(i)); 
        } 
        
        if ( patternsFound.size() > 0 ) {
            tf.setTicker(ticker);
            tf.setPatterns( patternsFound);
            return tf;
        }
        return null;
    }     
    
    // returns true if found today
    
    
    
    private boolean findPattern(DayRecord[] drs, Pattern p) {
        // update p performance, occrs
        // if occr todays return true
        boolean isRecent = false;
        List<Integer> foundDays = new ArrayList<Integer>();
        int patternBars = p.getDef().getOhlc().length;        
        
        for (int i=0; i< drs.length - patternBars; i++) {  
            
            if (!filterService.passAllFilters(drs, p.getFilterset(), i)) continue;
            
            // System.out.println("Pattern Pass all filters: " + drs[i].getDate() + " " + filterService.passAllFilters(drs, p.getFilterset(), i));
            
            DayRecord[] slice = Arrays.copyOfRange(drs, i, i + patternBars);                    
                   
            if (this.utils.containsNull(slice) || slice.length != patternBars) {
                Utils.printFirst5Rows(slice);
                System.out.println("SearchPatternService: findPattrn: Skip rest of days, slice after this date is null or contains null: " + drs[i].getDate());
                break; // in case of errors at end of data
            }
            
            PatternDef potential = getPatternDefFromSlice(slice);
            PatternDef normalizedPotential = getNormalizedPatternDef(potential);            
            double score = getPatternScore(p.getDef(), normalizedPotential);
            
            if (score < MIN_SCORE) {
                foundDays.add(i);
                if (i == 0 ) {
                    isRecent = true;
                }                
            }             
        }
        updatePatternPerformance(drs, p, foundDays);

        return isRecent;
    }
    
    
    private void updatePatternPerformance(DayRecord[] drs, Pattern p, List<Integer> foundDays) {
        // TODO: update p directly
        // foundDays[i] is the last day of pattern, days after i is (i-1) to (i-10)
        // pattern is from (i + pattern.bars - 1) to i inclusive
       // System.out.println("==================updatePatternPerformance"); 
      //  System.out.println(p.getName());
       // System.out.println("Before one stock");         
       // System.out.println("Occurance: " + p.getOccurrence());
        
        for ( int i : foundDays) {
            if ( i > 30 ) { // so there are enough days after pattern
               // double patternCloseAvg = getPatternAvgClose(drs, i, p.getDef().getOhlc().length); // pattern avg close
                double patternCloseAvg = drs[i].getOhlc()[3]; // pattern last day close
               
                //double avgClose10 = getDaysAvgClosePercentageAfter(drs, i, 10, patternCloseAvg);
                //double avgClose30 = getDaysAvgClosePercentageAfter(drs, i, 30, patternCloseAvg);
                final double high10 = getDaysHighestPercentageAfter(drs, i, 10, patternCloseAvg);
                final double high30 = getDaysHighestPercentageAfter(drs, i, 30, patternCloseAvg);
                final double low10 = getDaysLowestPercentageAfter(drs, i, 10, patternCloseAvg);
                final double low30 = getDaysLowestPercentageAfter(drs, i, 30, patternCloseAvg);
                
                //System.out.println("high10: " + high10);
                //System.out.println("low10: " + low10);
                
               // System.out.println(patternCloseAvg);
                if (patternCloseAvg != 0 && Math.abs(high30) < 3.0 && Math.abs(low30) > -0.67) {
                    p.setOccurrence(p.getOccurrence() + 1);
                    p.setCloseAvg10(p.getCloseAvg10() + getDaysAvgClosePercentageAfter(drs, i, 10, patternCloseAvg));
                    p.setCloseAvg30(p.getCloseAvg30() + getDaysAvgClosePercentageAfter(drs, i, 30, patternCloseAvg));
                    p.setHighest10(p.getHighest10() + high10);
                    p.setHighest30(p.getHighest30() + high30);
                    p.setLowest10(p.getLowest10() + getDaysLowestPercentageAfter(drs, i, 10, patternCloseAvg));
                    p.setLowest30(p.getLowest30() + low30);
                    if (high30 > 0.1) {
                        // System.out.println("high10: " + high10);
                        p.setNum10PercentRiseIn30Days(p.getNum10PercentRiseIn30Days() + 1);
                    }
                    if (high30 > 0.2) p.setNum10PercentLoseIn30Days(p.getNum10PercentLoseIn30Days() + 1);
                }                
            }
        }  
      //  System.out.println("After one stock:");
      //  System.out.println("Occurance: " + p.getOccurrence()); 
    }
      
    public double getPatternAvgClose(DayRecord[] drs, int lastPatternDate, int patternBars) {       
        if (patternBars == 0) return -1;
        double sum = 0.0;
        for (int i = lastPatternDate + patternBars -1; i >= lastPatternDate; i-- ) {
            sum += drs[i].getOhlc()[3];
        }
        return sum/patternBars;
    }
    
    public double getDaysAvgClosePercentageAfter(DayRecord[] drs, int startingDate, int days, double pAvg) {
        if (startingDate < days) return -1;       
        double avg = 0.0;
        int count = 0;
        for (int i = startingDate-1; i > startingDate - days; i--) {
            avg += drs[i].getOhlc()[3];
            count++;
        }
        return (avg/count - pAvg)/pAvg;
    }
    
    public double getDaysHighestPercentageAfter(DayRecord[] drs, int startDate, int days, double pAvg) {
        if (startDate < days) return -1;
        double highest = 0.0;
        for (int i = startDate - 1; i >= startDate - days; i--) {
            if (drs[i].getOhlc()[1] > highest) highest = drs[i].getOhlc()[1];
        }
        return (highest - pAvg)/pAvg;
    }
    
    public double getDaysLowestPercentageAfter(DayRecord[] drs, int startDate, int days, double pAvg) {
        if (startDate < days) return -1;
        double lowest = Double.MAX_VALUE;
        for (int i = startDate-1; i >= startDate - days; i--) {
            if (drs[i].getOhlc()[2] < lowest) lowest = drs[i].getOhlc()[2];
        }
     //   System.out.println("Lowest percentage: " + (lowest - drs[startDate].getOhlc()[2])/drs[startDate].getOhlc()[2]);
        return (lowest - pAvg)/pAvg;
    }
    
    public double getDaysPercentageChange(DayRecord[] drs, int startDate, int days) {
        if (startDate > days) return 0.0;
        if (drs.length < startDate + days + 1) return 0.0;
        double close  = drs[startDate + days].getOhlc()[3];
        if (Utils.isDoubleZero(close, 1e-6)) return 0.0;        
        return (drs[startDate].getOhlc()[3] - close)/close;
    }
    
    private PatternDef getPatternDefFromSlice(DayRecord[] drs) {
        
        // System.out.println("=========getPatternDefFromSlice called ");
        
        if (drs == null) {
            System.out.println("SearchPatternService ERROR: getPatternDefFromSlice received null Dayrecord");
            return null;
        }
        
      //  Utils.printFirst5Rows(drs);
        
        Utils.reverseArray(drs);
        
        PatternDef newPdf = new PatternDef();
        double[][] ohlc = new double[drs.length][4];
        double[] vols = new double[drs.length];
        for (int i=0; i< drs.length; i++ ) {
            ohlc[i] = drs[i].getOhlc();
            vols[i] = drs[i].getVol();
        }
        newPdf.setOhlc(ohlc);
        newPdf.setVols(vols);
        newPdf.setHighLow(true); // not used when calculating score
        newPdf.setVol(true);     // not used when calculating score
        return newPdf;
    }
    
    public PatternDef getNormalizedPatternDef(PatternDef pdf) {
        
       // System.out.println("=========getNormalizedPatternDef called ");
        
        PatternDef newPdf = new PatternDef();
        double highest = Utils.getLargest(pdf.getOhlc());
        double lowest = Utils.getSmallest(pdf.getOhlc());
        double hVol = Utils.getLargest(pdf.getVols());       
        double[][] ohlc = pdf.getOhlc();
        double[] vols = pdf.getVols();        
        double[][] newOhlc = new double[ohlc.length][4];
        double[] newVols = new double[vols.length];        
        // X = (X - Lowest)*100/(Highest - Lowest)
        for (int i=0; i< ohlc.length; i++ ) {
            for (int j = 0; j < ohlc[i].length; j++) {
                newOhlc[i][j] = (ohlc[i][j] - lowest) * 100 / (highest-lowest);
            }  
            newVols[i] = vols[i] * 100 / hVol ;
        }        
        newPdf.setOhlc(newOhlc);
        newPdf.setVols(newVols);
        newPdf.setHighLow(pdf.isHighLow()); 
        newPdf.setVol(pdf.isVol());       
        return newPdf;
    }
    
    public double getPatternScore(PatternDef pattern, PatternDef potential) {        
       // System.out.println("=========getPatternScore called ");    
        
        double score = 0.0;
        double factor = 5.0; // h && v
        if (pattern.isHighLow() && !pattern.isVol()) factor = 4.0;
        if (!pattern.isHighLow() && pattern.isVol()) factor = 3.0;
        if (!pattern.isHighLow() && !pattern.isVol()) factor = 2.0;        
        
        switch (pattern.getOhlc().length) {
            case 2: 
                factor = factor * 1.6;
                break;
            case 3: 
                factor = factor * 2.8;
                break;
            case 4: 
                factor = factor * 4.0;
                break;
            case 5: 
                factor = factor * 5.2;
                break;
            case 6: 
                factor = factor * 6.4;
                break;
            case 7: 
                factor = factor * 7.5;
                break;           
            default:
                factor = factor * pattern.getOhlc().length;
        }
       
        
        if (pattern.isOpenClose() || true) {
            for (int i=0; i < pattern.getOhlc().length; i++) {
                score += Math.abs(pattern.getOhlc()[i][0] - potential.getOhlc()[i][0]); // open
                score += Math.abs(pattern.getOhlc()[i][3] - potential.getOhlc()[i][3]); // close                
            }
        }
        
        if (pattern.isHighLow()) {
            for (int i=0; i < pattern.getOhlc().length; i++) {
                score += Math.abs(pattern.getOhlc()[i][1] - potential.getOhlc()[i][1]); // high
                score += Math.abs(pattern.getOhlc()[i][2] - potential.getOhlc()[i][2]); // low
            }
        }
        
        if (pattern.isVol()) {
            for (int i=0; i < pattern.getVols().length; i++) {
                score += Math.abs(pattern.getVols()[i] - potential.getVols()[i]); 
            }
        }
        //return score;
        return score/factor;
    }

}
