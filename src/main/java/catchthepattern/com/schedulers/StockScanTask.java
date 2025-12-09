package catchthepattern.com.schedulers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import catchthepattern.com.afilters.AfilterService;
import catchthepattern.com.afilters.AfilterSet;
import catchthepattern.com.afilters.TickerAfilterSet;
import catchthepattern.com.models.BestBullish;
import catchthepattern.com.models.DayRecord;
import catchthepattern.com.models.Gainer;
import catchthepattern.com.models.IbPosition;
import catchthepattern.com.models.Pattern;
import catchthepattern.com.models.StockInfo;
import catchthepattern.com.models.StockToScan;
import catchthepattern.com.models.TickerFound;
import catchthepattern.com.models.TopRated;
import catchthepattern.com.models.User;
import catchthepattern.com.models.VolSpike;
import catchthepattern.com.repositories.BestBearishRepository;
import catchthepattern.com.repositories.BestBullishRepository;
import catchthepattern.com.repositories.BreakoutRepository;
import catchthepattern.com.repositories.DoubleBottomRepository;
import catchthepattern.com.repositories.GainerRepository;
import catchthepattern.com.repositories.IbPositionRepository;
import catchthepattern.com.repositories.UserRepository;
import catchthepattern.com.repositories.VolSpikeRepository;
import catchthepattern.com.services.AlphaService;
import catchthepattern.com.services.DataFormatService;
import catchthepattern.com.services.EmailSenderService;
import catchthepattern.com.services.PatternService;
import catchthepattern.com.services.SearchPatternService;
import catchthepattern.com.services.StockDataService;
import catchthepattern.com.services.Utils;
import catchthepattern.com.services.IbGatewayService.Position;
import catchthepattern.com.services.FilterService;
import catchthepattern.com.services.IbAutoTradeService;
import catchthepattern.com.services.IbGatewayService;

@Component
public class StockScanTask {
    
    @Value("${catchthepattern.app.email}")
    private String APP_EMAIL;
    
    @Autowired
    private AlphaService alphaService;
    
    @Autowired
    private DataFormatService dataFormatService;
    
    @Autowired
    private PatternService patternService;
    
    @Autowired
    private IbAutoTradeService ibAutoTradeService;
    
    @Autowired
    private IbGatewayService ibGatewayService;
    
    @Autowired
    private FilterService filterService;
    /*
     * @Autowired
     * private InMemoryService inMemoryService;
     */
    
    @Autowired
    private AfilterService afilterService;
    
    @Autowired
    private SearchPatternService searchPatternService;
    
    @Autowired
    private BestBullishRepository bestBullishRepository;
    
    @Autowired
    private BestBearishRepository bestBearishRepository;
    
    @Autowired
    private VolSpikeRepository volSpikeRepository;
    
    @Autowired
    private GainerRepository gainerRepository;
    
    @Autowired
    private BreakoutRepository breakoutRepository;
    
    @Autowired
    private DoubleBottomRepository doubleBottomRepository;   
    
    @Autowired
    private EmailSenderService emailSenderService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private IbPositionRepository ibPositionRepository;
    
    
    public enum ListIndex {
        DAY,
        WEEK,
        MONTH,
        MONTH3,
        MONTH6,
        YEAR,
        PV
    }
    
    List<Gainer> dayList;
    List<Gainer> weekList;
    List<Gainer> monthList;
    List<Gainer> month3List;
    List<Gainer> month6List;
    List<Gainer> yearList;
    List<Gainer> pvList;
    List<Gainer> combinedList;
    
    List<Pattern> patterns;
    //List<AfilterSet> afilterSets;
    
    List<Position> currentPositions; // used to update position history
    
   // Map<String, DayRecord[]> newMap;
    
    int totalTickerScanned = 0;
    
    String dateString = "1990-01-01"; // will be overwritten by drs[0] date
    LocalDate localDate = LocalDate.parse(dateString);
    Date scanDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    
    String dateString2 = "1980-01-01"; // if have this date, top picks will be deleted
    LocalDate localDate2 = LocalDate.parse(dateString2);
    Date toBeDeletedDate = Date.from(localDate2.atStartOfDay(ZoneId.systemDefault()).toInstant());
    
    @Async
    @Scheduled(cron = "0 0 1 ? * 6#4", zone="America/Chicago") // 01:00 SAT every 4 weeks
   //  @Scheduled(cron="0 00 01 ? * SAT", zone="America/Chicago") // 01:00 SAT
     public void generateScanList() throws Exception {
         System.out.println("=========SCHEDULED TASK STARTED: generateScanList");
         List<String> tickers = alphaService.getAllTickersArr();
         tickers = Utils.removeDup(tickers); 
         
         patternService.deleteAllStockToScan();
         for (int i=0; i <tickers.size(); i++) {   
             try {
                 if (tickers.get(i).length() > 4) continue; // skip 5 letters stock
                 String[] dataArr = dataFormatService.getRawDataArr(tickers.get(i));
                 if( dataArr== null || dataArr.length < 30) {
                     continue;
                 }
                 DayRecord[] drs = dataFormatService.get20DayRecordsFromRawArr(dataArr); 
                  if (filterService.skipPenny(drs)) {
                      continue;
                  }
                  if (filterService.isAboutDelisted(drs)) {
                      continue;
                  }
                  patternService.saveStockToScan(new StockToScan(tickers.get(i)));
             } catch (Exception e) {
                 System.out.println(e);
             }
         }
         System.out.println("=========SCHEDULED TASK FINISHED: generateScanList");
     }

    @Async
    @Scheduled(cron="0 30 16 ? * MON-FRI", zone="America/Chicago") // 
      // @Scheduled(cron="0 0/30 20 * * MON-FRI", zone="America/Los_Angeles") // 13:15 M-F
      // @Scheduled(cron="0 38 14 * * MON-FRI", zone="America/Chicago") // test
      // @Scheduled(cron="0 0 * * * *", zone="America/Los_Angeles") // top of every hour of everyday
     //  @Scheduled(cron="0 0/30 * * * *", zone="America/Chicago") //8:00, 8:30, 9:00, 9:30, 10:00 and 10:30... every day      
     // @Scheduled(cron="0 13 13 * * *", zone="America/Chicago") // every hour
     // @Scheduled(cron="0 */15 * * * *", zone="America/Chicago") // every 5 minutes   
      public void scanStocks_schedule() throws Exception {
          
          if (false && !Utils.isTradingDay()) {
              System.out.println("Not Trading Day");
              return;
          }
          
          LocalDateTime currentDateTime = LocalDateTime.now();
          System.out.println("========= SCHEDULED TASK STARTED ============= " + currentDateTime);          
          beforeScan();
          scanStocksByList();
          System.out.println("========= STOCK SCAN FINISHED =============");          
          afterScan(); 
          currentDateTime = LocalDateTime.now();
          System.out.println("========= SCHEDULED TASK FINISHED ============= "+ currentDateTime);
       
          
          /*
           * 
           * if (inMemoryService.stockDataMap != null &&
           * inMemoryService.stockDataMap.size() > 1000 ) {
           * 
           * System.out.println("========= USE inMemoryService");
           * 
           * for (Map.Entry<String, DayRecord[]> entry :
           * inMemoryService.stockDataMap.entrySet()) {
           * String ticker = entry.getKey();
           * DayRecord[] drs = entry.getValue();
           * newMap.put(ticker, dataFormatService.getUpdatedDayRecords(ticker, drs));
           * }
           * 
           * inMemoryService.stockDataMap.clear();
           * inMemoryService.stockDataMap.putAll(newMap);
           * 
           * scanStocksByHashMap(inMemoryService.stockDataMap);
           * 
           * } else { // old way, but generate inMemoryService.stockDataMap at the same
           * time
           * System.out.println("========= USE List");
           * scanStocksByList();
           * }
           */
          
      }
     
      // Initial values and delete values from DB
      public void beforeScan() {
          
          totalTickerScanned = 0;
          
          patternService.deleteAllTickerFounds();
          afilterService.deleteAllTickAfilterSet();
          bestBullishRepository.deleteAll();
          bestBearishRepository.deleteAll();
          breakoutRepository.deleteAll();
          doubleBottomRepository.deleteAll();
          volSpikeRepository.deleteAll();
          
          dayList = new ArrayList<Gainer>();
          weekList = new ArrayList<Gainer>();
          monthList = new ArrayList<Gainer>();
          month3List = new ArrayList<Gainer>();
          month6List = new ArrayList<Gainer>();
          yearList = new ArrayList<Gainer>();
          pvList = new ArrayList<Gainer>();
          combinedList = new ArrayList<Gainer>();  
          
          patterns = this.patternService.getAllPatterns(); 
          patterns.removeIf(p -> !p.isValidPattern());         
          this.patternService.resetPatternPerformence(patterns); // only reset valid patterns   
          
          /*
          afilterSets = this.afilterService.getAllAfilterSets();
          afilterSets.removeIf(f -> !f.isValid());  
          this.afilterService.resetAfilterSetPerformence(afilterSets);
          */
         
      }
      
