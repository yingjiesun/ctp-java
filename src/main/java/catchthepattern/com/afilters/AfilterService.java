package catchthepattern.com.afilters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import catchthepattern.com.models.DayRecord;
import catchthepattern.com.models.Pattern;
import catchthepattern.com.models.TickerFound;
import catchthepattern.com.models.User;
import catchthepattern.com.models.UserRole;
import catchthepattern.com.repositories.AfilterSetRepository;
import catchthepattern.com.repositories.FilterARepository;
import catchthepattern.com.repositories.FilterBRepository;
import catchthepattern.com.repositories.TickerAfilterSetRepository;
import catchthepattern.com.repositories.UserRepository;
import catchthepattern.com.services.FilterService;
import catchthepattern.com.services.SearchPatternService;
import catchthepattern.com.services.Utils;

@Service
public class AfilterService {
    
 // used to skip patterns or filtersets that are happening too often or too few
    private final double MIN_VALID = 0.003;
    private final double MAX_VALID = 40;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AfilterSetRepository afilterSetRepository;
    
    @Autowired
    SearchPatternService searchPatternService;
    
    @Autowired
    private FilterService filterService;

    @Autowired
    private FilterARepository filterARepository;

    @Autowired
    private FilterBRepository filterBRepository;

    @Autowired
    private TickerAfilterSetRepository tickerAfilterSetRepository;

    private Authentication auth;
    private User activeUser;

    private boolean isValidUser() {

        // logger.debug("isValidUser() called");

        try {
            auth = SecurityContextHolder.getContext().getAuthentication();
            // System.out.println(auth);
            activeUser = userRepository.findByUsername(auth.getName());
            if (activeUser == null)
                return false;
            if (activeUser.isEnabled() && !activeUser.getLocked())
                return true;
            return false;
        } catch (Error e) {
            // logger.error("NO ACTIVE USER");
            return false;
        }

    }
    
    private boolean isValidUser(User user) {        
        try {         
          if (user == null) return false;
          if (user.isEnabled() && !user.getLocked()) return true;
          return false;
         } catch (Error e) {
          //logger.error("NO ACTIVE USER");
          return false;
         }         
    }
    
    public boolean passAfilterSet(AfilterSet f,  DayRecord[] drs, int startDate) {       
        
      //  System.out.println(drs);
      //  System.out.println(startDate);
        
        for (FilterA fa: f.getFilterAs()) {
            if (!passFilterA(fa, drs, startDate)) return false;
        }
        
        for (FilterB fb: f.getFilterBs()) {
            if (!passFilterB(fb, drs, startDate)) return false;
        }
        
      //  System.out.println("FilterB passed, both passed");
      //  System.out.println("PASS AFILTERSE!, true date: " + startDate);
     //   System.out.println("PASS AFILTERSE!, name: " + f.getName());
        return true;
    }

    public boolean passFilterA(FilterA f, DayRecord[] drs, int startDate) {
       // System.out.println("passFilterA f.isInclude(): " + f.isInclude());
        if (startDate + 6 > drs.length) return false;
        if (f.coef == 0) return false;
        double var1 = getValueByFilterVar(f.getVar1(), drs, startDate);
        double var2 = getValueByFilterVar(f.getVar2(), drs, startDate);
        

        
        if (var1 == 0 || var2 == 0) return false;
        if (f.isInclude()) {
            return varCompareResult(f.op, var1, var2, f.coef);
        } else {
            return !varCompareResult(f.op, var1, var2, f.coef);
        }
    }
    
    public boolean passFilterB(FilterB f, DayRecord[] drs, int startDate) {
        
      //  System.out.println("passFilterB f.isInclude(): " + f.isInclude());
        if (startDate + 6 > drs.length) return false;
        if (f.coef == 0) return false;
        double var1 = getValueByFilterVar(f.getVar1(), drs, startDate);
        double var2 = f.getVar2();
        if (var1 == 0 || var2 == 0) return false;
        if (f.isInclude()) {
            return varCompareResult(f.op, var1, var2, f.coef);
        } else {
            return !varCompareResult(f.op, var1, var2, f.coef);
        }
    }
    
