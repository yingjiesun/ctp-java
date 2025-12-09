package catchthepattern.com.services;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import catchthepattern.com.models.Breakout;
import catchthepattern.com.models.DayRecord;
import catchthepattern.com.models.DoubleBottom;
import catchthepattern.com.afilters.AfilterService;
import catchthepattern.com.afilters.AfilterSet;
import catchthepattern.com.afilters.TickerAfilterSet;
import catchthepattern.com.models.BestBearish;
import catchthepattern.com.models.BestBullish;
import catchthepattern.com.models.Filterset;
import catchthepattern.com.models.Gainer;
import catchthepattern.com.models.Pattern;
import catchthepattern.com.models.PatternDef;
import catchthepattern.com.models.StockToScan;
import catchthepattern.com.models.TickerFound;
import catchthepattern.com.models.TopRated;
import catchthepattern.com.models.User;
import catchthepattern.com.repositories.BestBearishRepository;
import catchthepattern.com.repositories.BestBullishRepository;
import catchthepattern.com.repositories.BreakoutRepository;
import catchthepattern.com.repositories.DoubleBottomRepository;
import catchthepattern.com.repositories.FiltersetRepository;
import catchthepattern.com.repositories.GainerRepository;
import catchthepattern.com.repositories.PatternDefRepository;
import catchthepattern.com.repositories.PatternRepository;
import catchthepattern.com.repositories.StockToScanRepository;
import catchthepattern.com.repositories.TickerRepository;
import catchthepattern.com.repositories.TopRatedRepository;
import catchthepattern.com.repositories.UserRepository;
import catchthepattern.com.repositories.VolSpikeRepository;
import catchthepattern.com.models.UserRole;
import catchthepattern.com.models.VolSpike;



@Service
// @AllArgsConstructor
public class PatternService {
   
    private String sp500 = "MMM,AOS,ABT,ABBV,ACN,ATVI,ADM,ADBE,ADP,AAP,AES,AFL,A,APD,AKAM,ALK,ALB,ARE,ALGN,ALLE,LNT,ALL,GOOGL,GOOG,MO,AMZN,AMCR,AMD,AEE,AAL,AEP,AXP,AIG,AMT,AWK,AMP,ABC,AME,AMGN,APH,ADI,ANSS,AON,APA,AAPL,AMAT,APTV,ACGL,ANET,AJG,AIZ,T,ATO,ADSK,AZO,AVB,AVY,BKR,BALL,BAC,BBWI,BAX,BDX,WRB,BRK.B,BBY,BIO,TECH,BIIB,BLK,BK,BA,BKNG,BWA,BXP,BSX,BMY,AVGO,BR,BRO,BF.B,BG,CHRW,CDNS,CZR,CPT,CPB,COF,CAH,KMX,CCL,CARR,CTLT,CAT,CBOE,CBRE,CDW,CE,CNC,CNP,CDAY,CF,CRL,SCHW,CHTR,CVX,CMG,CB,CHD,CI,CINF,CTAS,CSCO,C,CFG,CLX,CME,CMS,KO,CTSH,CL,CMCSA,CMA,CAG,COP,ED,STZ,CEG,COO,CPRT,GLW,CTVA,CSGP,COST,CTRA,CCI,CSX,CMI,CVS,DHI,DHR,DRI,DVA,DE,DAL,XRAY,DVN,DXCM,FANG,DLR,DFS,DISH,DIS,DG,DLTR,D,DPZ,DOV,DOW,DTE,DUK,DD,DXC,EMN,ETN,EBAY,ECL,EIX,EW,EA,ELV,LLY,EMR,ENPH,ETR,EOG,EPAM,EQT,EFX,EQIX,EQR,ESS,EL,ETSY,RE,EVRG,ES,EXC,EXPE,EXPD,EXR,XOM,FFIV,FDS,FICO,FAST,FRT,FDX,FITB,FRC,FSLR,FE,FIS,FISV,FLT,FMC,F,FTNT,FTV,FOXA,FOX,BEN,FCX,GRMN,IT,GEHC,GEN,GNRC,GD,GE,GIS,GM,GPC,GILD,GL,GPN,GS,HAL,HIG,HAS,HCA,PEAK,HSIC,HSY,HES,HPE,HLT,HOLX,HD,HON,HRL,HST,HWM,HPQ,HUM,HBAN,HII,IBM,IEX,IDXX,ITW,ILMN,INCY,IR,PODD,INTC,ICE,IFF,IP,IPG,INTU,ISRG,IVZ,INVH,IQV,IRM,JBHT,JKHY,J,JNJ,JCI,JPM,JNPR,K,KDP,KEY,KEYS,KMB,KIM,KMI,KLAC,KHC,KR,LHX,LH,LRCX,LW,LVS,LDOS,LEN,LNC,LIN,LYV,LKQ,LMT,L,LOW,LYB,MTB,MRO,MPC,MKTX,MAR,MMC,MLM,MAS,MA,MTCH,MKC,MCD,MCK,MDT,MRK,META,MET,MTD,MGM,MCHP,MU,MSFT,MAA,MRNA,MHK,MOH,TAP,MDLZ,MPWR,MNST,MCO,MS,MOS,MSI,MSCI,NDAQ,NTAP,NFLX,NWL,NEM,NWSA,NWS,NEE,NKE,NI,NDSN,NSC,NTRS,NOC,NCLH,NRG,NUE,NVDA,NVR,NXPI,ORLY,OXY,ODFL,OMC,ON,OKE,ORCL,OGN,OTIS,PCAR,PKG,PARA,PH,PAYX,PAYC,PYPL,PNR,PEP,PKI,PFE,PCG,PM,PSX,PNW,PXD,PNC,POOL,PPG,PPL,PFG,PG,PGR,PLD,PRU,PEG,PTC,PSA,PHM,QRVO,PWR,QCOM,DGX,RL,RJF,RTX,O,REG,REGN,RF,RSG,RMD,RHI,ROK,ROL,ROP,ROST,RCL,SPGI,CRM,SBAC,SLB,STX,SEE,SRE,NOW,SHW,SPG,SWKS,SJM,SNA,SEDG,SO,LUV,SWK,SBUX,STT,STLD,STE,SYK,SYF,SNPS,SYY,TMUS,TROW,TTWO,TPR,TRGP,TGT,TEL,TDY,TFX,TER,TSLA,TXN,TXT,TMO,TJX,TSCO,TT,TDG,TRV,TRMB,TFC,TYL,TSN,USB,UDR,ULTA,UNP,UAL,UPS,URI,UNH,UHS,VLO,VTR,VRSN,VRSK,VZ,VRTX,VFC,VTRS,VICI,V,VMC,WAB,WBA,WMT,WBD,WM,WAT,WEC,WFC,WELL,WST,WDC,WRK,WY,WHR,WMB,WTW,GWW,WYNN,XEL,XYL,YUM,ZBRA,ZBH,ZION,ZTS";
    
    
    // Logger logger = LoggerFactory.getLogger(PatternService.class);
    