      @Async
      @Scheduled(cron = "0 45 8-15 * * MON-FRI", zone = "America/Chicago") 
      public void updatePositionHistory() {
    	  System.out.println("*** updatePositionHistory started...");
    	  List<IbPosition> resultPositions = new ArrayList<>(); // new positions compared to current positions. Save it to DB.
    	  List<Position> newPositions;
    	  if (currentPositions == null || currentPositions.isEmpty()) { // app restarted
    		  System.out.println("*** currentPostions is null, get current positions and wait for next round...");
    		  currentPositions = ibGatewayService.getPositions();
    		  List<IbPosition> postionHistory= (List<IbPosition>) ibPositionRepository.findAll();
    		  if (postionHistory == null || postionHistory.size() == 0) { // no position history in DB, add current positions to DB
    			  Date now = new Date();
    			  for (Position p : currentPositions) {
    				  IbPosition ibPosition = new IbPosition(now, p.symbol, p.quantity, p.avgCost);
    				  resultPositions.add(ibPosition);
        		  }
        		  System.out.println("*** First Time save position history: " + resultPositions.size());
        		  if (!resultPositions.isEmpty()) ibPositionRepository.saveAll(resultPositions);
    		  }
    	  } else {
    		  newPositions = ibGatewayService.getPositions();
    		  if (newPositions == null) return;
    		  Date now = new Date();
    		  for (Position p : newPositions) {
    			  if (!hasPosition(p, currentPositions)) {
    				  IbPosition ibPosition = new IbPosition(now, p.symbol, p.quantity, p.avgCost);
    				  resultPositions.add(ibPosition);
    			  }
    		  }
    		  System.out.println("*** New Positions to be saved: " + resultPositions.size());
    		  if (!resultPositions.isEmpty()) ibPositionRepository.saveAll(resultPositions);
        	  currentPositions = newPositions;
    	  }
      }
      