    public boolean varCompareResult(EnumOp op, double var1, double var2, double coef) {
        /*
         * System.out.println("var1:" + var1);
         * System.out.println("var2:" + var2);
         * System.out.println("op:" + op);
         */
        
        if (var1 == -1 || var2 == -1) return false;
        switch (op) {
            case IS_GREATER_THAN:
                return var1 > var2 * coef;
            case IS_SMALLER_THAN:
                return var1 < var2 * coef;
            case IS_WITHIN_HALF_PERCENT_OF:
                return Math.abs((var1 - var2 * coef) / (var2 * coef)) < 0.005;
            case IS_WITHIN_1_PERCENT_OF:
                return Math.abs((var1 - var2 * coef) / (var2 * coef)) < 0.01;
            case IS_WITHIN_2_PERCENT_OF:
                return Math.abs((var1 - var2 * coef) / (var2 * coef)) < 0.02;
            case IS_WITHIN_3_PERCENT_OF:
                return Math.abs((var1 - var2 * coef) / (var2 * coef)) < 0.03;
            case IS_WITHIN_4_PERCENT_OF:
                return Math.abs((var1 - var2 * coef) / (var2 * coef)) < 0.04;
            case IS_WITHIN_5_PERCENT_OF:
                return Math.abs((var1 - var2 * coef) / (var2 * coef)) < 0.05;
            case IS_WITHIN_6_PERCENT_OF:
                return Math.abs((var1 - var2 * coef) / (var2 * coef)) < 0.06;
            case IS_WITHIN_10_PERCENT_OF:
                return Math.abs((var1 - var2 * coef) / (var2 * coef)) < 0.1;
            case IS_WITHIN_15_PERCENT_OF:
                return Math.abs((var1 - var2 * coef) / (var2 * coef)) < 0.15;
            case IS_WITHIN_20_PERCENT_OF:
                return Math.abs((var1 - var2 * coef) / (var2 * coef)) < 0.2;
            default:
                return false;
        }        
    }