    // used to skip patterns or filtersets that are happening too often or too few
    private final double MIN_VALID = 0.003;
    private final double MAX_VALID = 40;
    
    private final double TRAILING_STOP = 0.13;
    private final double STOP_GAIN = 0.23;

 
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatternRepository patternRepository;

    @Autowired
    private PatternDefRepository patternDefRepository;

    @Autowired
    private FiltersetRepository filtersetRepository;

    @Autowired
    private TickerRepository tickerRepository;
    
    @Autowired
    private BestBullishRepository bestBullishRepository;
    
    @Autowired
    BestBearishRepository bestBearishRepository;
    
    @Autowired
    VolSpikeRepository volSpikeRepository;
    
    @Autowired
    TopRatedRepository topRatedRepository;
    
    @Autowired
    GainerRepository gainerRepository;
    
    @Autowired
    private BreakoutRepository breakoutRepository;
    
    @Autowired
    private DoubleBottomRepository doubleBottomRepository;
    
    @Autowired
    private StockToScanRepository stockToScanRepository;
    
    @Autowired
    private AfilterService afilterService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private DataFormatService dataFormatService;


    public PatternService() {}
    
    
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
    
    public double getTrailingStop() {return TRAILING_STOP; }
    public double getStopGain() {return STOP_GAIN; }

    public void savePattern(Pattern pattern) {  
    	User activeUser = userService.getActiveUser();
        if (isValidUser(activeUser)) {
            if (pattern.getId() != -1) {
                patternRepository.findById(pattern.getId()).ifPresent(p -> {
                    p.setUser(activeUser);
                    p.setName(pattern.getName());

                    PatternDef patternDef = patternDefRepository.save(pattern.getDef());
                    p.setDef(patternDef);

                    Filterset filterset = filtersetRepository.save(pattern.getFilterset());
                    p.setFilterset(filterset);
                    p.setValidPattern(true);
                    resetPatternPerformance(p);
                    patternRepository.save(p);
                });

            } else {
                pattern.setUser(activeUser);
                PatternDef tempDef = pattern.getDef();
                tempDef.setId((long) -1);
                PatternDef patternDef = patternDefRepository.save(tempDef);
                pattern.setDef(patternDef);
                Filterset tempSet = pattern.getFilterset();
                tempSet.setId((long) -1);
                Filterset filterset = filtersetRepository.save(tempSet);
                pattern.setFilterset(filterset);
                pattern.setValidPattern(true);
                resetPatternPerformance(pattern);
                patternRepository.save(pattern);
            }
        }
    }
    
    private void resetPatternPerformance(Pattern p) {
        p.setCloseAvg10(0);
        p.setCloseAvg30(0);
        p.setHighest10(0);
        p.setHighest30(0);
        p.setLowest10(0);
        p.setLowest30(0);
        p.setOccurrence(0);
    }
    
    public void resetPatternPerformence(List<Pattern> patterns) {
        for (Pattern p : patterns) {
            p.setCloseAvg10(0.0);
            p.setCloseAvg30(0.0);
            p.setHighest10(0.0);
            p.setHighest30(0.0);
            p.setLowest10(0.0);
            p.setLowest30(0.0);
            p.setOccurrence(0);
            p.setNum10PercentLoseIn30Days(0);
            p.setNum10PercentRiseIn30Days(0);
            p.setTotalTickerScanned(0);
        }
    }
    
    public void updatePatternPerformance(Pattern pattern, int totalTickerScanned) {
        if (pattern.getId() != -1) {
            patternRepository.findById(pattern.getId()).ifPresent(p -> {          

                p.setCloseAvg10(pattern.getCloseAvg10());
                p.setCloseAvg30(pattern.getCloseAvg30());
                p.setHighest10(pattern.getHighest10());
                p.setHighest30(pattern.getHighest30());
                p.setLowest10(pattern.getLowest10());
                p.setLowest30(pattern.getLowest30());
                p.setOccurrence(pattern.getOccurrence());;
                p.setNum10PercentLoseIn30Days(pattern.getNum10PercentLoseIn30Days());
                p.setNum10PercentRiseIn30Days(pattern.getNum10PercentRiseIn30Days());
                p.setTotalTickerScanned(totalTickerScanned);
                
                if (totalTickerScanned >= 500) {
                    if ((float)pattern.getOccurrence()/totalTickerScanned < MIN_VALID || 
                            (float)pattern.getOccurrence()/totalTickerScanned > MAX_VALID
                            ) {
                        
                        p.setValidPattern(false);
                    } else {
                        p.setValidPattern(true);
                    }
                }                
                patternRepository.save(p);
            });
        }
    }