      private boolean hasPosition(Position p, List<Position> positions) {
    	  for (Position pp : positions) {
    		  if (pp.symbol.equals(p.symbol)) return true;
    	  }
    	  return false;
      }
     
/*
     public void scanStocksByHashMap(Map<String, DayRecord[]> stockMap) {
         
         System.out.println("========= scanStocksByHashMap STARTED");
         
         for (Map.Entry<String, DayRecord[]> entry : stockMap.entrySet()) {
             String ticker = entry.getKey();
             DayRecord[] drs = entry.getValue();             
             processTicker(ticker, drs, patterns, afilterSets);
             totalTickerScanned++;
         }
         for (Pattern p : patterns) {                    
             patternService.updatePatternPerformance(p, totalTickerScanned); // save to DB            
         } 
         for (AfilterSet f : afilterSets) {                    
             afilterService.saveAfilterSetPerformance(f, totalTickerScanned); // save to DB               
         }
     }
*/
    public void scanStocksByList() throws Exception {
        
        System.out.println("========= scanStocksByList STARTED");
        
        List<String> tickers = new ArrayList<String>();        
        List<StockToScan> stockToScanList = patternService.getAllStockToScan();
        
        if (stockToScanList != null && stockToScanList.size() > 1000) {
            for (StockToScan s: stockToScanList) {
                tickers.add(s.getTicker());
            }
        } else {
            tickers = alphaService.getAllTickersArr();
        }
        
        tickers = Utils.removeDup(tickers);
        
      // **** TESTING CODE MUST BE COMMENTED OUT BEFORE PUSH TO PROD
        
      // String sp500 = "MMM,AOS,ABT,ABBV,ACN,ADBE,AMD,AES,AFL,A,APD,ABNB,AKAM,ALB,ARE,ALGN,ALLE,LNT,ALL,GOOGL,GOOG,MO,AMZN,AMCR,AEE,AEP,AXP,AIG,AMT,AWK,AMP,AME,AMGN,APH,ADI,AON,APA,APO,AAPL,AMAT,APTV,ACGL,ADM,ANET,AJG,AIZ,T,ATO,ADSK,ADP,AZO,AVB,AVY,AXON,BKR,BALL,BAC,BAX,BDX,BRK.B,BBY,TECH,BIIB,BLK,BX,BK,BA,BKNG,BSX,BMY,AVGO,BR,BRO,BF.B,BLDR,BG,BXP,CHRW,CDNS,CZR,CPT,CPB,COF,CAH,KMX,CCL,CARR,CAT,CBOE,CBRE,CDW,COR,CNC,CNP,CF,CRL,SCHW,CHTR,CVX,CMG,CB,CHD,CI,CINF,CTAS,CSCO,C,CFG,CLX,CME,CMS,KO,CTSH,COIN,CL,CMCSA,CAG,COP,ED,STZ,CEG,COO,CPRT,GLW,CPAY,CTVA,CSGP,COST,CTRA,CRWD,CCI,CSX,CMI,CVS,DHR,DRI,DDOG,DVA,DAY,DECK,DE,DELL,DAL,DVN,DXCM,FANG,DLR,DG,DLTR,D,DPZ,DASH,DOV,DOW,DHI,DTE,DUK,DD,EMN,ETN,EBAY,ECL,EIX,EW,EA,ELV,EMR,ENPH,ETR,EOG,EPAM,EQT,EFX,EQIX,EQR,ERIE,ESS,EL,EG,EVRG,ES,EXC,EXE,EXPE,EXPD,EXR,XOM,FFIV,FDS,FICO,FAST,FRT,FDX,FIS,FITB,FSLR,FE,FI,F,FTNT,FTV,FOXA,FOX,BEN,FCX,GRMN,IT,GE,GEHC,GEV,GEN,GNRC,GD,GIS,GM,GPC,GILD,GPN,GL,GDDY,GS,HAL,HIG,HAS,HCA,DOC,HSIC,HSY,HES,HPE,HLT,HOLX,HD,HON,HRL,HST,HWM,HPQ,HUBB,HUM,HBAN,HII,IBM,IEX,IDXX,ITW,INCY,IR,PODD,INTC,ICE,IFF,IP,IPG,INTU,ISRG,IVZ,INVH,IQV,IRM,JBHT,JBL,JKHY,J,JNJ,JCI,JPM,K,KVUE,KDP,KEY,KEYS,KMB,KIM,KMI,KKR,KLAC,KHC,KR,LHX,LH,LRCX,LW,LVS,LDOS,LEN,LII,LLY,LIN,LYV,LKQ,LMT,L,LOW,LULU,LYB,MTB,MPC,MKTX,MAR,MMC,MLM,MAS,MA,MTCH,MKC,MCD,MCK,MDT,MRK,META,MET,MTD,MGM,MCHP,MU,MSFT,MAA,MRNA,MHK,MOH,TAP,MDLZ,MPWR,MNST,MCO,MS,MOS,MSI,MSCI,NDAQ,NTAP,NFLX,NEM,NWSA,NWS,NEE,NKE,NI,NDSN,NSC,NTRS,NOC,NCLH,NRG,NUE,NVDA,NVR,NXPI,ORLY,OXY,ODFL,OMC,ON,OKE,ORCL,OTIS,PCAR,PKG,PLTR,PANW,PARA,PH,PAYX,PAYC,PYPL,PNR,PEP,PFE,PCG,PM,PSX,PNW,PNC,POOL,PPG,PPL,PFG,PG,PGR,PLD,PRU,PEG,PTC,PSA,PHM,PWR,QCOM,DGX,RL,RJF,RTX,O,REG,REGN,RF,RSG,RMD,RVTY,ROK,ROL,ROP,ROST,RCL,SPGI,CRM,SBAC,SLB,STX,SRE,NOW,SHW,SPG,SWKS,SJM,SW,SNA,SOLV,SO,LUV,SWK,SBUX,STT,STLD,STE,SYK,SMCI,SYF,SNPS,SYY,TMUS,TROW,TTWO,TPR,TRGP,TGT,TEL,TDY,TER,TSLA,TXN,TPL,TXT,TMO,TJX,TKO,TTD,TSCO,TT,TDG,TRV,TRMB,TFC,TYL,TSN,USB,UBER,UDR,ULTA,UNP,UAL,UPS,URI,UNH,UHS,VLO,VTR,VLTO,VRSN,VRSK,VZ,VRTX,VTRS,VICI,V,VST,VMC,WRB,GWW,WAB,WBA,WMT,DIS,WBD,WM,WAT,WEC,WFC,WELL,WST,WDC,WY,WSM,WMB,WTW,WDAY,WYNN,XEL,XYL,YUM,ZBRA,ZBH,ZTS";
		
        
      // String sp500 = "AFL,GOOG,MO,AMZN,MMM,BRKB,BFB,AOS,ABT,ABBV,ACN,ATVI,AYI,ADBE,AAP,AMD,AES,AMG,A,APD,ALK,AKAM,ALB,ARE,ALXN,ALGN,ALLE,AGN,ADS,LNT,ALL,AEE,AAL,AEP,AXP,AIG,AMT,AWK,AMP,ABC,AME,AMGN,APH,APC,ADI,ANSS,ANTM,AON,APA,AIV,AAPL,AMAT,APTV,ADM,ARNC,AJG,AIZ,T,ADSK,ADP,AZO,AVB,AVY,BHGE,BLL,BAC,BAX,BBT,BDX,BBY,BIIB,BLK,HRB,BA,BKNG,BWA,BXP,BSX,BHF,BMY,AVGO,CHRW,COG,CDNS,CPB,COF,CAH,KMX,CCL,CAT,CBOE,CBRE,CBS,CELG,CNC,CNP,CTL,CERN,CF,SCHW,CHTR,CVX,CMG,CB,CHD,CI,XEC,CINF,CTAS,CSCO,C,CFG,CTXS,CME,CMS,KO,CTSH,CL,CMCSA,CMA,CAG,CXO,COP,ED,STZ,GLW,COST,COTY,CCI,CSX,CMI,CVS,DHI,DHR,DRI,DVA,DE,DAL,XRAY,DVN,DLR,DFS,DISCA,DISCK,DISH,DG,DLTR,D,DOV,DWDP,DTE,DUK,DRE,DXC,ETFC,EMN,ETN,EBAY,ECL,EIX,EW,EA,EMR,ETR,EOG,EQT,EFX,EQIX,EQR,ESS,EL,RE,ES,EXC,EXPE,EXPD,EXR,XOM,FFIV,FB,FAST,FRT,FDX,FIS,FITB,FE,FISV,FLIR,FLS,FLR,FMC,FL,F,FTV,FBHS,BEN,FCX,GPS,GRMN,IT,GD,GE,GIS,GM,GPC,GILD,GPN,GS,GT,GWW,HAL,HBI,HOG,HRS,HIG,HAS,HCA,HCP,HP,HSIC,HES,HPE,HLT,HOLX,HD,HON,HRL,HST,HPQ,HUM,HBAN,HII,IDXX,INFO,ITW,ILMN,INCY,IR,INTC,ICE,IBM,IP,IPG,IFF,INTU,ISRG,IVZ,IPGP,IQV,IRM,JBHT,JEC,SJM,JNJ,JCI,JPM,JNPR,KSU,K,KEY,KMB,KIM,KMI,KLAC,KSS,KHC,KR,LB,LLL,LH,LRCX,LEG,LEN,LLY,LNC,LKQ,LMT,L,LOW,LYB,MAC,M,MRO,MPC,MAR,MMC,MLM,MAS,MA,MAT,MKC,MCD,MCK,MDT,MRK,MET,MTD,MGM,KORS,MCHP,MU,MSFT,MAA,MHK,TAP,MDLZ,MNST,MCO,MS,MSI,MTB,MYL,NDAQ,NOV,NAVI,NKTR,NTAP,NFLX,NWL,NFX,NEM,NWSA,NWS,NEE,NLSN,NKE,NI,NBL,JWN,NSC,NTRS,NOC,NCLH,NRG,NUE,NVDA,ORLY,OXY,OMC,OKE,ORCL,PCAR,PKG,PH,PAYX,PYPL,PNR,PBCT,PEP,PKI,PRGO,PFE,PCG,PM,PSX,PNW,PXD,PNC,RL,PPG,PPL,PX,PFG,PG,PGR,PLD,PRU,PEG,PSA,PHM,PVH,QRVO,QCOM,PWR,DGX,RRC,RJF,RTN,O,RHT,REG,REGN,RF,RSG,RMD,RHI,ROK,ROP,ROST,RCL,SPGI,CRM,SBAC,SLB,STX,SEE,SRE,SHW,SPG,SWKS,SLG,SNA,SO,LUV,SWK,SBUX,STT,SRCL,SYK,STI,SIVB,SYMC,SYF,SNPS,SYY,TROW,TTWO,TPR,TGT,TEL,FTI,TXN,TXT,BK,CLX,COO,HSY,MOS,TRV,DIS,TMO,TIF,TJX,TMK,TSS,TSCO,TDG,TRIP,FOXA,FOX,TSN,USB,UDR,ULTA,UAA,UA,UNP,UAL,UNH,UPS,URI,UTX,UHS,UNM,VFC,VLO,VAR,VTR,VRSN,VRSK,VZ,VRTX,VIAB,V,VNO,VMC,WMT,WBA,WM,WAT,WEC,WFC,WELL,WDC,WU,WRK,WY,WHR,WMB,WLTW,WYNN,XEL,XRX,XLNX,XYL,YUM,ZBH,ZION,ZTS,TSLA,MSFT,TWOU,LYFT,UBER,RTN";
     //  String sp500 = "ABNB,CDLX,TGTX,MMM,BRKB,BFB,AOS,ABT,ABBV,ACN,ATVI,AYI,ADBE,AAP,AMD,AES,AMG,AFL,A,APD,ALK,AKAM,ALB,ARE,ALXN,ALGN,ALLE,AGN,ADS,LNT,ALL,GOOGL,GOOG,MO,AMZN,AEE,AAL,AEP,AXP,AIG,AMT,AWK,AMP,ABC,AME,AMGN,APH,APC,ADI,ANSS,ANTM,AON,APA,AIV,AAPL,AMAT,APTV,ADM,ARNC,AJG,AIZ,T,ADSK,ADP,AZO,AVB,AVY,BHGE,BLL,BAC,BAX,BBT,BDX,BBY,BIIB,BLK,HRB,BA,BKNG,BWA,BXP,BSX,BHF,BMY,AVGO,CHRW,COG,CDNS,CPB,COF,CAH,KMX,CCL,CAT,CBOE,CBRE,CBS,CELG,CNC,CNP,CTL,CERN,CF,SCHW,CHTR,CVX,CMG,CB,CHD,CI,XEC,CINF,CTAS,CSCO,C,CFG,CTXS,CME,CMS,KO,CTSH,CL,CMCSA,CMA,CAG,CXO,COP,ED,STZ";
      // String sp500 = "ORLA,SPOT,QQQ,AAPL,MMM,MSFT,NVDA,BFB,AOS,ABT,ABBV,ACN,ATVI,AYI,ADBE,AAP,AMD,AES,AMG,AFL,A,APD,ALK,AKAM,ALB,ARE,ALXN,ALGN,ALLE,AGN,ADS,LNT,ALL,GOOG,MO,AEE,AAL,AEP,AXP,AIG,AMT";
      // String sp500 = "ORLA, SPOT"; //,APLS,CRM,TCMD,HROW,SPOT,NVDA,RIOT,META,GDRX,DKNG,PHM,SHOP,ACN,ICE,AYI,AUPH,OM,ASAI,SPY,QQQ";
      // String[] tickers = sp500.split(",");           
      // String[] tickers = {"AFL","AAPL","QQQ","AMZN","TSLA","NVDA","NFLX","MSFT","AMD","MARA","MAXR","GOOG","META"};          
      // String sp500 = "BDC,BSAC,MSFT,SCPH,BBAI,BHF,L,SFY,SFL,SKY,SFL,SKY,QNST,MSGS,BMI,BKR,NVST,SSD,SPR,BMO,BSY,BOX,TSN,BCI,TUP,GBCI,CVS,D,DBO,TIMB,ARQQ,CHGG,DJP,CS,UCO,DOG,VORB,EA,EPV,EBC,LSXMA,LSXMK,MMM,BRKB,BFB,AOS,ABT,ABBV,ACN,ATVI,AYI,ADBE,AAP,AMD,AES,AMG,AFL,A,APD,ALK,AKAM,ALB,ARE,ALXN,ALGN,ALLE,AGN,ADS,LNT,ALL,GOOGL,GOOG,MO,AMZN,AEE,AAL,AEP,AXP,AIG,AMT,AWK,AMP,ABC,AME,AMGN,APH,APC,ADI,ANSS,ANTM,AON,APA,AIV,AAPL,AMAT,APTV,ADM,ARNC,AJG,AIZ,T,ADSK,ADP,AZO,AVB,AVY,BHGE,BLL,BAC,BAX,BBT,BDX,BBY,BIIB,BLK,HRB,BA,BKNG,BWA,BXP,BSX,BHF,BMY,AVGO,CHRW,COG,CDNS,CPB,COF,CAH,KMX,CCL,CAT,CBOE,CBRE,CBS,CELG,CNC,CNP,CTL,CERN,CF,SCHW,CHTR,CVX,CMG,CB,CHD,CI,XEC,CINF,CTAS,CSCO,C,CFG,CTXS,CME,CMS,KO,CTSH,CL,CMCSA,CMA,CAG,CXO,COP,ED,STZ";
     //  String sp500 = "COCO,ASAN,ACVA,CRWD,CX,ETNB,OSCR,RELY,TECL,TPH,MSFT,CNDT,MJ,HUBS,IONQ,PLTR,NFLX,ORCL,AVGO,UGP,DELL,QLD,MDB,AMD,FNGU,JOBY,TQQQ";  
      //  String sp500 = "TDUP,QBTS,PLTR,HMY";
      // tickers = Arrays.asList(sp500.split(","));  
       
    // **** END OF TESTING COD
        
        System.out.println("before loop all ticker size: " + tickers.size());
        
       // Map<String, DayRecord[]> newMap = new HashMap<String, DayRecord[]>(); // hold the map for inMemoryService.stockDataMap
        
        List<String> tickers_redo = new ArrayList<String>(); // tickers that data is not updated from alpha
        
        for (int i=0; i <tickers.size(); i++) {  
            
           try {      
               if (tickers.get(i).length() > 4) continue; // skip 5 letters stock
               
                final String[] dataArr = dataFormatService.getRawDataArr(tickers.get(i));
                /*
                final String[] dataMa10Arr = dataFormatService.getRawMaDataArr5Years(tickers.get(i), "daily", "10");                
                final String[] dataMa50Arr = dataFormatService.getRawMaDataArr5Years(tickers.get(i), "daily", "50");
                final String[] dataMa250Arr = dataFormatService.getRawMaDataArr5Years(tickers.get(i), "daily", "250");
                */
                
                //final DayRecord[] drs = dataFormatService.getDayRecordsFromRawArrs(dataArr, dataMa10Arr, dataMa50Arr, dataMa250Arr);
                final DayRecord[] drs = dataFormatService.getDayRecordsFromRawArrs(dataArr);
                
              // if (drs != null) System.out.println("drs.length: " + drs.length);
                
                if (filterService.skipPenny(drs)) {                    
                    continue;
                }
                if (filterService.isAboutDelisted(drs)) {                    
                    continue;
                } 
                
                LocalDate stockLastDay = LocalDate.parse(drs[0].getDate());
                LocalDate today = LocalDate.now();

                if (stockLastDay.compareTo(today) < 0) {
                   // System.out.println("Yesterday stock");
                    tickers_redo.add(tickers.get(i));                    
                   // continue;
                }
                
                
                
                

                /* DO NOT SAVE STOCK DATA TO DB ANY MORE */
              //  stockDataService.saveStockData(new StockData(tickers.get(i), drs));
                
               // System.out.println("in loop newMap size: " + newMap.size());
                
                // processTicker(tickers.get(i), drs, patterns, afilterSets); 
                processTicker(tickers.get(i), drs, patterns); 
                
                totalTickerScanned++;
                
                if ((i+1) % 500 == 0) {
                	System.out.println("Stock scanned: " + totalTickerScanned);
                  
                    for (Pattern p : patterns) {                    
                        patternService.updatePatternPerformance(p, totalTickerScanned); // save to DB
                        patterns = this.patternService.getAllPatterns();  // update patterns so invalid pattern will be skiped
                        patterns.removeIf(pp -> !pp.isValidPattern());
                    } 
                    /*
                    for (AfilterSet f : afilterSets) {                    
                        afilterService.saveAfilterSetPerformance(f, totalTickerScanned); // save to DB
                        afilterSets = this.afilterService.getAllAfilterSets(); 
                        afilterSets.removeIf(ff -> !ff.isValid());        
                    } 
                    */
                                        
                }
                
             } catch (Exception e) {
                System.out.println("StockScanTask Exception: " + e);
           }
                  
        } // END OF SCAN FOR LOOP 
        
        
        System.out.println("tickers_redo.size(): " + tickers_redo.size());
        
        /* DO NOT DO RE_DO, SIMPLE IS BEST
        // yesterday tickers RE-DO one time
        if (tickers_redo.size() > 0) { 
            for (int i=0; i <tickers_redo.size(); i++) {  
                
                try {     
                    
                     final String[] dataArr = dataFormatService.getRawDataArr(tickers_redo.get(i));
                     
                    // final String[] dataMa10Arr = dataFormatService.getRawMaDataArr5Years(tickers_redo.get(i), "daily", "10");                
                    // final String[] dataMa50Arr = dataFormatService.getRawMaDataArr5Years(tickers_redo.get(i), "daily", "50");
                    // final String[] dataMa250Arr = dataFormatService.getRawMaDataArr5Years(tickers_redo.get(i), "daily", "250");
                     
                     
                     //final DayRecord[] drs = dataFormatService.getDayRecordsFromRawArrs(dataArr, dataMa10Arr, dataMa50Arr, dataMa250Arr);
                     final DayRecord[] drs = dataFormatService.getDayRecordsFromRawArrs(dataArr);
                     
                     //processTicker(tickers_redo.get(i), drs, patterns, afilterSets); 
                     processTicker(tickers_redo.get(i), drs, patterns); 
                     
                     totalTickerScanned++;         
                     
                  } catch (Exception e) {
                     System.out.println("StockScanTask Exception: " + e);
                }
                       
             } // END OF RE-DO FOR LOOP 
        } 
        */
        
    } // END OF scanStock() function
    