    public double getValueByFilterVar(EnumFilterVar fVar, DayRecord[] drs, int startDate) {
        /*
         * System.out.println("drs[0]:" + drs[0].getDate());
         * System.out.println("drs[startDate]:" + drs[startDate].getDate());
         * System.out.println("drs[startDate + 1]:" + drs[startDate + 1].getDate());
         */
        switch (fVar) {
            case DAY0_O:
                return drs[startDate].getOhlc()[0];
            case DAY0_H:
                return drs[startDate].getOhlc()[1];
            case DAY0_L:
                return drs[startDate].getOhlc()[2];
            case DAY0_C:
                return drs[startDate].getOhlc()[3];
            case DAY0_V:
                return drs[startDate].getVol();
            case DAY0_T:
                return drs[startDate].getTop();
            case DAY0_B:
                return drs[startDate].getBottom();
            case DAY0_M:
                return drs[startDate].getMiddle();
            case DAY0_MA10:
                return drs[startDate].getMa10();
            case DAY0_MA50:
                return drs[startDate].getMa50();
            case DAY0_MA250:
                return drs[startDate].getMa250();

            case DAY1_O:
                return drs[startDate + 1].getOhlc()[0];
            case DAY1_H:
                return drs[startDate + 1].getOhlc()[1];
            case DAY1_L:
                return drs[startDate + 1].getOhlc()[2];
            case DAY1_C:
                return drs[startDate + 1].getOhlc()[3];
            case DAY1_V:
                return drs[startDate + 1].getVol();
            case DAY1_T:
                return drs[startDate + 1].getTop();
            case DAY1_B:
                return drs[startDate + 1].getBottom();
            case DAY1_M:
                return drs[startDate + 1].getMiddle();
            case DAY1_MA10:
                return drs[startDate + 1].getMa10();
            case DAY1_MA50:
                return drs[startDate + 1].getMa50();
            case DAY1_MA250:
                return drs[startDate + 1].getMa250();

            case DAY2_O:
                return drs[startDate + 2].getOhlc()[0];
            case DAY2_H:
                return drs[startDate + 2].getOhlc()[1];
            case DAY2_L:
                return drs[startDate + 2].getOhlc()[2];
            case DAY2_C:
                return drs[startDate + 2].getOhlc()[3];
            case DAY2_V:
                return drs[startDate + 2].getVol();
            case DAY2_T:
                return drs[startDate + 2].getTop();
            case DAY2_B:
                return drs[startDate + 2].getBottom();
            case DAY2_M:
                return drs[startDate + 2].getMiddle();
            case DAY2_MA10:
                return drs[startDate + 2].getMa10();
            case DAY2_MA50:
                return drs[startDate + 2].getMa50();
            case DAY2_MA250:
                return drs[startDate + 2].getMa250();

            case DAY3_O:
                return drs[startDate + 3].getOhlc()[0];
            case DAY3_H:
                return drs[startDate + 3].getOhlc()[1];
            case DAY3_L:
                return drs[startDate + 3].getOhlc()[2];
            case DAY3_C:
                return drs[startDate + 3].getOhlc()[3];
            case DAY3_V:
                return drs[startDate + 3].getVol();
            case DAY3_T:
                return drs[startDate + 3].getTop();
            case DAY3_B:
                return drs[startDate + 3].getBottom();
            case DAY3_M:
                return drs[startDate + 3].getMiddle();
            case DAY3_MA10:
                return drs[startDate + 3].getMa10();
            case DAY3_MA50:
                return drs[startDate + 3].getMa50();
            case DAY3_MA250:
                return drs[startDate + 3].getMa250();

            case DAY4_O:
                return drs[startDate + 4].getOhlc()[0];
            case DAY4_H:
                return drs[startDate + 4].getOhlc()[1];
            case DAY4_L:
                return drs[startDate + 4].getOhlc()[2];
            case DAY4_C:
                return drs[startDate + 4].getOhlc()[3];
            case DAY4_V:
                return drs[startDate + 4].getVol();
            case DAY4_T:
                return drs[startDate + 4].getTop();
            case DAY4_B:
                return drs[startDate + 4].getBottom();
            case DAY4_M:
                return drs[startDate + 4].getMiddle();
            case DAY4_MA10:
                return drs[startDate + 4].getMa10();
            case DAY4_MA50:
                return drs[startDate + 4].getMa50();
            case DAY4_MA250:
                return drs[startDate + 4].getMa250();

            case DAY5_O:
                return drs[startDate + 5].getOhlc()[0];
            case DAY5_H:
                return drs[startDate + 5].getOhlc()[1];
            case DAY5_L:
                return drs[startDate + 5].getOhlc()[2];
            case DAY5_C:
                return drs[startDate + 5].getOhlc()[3];
            case DAY5_V:
                return drs[startDate + 5].getVol();
            case DAY5_T:
                return drs[startDate + 5].getTop();
            case DAY5_B:
                return drs[startDate + 5].getBottom();
            case DAY5_M:
                return drs[startDate + 5].getMiddle();
            case DAY5_MA10:
                return drs[startDate + 5].getMa10();
            case DAY5_MA50:
                return drs[startDate + 5].getMa50();
            case DAY5_MA250:
                return drs[startDate + 5].getMa250();
            default:
                return -1;
        }
    }

    public void saveAfilterSet(AfilterSet afilterSet) {
        System.out.println("isValidUser: " + isValidUser());
        if (isValidUser()) {
            
            if (afilterSet.getId() != -1) {
                System.out.println("AfilterSetService : DELETE OLD AFILTERSET ");
                afilterSetRepository.findById(afilterSet.getId()).ifPresent(f -> {
                    tickerAfilterSetRepository.deleteTickerByAfilterSetId(afilterSet.getId());
                    afilterSetRepository.deleteById(afilterSet.getId());
                });
            } 
            System.out.println("AfilterSetService : SAVE AFILTERSET (new id)");

            AfilterSet tempAfilterSet = new AfilterSet(); // so FilterAs FilterBs are not saved at this time
            tempAfilterSet.setUser(activeUser);
            tempAfilterSet.setName(afilterSet.getName());
            tempAfilterSet.setValid(true);
            AfilterSet thisAfilterSet = afilterSetRepository.save(tempAfilterSet); // with id in DB
            for (FilterA f : afilterSet.getFilterAs()) {                
                f.setAfilterSet(thisAfilterSet);
                filterARepository.save(f);
            }
            for (FilterB f : afilterSet.getFilterBs()) {
                f.setCoef(1.0);
                f.setAfilterSet(thisAfilterSet);
                filterBRepository.save(f);
            }       
               
        }
    }
    