    public void saveTicker(TickerFound tickerFound) {

        TickerFound ticker = tickerRepository.findByTicker(tickerFound.getTicker());
        if (ticker != null) {
            ticker.setPatterns(tickerFound.getPatterns());
            tickerRepository.save(ticker);
        } else {
            tickerRepository.save(tickerFound);
        }
    }
    
    public void saveStockToScan(StockToScan ticker) {        
        stockToScanRepository.save(ticker);         
     }
    
    public void deleteAllStockToScan() {        
        stockToScanRepository.deleteAll();         
     }
    
    public List<StockToScan> getAllStockToScan() { 
        List<StockToScan> tickers = (List<StockToScan>) stockToScanRepository.findAll();       
        return tickers;
    }
    
    // active user 
    public List<Pattern> getUserPatterns() {
        List<Pattern> patterns = new ArrayList<Pattern>();
        User activeUser = userService.getActiveUser();
        if (isValidUser(activeUser)) {            
            Iterable<Pattern> patternsSet = patternRepository.findByUserId(activeUser.getId());
            for (Pattern p : patternsSet) {
                patterns.add(p);
            }
            if (patterns.size() > 0) return patterns;
        }
        
        /*
         * User sampleUser =
         * userRepository.findByUsername("sampleuser@catchthepattern.com");
         * Iterable<Pattern> patternsSet =
         * patternRepository.findByUserId(sampleUser.getId());
         * for (Pattern p : patternsSet) {
         * patterns.add(p);
         * }
         */
        return patterns;        
    }
    
    // given user's pattern
    // used to send daily emails to user
    private List<Pattern> getUserPatterns(User user) {
        List<Pattern> patterns = new ArrayList<Pattern>();
        if (isValidUser(user)) {            
            Iterable<Pattern> patternsSet = patternRepository.findByUserId(user.getId());
            for (Pattern p : patternsSet) {
                patterns.add(p);
            }
            if (patterns.size() > 0) return patterns;
        } 
        return patterns;        
    }

    public List<Pattern> getAllPatterns() {
        
        return (List<Pattern>) patternRepository.findAll();
    }
    
    
    public List<Gainer> getAllGainers() {
        
        return (List<Gainer>) gainerRepository.findAll();
    }
    
    public Set<Pattern> getTop10Patterns() {         
        return (Set<Pattern>) patternRepository.findTop10();
    }
    
    public Set<Pattern> getTopPatterns() {         
        return (Set<Pattern>) patternRepository.findTopPatterns();
    }
    
    public List<BestBullish> getBestBullish(boolean isServerSide){        
        List<BestBullish> lbb = (List<BestBullish>) bestBullishRepository.findAll();         
        if (!isValidMember(isServerSide) && lbb != null && lbb.size()>0) {
            //List<TickerFound> newTF = new ArrayList<>();
            for (int i=1;i<lbb.size();i++) {
                lbb.get(i).setTicker(Utils.encodeTicker(lbb.get(i).getTicker()));
            }            
        }        
        return lbb;
    }

    /*
     * 
     * public String getSp500_old() {
     * String result = "";
     * String[] sp500Arr = sp500.split(",");
     * for (String ticker: sp500Arr) {
     * String str = (!isValidUser())?
     * Utils.encodeTicker(ticker) : ticker;
     * str = str + ",";
     * result += str;
     * }
     * return result;
     * }
     */
    public String[] getSp500() {
        String[] result = sp500.split(",");
        if (!isValidMember(false)) {
            String[] newArr = new String[result.length];
            for (int i=0; i < result.length; i++) {
                if (i > 9) newArr[i] = Utils.encodeTicker(result[i]);
                else newArr[i] = result[i];
            }
            return newArr;
        } else {
            return sp500.split(",");
        }       
    }
    
    
    public List<BestBearish> getBestBearish(){        
        List<BestBearish> lbb = (List<BestBearish>) bestBearishRepository.findAll();        
        if (!isValidMember(false)) {
            for (BestBearish bb: lbb) {
                bb.setTicker(Utils.encodeTicker(bb.getTicker()));
            }            
        }        
        return lbb;
    }
    
    public List<Breakout> getBreakouts(){        
        List<Breakout> lbb = (List<Breakout>) breakoutRepository.findAll();        
        if (!isValidMember(false)) {
            for (Breakout bb: lbb) {
                bb.setTicker(Utils.encodeTicker(bb.getTicker()));
            }            
        }        
        return lbb;
    }
    
    public List<DoubleBottom> getDoubleBottoms(){        
        List<DoubleBottom> lbb = (List<DoubleBottom>) doubleBottomRepository.findAll();        
        if (!isValidMember(false)) {
            for (DoubleBottom bb: lbb) {
                bb.setTicker(Utils.encodeTicker(bb.getTicker()));
            }            
        }        
        return lbb;
    }
    
    public List<VolSpike> getVolSpike(boolean isServerSide){        
        List<VolSpike> lbb = (List<VolSpike>) volSpikeRepository.findAll();        
        if (!isValidMember(false)) {
        	boolean skippedFirstOne = false;
            for (VolSpike bb: lbb) {
            	if (skippedFirstOne) {
            		bb.setTicker(Utils.encodeTicker(bb.getTicker()));
            	} else {
            		skippedFirstOne = true;
            	}
            }            
        }        
        return lbb;
    }