    public void afterScan() throws Exception {
        
       // inMemoryService.stockDataMap = new HashMap<String, DayRecord[]>();
        // newMap = null;
        
        // save performance after all ticker scanned
        for (Pattern p : patterns) {
           
            patternService.updatePatternPerformance(p, totalTickerScanned); // save to DB
        } 
        
        /*
        for (AfilterSet f : afilterSets) {   
            
            afilterService.saveAfilterSetPerformance(f, totalTickerScanned); // save to DB
        } 
        */      

        gainerRepository.deleteAll();
        gainerRepository.saveAll(getAllCombinedGainerList());
        
        // remove invalid founds
        cleanTickerFounds();
        // cleanTickerAfilterSet(); 
        
        LocalDate localDate = LocalDate.now().minusDays(365);        
        Date cutOffDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        
        patternService.deleteTopRatedOlderThan(cutOffDate);
        patternService.calcSaveTopRated_new(scanDate);
        updateTopPicked(scanDate);
        // sendDailyEmailsTask();       
        ibGatewayService.updateIbNet();
    }
    
    private List<TopRated> getLast50SortedById(List<TopRated> givenList) {
    	 // Sort the original list by id in ascending order
        Collections.sort(givenList, new Comparator<TopRated>() {
            @Override
            public int compare(TopRated o1, TopRated o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });

        // Calculate the start index for the last 50 items
        int start = Math.max(0, givenList.size() - 50);

        // Extract the sublist
        List<TopRated> resultList = new ArrayList<>();
        for (int i = start; i < givenList.size(); i++) {
            resultList.add(givenList.get(i));
        }

        return resultList;
    }
    
    private void updateTopPicked(Date scanDate) throws Exception {  
    	
    	System.out.println("==========updateTopPicked");
     
        List<TopRated> origin_tps = patternService.getTopRated(true);
        List<TopRated> tps = getLast50SortedById(origin_tps); //only update the most recent 50 top picks
        
        Map<String, DayRecord[]> tickerDrs = new HashMap<String, DayRecord[]>(); 
               
        for (TopRated tp : tps) {
          try {
            DayRecord[] drs = tickerDrs.get(tp.getTicker());
            if ( drs == null ) {
                String[] dataArr = dataFormatService.getRawDataArr(tp.getTicker());
                if( dataArr== null || dataArr.length < 60) {
                    continue;
                }
                // drs = dataFormatService.get60DayRecordsFromRawArr(dataArr); 
                drs = dataFormatService.getDayRecordsFromRawArrs(dataArr);
                
                tickerDrs.put(tp.getTicker(), drs);
            }
        
            boolean isRecentJump = false;
            if (drs[1].getOhlc()[3] != 0 && drs[2].getOhlc()[3] != 0 && drs[3].getOhlc()[3] != 0 && drs[4].getOhlc()[3] != 0) {
                if ((drs[0].getOhlc()[3] - drs[1].getOhlc()[3])/drs[1].getOhlc()[3] > 0.1) isRecentJump = true;
                if ((drs[1].getOhlc()[3] - drs[2].getOhlc()[3])/drs[2].getOhlc()[3] > 0.1) isRecentJump = true;
                if ((drs[2].getOhlc()[3] - drs[3].getOhlc()[3])/drs[3].getOhlc()[3] > 0.1) isRecentJump = true;
                if ((drs[3].getOhlc()[3] - drs[4].getOhlc()[3])/drs[4].getOhlc()[3] > 0.1) isRecentJump = true;
            } 
            
            String dateString;
            LocalDate localDate; // = LocalDate.parse(dateString);
            Date updatedDate; // = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            
            try {
            	if (drs[0].getDate() != null) {
            		dateString = drs[0].getDate().toString();
                	localDate = LocalDate.parse(dateString);
                    updatedDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            	} else {
            		LocalDate today = LocalDate.now();
                	updatedDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
            	}
            } catch (Exception e) {
            	System.out.println("=====Date ERROR in updateTopPicked: " + tp.getTicker() + " : " + e);
            	LocalDate today = LocalDate.now();
            	updatedDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
            }
            
            if (tp.getStatus() != null && tp.getStatus().equals("pending")) {
            	tp.setFoundDate(updatedDate);
            	tp.setBoughtPrice(drs[0].getOhlc()[3]);
                
                // check if same ticker is found as top pick and price was lower, if yes, skip LocalDate 
               if (!tp.getTicker().equals("SPY") && (
                       isRecentJump
                       // || isPickedIn3DaysAndLower(tp, tps, drs)
                       )) {                   
                   tp.setStatus("toBeRemoved");
               } else {
            	   tp.setStatus("toBeOrdered");
               } 
            }
           
            final double todayLow = drs[0].getOhlc()[2];
            final double todayHigh = drs[0].getOhlc()[1];
            final double pickDayClose = getCloseByDate(drs, tp.getFoundDate());    
            if (tp.getBoughtPrice() == 0) tp.setBoughtPrice(pickDayClose);
            final double percentageChange = Utils.isDoubleZero(pickDayClose, 1e-6) ? 0 : (todayLow - pickDayClose)/pickDayClose;
            final double highestSincePick = getHighestSince(drs, tp.getFoundDate());
            final double highestPercentageChange = Utils.isDoubleZero(pickDayClose, 1e-6) ? 0 : (highestSincePick - pickDayClose)/pickDayClose; 
            
            if (tp.getSoldDate() == null && highestPercentageChange - percentageChange > patternService.getTrailingStop()) {
            	tp.setSoldDate(updatedDate);
            	tp.setSoldPercentageChange(highestPercentageChange - patternService.getTrailingStop());
            }
            
            if (tp.getSoldDate() == null && pickDayClose != 0 && todayHigh > pickDayClose * (1 + patternService.getStopGain())) { // stop win
		    	tp.setSoldPercentageChange(patternService.getStopGain());
		    	tp.setSoldDate(updatedDate);
		    } 
            
            // patternService.fixToppickPerformance(tp, drs, trailingStop, gainStop); //this is one time fix, remove this line after the fix is done
            
            tp.setPercentageChange(percentageChange);
            tp.setHighest(highestPercentageChange);
          } catch(Exception e) {
            System.out.println("=====ERROR in updateTopPicked: ====" + e);
          }
        }  
        tps.removeIf( t -> t.getStatus() != null && t.getStatus().equals("toBeRemoved"));
        tps.forEach(t -> {
        	if ( t.getStatus() != null && !t.getStatus().equals("toBeOrdered")) t.setStatus("done"); // If status is not toBeOrdered, then set it done
        });
        
        //patternService.deleteAllTopRated();
        patternService.saveAllTopRated(tps);
        System.out.println("=====Start placing oders: ====");
        ibAutoTradeService.placeOrders();
    }
    
    private boolean isPickedIn3DaysAndLower_ai(TopRated tp, List<TopRated> tps, DayRecord[] drs) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Prepare a set of date strings from the last 8 days
        Set<String> recentDates = new HashSet<>();
        for (int i = 1; i <= 8; i++) {
            recentDates.add(LocalDate.now().minusDays(i).format(formatter));
        }

        // Check if today's low is greater than any of the past 8 days' lows
        double todayLow = drs[0].getOhlc()[3];
        boolean isLowerThanAny = false;
        for (int i = 1; i <= 8; i++) {
            if (todayLow > drs[i].getOhlc()[3]) {
                isLowerThanAny = true;
                break;
            }
        }

        if (!isLowerThanAny) {
            return false;
        }
        
        for (TopRated thisTp : tps) {
            try {
                if (tp.getTicker().equals(thisTp.getTicker())) {
                    if (thisTp.getFoundDate() == null) {
                        System.out.println("⚠️ Found null date for ticker: " + thisTp.getTicker());
                        continue;
                    }

                    LocalDate foundDate = thisTp.getFoundDate().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate();
                    String foundDateStr = foundDate.format(formatter);

                    if (recentDates.contains(foundDateStr)) {
                        return true;
                    }
                }
            } catch (Exception e) {
                System.out.println("=====ERROR in isPickedIn3DaysAndLower: ====" + e);
            }
        }
        return false;
    }
    