    // Save as new, do not delete old one
    public void saveAfilterSetAs(AfilterSet afilterSet) {
        System.out.println("isValidUser: " + isValidUser());
        if (isValidUser()) {            
            
            System.out.println("AfilterSetService : SAVE AS NEW AFILTERSET ");

            AfilterSet tempAfilterSet = new AfilterSet(); // so FilterAs FilterBs are not saved at this time
            tempAfilterSet.setUser(activeUser);
            tempAfilterSet.setName(afilterSet.getName());
            tempAfilterSet.setValid(true);
            AfilterSet thisAfilterSet = afilterSetRepository.save(tempAfilterSet); // with id in DB
            for (FilterA f : afilterSet.getFilterAs()) {    
                f.setId((long) -1);
                f.setAfilterSet(thisAfilterSet);
                filterARepository.save(f);
            }
            for (FilterB f : afilterSet.getFilterBs()) {
                f.setId((long) -1);
                f.setCoef(1.0);
                f.setAfilterSet(thisAfilterSet);
                filterBRepository.save(f);
            }       
               
        }
    }
    
    public void deleteAllTickAfilterSet() {
        tickerAfilterSetRepository.deleteAll();
    }

    public List<AfilterSet> getUserAfilterSets() {
        // System.out.println("getUserAfilterSets() called");
        List<AfilterSet> afilterSets = new ArrayList<AfilterSet>();
        if (isValidUser()) {
            Iterable<AfilterSet> afilterSetsDb = afilterSetRepository.findByUserId(activeUser.getId());
            for (AfilterSet f : afilterSetsDb) {
                afilterSets.add(f);
            }
            if (afilterSets.size() > 0)
                return afilterSets;
        }
        return null;
    }
    
    public List<AfilterSet> getUserAfilterSets(User user) {
        List<AfilterSet> afilterSets = new ArrayList<AfilterSet>();
        if (isValidUser(user)) {
            Iterable<AfilterSet> afilterSetsDb = afilterSetRepository.findByUserId(user.getId());
            for (AfilterSet f : afilterSetsDb) {
                afilterSets.add(f);
            }
            if (afilterSets.size() > 0)
                return afilterSets;
        }
        return null;
    }

    public List<AfilterSet> getAllAfilterSets() {
       // System.out.println("getAllAfilterSets() called");
        return (List<AfilterSet>) afilterSetRepository.findAll();
    }

    public Optional<AfilterSet> getAfilterSetById(long id) {
        if (isValidUser()) {
            return afilterSetRepository.findById(id);
        }
        return null;
    }

    public void deleteAfilterSetById(long id) {
        System.out.println("deleteAfilterSetById() called");

        tickerAfilterSetRepository.deleteTickerByAfilterSetId(id);

        afilterSetRepository.deleteById(id);
    }
    
    public void deletTickerById(long id) {
        tickerAfilterSetRepository.deleteById(id);
    }

    public void saveTickerAfilterSet(TickerAfilterSet tickerAfilterSet) {

        TickerAfilterSet ticker = tickerAfilterSetRepository.findByTicker(tickerAfilterSet.getTicker());
        if (ticker != null) {
            ticker.setAfilterSets(tickerAfilterSet.getAfilterSets());
            tickerAfilterSetRepository.save(ticker);
        } else {
            tickerAfilterSetRepository.save(tickerAfilterSet);
        }
    }

    public List<TickerAfilterSet> getAllTickerAfilterSets(boolean isServerSide) {
        List<TickerAfilterSet> tickerAfilterSets = (List<TickerAfilterSet>) tickerAfilterSetRepository.findAll();

        if (!isServerSide && (!isValidUser() || activeUser.getUserRole() != UserRole.PRO)) {
            for (TickerAfilterSet tf : tickerAfilterSets) {
                tf.setTicker(Utils.encodeTicker(tf.getTicker()));
            }
        }
        return tickerAfilterSets;
    }