    public Optional<Pattern> getPatternById(long id) {
        if (isValidUser(userService.getActiveUser())) {
            return patternRepository.findById(id);
        }
        return null;
    }
    
    public List<TickerFound> getAllTickers(boolean isServerSide) { 
        List<TickerFound> tickerFounds = (List<TickerFound>) tickerRepository.findAll();
        
        if (!isValidMember(isServerSide) && tickerFounds != null && tickerFounds.size() > 0) {
            for (int i=1; i< tickerFounds.size(); i++) {
            	tickerFounds.get(i).setTicker(Utils.encodeTicker(tickerFounds.get(i).getTicker()));
            } 
        }
        return tickerFounds;
    }
    
    
    public List<TickerFound> getUserTickers() {
        if (!isValidUser(userService.getActiveUser())) {
            return null;          
        }   
        List<TickerFound> result = new ArrayList<TickerFound>();
        List<TickerFound> tickerFounds = (List<TickerFound>) tickerRepository.findAll();
        List<Pattern> userPatterns = getUserPatterns();
        
        for (TickerFound tf : tickerFounds) {
            Set<Pattern> foundPatterns =  new HashSet<Pattern>();
            for (Pattern p : userPatterns) {            
                for (Pattern p2 : tf.getPatterns()) {
                    if (p.getId() == p2.getId()) {
                        foundPatterns.add(p);
                    }
                }
            }
            if (foundPatterns.size() > 0) {
                TickerFound newTf = new TickerFound();
                newTf.setTicker(tf.getTicker());
                newTf.setPatterns(foundPatterns);
                result.add(newTf);
            }
        }
        
        if (!isValidMember(false) && result.size() > 0) { // encode from second for non-pro
            for (int i=1; i<result.size(); i++ ) {
                result.get(i).setTicker(Utils.encodeTicker(result.get(i).getTicker()));
            }            
        } 
        
        return result;
    }
    
    public List<TickerFound> getUserTickers(User user) {
        if (!isValidUser(user)) {
            return null;          
        }   
        List<TickerFound> result = new ArrayList<TickerFound>();
        List<TickerFound> tickerFounds = (List<TickerFound>) tickerRepository.findAll();
        List<Pattern> userPatterns = getUserPatterns(user);
        
        for (TickerFound tf : tickerFounds) {
            Set<Pattern> foundPatterns =  new HashSet<Pattern>();
            for (Pattern p : userPatterns) {            
                for (Pattern p2 : tf.getPatterns()) {
                    if (p.getId() == p2.getId()) {
                        foundPatterns.add(p);
                    }
                }
            }
            if (foundPatterns.size() > 0) {
                TickerFound newTf = new TickerFound();
                newTf.setTicker(tf.getTicker());
                newTf.setPatterns(foundPatterns);
                result.add(newTf);
            }
        }
        
        if (!isValidMember(false)) { 
            for (int i=1; i<result.size(); i++ ) {
                result.get(i).setTicker(Utils.encodeTicker(result.get(i).getTicker()));
            }            
        } 
        
        return result;
    }
    
    public List<TickerFound> getSampleUserTickers() {       
        List<TickerFound> result = new ArrayList<TickerFound>();
        List<TickerFound> tickerFounds = (List<TickerFound>) tickerRepository.findAll();
        List<Pattern> userPatterns = new ArrayList<Pattern>();        
        User sampleUser = userRepository.findByUsername("sampleuser@catchthepattern.com");    
        if (sampleUser == null) return null;
        Iterable<Pattern> patternsSet = patternRepository.findByUserId(sampleUser.getId());
        for (Pattern p : patternsSet) {
            userPatterns.add(p);
        }
        
        for (TickerFound tf : tickerFounds) {
            Set<Pattern> foundPatterns =  new HashSet<Pattern>();
            for (Pattern p : userPatterns) {            
                for (Pattern p2 : tf.getPatterns()) {
                    if (p.getId() == p2.getId()) {
                        foundPatterns.add(p);
                    }
                }
            }
            if (foundPatterns.size() > 0) {
                TickerFound newTf = new TickerFound();
                newTf.setTicker(tf.getTicker());
                newTf.setPatterns(foundPatterns);
                result.add(newTf);
            }
        }
        return result;
    }
    
    public void deleteAllTickerFounds() {
        tickerRepository.deleteAll();
    }
    
    public void deletTickerById(long id) {
        tickerRepository.deleteById(id);
    }

    public void deletePatternById(long id) {
        tickerRepository.deleteTickerByPatternId(id);
        patternRepository.deleteById(id);
    }

    public void deletePatternDefById(long id) {
        patternDefRepository.deleteById(id);
    }
    
    
    public List<TopRated> getTopRated(boolean isServerSide) {
        List<TopRated> trs = (List<TopRated>) topRatedRepository.findAll(); 
        if (!isServerSide && !isValidMember(false) && trs != null && trs.size()>0) {  
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date curScanDate;
			try {
				curScanDate = formatter.parse("1900-01-01"); // a trick to encode from second ticker.
				for (TopRated t: trs) {
	            	if (t.getFoundDate().equals(curScanDate)) {
	            		if ( !(t.getTicker().equals("SPY") || t.getTicker().equals("QQQ"))) t.setTicker(Utils.encodeTicker(t.getTicker()));
	            	} else {
	            		curScanDate = t.getFoundDate();
	            	}
	            }  
			} catch (ParseException e) {
				e.printStackTrace();
			}
        }
        return trs;
    }