    // return true if is picked in recent 3 days 
    private boolean isPickedIn3DaysAndLower(TopRated tp, List<TopRated> tps, DayRecord[] drs) {
        LocalDate today = LocalDate.now();
        LocalDate day1 = today.minusDays(1);
        LocalDate day2 = today.minusDays(2);
        LocalDate day3 = today.minusDays(3);
        LocalDate day4 = today.minusDays(4);
        LocalDate day5 = today.minusDays(5);
        LocalDate day6 = today.minusDays(6);
        LocalDate day7 = today.minusDays(7);
        LocalDate day8 = today.minusDays(8);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String day1Str = day1.format(formatter);
        String day2Str = day2.format(formatter);
        String day3Str = day3.format(formatter);
        String day4Str = day4.format(formatter);
        String day5Str = day5.format(formatter);
        String day6Str = day6.format(formatter);
        String day7Str = day7.format(formatter);
        String day8Str = day8.format(formatter);
        
        String pattern = "yyyy-MM-dd";
        DateFormat df = new SimpleDateFormat(pattern);
    
        for (TopRated thisTp : tps) {
            
            try {
				String foundDate = df.format(thisTp.getFoundDate());           
				
				if (tp.getTicker().equals(thisTp.getTicker()) && 
				    ( 
				        foundDate.equals(day1Str) ||
				        foundDate.equals(day2Str) ||
				        foundDate.equals(day3Str) ||
				        foundDate.equals(day4Str) ||
				        foundDate.equals(day5Str) ||
				        foundDate.equals(day6Str) ||
				        foundDate.equals(day7Str) ||
				        foundDate.equals(day8Str)
				    ) && 
				    (
				       drs[0].getOhlc()[3] >  drs[1].getOhlc()[3] ||
				       drs[0].getOhlc()[3] >  drs[2].getOhlc()[3] ||
				       drs[0].getOhlc()[3] >  drs[3].getOhlc()[3] ||
				       drs[0].getOhlc()[3] >  drs[4].getOhlc()[3] ||
				       drs[0].getOhlc()[3] >  drs[5].getOhlc()[3] ||
				       drs[0].getOhlc()[3] >  drs[6].getOhlc()[3] ||
				       drs[0].getOhlc()[3] >  drs[7].getOhlc()[3] ||
				       drs[0].getOhlc()[3] >  drs[8].getOhlc()[3]
				    )
				) {
				    return true;
				}
			} catch (Exception e) {
				System.out.println("=====ERROR in isPickedIn3DaysAndLower: ====" + e);
			}
        }
        return false;
    }
    
    private double getCloseByDate(DayRecord[] drs, Date date) {

        if (date == null) {
            System.out.println("getCloseByDate: input date is null");
            return 0.0;
        }

        String dateStr = Utils.getDateStrFromSqlDate(date);

        for (int i = 0; i < drs.length; i++) {
            try {
                if (dateStr.equals(drs[i].getDate())) {
                    return drs[i].getOhlc()[3];
                }
            } catch (Exception e) {
                System.out.println("EXCEPTION: StockScanTask getCloseByDate");
                e.printStackTrace();
            }
        }
        return 0.0;
    }
    
    private int getIndexByDateIndex(DayRecord[] drs, Date date) {
    	
    	if (date == null) {
            System.out.println("getIndexByDateIndex: input date is null");
            return 0;
        }

    	String dateStr = Utils.getDateStrFromSqlDate(date);
        
        for (int i=0; i < drs.length; i++) {
            try {
                if (dateStr.equals(drs[i].getDate())) {
                    return i;
                }
            } catch (Exception e) {
                System.out.println("EXCEPTION: StockScanTask getCloseByDateInex");
            }
        }
        return 0;
    }
    
    
    private double getHighestSince(DayRecord[] drs, Date date) {
    	 if (date == null) {
             System.out.println("getHighestSince: input date is null");
             return 0.0;
         }

         String dateStr = Utils.getDateStrFromSqlDate(date);
        
        double highest = drs[0].getOhlc()[1];
        for (int i=0; i < drs.length; i++) {
            if (drs[i].getOhlc()[1] > highest) highest = drs[i].getOhlc()[1];
            try {                
                if (dateStr.equals(drs[i].getDate())) {
                    return highest;
                } 
            } catch (Exception e) {
                System.out.println("EXCEPTION: StockScanTask getHigestSince");
            }
        }
        return highest;
    }
    
    // remove invlaid tickerFounds
    private void cleanTickerFounds() {
        List<TickerFound> allTickers = patternService.getAllTickers(true);
        for (TickerFound tf : allTickers) {
            if (!hasValidPatterns(tf)) {
                patternService.deletTickerById(tf.getId());
            }
        }
    }
    
    private boolean hasValidPatterns(TickerFound tf) {
        for (Pattern p : tf.getPatterns()) {
            if (p.isValidPattern()) return true;
        }        
        return false;
    }
    
    // remove invlaid tickerFounds
    private void cleanTickerAfilterSet() {
        List<TickerAfilterSet> allTickerAfilterSet = afilterService.getAllTickerAfilterSets(true);
       
        for (TickerAfilterSet tf : allTickerAfilterSet) {
         
            if (!hasValidAfilters(tf)) {
               
                afilterService.deletTickerById(tf.getId());
            }
        }
    }
    
    private boolean hasValidAfilters(TickerAfilterSet taf) {
        for (AfilterSet f : taf.getAfilterSets()) {
            if (f.isValid()) return true;
        }        
        return false;
    }
    
    private void updateGainList(Gainer gainer, List<Gainer> gainerList, ListIndex index) {
        if (gainerList.size() < 10) {gainerList.add(gainer); }
        else {
            replaceSmallestOne(gainer, gainerList, index);
        }
    }
    
    private void replaceSmallestOne(Gainer gainer, List<Gainer> gainerList,  ListIndex index) {
        int smalleseIndex = 0;
        double smallest = gainerList.get(0).getValueByEnum(index);
        for (int i = 0; i < gainerList.size(); i++) {
            if (gainerList.get(i).getValueByEnum(index) < smallest) {
                smalleseIndex = i;
                smallest = gainerList.get(i).getValueByEnum(index);
            }
        }
        if (gainer.getValueByEnum(index) > smallest) {
            gainerList.set(smalleseIndex, gainer);
        }
    } 
    
    private List<Gainer> getAllCombinedGainerList() {         
        return getCombinedGainList(
                getCombinedGainList(
                    getCombinedGainList(
                            getCombinedGainList(
                                    getCombinedGainList(
                                            getCombinedGainList(dayList, weekList), 
                                            monthList), 
                                    month3List), 
                            month6List), 
                    yearList),
                pvList);
    }
    
    private List<Gainer> getCombinedGainList(List<Gainer> list1, List<Gainer> list2) { 
        List<Gainer> combinedList = new ArrayList<Gainer>(list1);
        for (Gainer g: list2) {
            if (!gainerListContains(combinedList, g.getTicker())) {
                combinedList.add(g);
            }
        }        
        return combinedList;
    }
    
    private boolean gainerListContains(List<Gainer> gainers, String ticker) {
        for (Gainer g : gainers) {
            if (g.getTicker().equals(ticker)) return true;
        }
        return false;
    }  

    /*
     // not used, decided to update DB if stockData is not current
    public void cleanStockDataDB() {
        
        List<String> list1 = stockDataService.getAllTickersFromDB();        
        List<String> list2 = new ArrayList<String>();        
        List<StockToScan> stockToScanList = patternService.getAllStockToScan();        
        if (stockToScanList != null && stockToScanList.size() > 1000) {
            for (StockToScan s: stockToScanList) {
                list2.add(s.getTicker());
            }
        } 
        
        List<String> missingTickers = Utils.findMissingStrings(list1, list2);
        
        System.out.println("cleanStockDataDB, found stockData to be deleted: " + missingTickers.size());
        
        for (String t : missingTickers ) {
            stockDataService.deleteStockDataByTicker(t);
        }
    }
*/
    // share function, scan pattern filterset for one stock 
    //public void processTicker(String ticker, DayRecord[] drs, List<Pattern> patterns, List<AfilterSet> afilterSets) {
    // do not search afilter any more
    	