    public List<TickerAfilterSet> getUserTickerAfilterSets() {
        if (!isValidUser()) {
            return null;
        }
        List<TickerAfilterSet> result = new ArrayList<TickerAfilterSet>(); // result to be returned
        List<TickerAfilterSet> tickerFounds = (List<TickerAfilterSet>) tickerAfilterSetRepository.findAll(); 
        List<AfilterSet> userAfilterSets = getUserAfilterSets();
        
        if (tickerFounds == null) return null;
        if (userAfilterSets == null) return null;

        for (TickerAfilterSet tf : tickerFounds) {
            Set<AfilterSet> afilterSets = new HashSet<AfilterSet>();
            for (AfilterSet f : userAfilterSets) {
                for (AfilterSet p2 : tf.getAfilterSets()) {
                    if (f.getId() == p2.getId()) {
                        afilterSets.add(f);
                    }
                }
            }
            if (afilterSets.size() > 0) {
                TickerAfilterSet newTf = new TickerAfilterSet();
                newTf.setTicker(tf.getTicker());
                newTf.setAfilterSets(afilterSets);
                result.add(newTf);
            }
        }
        
        if (activeUser.getUserRole() != UserRole.PRO && result.size() > 5) {            
            for (int i=5; i<result.size(); i++ ) {
                result.get(i).setTicker(Utils.encodeTicker(result.get(i).getTicker()));
            }     
        }        
        return result;
    }
    
    public List<TickerAfilterSet> getUserTickerAfilterSets(User user) {
        if (!isValidUser(user)) {
            return null;
        }
        List<TickerAfilterSet> result = new ArrayList<TickerAfilterSet>(); // result to be returned
        List<TickerAfilterSet> tickerFounds = (List<TickerAfilterSet>) tickerAfilterSetRepository.findAll(); 
        List<AfilterSet> userAfilterSets = getUserAfilterSets(user);
        
        if (tickerFounds == null) return null;
        if (userAfilterSets == null) return null;

        for (TickerAfilterSet tf : tickerFounds) {
            Set<AfilterSet> afilterSets = new HashSet<AfilterSet>();
            for (AfilterSet f : userAfilterSets) {
                for (AfilterSet p2 : tf.getAfilterSets()) {
                    if (f.getId() == p2.getId()) {
                        afilterSets.add(f);
                    }
                }
            }
            if (afilterSets.size() > 0) {
                TickerAfilterSet newTf = new TickerAfilterSet();
                newTf.setTicker(tf.getTicker());
                newTf.setAfilterSets(afilterSets);
                result.add(newTf);
            }
        }
        
        if (user.getUserRole() != UserRole.PRO && result.size() > 5) {            
            for (int i=5; i<result.size(); i++ ) {
                result.get(i).setTicker(Utils.encodeTicker(result.get(i).getTicker()));
            }     
        }        
        return result;
    }
    
    public void resetAfilterSetPerformence(List<AfilterSet> afilterSets) {
        for (AfilterSet f : afilterSets) {
            f.setCloseAvg10(0.0);
            f.setCloseAvg30(0.0);
            f.setHighest10(0.0);
            f.setHighest30(0.0);
            f.setLowest10(0.0);
            f.setLowest30(0.0);
            f.setOccurrence(0);
            f.setNum10PercentLoseIn30Days(0);
            f.setNum10PercentRiseIn30Days(0);
            f.setTotalTickerScanned(0);
        }
    }
    
    public void saveAfilterSetPerformance(AfilterSet afilterSet, int totalTickerScanned) {
        if (afilterSet.getId() != -1) {
            afilterSetRepository.findById(afilterSet.getId()).ifPresent(f -> {
                //System.out.println("afilterService : saveAfilterSetPerformance caled");
                f.setCloseAvg10(afilterSet.getCloseAvg10());
                f.setCloseAvg30(afilterSet.getCloseAvg30());
                f.setHighest10(afilterSet.getHighest10());
                f.setHighest30(afilterSet.getHighest30());
                f.setLowest10(afilterSet.getLowest10());
                f.setLowest30(afilterSet.getLowest30());
                f.setOccurrence(afilterSet.getOccurrence());
                f.setNum10PercentLoseIn30Days(afilterSet.getNum10PercentLoseIn30Days());
                f.setNum10PercentRiseIn30Days(afilterSet.getNum10PercentRiseIn30Days());
                f.setTotalTickerScanned(totalTickerScanned);                
                
                if (totalTickerScanned >= 500) {
                    
                    
                  //  System.out.println("afilterSet afilterSet.getOccurrence(): " + afilterSet.getOccurrence() );
                  //  System.out.println("afilterSet totalTickerScanned: " + totalTickerScanned );
                    
                    if ((float)afilterSet.getOccurrence()/totalTickerScanned < MIN_VALID || 
                            (float)afilterSet.getOccurrence()/totalTickerScanned > MAX_VALID
                            ) {                        
                        f.setValid(false);
                    } else {
                      //  System.out.println("save afilterSet valid  "  );
                        f.setValid(true);
                    }
                }                
                
                afilterSetRepository.save(f);
            });
        }
    }
    