    public List<TopRated> getUserTopRated(User user) {
        List<TopRated> trs = (List<TopRated>) topRatedRepository.findAll(); 
        if (!isUserValidMember(user) && trs != null && trs.size()>0) {  
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date curScanDate;
			try {
				curScanDate = formatter.parse("1900-01-01");
				for (TopRated t: trs) {
	            	if (t.getFoundDate().equals(curScanDate)) {
	            		if ( !(t.getTicker().equals("SPY") || t.getTicker().equals("QQQ")) 
		                        ) t.setTicker(Utils.encodeTicker(t.getTicker()));
	            	} else {
	            		curScanDate = t.getFoundDate();
	            	}
	            }  
			} catch (ParseException e) {
				e.printStackTrace();
			}
        }
        return trs;
    }
    
    public String getFirstTopRatedDate() {
        List<TopRated> trs = (List<TopRated>) topRatedRepository.findAll(); 
        if (trs != null && trs.size() > 0) return trs.get(0).getFoundDate().toString();
        else return "";
    }
    
    public String getHighestPossibleUnrealizedGain() {
        List<TopRated> trs = (List<TopRated>) topRatedRepository.findAll(); 
    	TopRated tp = new TopRated();
    	double highest = 0.0;
    	for (TopRated t: trs) {
        	if (t.getHighest() > highest) {
        		highest = t.getHighest();
        		tp = t;
        	} 
    	}
    	return "Highest: " + tp.getTicker() + " " + toPercentage(tp.getHighest()) + "\nFound Date " + tp.getFoundDate();
    }
    
    public String getHighestUnrealizedGain() {
        List<TopRated> trs = (List<TopRated>) topRatedRepository.findAll(); 
    	TopRated tp = new TopRated();
    	double highest = 0.0;
    	for (TopRated t: trs) {
        	if (t.getSoldDate() == null && t.getPercentageChange() > highest) {
        		highest = t.getPercentageChange();
        		tp = t;
        	} 
    	}
    	return "Highest Unrealized: " + tp.getTicker() + " " + toPercentage(tp.getPercentageChange()) + "\nFound Date " + tp.getFoundDate();
    }
    
    public String getHighestRealizedGain() {
        List<TopRated> trs = (List<TopRated>) topRatedRepository.findAll(); 
    	TopRated tp = new TopRated();
    	int count = 0;
    	String stopGainStocks = "";
    	for (TopRated t: trs) {
        	if  (t.getSoldDate() != null && Utils.areDoublesEqual(t.getSoldPercentageChange(), STOP_GAIN, 1e-6)) {
        		stopGainStocks += t.getTicker() + " ";
        		count++;
        	}
    	}
    	return "Stop-Gain Realized (" + toPercentage(STOP_GAIN) + " " + count + " trades) :\n"+ stopGainStocks + " " ;
    }
    
    
    public String getAverageRealizedGain() {
        List<TopRated> trs = (List<TopRated>) topRatedRepository.findAll(); 
    	TopRated tp = new TopRated();
    	int count = 0;
    	double total = 0.0;
    	double avg = 0.0;
    	int totalDays = 0;
    	int avgDays = 0;
    	for (TopRated t: trs) {
        	if (t.getSoldDate() != null) {
        		total = total + t.getSoldPercentageChange();
        		totalDays += Utils.daysBetween(t.getFoundDate().toString(), t.getSoldDate().toString());
        		count++;
        	} 
    	}
    	if (count != 0) {
    		avg = total / count;
    		avgDays = totalDays / count;
    	}
    	return "Average Realized Gain:\n" + toPercentage(avg) + ", " + count + " trades, average holding days: " + avgDays;
    	//return " " + toPercentage(avg);
    }
    
    public double getAverageRealizedGain_number(double trailing, double stopGain) {
        List<TopRated> trs = (List<TopRated>) topRatedRepository.findAll(); 
    	TopRated tp = new TopRated();
    	int count = 0;
    	double total = 0.0;
    	double avg = 0.0;
    	int totalDays = 0;
    	int avgDays = 0;
    	for (TopRated t: trs) {
        	if (t.getSoldDate() != null) {
        		total = total + t.getSoldPercentageChange();
        		totalDays += Utils.daysBetween(t.getFoundDate().toString(), t.getSoldDate().toString());
        		count++;
        	} 
    	}
    	if (count != 0) {
    		avg = total / count;
    		avgDays = totalDays / count;
    	}
    	
    	//if (avgDays != 0 ) return (avg/avgDays) * 100;
    	//else return avg * 100;
    	
    	if (avgDays != 0 ) {

        	System.out.println("trailing: " + trailing + " stopGain: "+ stopGain +  " count: " + count + " avg: " + avg + " avgDays: " + avgDays + " avg/avgDays: " + avg/avgDays);
        	
    		return (avg/avgDays);
    	}
    	else return avg;
    	//return " " + toPercentage(avg);
    }
    
    public String getAverageUnRealizedGain() { //still holding, not sold
        List<TopRated> trs = (List<TopRated>) topRatedRepository.findAll(); 
    	TopRated tp = new TopRated();
    	int count = 0;
    	double total = 0.0;
    	double avg = 0.0;
    	int totalDays = 0;
    	int avgDays = 0;
    	LocalDate today = LocalDate.now(); // Get today's date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String todayStr = today.format(formatter);
    	for (TopRated t: trs) {
        	if (t.getSoldDate() == null) {
        		total = total + t.getPercentageChange();
        		totalDays += Utils.daysBetween(t.getFoundDate().toString(), todayStr);
        		count++;
        	} 
    	}
    	if (count != 0) {
    		avg = total / count;
    		avgDays = totalDays / count;
    	}
    	return "Average Unrealized Gain:\n" + toPercentage(avg)+ ", " + count + " Stocks, average holding days: " + avgDays;
    }
    