    public void processTicker(String ticker, DayRecord[] drs, List<Pattern> patterns) {
        
        
        StockInfo si = alphaService.getStockInfoFromDB(ticker);
        
          boolean isLife = false;
          if (si != null) try {
          isLife = si.getSector().toLowerCase().contains("life") || 
        		  si.getDesc().toLowerCase().contains("pharm") || 
        		  si.getDesc().toLowerCase().contains("therap") || 
        		  si.getName().toLowerCase().contains("pharm") || 
        		  si.getName().toLowerCase().contains("therap")
        		  ;
          } catch(Exception e) {
              isLife = false;
          } 
        
        // System.out.println(" ============ processTicker: " + ticker );
        // Process Gainers
           if (drs != null && drs.length > 0) {
               Gainer thisGainer = new Gainer(ticker, 
                       drs[0].getOhlc()[3], 
                       searchPatternService.getDaysPercentageChange(drs, 0, 1),
                       searchPatternService.getDaysPercentageChange(drs, 0, 5),
                       searchPatternService.getDaysPercentageChange(drs, 0, 22),
                       searchPatternService.getDaysPercentageChange(drs, 0, 66),
                       searchPatternService.getDaysPercentageChange(drs, 0, 126),
                       searchPatternService.getDaysPercentageChange(drs, 0, 252),
                       drs[0].getOhlc()[3] * drs[0].getVol()
                       );
               updateGainList(thisGainer, dayList, ListIndex.DAY);
               updateGainList(thisGainer, weekList, ListIndex.WEEK);
               updateGainList(thisGainer, monthList, ListIndex.MONTH);
               updateGainList(thisGainer, month3List, ListIndex.MONTH3);
               updateGainList(thisGainer, month6List, ListIndex.MONTH6);
               updateGainList(thisGainer, yearList, ListIndex.YEAR);
               updateGainList(thisGainer, pvList, ListIndex.PV);
           }                
       
           int scoreBullish = 0;
         //  int scoreBearish = 0;
           int scoreVol = filterService.getVolSpikeScore(drs);           
           
           
           if (filterService.isBullish(drs, 20, 0)) scoreBullish++; 
           if (filterService.isBullish(drs, 40, 0)) scoreBullish++; 
           if (filterService.isBullish(drs, 60, 0)) scoreBullish++; 
           if (filterService.isBullish(drs, 80, 0)) scoreBullish++; 
           if (filterService.isBullish(drs, 100, 0)) scoreBullish++; 
           if (filterService.isBullish(drs, 120, 0)) scoreBullish++;
           if (filterService.isBullish(drs, 40, 20)) scoreBullish++; 
           if (filterService.isBullish(drs, 40, 40)) scoreBullish++; 
           if (filterService.isBullish(drs, 40, 60)) scoreBullish++; 
           if (filterService.isBullish(drs, 40, 80)) scoreBullish++;          
           if (isLife) scoreBullish = scoreBullish - 2;  
           
          
       
           /*
           if (filterService.isBearish(drs, 20, 0)) scoreBearish++;
           if (filterService.isBearish(drs, 40, 0)) scoreBearish++;
           if (filterService.isBearish(drs, 60, 0)) scoreBearish++;
           if (filterService.isBearish(drs, 80, 0)) scoreBearish++;
           if (filterService.isBearish(drs, 100, 0)) scoreBearish++;
           if (filterService.isBearish(drs, 120, 0)) scoreBearish++;
           if (filterService.isBearish(drs, 40, 20)) scoreBearish++;
           if (filterService.isBearish(drs, 40, 40)) scoreBearish++;
           if (filterService.isBearish(drs, 40, 60)) scoreBearish++;
           if (filterService.isBearish(drs, 40, 80)) scoreBearish++;
           */
           
           if (filterService.isBigStock(drs)) {
               scoreBullish++;
             //  scoreBearish++;
           }
           
           if (filterService.isBigStock2(drs)) {
               scoreBullish++;
           }
            
           
          // if (filterService.isBullishCorrection(drs, 0)) scoreBullish = scoreBullish + 12;
           scoreBullish = scoreBullish + filterService.getBullishCorrectionScore(drs, 0);
          
           if (scoreBullish > 5) bestBullishRepository.save(new BestBullish(ticker, scoreBullish));
           
           /*
           if ( scoreBearish > 7 ) { 
               bestBearishRepository.save(new BestBearish(ticker, scoreBearish));
           } 
           */
           
           if ( scoreVol > 30 ) volSpikeRepository.save(new VolSpike(ticker, scoreVol));
           
          // save breakout if happen in 5 days, save breakout if happen in 5 days
           /*
          
           if (filterService.isBreakout(drs, 250, 0) || filterService.isBreakout(drs, 120, 0)) {
               breakoutRepository.save(new Breakout(ticker));                   
           }                               


           if (filterService.isDoubleBottom(drs, 0)) {
               doubleBottomRepository.save(new DoubleBottom(ticker)); 
           }

*/
           // Pattern Ticker Found
           TickerFound tf = new TickerFound();            
           try {
               tf = searchPatternService.findPatternsAndCalcPerformance(ticker, drs, patterns);                
           } catch (Exception e) {
               e.printStackTrace();
           }            
           if (tf != null) {
               patternService.saveTicker(tf);
           } 
           
           // Afilter Ticker Found   
           
           /*
           
           TickerAfilterSet ta = new TickerAfilterSet();
           try {
               ta = afilterService.findAfilterSetsAndCalcPerformance(ticker, drs, afilterSets);                     
           } catch (Exception e) {
               e.printStackTrace();
           }            
           if (ta != null) {
               afilterService.saveTickerAfilterSet(ta);
           }          
     
            */
       }
    
     
     // @Scheduled(cron = "0 0 2 ? 1/3 1#1", zone="America/Chicago") // 2am on the first Sunday of every third month
    @Async
    @Scheduled(cron = "0 0 2 ? * SUN#1", zone="America/Chicago") // 2 am on the first Sunday of every month
    // @Scheduled(cron="0 */30 * * * *", zone="America/Chicago") // every 5 minutes 
    public void updateStockInfo() throws Exception {
        LocalDateTime currentDateTime = LocalDateTime.now();
        
        System.out.println("========= SCHEDULED TASK Update StockInfo STARTED ============= " + currentDateTime);
        
        /*        
        List<String> tickers = new ArrayList<String>();        
        List<StockToScan> stockToScanList = patternService.getAllStockToScan();
        
        if (stockToScanList != null && stockToScanList.size() > 0) {
            for (StockToScan s: stockToScanList) {
                tickers.add(s.getTicker());
            }
        } 
        */
        
        List<String> tickers = alphaService.getAllTickersArr();
        
        if (tickers.size() > 0) {
            tickers = Utils.removeDup(tickers);
            for (String ticker: tickers) {
                try {
                    StockInfo si = alphaService.getStockInfo(ticker);
                    alphaService.saveStockInfo(si);
                } catch (Exception e) {}
            }            
        }
        
        currentDateTime = LocalDateTime.now();
        System.out.println("========= SCHEDULED TASK Update StockInfo FINISHED ============= " + currentDateTime);
    }    
    
    public void sendDailyEmailsTask() throws Exception {
        
        System.out.println("sendDailyEmails STARTED ");
        
        List<User> users = (List<User>) userRepository.findAll();
        
        for (User u : users) {
            if (u.getReceiveEmails() != null && u.getReceiveEmails().equals("Y")) {
                LocalDate today = LocalDate.now();
                DayOfWeek dayOfWeek = today.getDayOfWeek();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                String todayString = today.format(formatter);
                String emailTitle = "STOCKS FOUND!";
                String emailBodyFirstLine = "DATE: " + todayString + "\n";
                String text = ""; 
                
                text += "Account: " + u.getUsername() + "\n\n";
                
                List<String> performance = new ArrayList<String>();
            	
            	performance.add("Performance From " + patternService.getFirstTopRatedDate());
            	performance.add(patternService.getHighestPossibleUnrealizedGain());
            	performance.add(patternService.getHighestRealizedGain());
            	performance.add(patternService.getHighestUnrealizedGain());
            	performance.add(patternService.getAverageRealizedGain());
            	performance.add(patternService.getAverageUnRealizedGain());
            	performance.add("Strategy:\nStop-Gain: +23%, Trailing Stop-Loss: -13%");
            	
            	 if (performance != null && performance.size() > 0) {
                     for (String tf : performance) {
                     	text += tf + "\n";
                     }
                 }  
                
            	 text += "\n";
                
                List<TopRated> tps = patternService.getUserTopRated(u);
                
                if (tps != null && tps.size() > 0) {
                    
                    text += "\nTOP PICKS:\n\n";
                    
                    for (TopRated tf : tps) {
                    	Date thisDate = tf.getFoundDate();
                    	String thisDateStr = thisDate == null ? "" : thisDate.toString();
                    	text += thisDateStr + " : ";
                        text += tf.getTicker().startsWith("*")? "****" : tf.getTicker();
                        text += "\n";
                    }
                }  
                
                List<TickerFound> tfs = patternService.getUserTickers(u);
                
                text += "\n";
                
                if (tfs != null && tfs.size() > 0) {
                    
                    text += "\n\nPATTERNS:\n\n";
                    
                    for (TickerFound tf : tfs) {
                        text += tf.getTicker() + " : ";
                        for (Pattern p : tf.getPatterns()) {
                            text += " " + p.getName();
                        }
                        text += "\n";
                    }
                }  
                
                List<TickerAfilterSet> tfs2 = afilterService.getUserTickerAfilterSets(u);
                
                if (tfs2 != null && tfs2.size() > 0) {
                    
                    text += "\n\nFILTERSETS:\n\n";
                    
                    for (TickerAfilterSet tf : tfs2) {
                        text += tf.getTicker() + " : ";
                        for (AfilterSet p : tf.getAfilterSets()) {
                            text += " " + p.getName();
                        }
                        text += "\n";
                    }
                }                 
                
                if (!text.equals("")) {
                    sendDailyEmails(u.getUsername(), emailBodyFirstLine + text, emailTitle);
                }
                /*
                if (dayOfWeek == DayOfWeek.MONDAY && text.equals("")) {
                    sendDailyEmails(u.getUsername(), "Greetings from CatchThePattern.com!, \n\nIt appears that no stock matching your patterns or filtersets was found. You may want to create or modify your patterns or filtersets, and the platform will automatically search for matching stocks every day.", "Greetings from CatchThePattern.com!");
                }
                */
            }            
            
        }
        
        System.out.println("sendDailyEmails FINISHED" );
    }
    