    public TickerAfilterSet findAfilterSetsAndCalcPerformance(String ticker, DayRecord[] drs, List<AfilterSet> afilterSets) throws Exception {
        // system.out.println("findAfilterSetsAndCalcPerformance called");
        /*
         * if (drs == null) System.out.println("drs is null *************");
         * else System.out.println("drs.length: " + drs.length);
         */
        
        TickerAfilterSet ta = new TickerAfilterSet();
        ta.setTicker(ticker);
        ta.setBigStock(filterService.isBigStock(drs));  
        
        for (AfilterSet f : afilterSets) {           
            List<Integer> foundDays = new ArrayList<Integer>();              
             for (int i=0; i < drs.length; i++) {                
                if (passAfilterSet(f, drs, i)) {
                    foundDays.add(i);                    
                    if (i == 0) {
                        ta.getAfilterSets().add(f);
                    }
                }
            }
            if (foundDays.size() > 0) updateAfilterSetPerformance(drs, f, foundDays);
        }
        if (ta.getAfilterSets() != null && ta.getAfilterSets().size() > 0) return ta;
        return null;
    }
    
    private void updateAfilterSetPerformance(DayRecord[] drs, AfilterSet f, List<Integer> foundDays) {
        // system.out.println("updateAfilterSetPerformance called");
        for ( int i : foundDays) {
            if ( i > 30 ) { // so there are enough days after pattern
                double patternCloseAvg = drs[i].getOhlc()[3]; // pattern last day close
                
               // double avgClose10 = searchPatternService.getDaysAvgClosePercentageAfter(drs, i, 10, patternCloseAvg);
                //double avgClose30 = searchPatternService.getDaysAvgClosePercentageAfter(drs, i, 30, patternCloseAvg);
               // double avgHigh10 = searchPatternService.getDaysHighestPercentageAfter(drs, i, 10, patternCloseAvg);
                double high30 = searchPatternService.getDaysHighestPercentageAfter(drs, i, 30, patternCloseAvg);
                //double avgLow10 = searchPatternService.getDaysLowestPercentageAfter(drs, i, 10, patternCloseAvg);
                double low30 = searchPatternService.getDaysLowestPercentageAfter(drs, i, 30, patternCloseAvg);                
              
                if (patternCloseAvg != 0 && Math.abs(high30) < 3.0 && Math.abs(low30) > -0.67) {
                    f.setOccurrence(f.getOccurrence() + 1);
                    f.setCloseAvg10(f.getCloseAvg10() + searchPatternService.getDaysAvgClosePercentageAfter(drs, i, 10, patternCloseAvg));
                    f.setCloseAvg30(f.getCloseAvg30() + searchPatternService.getDaysAvgClosePercentageAfter(drs, i, 30, patternCloseAvg));
                    f.setHighest10(f.getHighest10() + searchPatternService.getDaysHighestPercentageAfter(drs, i, 10, patternCloseAvg));
                    f.setHighest30(f.getHighest30() + high30);
                    f.setLowest10(f.getLowest10() + searchPatternService.getDaysLowestPercentageAfter(drs, i, 10, patternCloseAvg));
                    f.setLowest30(f.getLowest30() + low30);
                    
                    if (high30 > 0.1) {
                        // System.out.println("high10: " + high10);
                        f.setNum10PercentRiseIn30Days(f.getNum10PercentRiseIn30Days() + 1);
                    }
                    if (high30 > 0.2) f.setNum10PercentLoseIn30Days(f.getNum10PercentLoseIn30Days() + 1);
                    
                }                
            }
        }  
    }
    
    public  Set<AfilterSet> getTopFilterSets() {
        return (Set<AfilterSet>) afilterSetRepository.findTopFilterSets();
    }
    
    public String hasTopAfilterSet(TickerAfilterSet tkf, Set<AfilterSet> topFilterSets) {
        for (AfilterSet p1: topFilterSets) {
            for (AfilterSet p2 : tkf.getAfilterSets()) {
                if (p1.getId() == p2.getId()) return p1.getName();
            }
        }
        return "";
    }

}