    public String getAverageNotSellingGain() { //if do not sell
        List<TopRated> trs = (List<TopRated>) topRatedRepository.findAll(); 
    	TopRated tp = new TopRated();
    	int count = 0;
    	double total = 0.0;
    	double avg = 0.0;
    	for (TopRated t: trs) {
        	total = total + t.getPercentageChange();
        	count++;
    	}
    	if (count != 0) avg = total / count;
    	return "Average Gain - No Selling ("+ count + " Stocks): " + toPercentage(avg);
    }
    
    public static String toPercentage(double value) {
        String sign = value >= 0 ? "+" : "-";
        double absValue = Math.abs(value * 100);
        return String.format("%s%.2f%%", sign, absValue);
    }
    
    
    public void deleteAllTopRated() {
        topRatedRepository.deleteAll();
    }
    

    
    public void calcSaveTopRated_new(Date scanDate) {
    	
    	System.out.println("========= calcSaveTopRated_new");
    	
        List<TopRated> topRatedList = new ArrayList<TopRated>();
        // List<TickerFound> tks = getAllTickers(true);
        // List<TickerAfilterSet> tkAfilters = afilterService.getAllTickerAfilterSets(true);
        // Set<Pattern> topPatterns = getTopPatterns(); 
        // Set<AfilterSet> topAfilterSets = afilterService.getTopFilterSets(); 
        List<BestBullish> bbls = getBestBullish(true);
        /*
         * TimeZone timeZone = TimeZone.getTimeZone("America/Chicago");
         * Calendar calendar = Calendar.getInstance(timeZone);
         * Date date = calendar.getTime();
         */
        // Date date = Utils.getTodayInChicago();
        /*
        for (TickerFound t : tks) {
            String hasTopPattern = hasTopPattern(t, topPatterns);
            if (!hasTopPattern.equals("")) {
                TopRated topRated = new TopRated();
                topRated.setTicker(t.getTicker());
                topRated.setStatus("pending");
                topRated.setPattern(hasTopPattern);
                addTopRatdIfNotExists(topRatedList, topRated);
            }
            if (topRatedList.size() > 0) break; // maximum 2 toppicks from patterns
        } 
        */
        /*
        
        for (TickerAfilterSet t : tkAfilters) {
            String hasTopAfilterSet = afilterService.hasTopAfilterSet(t, topAfilterSets);
            
            //System.out.println("=====");
            //System.out.println(t.getTicker());
            //System.out.println(hasTopAfilterSet);
            
            if (!hasTopAfilterSet.equals("")) {
                TopRated topRated = new TopRated();
                topRated.setTicker(t.getTicker());
                topRated.setStatus("pending");
                topRated.setFilterSet(hasTopAfilterSet);
                addTopRatdIfNotExists(topRatedList, topRated);
                break;
            }
        }
        */
        
        if (topRatedList.size() < 8) {
            for (BestBullish bbl : bbls) {
                if (bbl.getScore() > 12) {
                    TopRated topRated = new TopRated();
                    topRated.setTicker(bbl.getTicker());
                    topRated.setStatus("pending");
                    addTopRatdIfNotExists(topRatedList, topRated);
                }
                if (topRatedList.size() > 8) break;
            }  
        }
        
        if (topRatedList.size() < 8) {
            for (BestBullish bbl : bbls) {
                if (bbl.getScore() > 11) {
                    TopRated topRated = new TopRated();
                    topRated.setTicker(bbl.getTicker());
                    topRated.setStatus("pending");
                    addTopRatdIfNotExists(topRatedList, topRated);
                }
                if (topRatedList.size()> 8) break;
            }  
        }
        
        if (topRatedList.size() < 8) {
            for (BestBullish bbl : bbls) {
                if (bbl.getScore() > 10) {
                    TopRated topRated = new TopRated();
                    topRated.setTicker(bbl.getTicker());
                    topRated.setStatus("pending");
                    addTopRatdIfNotExists(topRatedList, topRated);
                }
                if (topRatedList.size()> 8) break;
            }  
        }
        
        if (topRatedList.size() < 8) {
            for (BestBullish bbl : bbls) {
                if (bbl.getScore() > 9) {
                    TopRated topRated = new TopRated();
                    topRated.setTicker(bbl.getTicker());
                    topRated.setStatus("pending");
                    addTopRatdIfNotExists(topRatedList, topRated);
                }
                if (topRatedList.size()> 8) break;
            }  
        }
        
        if (topRatedList.size() < 8) {
            for (BestBullish bbl : bbls) {
                if (bbl.getScore() > 8) {
                    TopRated topRated = new TopRated();
                    topRated.setTicker(bbl.getTicker());
                    topRated.setStatus("pending");
                    addTopRatdIfNotExists(topRatedList, topRated);
                }
                if (topRatedList.size()> 8) break;
            }  
        }
        
        if (topRatedList.size() < 8) {
            for (BestBullish bbl : bbls) {
                if (bbl.getScore() > 7) {
                    TopRated topRated = new TopRated();
                    topRated.setTicker(bbl.getTicker());
                    topRated.setStatus("pending");
                    addTopRatdIfNotExists(topRatedList, topRated);
                }
                if (topRatedList.size()> 8) break;
            }  
        }
        
       
        
        /*
        TopRated topRatedSPY = new TopRated();
        topRatedSPY.setTicker("SPY");
        topRatedSPY.setFoundDate(scanDate);
        addTopRatdIfNotExists(topRatedList, topRatedSPY);    
        */    
   
        topRatedRepository.saveAll(topRatedList);               
        
    }
    