    public void sendDailyEmails(String emailAddress, String msg, String subject) {
    	if (!emailAddress.contains("anonymous")) {
    		final SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(emailAddress);
            mailMessage.setSubject(subject);
            mailMessage.setFrom(APP_EMAIL);
            mailMessage.setText(msg);
            emailSenderService.sendEmail(mailMessage);
    	}
    }
}



    /* BACKUP scanStocks()
     * 
     * 
   // @Scheduled(cron="0 0/30 14 * * MON-FRI", zone="America/Los_Angeles") // 13:15 M-F
   // @Scheduled(cron="0 45 1 * * MON-FRI", zone="America/Los_Angeles") // test
   // @Scheduled(cron="0 0 * * * *", zone="America/Los_Angeles") // top of every hour of everyday
   // @Scheduled(cron="0 0/30 8-23 * * *", zone="America/Los_Angeles") //8:00, 8:30, 9:00, 9:30, 10:00 and 10:30... every day
   // @Scheduled(cron="0 * /38 * * * *", zone="America/Los_Angeles") // every hour   

 public void scanStocks() throws Exception {
     
     System.out.println("=========SCHEDULED TASK STARTED: scanStocks");
     
     List<String> tickers = new ArrayList<String>();
     
     List<StockToScan> stockToScanList = patternService.getAllStockToScan();
     
     System.out.println("scanStocks size" + stockToScanList.size());
     
     if (stockToScanList != null && stockToScanList.size() > 1000) {
         for (StockToScan s: stockToScanList) {
             tickers.add(s.getTicker());
         }
     } else {
         tickers = alphaService.getAllTickersArr();
     }
     
     System.out.println("original all ticker size: " + tickers.size());
     
     tickers = Utils.removeDup(tickers);
     
     System.out.println("after remove dup all ticker size: " + tickers.size());
     
     int totalTickerScanned = 0;
     
     patternService.deleteAllTickerFounds();
     afilterService.deleteAllTickAfilterSet();
     bestBullishRepository.deleteAll();
     bestBearishRepository.deleteAll();
     breakoutRepository.deleteAll();
     doubleBottomRepository.deleteAll();
     volSpikeRepository.deleteAll();
     
     dayList = new ArrayList<Gainer>();
     weekList = new ArrayList<Gainer>();
     monthList = new ArrayList<Gainer>();
     month3List = new ArrayList<Gainer>();
     month6List = new ArrayList<Gainer>();
     yearList = new ArrayList<Gainer>();
     pvList = new ArrayList<Gainer>();
     combinedList = new ArrayList<Gainer>();
     
   //  String sp500 = "AFL,GOOG,MO,AMZN,MMM,BRKB,BFB,AOS,ABT,ABBV,ACN,ATVI,AYI,ADBE,AAP,AMD,AES,AMG,A,APD,ALK,AKAM,ALB,ARE,ALXN,ALGN,ALLE,AGN,ADS,LNT,ALL,AEE,AAL,AEP,AXP,AIG,AMT,AWK,AMP,ABC,AME,AMGN,APH,APC,ADI,ANSS,ANTM,AON,APA,AIV,AAPL,AMAT,APTV,ADM,ARNC,AJG,AIZ,T,ADSK,ADP,AZO,AVB,AVY,BHGE,BLL,BAC,BAX,BBT,BDX,BBY,BIIB,BLK,HRB,BA,BKNG,BWA,BXP,BSX,BHF,BMY,AVGO,CHRW,COG,CDNS,CPB,COF,CAH,KMX,CCL,CAT,CBOE,CBRE,CBS,CELG,CNC,CNP,CTL,CERN,CF,SCHW,CHTR,CVX,CMG,CB,CHD,CI,XEC,CINF,CTAS,CSCO,C,CFG,CTXS,CME,CMS,KO,CTSH,CL,CMCSA,CMA,CAG,CXO,COP,ED,STZ,GLW,COST,COTY,CCI,CSX,CMI,CVS,DHI,DHR,DRI,DVA,DE,DAL,XRAY,DVN,DLR,DFS,DISCA,DISCK,DISH,DG,DLTR,D,DOV,DWDP,DTE,DUK,DRE,DXC,ETFC,EMN,ETN,EBAY,ECL,EIX,EW,EA,EMR,ETR,EOG,EQT,EFX,EQIX,EQR,ESS,EL,RE,ES,EXC,EXPE,EXPD,EXR,XOM,FFIV,FB,FAST,FRT,FDX,FIS,FITB,FE,FISV,FLIR,FLS,FLR,FMC,FL,F,FTV,FBHS,BEN,FCX,GPS,GRMN,IT,GD,GE,GIS,GM,GPC,GILD,GPN,GS,GT,GWW,HAL,HBI,HOG,HRS,HIG,HAS,HCA,HCP,HP,HSIC,HES,HPE,HLT,HOLX,HD,HON,HRL,HST,HPQ,HUM,HBAN,HII,IDXX,INFO,ITW,ILMN,INCY,IR,INTC,ICE,IBM,IP,IPG,IFF,INTU,ISRG,IVZ,IPGP,IQV,IRM,JBHT,JEC,SJM,JNJ,JCI,JPM,JNPR,KSU,K,KEY,KMB,KIM,KMI,KLAC,KSS,KHC,KR,LB,LLL,LH,LRCX,LEG,LEN,LLY,LNC,LKQ,LMT,L,LOW,LYB,MAC,M,MRO,MPC,MAR,MMC,MLM,MAS,MA,MAT,MKC,MCD,MCK,MDT,MRK,MET,MTD,MGM,KORS,MCHP,MU,MSFT,MAA,MHK,TAP,MDLZ,MNST,MCO,MS,MSI,MTB,MYL,NDAQ,NOV,NAVI,NKTR,NTAP,NFLX,NWL,NFX,NEM,NWSA,NWS,NEE,NLSN,NKE,NI,NBL,JWN,NSC,NTRS,NOC,NCLH,NRG,NUE,NVDA,ORLY,OXY,OMC,OKE,ORCL,PCAR,PKG,PH,PAYX,PYPL,PNR,PBCT,PEP,PKI,PRGO,PFE,PCG,PM,PSX,PNW,PXD,PNC,RL,PPG,PPL,PX,PFG,PG,PGR,PLD,PRU,PEG,PSA,PHM,PVH,QRVO,QCOM,PWR,DGX,RRC,RJF,RTN,O,RHT,REG,REGN,RF,RSG,RMD,RHI,ROK,ROP,ROST,RCL,SPGI,CRM,SBAC,SLB,STX,SEE,SRE,SHW,SPG,SWKS,SLG,SNA,SO,LUV,SWK,SBUX,STT,SRCL,SYK,STI,SIVB,SYMC,SYF,SNPS,SYY,TROW,TTWO,TPR,TGT,TEL,FTI,TXN,TXT,BK,CLX,COO,HSY,MOS,TRV,DIS,TMO,TIF,TJX,TMK,TSS,TSCO,TDG,TRIP,FOXA,FOX,TSN,USB,UDR,ULTA,UAA,UA,UNP,UAL,UNH,UPS,URI,UTX,UHS,UNM,VFC,VLO,VAR,VTR,VRSN,VRSK,VZ,VRTX,VIAB,V,VNO,VMC,WMT,WBA,WM,WAT,WEC,WFC,WELL,WDC,WU,WRK,WY,WHR,WMB,WLTW,WYNN,XEL,XRX,XLNX,XYL,YUM,ZBH,ZION,ZTS,TSLA,MSFT,TWOU,LYFT,UBER,RTN";
   // String sp500 = "MMM,BRKB,BFB,AOS,ABT,ABBV,ACN,ATVI,AYI,ADBE,AAP,AMD,AES,AMG,AFL,A,APD,ALK,AKAM,ALB,ARE,ALXN,ALGN,ALLE,AGN,ADS,LNT,ALL,GOOGL,GOOG,MO,AMZN,AEE,AAL,AEP,AXP,AIG,AMT,AWK,AMP,ABC,AME,AMGN,APH,APC,ADI,ANSS,ANTM,AON,APA,AIV,AAPL,AMAT,APTV,ADM,ARNC,AJG,AIZ,T,ADSK,ADP,AZO,AVB,AVY,BHGE,BLL,BAC,BAX,BBT,BDX,BBY,BIIB,BLK,HRB,BA,BKNG,BWA,BXP,BSX,BHF,BMY,AVGO,CHRW,COG,CDNS,CPB,COF,CAH,KMX,CCL,CAT,CBOE,CBRE,CBS,CELG,CNC,CNP,CTL,CERN,CF,SCHW,CHTR,CVX,CMG,CB,CHD,CI,XEC,CINF,CTAS,CSCO,C,CFG,CTXS,CME,CMS,KO,CTSH,CL,CMCSA,CMA,CAG,CXO,COP,ED,STZ";
    // String sp500 = "ABCDE,QQQ,AAPL,MMM,MSFT,NVDA,BFB,AOS,ABT,ABBV,ACN,ATVI,AYI,ADBE,AAP,AMD,AES,AMG,AFL,A,APD,ALK,AKAM,ALB,ARE,ALXN,ALGN,ALLE,AGN,ADS,LNT,ALL,GOOG,MO,AEE,AAL,AEP,AXP,AIG,AMT";
   //  String sp500 = "MMM,BRKB,BFB,AOS,ABT,ABBV,ACN,ATVI,AYI,ADBE,AAP,AMD,AES,AMG,AFL,A,APD,ALK,AKAM,ALB,ARE";
     //  String[] tickers = sp500.split(",");           
   //  String[] tickers = {"AFL","AAPL","QQQ","AMZN","TSLA","NVDA","NFLX","MSFT","AMD","MARA","MAXR","GOOG","META"};          
   //  String sp500 = "BDC,BSAC,MSFT,SCPH,BBAI,BHF,L,SFY,SFL,SKY,SFL,SKY,QNST,MSGS,BMI,BKR,NVST,SSD,SPR,BMO,BSY,BOX,TSN,BCI,TUP,GBCI,CVS,D,DBO,TIMB,ARQQ,CHGG,DJP,CS,UCO,DOG,VORB,EA,EPV,EBC,LSXMA,LSXMK,MMM,BRKB,BFB,AOS,ABT,ABBV,ACN,ATVI,AYI,ADBE,AAP,AMD,AES,AMG,AFL,A,APD,ALK,AKAM,ALB,ARE,ALXN,ALGN,ALLE,AGN,ADS,LNT,ALL,GOOGL,GOOG,MO,AMZN,AEE,AAL,AEP,AXP,AIG,AMT,AWK,AMP,ABC,AME,AMGN,APH,APC,ADI,ANSS,ANTM,AON,APA,AIV,AAPL,AMAT,APTV,ADM,ARNC,AJG,AIZ,T,ADSK,ADP,AZO,AVB,AVY,BHGE,BLL,BAC,BAX,BBT,BDX,BBY,BIIB,BLK,HRB,BA,BKNG,BWA,BXP,BSX,BHF,BMY,AVGO,CHRW,COG,CDNS,CPB,COF,CAH,KMX,CCL,CAT,CBOE,CBRE,CBS,CELG,CNC,CNP,CTL,CERN,CF,SCHW,CHTR,CVX,CMG,CB,CHD,CI,XEC,CINF,CTAS,CSCO,C,CFG,CTXS,CME,CMS,KO,CTSH,CL,CMCSA,CMA,CAG,CXO,COP,ED,STZ";
    // String sp500 = "AFL,CAE";  
   //  tickers = Arrays.asList(sp500.split(","));       
     
     
     List<Pattern> patterns = this.patternService.getAllPatterns();        
     
     
     // TESTING CODE
     // patterns.removeIf(p -> p.getId() != 43);
     
     
     patterns.removeIf(p -> !p.isValidPattern());
            
     System.out.println("patterns:" + patterns.size());
     
     this.patternService.resetPatternPerformence(patterns); // only reset valid patterns
     
     List<AfilterSet> afilterSets = this.afilterService.getAllAfilterSets();         
     
     afilterSets.removeIf(f -> !f.isValid());        
             
     this.afilterService.resetAfilterSetPerformence(afilterSets);
     System.out.println("afilterSets:" + afilterSets.size());  
     
     System.out.println("before loop all ticker size: " + tickers.size());
     
     for (int i=0; i <tickers.size(); i++) {  
         
        try {
            // String stockData = alphaService.getStock(tickers.get(i), false);            
           //  if (stockData.contains("Error")) continue;
            System.out.println("===============================================" + tickers.get(i));
            if (tickers.get(i).length() > 4) continue; // skip 5 letters stock
            
            
             String[] dataArr = dataFormatService.getRawDataArr(tickers.get(i));
             
             String[] dataMa10Arr = dataFormatService.getRawMaDataArr(tickers.get(i), "daily", "10");                
             String[] dataMa50Arr = dataFormatService.getRawMaDataArr(tickers.get(i), "daily", "50");
             String[] dataMa250Arr = dataFormatService.getRawMaDataArr(tickers.get(i), "daily", "250");
             
             DayRecord[] drs = dataFormatService.getDayRecordsFromRawArrs(dataArr, dataMa10Arr, dataMa50Arr, dataMa250Arr);   
             
             
            if (drs != null) System.out.println("drs.length: " + drs.length);
             
             if (filterService.skipPenny(drs)) {                    
                 continue;
             }
             if (filterService.isAboutDelisted(drs)) {                    
                 continue;
             }
         
             // Process Gainers
             if (drs != null && drs.length > 0) {
                 Gainer thisGainer = new Gainer(tickers.get(i), 
                         drs[0].getOhlc()[3], 
                         searchPatternService.getDaysPercentageChange(drs, 0, 1),
                         searchPatternService.getDaysPercentageChange(drs, 0, 5),
                         searchPatternService.getDaysPercentageChange(drs, 0, 22),
                         searchPatternService.getDaysPercentageChange(drs, 0, 66),
                         searchPatternService.getDaysPercentageChange(drs, 0, 126),
                         searchPatternService.getDaysPercentageChange(drs, 0, 252),
                         drs[0].getOhlc()[3] * drs[0].getVol()
                         );
                 updateGainList(thisGainer, dayList, ListIndex.DAY);
                 updateGainList(thisGainer, weekList, ListIndex.WEEK);
                 updateGainList(thisGainer, monthList, ListIndex.MONTH);
                 updateGainList(thisGainer, month3List, ListIndex.MONTH3);
                 updateGainList(thisGainer, month6List, ListIndex.MONTH6);
                 updateGainList(thisGainer, yearList, ListIndex.YEAR);
                 updateGainList(thisGainer, pvList, ListIndex.PV);
             }                
         
             int scoreBullish = 0;
             int scoreBearish = 0;
             int scoreVol = filterService.getVolSpikeScore(drs); 
            
             if (filterService.isBullish(drs, 20, 0)) scoreBullish++; 
             if (filterService.isBullish(drs, 40, 0)) scoreBullish++; 
             if (filterService.isBullish(drs, 60, 0)) scoreBullish++; 
             if (filterService.isBullish(drs, 80, 0)) scoreBullish++; 
             if (filterService.isBullish(drs, 100, 0)) scoreBullish++; 
             if (filterService.isBullish(drs, 120, 0)) scoreBullish++;
             if (filterService.isBullish(drs, 40, 20)) scoreBullish++; 
             if (filterService.isBullish(drs, 40, 40)) scoreBullish++; 
             if (filterService.isBullish(drs, 40, 60)) scoreBullish++; 
             if (filterService.isBullish(drs, 40, 80)) scoreBullish++;
             
             if (filterService.isBearish(drs, 20, 0)) scoreBearish++;
             if (filterService.isBearish(drs, 40, 0)) scoreBearish++;
             if (filterService.isBearish(drs, 60, 0)) scoreBearish++;
             if (filterService.isBearish(drs, 80, 0)) scoreBearish++;
             if (filterService.isBearish(drs, 100, 0)) scoreBearish++;
             if (filterService.isBearish(drs, 120, 0)) scoreBearish++;
             if (filterService.isBearish(drs, 40, 20)) scoreBearish++;
             if (filterService.isBearish(drs, 40, 40)) scoreBearish++;
             if (filterService.isBearish(drs, 40, 60)) scoreBearish++;
             if (filterService.isBearish(drs, 40, 80)) scoreBearish++;
             
             if (filterService.isBigStock(drs)) {
                 scoreBullish++;
                 scoreBearish++;
             }
             if (filterService.isBigStock2(drs)) {
                 scoreBullish++;
                 scoreBearish++;
             }
             
             System.out.println("***************************" + tickers.get(i));                
             
             // TODO: calculate Best Bullish etc and save
             if (scoreBullish > 7) bestBullishRepository.save(new BestBullish(tickers.get(i), scoreBullish));
             
             if ( scoreBearish > 7 ) { 
                 bestBearishRepository.save(new BestBearish(tickers.get(i), scoreBearish));
             } 
             
             if ( scoreVol > 2 ) volSpikeRepository.save(new VolSpike(tickers.get(i), scoreVol));
             
            // save breakout if happen in 5 days, save breakout if happen in 5 days
            
             if (filterService.isBreakout(drs, 250, 0) || filterService.isBreakout(drs, 120, 0)) {
                 breakoutRepository.save(new Breakout(tickers.get(i)));                   
             }                               


             if (filterService.isDoubleBottom(drs, 0)) {
                 doubleBottomRepository.save(new DoubleBottom(tickers.get(i))); 
             }

             // Pattern Ticker Found
             TickerFound tf = new TickerFound();            
             try {
                 tf = searchPatternService.findPatternsAndCalcPerformance(tickers.get(i), drs, patterns);                
             } catch (Exception e) {
                 e.printStackTrace();
             }            
             if (tf != null) {
                 patternService.saveTicker(tf);
             } 
             
             // Afilter Ticker Found
             
             
             TickerAfilterSet ta = new TickerAfilterSet();
             try {
                 ta = afilterService.findAfilterSetsAndCalcPerformance(tickers.get(i), drs, afilterSets);                     
             } catch (Exception e) {
                 e.printStackTrace();
             }            
             if (ta != null) {
                 afilterService.saveTickerAfilterSet(ta);
             } 
             
             
             totalTickerScanned++;
             
             if (i % 500 == 0) {
                 for (Pattern p : patterns) {                    
                     patternService.updatePatternPerformance(p, totalTickerScanned); // save to DB
                     patterns = this.patternService.getAllPatterns();  // update patterns so invalid pattern will be skiped
                     patterns.removeIf(pp -> !pp.isValidPattern());
                 } 
                 for (AfilterSet f : afilterSets) {                    
                     afilterService.saveAfilterSetPerformance(f, totalTickerScanned); // save to DB
                     afilterSets = this.afilterService.getAllAfilterSets(); 
                     afilterSets.removeIf(ff -> !ff.isValid());        
                 } 
             }
             
          } catch (Exception e) {
             System.out.println("StockScanTask Exception: " + e);
        }
               
     } // END OF SCAN FOR LOOP
     
     // save performance after all ticker scanned
     for (Pattern p : patterns) {
         System.out.println("p.getOccurrence(): " + p.getOccurrence());
         patternService.updatePatternPerformance(p, totalTickerScanned); // save to DB
     } 
     for (AfilterSet f : afilterSets) {                    
         afilterService.saveAfilterSetPerformance(f, totalTickerScanned); // save to DB
     }       

     gainerRepository.deleteAll();
     gainerRepository.saveAll(getAllCombinedGainerList());
     
     // remove invalid founds
     cleanTickerFounds();
     cleanTickerAfilterSet();
     
     
 } // END OF scanStock() function

*/