    public void saveAllTopRated(List<TopRated> topRatedList) {
        topRatedRepository.saveAll(topRatedList);  
    }
    
    
    
    private void addTopRatdIfNotExists(List<TopRated> tps, TopRated topRated) {
        boolean exist = false;
        for (TopRated tp : tps) {
            if (tp.getTicker().equals(topRated.getTicker())) { 
                exist = true;
                break;
            }
        }
        if (!exist) {
            tps.add(topRated);
        }
    }
    
    public void deleteTopRatedOlderThan(Date cutoffDate) {
        topRatedRepository.deleteOlderThan(cutoffDate);
    }
    
    /*
     * public void saveAndUpdateTopRated(TopRated topRated) {
     * TopRated tp = topRatedRepository.finByTicker(topRated.getTicker());
     * if (tp != null ) {
     * topRated.setFoundDate(tp.getFoundDate());
     * }
     * topRatedRepository.save(topRated);
     * }
     */
    
    public boolean hasTop10Pattern(TickerFound tkf, Set<Pattern> top10Patterns) {
        for (Pattern p1: top10Patterns) {
            for (Pattern p2 : tkf.getPatterns()) {
                if (p1.getId() == p2.getId()) return true;
            }
        }
        return false;
    }
    
    public String hasTopPattern(TickerFound tkf, Set<Pattern> top10Patterns) {
        for (Pattern p1: top10Patterns) {
            for (Pattern p2 : tkf.getPatterns()) {
                if (p1.getId() == p2.getId()) return p1.getName();
            }
        }
        return "";
    }
    
    public boolean isBestBullish(TickerFound tkf, List<BestBullish> bbl) {
        for (BestBullish b: bbl) {
            if (b.getTicker().equalsIgnoreCase(tkf.getTicker())) return true;
        }
        return false;
    }
    
    public boolean isVolSpike(TickerFound tkf, List<VolSpike> vsk) {
        for (VolSpike v: vsk) {
            if (v.getTicker().equalsIgnoreCase(tkf.getTicker())) return true;
        }
        return false;
    }
    
    
    // FOR TESTING, do not call from prod
    // To call this function, use postman GET from endpoint localhost:5000/open/topTrailResearch, 
    // and the realized gains for different trailing stop will be calculated and returned.
    public List<String>  getRealizedGains() throws Exception { 
        List<TopRated> tps = getTopRated(true);
        
        Map<String, DayRecord[]> tickerDrs = new HashMap<String, DayRecord[]>();   
        
        List<String> AvgRealizedGains = new ArrayList<String>();
        List<Double[]> AvgRealizedGain_nums = new ArrayList<Double[]>();
        
        // save stock data to map
        for (TopRated tp : tps) {
            
            try {
              DayRecord[] drs = tickerDrs.get(tp.getTicker());
              if ( drs == null ) {
                  String[] dataArr = dataFormatService.getRawDataArr(tp.getTicker());
                  if( dataArr== null || dataArr.length < 60) {
                      continue;
                  }
                  drs = dataFormatService.getDayRecordsFromRawArrs(dataArr);
                  
                  tickerDrs.put(tp.getTicker(), drs);
              }
            }catch(Exception e) {
                System.out.println("=====ERROR 980: ====" + e);
            }
        }
        
        System.out.println("******** tickerDrs.size(): " + tickerDrs.size());
        
        for (double d = 0.06; d < 0.09; d = d + 0.001) {// stop loss
        	for (double stopWin = 0.01; stopWin < 0.03; stopWin = stopWin + 0.001) {// stop gain
	        	for (TopRated tp : tps) {
	                try {
	                  final DayRecord[] drs = tickerDrs.get(tp.getTicker());
	                  SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	                  Date updatedDate = dateFormat.parse(drs[0].getDate());   
	                  
	                  final double todayClose = drs[0].getOhlc()[3];
	                  final double pickDayClose = getCloseByDate(drs, tp.getFoundDate());            
	                  final double percentageChange = Utils.isDoubleZero(pickDayClose, 1e-6) ? 0 : (todayClose - pickDayClose)/pickDayClose;
	                  final double highestSincePick = getHighestSince(drs, tp.getFoundDate());
	                  final double highestPercentageChange = Utils.isDoubleZero(pickDayClose, 1e-6) ? 0 : (highestSincePick - pickDayClose)/pickDayClose;        
	                 /* 
	                  if (tp.getSoldDate() == null && highestPercentageChange - percentageChange > 0.116) {
	                  	tp.setSoldDate(updatedDate);
	                  	tp.setSoldPercentageChange(highestPercentageChange - 0.116);
	                  }
	                  */
	                 
	                 fixToppickPerformance(tp,drs, d, stopWin); 
	                  
	                  tp.setPercentageChange(percentageChange);
	                  tp.setHighest(highestPercentageChange);
	                } catch(Exception e) {
	                  System.out.println("=====ERROR 1010:" + tp.getTicker() + " " + e);
	                }
	          }  

	          deleteAllTopRated();
	          saveAllTopRated(tps);
	          
	          AvgRealizedGain_nums.add(new Double[] {d, stopWin, getAverageRealizedGain_number(d, stopWin)});
	          

          
	          AvgRealizedGains.add("Trailing stop " + toPercentage(d) +  " Stop win " + toPercentage(stopWin) + " : Average Gain Per Trade " + getAverageRealizedGain());
        	}
        }
        
        AvgRealizedGain_nums.sort(Comparator.comparing(arr -> arr[2]));
        for (Double[] arr : AvgRealizedGain_nums) {
            System.out.println(Arrays.toString(arr));
        }
               
        return AvgRealizedGains;
        
    }
    
    // Find the best strategy in the past 60 days
    // return [offerPrice, takeProfitPrice, trailingSalePrice]
    public double[] getBestStrategy(String ticker) {
    	
    	return null;
    }
    
    // fix single TopPick
    public void fixToppickPerformance(TopRated tp, DayRecord[] drs, double trailingStop, double stopWin) {
    	try {
    		LocalDate today = LocalDate.now(); // Get today's date
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String todayStr = today.format(formatter);
    		
    		tp.setSoldDate(null);
    		tp.setSoldPercentageChange(0);
    		
    		double closeOfFoundDate = getCloseByDate(drs, tp.getFoundDate()); 
			double highest = getHighByDate(drs, tp.getFoundDate()); 
			int foundDateIndex = getIndexByDateIndex(drs, tp.getFoundDate()); 
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			
			// System.out.println("closeOfFoundDate: " + closeOfFoundDate);
			
			for (int i=foundDateIndex; i >=0; i--) {
			    if (drs[i].getOhlc()[1] > highest) highest = drs[i].getOhlc()[1]; //hold highest
			    if (drs[i].getOhlc()[2] < highest * (1 - trailingStop)) { // trailing top sale happens
			    	tp.setSoldPercentageChange((highest * (1 - trailingStop) - closeOfFoundDate) / closeOfFoundDate );
			    	try {
						tp.setSoldDate(dateFormat.parse(drs[i].getDate()));
					} catch (ParseException e) {
						e.printStackTrace();
					}
			    	break;
			    } else if (drs[i].getOhlc()[1] > (closeOfFoundDate * (1 + stopWin))) { // stop win
			    	tp.setSoldPercentageChange(stopWin);
			    	tp.setSoldDate(dateFormat.parse(drs[i].getDate()));
			    	break;
			    } 
			    
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    

    
    private boolean isNoMove(double foundPrice, double todayPrice, long days) {
    	try {
			if (days > 45 && 
					foundPrice !=0 && 
					todayPrice > foundPrice && 
					((todayPrice - foundPrice)/foundPrice) < (0.0015 * days)
					) {
				// System.out.println("days: " + days + " foundPrice: "+ foundPrice + " todayPrice: " + todayPrice);
				return true;
			}
			return false;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
    	
    }
    
    private int getIndexByDateIndex(DayRecord[] drs, Date date) {
        
        for (int i=0; i < drs.length; i++) {
            try {
                if (date.toString().equals(drs[i].getDate())) {
                    return i;
                }
            } catch (Exception e) {
                System.out.println("EXCEPTION: StockScanTask getIndexByDateIndex");
            }
        }
        return 0;
    }
    
    private double getHighestSince(DayRecord[] drs, Date date) {
        
        double highest = drs[0].getOhlc()[1];
        for (int i=0; i < drs.length; i++) {
            if (drs[i].getOhlc()[1] > highest) highest = drs[i].getOhlc()[1];
            try {                
                if (date.toString().equals(drs[i].getDate())) {
                    return highest;
                } 
            } catch (Exception e) {
                System.out.println("EXCEPTION: StockScanTask getHigestSince");
            }
        }
        return highest;
    }
    
    
    
    
    private double getCloseByDate(DayRecord[] drs, Date date) {
        for (int i=0; i < drs.length; i++) {
            try {
                if (date.toString().equals(drs[i].getDate())) {
                    return drs[i].getOhlc()[3];
                }
            } catch (Exception e) {
                System.out.println("EXCEPTION: StockScanTask getCloseByDate");
            }
        }
        return 0.0;
    }
    
    
    private double getHighByDate(DayRecord[] drs, Date date) {
        for (int i=0; i < drs.length; i++) {
            try {
                if (date.toString().equals(drs[i].getDate())) {
                    return drs[i].getOhlc()[1];
                }
            } catch (Exception e) {
                System.out.println("EXCEPTION: StockScanTask getCloseByDate");
            }
        }
        return 0.0;
    }
    
 
    
    public boolean isValidMember(boolean isServerSide) {
    	
    	if (isServerSide) return true;
    	User activeUser = userService.getActiveUser();
    	try {
             if (activeUser == null) return false;
             if (activeUser.getUserRole() == UserRole.PRO) return true;
             if (!activeUser.isEnabled() || activeUser.getLocked()) return false;
             if (activeUser.getUsername().equals("anonymous@catchthepattern.com")) return false;
             LocalDate today = LocalDate.now();
             LocalDate serviceEndDate = null;
             if (activeUser.getServiceEndDate() != null) serviceEndDate = activeUser.getServiceEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
             if (serviceEndDate == null ) {
            	 return false;
             } else {
            	 return serviceEndDate.isBefore(today);  
             }
            } catch (Error e) {
             return false;
            }
    	
    }
    
	public boolean isUserValidMember(User activeUser) {
	    	try {
	             if (activeUser == null) return false;
	             if (activeUser.getUserRole() == UserRole.PRO) return true;
	             if (!activeUser.isEnabled() || activeUser.getLocked()) return false;
	             if (activeUser.getUsername().equals("anonymous@catchthepattern.com")) return false;
	             LocalDate today = LocalDate.now();
	             LocalDate serviceEndDate = null;
	             if (activeUser.getServiceEndDate() != null) serviceEndDate = activeUser.getServiceEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	             if (serviceEndDate == null ) {
	            	 return false;
	             } else {
	            	 return serviceEndDate.isBefore(today);  
	             }
	            } catch (Error e) {
	             return false;
	            }
	    	
	    }
    
    
}