package catchthepattern.com.services;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import catchthepattern.com.models.DayRecord;
import catchthepattern.com.models.Strategy;

@Service
public class DataFormatService {

    @Autowired
    private AlphaService alphaService;
    
    @Autowired
    private Utils utils;
    
    
    // stock data 20 days only, no ma10 etc, used to skip penny and generate scan list
    public DayRecord[] get20DayRecordsFromRawArr(String[] stockDataArr) {

        if (stockDataArr == null || stockDataArr.length < 20) return null;        

        LocalDate stockLastDay = LocalDate.parse(stockDataArr[0].split(",")[0]);
        LocalDate day5 = LocalDate.now().minus(Period.ofDays(5));

        if (stockLastDay.compareTo(day5) < 0) {
            System.out.println("Old stock 31, skip: ");
            return null;
        }
        
        DayRecord[] drs = new DayRecord[32];

        try {
            for (int i = 0; i < 32; i++) {
                try {
                    String[] thisRow = stockDataArr[i].split(",");

                    if (thisRow == null || thisRow.length == 0) {
                        System.out.println("*************DATA ERROR: Empty record! " + i);
                        continue;
                    }

                    final String date = thisRow[0];
                    final double splitRate = Double.parseDouble(thisRow[5]) / Double.parseDouble(thisRow[4]);
                    final double[] ohlc = { Double.parseDouble(thisRow[1]) * splitRate,
                            Double.parseDouble(thisRow[2]) * splitRate, Double.parseDouble(thisRow[3]) * splitRate,
                            Double.parseDouble(thisRow[5]) };
                    final int vol = Integer.parseInt(thisRow[6]);
                    DayRecord dr = new DayRecord();
                    dr.setDate(date);
                    dr.setOhlc(ohlc);
                    dr.setVol(vol);
                 
                    drs[i] = dr;

                } catch (Exception e) {
                    System.out.println("EXCEPTION Generating day record");
                    System.out.println(e);
                }
            }
            return drs;
        } catch (Exception e) {
            System.out.println("EXCEPTION Generating drs");
            System.out.println(e);
            return null;
        }

    }
    
    public DayRecord[] get60DayRecordsFromRawArr(String[] stockDataArr) {

        if (stockDataArr == null || stockDataArr.length < 60) return null;        

        LocalDate stockLastDay = LocalDate.parse(stockDataArr[0].split(",")[0]);
        LocalDate day5 = LocalDate.now().minus(Period.ofDays(5));

        if (stockLastDay.compareTo(day5) < 0) {
            System.out.println("Old stock 82, skip: ");
            return null;
        }
        
        DayRecord[] drs = new DayRecord[60];

        try {
            for (int i = 0; i < 60; i++) {
                try {
                    String[] thisRow = stockDataArr[i].split(",");

                    if (thisRow == null || thisRow.length == 0) {
                        System.out.println("*************DATA ERROR: Empty record! " + i);
                        continue;
                    }

                    final String date = thisRow[0];
                    final double splitRate = Double.parseDouble(thisRow[5]) / Double.parseDouble(thisRow[4]);
                    final double[] ohlc = { Double.parseDouble(thisRow[1]) * splitRate,
                            Double.parseDouble(thisRow[2]) * splitRate, Double.parseDouble(thisRow[3]) * splitRate,
                            Double.parseDouble(thisRow[5]) };
                    final int vol = Integer.parseInt(thisRow[6]);
                    DayRecord dr = new DayRecord();
                    dr.setDate(date);
                    dr.setOhlc(ohlc);
                    dr.setVol(vol);
                 
                    drs[i] = dr;

                } catch (Exception e) {
                    System.out.println("EXCEPTION Generating day record");
                    System.out.println(e);
                }
            }
            return drs;
        } catch (Exception e) {
            System.out.println("EXCEPTION Generating drs");
            System.out.println(e);
            return null;
        }

    }
    

    // stockDataArr is array of stock data in original format, metadata has been
    // removed
    //public DayRecord[] getDayRecordsFromRawArrs(String[] stockDataArr, String[] ma10Arr, String[] ma50Arr, String[] ma250Arr) {
    public DayRecord[] getDayRecordsFromRawArrs(String[] stockDataArr) {

        if (stockDataArr == null || stockDataArr.length < 10) return null;        
        try {
            LocalDate stockLastDay = LocalDate.parse(stockDataArr[0].split(",")[0]);
            LocalDate day10 = LocalDate.now().minus(Period.ofDays(10));

            if (stockLastDay.compareTo(day10) < 0) {
                System.out.println("getDayRecordsFromRawArrs - Old stock, skip");
                return null;
            }
        } catch(Exception e) {
            System.out.println("EXCEPTION : DataFormatService - getDayRecordsFromRawArrs()");
            System.out.println(e);
        }
       
/*
        System.out.println("stockDataArr.length: " + stockDataArr.length);
        
        if (ma10Arr == null ) { System.out.println("ma10Arr is null " ); } else { System.out.println("ma10Arr.length: " + ma10Arr.length); }         
        if (ma50Arr == null ) { System.out.println("ma50Arr is null " ); } else { System.out.println("ma50Arr.length: " + ma50Arr.length); }        
        if (ma250Arr == null ) { System.out.println("ma250Arr is null " ); } else { System.out.println("ma250Arr.length: " + ma250Arr.length); }
*/
        // in case the lengths are different
        /*
        int min_length = ma10Arr == null? stockDataArr.length : Math.min(stockDataArr.length, ma10Arr.length);
        min_length = ma50Arr == null? min_length :  Math.min(min_length, ma50Arr.length);
        min_length = ma250Arr == null? min_length : Math.min(min_length, ma250Arr.length);
        */
        
        int min_length = stockDataArr.length;

      //  System.out.println("min_length: " + min_length);
        
        DayRecord[] drs = new DayRecord[min_length];

        try {
            for (int i = 0; i < min_length; i++) {
                try {
                    String[] thisRow = stockDataArr[i].split(",");

                    if (thisRow == null || thisRow.length == 0) {
                        System.out.println("*************DATA ERROR: Empty record! " + i);
                        continue;
                    }

                    final String date = thisRow[0];
                    final double splitRate = Double.parseDouble(thisRow[5]) / Double.parseDouble(thisRow[4]);
                    final double[] ohlc = { 
                          utils.get2DecimalPlaces(Double.parseDouble(thisRow[1]) * splitRate),
                          utils.get2DecimalPlaces(Double.parseDouble(thisRow[2]) * splitRate), 
                          utils.get2DecimalPlaces(Double.parseDouble(thisRow[3]) * splitRate),
                          utils.get2DecimalPlaces(Double.parseDouble(thisRow[5])) 
                          };
                    final int vol = Integer.parseInt(thisRow[6]);
                    DayRecord dr = new DayRecord();
                    dr.setDate(date);
                    dr.setOhlc(ohlc);
                    dr.setVol(vol);

                    dr.setTop(Math.max(dr.getOhlc()[0], dr.getOhlc()[3]));
                    dr.setBottom(Math.min(dr.getOhlc()[0], dr.getOhlc()[3]));
                    dr.setMiddle(utils.get2DecimalPlaces((dr.getOhlc()[0] + dr.getOhlc()[3]) / 2));

                    // now MA10 MA50 MA250
                    /*
                     * System.out.println(date);
                     * System.out.println("splitRate: " + splitRate);
                     * System.out.println(ohlc[3]);
                     * System.out.println(ma10Arr[i]);
                     * System.out.println(ma50Arr[i]);
                     * System.out.println(ma250Arr[i]);
                     */
                    
                    /*
                    if (ma10Arr == null || ma10Arr.length == 1) dr.setMa10(0.0); 
                    else dr.setMa10(Double.parseDouble(ma10Arr[i].split(",")[1]));
                    if (ma50Arr == null || ma50Arr.length == 1) dr.setMa50(0.0); 
                    else dr.setMa50(Double.parseDouble(ma50Arr[i].split(",")[1]));
                    if (ma250Arr == null || ma250Arr.length == 1) dr.setMa250(0.0); 
                    else dr.setMa250(Double.parseDouble(ma250Arr[i].split(",")[1]));
                    */

                    drs[i] = dr;

                } catch (Exception e) {
                   // System.out.println("EXCEPTION DataFormatService - getDayRecordsFromRawArrs 160");
                  //  System.out.println(e);
                }
            }
            return drs;
        } catch (Exception e) {
            System.out.println("EXCEPTION DataFormatService - getDayRecordsFromRawArrs 166");
            System.out.println(e);
            return null;
        }

    }

    /*
     * public DayRecord[] addExtraFields(DayRecord[] drs) {
     * if (drs != null && drs.length > 0) {
     * for (int i = 0; i< drs.length; i++) {
     * drs[i].setTop(Math.max(drs[i].getOhlc()[0], drs[i].getOhlc()[3]));
     * drs[i].setBottom(Math.min(drs[i].getOhlc()[0], drs[i].getOhlc()[3]));
     * drs[i].setMiddle((drs[i].getOhlc()[0] + drs[i].getOhlc()[3])/2);
     * 
     * drs[i].setMa10(getMa10(drs, i));
     * drs[i].setMa50(getMa50(drs, i));
     * drs[i].setMa250(getMa250(drs, i));
     * 
     * }
     * }
     * // setAvgs(drs, 10);
     * // setAvgs(drs, 50);
     * //setAvgs(drs, 250);
     * return drs;
     * }
     */

    /*
     * public double getMa10(DayRecord[] drs, int day) {
     * return 0.0;
     * }
     * 
     * public double getMa50(DayRecord[] drs, int day) {
     * return 0.0;
     * }
     * 
     * public double getMa250(DayRecord[] drs, int day) {
     * return 0.0;
     * }
     */

    // return original stock data array, first line removed
    // FE is not using this service
    public String[] getRawDataArr(String ticker) throws Exception {
        String stockData = alphaService.getStock(ticker, false);
        String[] dataArrOri = stockData.split("\\r?\\n");
        
        // system.out.println("dataArrOri.length: " + dataArrOri.length);
        
        if (dataArrOri == null || dataArrOri.length < 10)
            return null;
        return Arrays.copyOfRange(dataArrOri, 1, Math.min(1260, dataArrOri.length) ); // remove first line and return recent 5 years
    }   


    public String[] getRawMaDataArr5Years(String ticker, String interval, String time_period) throws Exception {
        String stockData = alphaService.getStockMa(ticker, interval, time_period);
        String[] dataArrOri = stockData.split("\\r?\\n");
        if (dataArrOri == null || dataArrOri.length < 10)
            return null;
        return Arrays.copyOfRange(dataArrOri, 1, Math.min(1260, dataArrOri.length)); // remove first line and return recent 5 years
    }
    
    // For FE
    public String[] getRawDataArrOneYear(String ticker) throws Exception {
        String stockData = alphaService.getStock(ticker, false);
        String[] dataArrOri = stockData.split("\\r?\\n");
        if (dataArrOri == null || dataArrOri.length < 10)
            return null;
        return Arrays.copyOfRange(dataArrOri, 1, Math.min(253, dataArrOri.length) ); // remove first line and return recent 1 years
    }
    
    // For FE, getRawMaDataArrOneYear("TSLA", "daily", "50")
    public String[] getRawMaDataArrOneYear(String ticker, String interval, String time_period) throws Exception {
        String stockData = alphaService.getStockMa(ticker, interval, time_period);
        String[] dataArrOri = stockData.split("\\r?\\n");
        if (dataArrOri == null || dataArrOri.length < 10)
            return null;
        return Arrays.copyOfRange(dataArrOri, 1, Math.min(253, dataArrOri.length)); // remove first line and return recent 1 years
    }
    
    // For FE request only, daily only, return one year stock data with ma 10 50 250
    public DayRecord[] getStockWithMa(String ticker) throws Exception {
        String[] stockData = getRawDataArrOneYear(ticker);
        /*
        String[] ma10s = getRawMaDataArrOneYear(ticker, "daily", "10");
        String[] ma50s = getRawMaDataArrOneYear(ticker, "daily", "50");
        String[] ma250s = getRawMaDataArrOneYear(ticker, "daily", "250");   
        */     
        DayRecord[] drs = getDayRecordsFromRawArrs(stockData);        
        return drs;
    }
    
    // called from scheduled stock scan task
    public DayRecord[] getUpdatedDayRecords(String ticker, DayRecord[] oriDayRecords) throws Exception {
        String[] dataArr = getRawDataArr(ticker);        
        DayRecord[] drs = get20DayRecordsFromRawArr(dataArr); 
        
        // system.out.println("getUpdatedDayRecords: " + drs[0].getDate());
        // system.out.println("last date in drs: " + (oriDayRecords[0].getDate()));
        
        // TODO: check if last date is same        
        if (drs[0].getDate().equals(oriDayRecords[0].getDate())) {
            return oriDayRecords; // is up to dates
        }
        
        DayRecord[] newArr = new DayRecord[oriDayRecords.length];        
        System.arraycopy(oriDayRecords, 0, newArr, 1, oriDayRecords.length-1); // copy full array except last one, put into new array from index 1 
        
        
//        drs[0].setMa10(getMaValue(drs, 10));
//        drs[0].setMa50(getMaValue(drs, 50));
//        drs[0].setMa250(getMaValue(drs, 250));
//        
        drs[0].setTop(Math.max(drs[0].getOhlc()[0], drs[0].getOhlc()[3]));
        drs[0].setBottom(Math.min(drs[0].getOhlc()[0], drs[0].getOhlc()[3]));
        drs[0].setMiddle((drs[0].getOhlc()[0] + drs[0].getOhlc()[3])/2);
        
        newArr[0] = drs[0];
        return newArr;
    }
    
    public double getMaValue(DayRecord[] drs, int maDays) {
        if (maDays == 0) return 0.0;
        int count = Math.min(maDays, drs.length);        
        double sum = 0.0;
        for (int i=0; i<count; i++ ) { sum =+ drs[i].getOhlc()[3]; }
        return utils.get2DecimalPlaces(maDays/count);
    }
    
    // return average (low - previous close)/(previous close), should be negative number
	public double getBestOrderPrice(String ticker) {
		String[] dataArr;
		double totalSecondDayDrop = 0.0;
		int count = 0;
		try {
			dataArr = getRawDataArr(ticker);
	        if( dataArr== null || dataArr.length < 60) {
	            return 0.0;
	        }
	        DayRecord[] drs = getDayRecordsFromRawArrs(dataArr);
	        for (int i=0; i<60;i++) {
	        	totalSecondDayDrop += ((drs[i].getOhlc()[2] - drs[i+1].getOhlc()[3]) / drs[i+1].getOhlc()[3]);
	        	count++;
	        }
	        if (count != 0) { // the expected result
	        	
	        	System.out.println(ticker + " average drop : " + (totalSecondDayDrop / count));
	        	
	        	return (drs[0].getOhlc()[3] * ( 1 + (totalSecondDayDrop / count)));
	        }
	        else return  0.0;
	        
	        
		} catch (Exception e) {
			e.printStackTrace();
		}
        
		return 0.0;
	}
	
	public double getBestOrderPrice_median(String ticker) {
		String[] dataArr;
		List<Double> SecondDayDrops = new ArrayList<Double>();
		try {
			dataArr = getRawDataArr(ticker);
	        if( dataArr== null || dataArr.length < 60) {
	            return 0.0;
	        }
	        DayRecord[] drs = getDayRecordsFromRawArrs(dataArr);
	        for (int i=0; i<60;i++) {
	        	SecondDayDrops.add((drs[i].getOhlc()[2] - drs[i+1].getOhlc()[3]) / drs[i+1].getOhlc()[3]);
	        }
	        double medianDrop = Utils.findMedian(SecondDayDrops);
	        	
	        return (drs[0].getOhlc()[3] * ( 1 + medianDrop));
	        
	        
		} catch (Exception e) {
			e.printStackTrace();
		}
        
		return 0.0;
	}
	
	public double getBestSalePrice(String ticker) {
		String[] dataArr;
		double totalSecondDayRise = 0.0;
		int count = 0;
		try {
			dataArr = getRawDataArr(ticker);
	        if( dataArr== null || dataArr.length < 60) {
	            return 0.0;
	        }
	        DayRecord[] drs = getDayRecordsFromRawArrs(dataArr);
	        for (int i=0; i<60;i++) {
	        	totalSecondDayRise += ((drs[i].getOhlc()[1] - drs[i+1].getOhlc()[3]) / drs[i+1].getOhlc()[3]);
	        	count++;
	        }
	        if (count != 0) { // the expected result
	        	double avgRise = totalSecondDayRise / count;
	        	
	        	System.out.println(ticker + " average rise : " + avgRise);
	        	
	        	if (avgRise > 0.017) return (drs[0].getOhlc()[3] * ( 1 + (totalSecondDayRise / count)));
	        	else return drs[0].getOhlc()[3] * 1.017;
	        }
	        else return  0.0;
	        
	        
		} catch (Exception e) {
			e.printStackTrace();
		}
        
		return 0.0;
	}
	
	public double getBestSalePrice_median(String ticker) {
		String[] dataArr;
		List<Double> SecondDayRises = new ArrayList<Double>();
		try {
			dataArr = getRawDataArr(ticker);
	        if( dataArr== null || dataArr.length < 60) {
	            return 0.0;
	        }
	        DayRecord[] drs = getDayRecordsFromRawArrs(dataArr);
	        for (int i=0; i<60;i++) {
	        	SecondDayRises.add((drs[i].getOhlc()[1] - drs[i+1].getOhlc()[3]) / drs[i+1].getOhlc()[3]);
	        }
	        
	        double medianRise = Utils.findMedian(SecondDayRises);
	        
	        if (medianRise > 0.017) medianRise = (medianRise + 0.017)/2; // make sales quicker. 8.4% and 1.7% is the average best turn around sale rate
        	
	        System.out.println(ticker + " median rise : " + medianRise);
	        	
	        return (drs[0].getOhlc()[3] * ( 1 + medianRise));
	        
		} catch (Exception e) {
			e.printStackTrace();
		}
        
		return 0.0;
	}
	
	public double getBestSalePriceBasedOnOrderPrice(double orderPrice, String ticker) {
		double result = 0.0;
		double minimumSalePrice = orderPrice * 1.017; 
		
		String[] dataArr;
		List<Double> secondDayRises = new ArrayList<Double>();
		try {
			dataArr = getRawDataArr(ticker);
	        if( dataArr== null || dataArr.length < 61) {
	            return 0.0;
	        }
	        DayRecord[] drs = getDayRecordsFromRawArrs(dataArr);
	        for (int i=0; i<60;i++) {
	        	secondDayRises.add((drs[i].getOhlc()[1] - drs[i+1].getOhlc()[3]) / drs[i+1].getOhlc()[3]);
	        }
	       
	        double medianRise = Utils.findMedian(secondDayRises);
	        result = (drs[0].getOhlc()[3] * ( 1 + medianRise));
	        result = result > minimumSalePrice ? (result + (minimumSalePrice * 2) )/3 : minimumSalePrice;
	        	
	        return result;
	        
		} catch (Exception e) {
			e.printStackTrace();
		}
        
		return 0.0;
	}
	
	public Strategy getBestStrategy(DayRecord[] drs, int startDay, int tradingDays) {
		Strategy finalStrategy = new Strategy();
		double result = 0;
		
		try {
			
	        
	        for (double buy = 0.95; buy < 1; buy=buy+0.005) {
				for (double sell = 1.005; sell < 1.25; sell=sell+0.005) {
					for (double trail = 0.02; trail < 0.3; trail=trail+0.005) {
						Strategy thisStrategy = new Strategy(0, buy, 0, trail, 0, sell, 0, 0, "", "", "", "", 0, tradingDays, startDay, 0.0);
						double thisResult = getStrategyPerformance(drs, thisStrategy);
						if (thisResult > result) {
							result = thisResult;
							finalStrategy = new Strategy(thisStrategy);
						}
					}
				}
			}
		} catch (Exception e) {}
		return finalStrategy;
	}
	
	
	public void testStrategyFromPreviousPeriod(String tickerInput) {
		String[] tickers = tickerInput.split(",");
		
		List<Strategy> strategies = new ArrayList<>();
		double r1 = 1;
		double r2 = 2;
		
		
		for (String ticker: tickers) {
			try {
				String[] dataArr = getRawDataArr(ticker);
		        if( dataArr== null) {
		            continue;
		        }
		        DayRecord[] drs = getDayRecordsFromRawArrs(dataArr);
		        
		        
		        for (int i = 20; i >1; i--) {
		        	Strategy sg1 = getBestStrategy(drs, 60 * i + 252, 252);
		        	sg1.setStartDay(60*(i-1));
		        	sg1.setTradingDays(60);
		        	getStrategyPerformance(drs, sg1);
		        	strategies.add(new Strategy(sg1));
					
		        }
		        
		        for (Strategy sg: strategies) {
		        	printStrategy(sg);
		        	r1 = r1 * (1+sg.getResultPercentage());
		        	r2 = r2 * (1+sg.getBuyHoldResultPercentage());
		        }
		        
		        System.out.println("strategy performance: " + r1);
		        System.out.println("B & H performance: " + r2);
		        
			} catch(Exception e) {}
		}
		
	}
	
	// localhost:5000/open/testStrategy/TSLA
	// localhost:5000/open/testStrategy/SPY,TSLA,AAPL
	
	public void testBestStrategyInDifferentPeriod(String tickerInput) {
		
		String[] tickers = tickerInput.split(",");
		
		int tradingDays = 60;
		int startDay = 60;
		
		List<Strategy> strategies = new ArrayList<>();
		
		
		for (String ticker: tickers) {
			System.out.println("==============\n" + ticker + "\n");
			
			double result = 0;
			Strategy finalStrategy = new Strategy();
			
			try {
				String[] dataArr = getRawDataArr(ticker);
		        if( dataArr== null || dataArr.length < startDay + 1) {
		            continue;
		        }
		        DayRecord[] drs = getDayRecordsFromRawArrs(dataArr);
		        
		        for (double buy = 0.95; buy < 1; buy=buy+0.005) {
					for (double sell = 1.005; sell < 1.25; sell=sell+0.005) {
						for (double trail = 0.02; trail < 0.2; trail=trail+0.005) {
							Strategy thisStrategy = new Strategy(0, buy, 0, trail, 0, sell, 0, 0, "", "", "", "", 0, tradingDays, startDay, 0.0);
							double thisResult = getStrategyPerformance(drs, thisStrategy);
							if (thisResult > result) {
								result = thisResult;
								finalStrategy = new Strategy(thisStrategy);
								
							}
						}
					}
				}
		        
		        finalStrategy.setTicker(ticker);
		        /* These are already done 
		        double finalResultPercentage = result / (drs[startDay].getOhlc()[3]);
		        double buyHoldResultPercentage = (drs[0].getOhlc()[3] - drs[startDay].getOhlc()[3])/drs[0].getOhlc()[3]; // no strategy performance
		        
		        finalStrategy.setResultPercentage(finalResultPercentage);
		        */
		        
		        if (true || finalStrategy.getResultPercentage() > finalStrategy.getBuyHoldResultPercentage() && 
		        		finalStrategy.getCountTransactions() > 9 && 
		        		finalStrategy.getResultPercentage() > 0.29) {
		        	 strategies.add(finalStrategy);
		        }
		       
		        strategies.sort(Comparator.comparingDouble(p -> p.getResultPercentage()));
			} catch (Exception e) {}
		}

		for (Strategy sg: strategies) {
        	// System.out.println(sg.getTicker() + " : " + sg.getResultPercentage());
        	printStrategy(sg);
        }
		
		// now we have the best strategy for recent 60 days, use it to trade for 120-60, 180-120, etc
		
		
		try {
			String[] dataArr = getRawDataArr(tickers[0]);
	        if( dataArr== null || dataArr.length < startDay + 1) {
	            return;
	        }
	        DayRecord[] drs = getDayRecordsFromRawArrs(dataArr);
	        
	        double p1 = 1;
	        double p2 = 1;
	        
	        for (int i = 2; i < 10; i ++) {
	        	Strategy sg1 = new Strategy(strategies.get(0));
				sg1.setStartDay(60 * i);
				sg1.setTradingDays(60);
				getStrategyPerformance(drs, sg1);		
				printStrategy(sg1);
				p1 = p1 * (1+sg1.getResultPercentage());
				p2 = p2 * (1+sg1.getBuyHoldResultPercentage());
	        }
	        
	        System.out.println("strategy performance: " + p1);
	        System.out.println("B & H performance: " + p2);
			
		} catch(Exception e) {}
		
		
	}
	
	
	public void testStrategy(String tickerInput) {
		//Strategy testStrategy = new Strategy(0, 0.97, 0, 0.284, 0, 1.03, 0, 0, "", "", "");
		// (double buy, double buyPercentage, double trail, double trailPercentage, double sell, double sellPercentage, double result, double resultPercentage, String status)
	
		String sp500 = "MMM,AOS,ABT,ABBV,ACN,ADBE,AMD,AES,AFL,A,APD,ABNB,AKAM,ALB,ARE,ALGN,ALLE,LNT,ALL,GOOGL,GOOG,MO,AMZN,AMCR,AEE,AEP,AXP,AIG,AMT,AWK,AMP,AME,AMGN,APH,ADI,AON,APA,APO,AAPL,AMAT,APTV,ACGL,ADM,ANET,AJG,AIZ,T,ATO,ADSK,ADP,AZO,AVB,AVY,AXON,BKR,BALL,BAC,BAX,BDX,BRK.B,BBY,TECH,BIIB,BLK,BX,BK,BA,BKNG,BSX,BMY,AVGO,BR,BRO,BF.B,BLDR,BG,BXP,CHRW,CDNS,CZR,CPT,CPB,COF,CAH,KMX,CCL,CARR,CAT,CBOE,CBRE,CDW,COR,CNC,CNP,CF,CRL,SCHW,CHTR,CVX,CMG,CB,CHD,CI,CINF,CTAS,CSCO,C,CFG,CLX,CME,CMS,KO,CTSH,COIN,CL,CMCSA,CAG,COP,ED,STZ,CEG,COO,CPRT,GLW,CPAY,CTVA,CSGP,COST,CTRA,CRWD,CCI,CSX,CMI,CVS,DHR,DRI,DDOG,DVA,DAY,DECK,DE,DELL,DAL,DVN,DXCM,FANG,DLR,DG,DLTR,D,DPZ,DASH,DOV,DOW,DHI,DTE,DUK,DD,EMN,ETN,EBAY,ECL,EIX,EW,EA,ELV,EMR,ENPH,ETR,EOG,EPAM,EQT,EFX,EQIX,EQR,ERIE,ESS,EL,EG,EVRG,ES,EXC,EXE,EXPE,EXPD,EXR,XOM,FFIV,FDS,FICO,FAST,FRT,FDX,FIS,FITB,FSLR,FE,FI,F,FTNT,FTV,FOXA,FOX,BEN,FCX,GRMN,IT,GE,GEHC,GEV,GEN,GNRC,GD,GIS,GM,GPC,GILD,GPN,GL,GDDY,GS,HAL,HIG,HAS,HCA,DOC,HSIC,HSY,HES,HPE,HLT,HOLX,HD,HON,HRL,HST,HWM,HPQ,HUBB,HUM,HBAN,HII,IBM,IEX,IDXX,ITW,INCY,IR,PODD,INTC,ICE,IFF,IP,IPG,INTU,ISRG,IVZ,INVH,IQV,IRM,JBHT,JBL,JKHY,J,JNJ,JCI,JPM,K,KVUE,KDP,KEY,KEYS,KMB,KIM,KMI,KKR,KLAC,KHC,KR,LHX,LH,LRCX,LW,LVS,LDOS,LEN,LII,LLY,LIN,LYV,LKQ,LMT,L,LOW,LULU,LYB,MTB,MPC,MKTX,MAR,MMC,MLM,MAS,MA,MTCH,MKC,MCD,MCK,MDT,MRK,META,MET,MTD,MGM,MCHP,MU,MSFT,MAA,MRNA,MHK,MOH,TAP,MDLZ,MPWR,MNST,MCO,MS,MOS,MSI,MSCI,NDAQ,NTAP,NFLX,NEM,NWSA,NWS,NEE,NKE,NI,NDSN,NSC,NTRS,NOC,NCLH,NRG,NUE,NVDA,NVR,NXPI,ORLY,OXY,ODFL,OMC,ON,OKE,ORCL,OTIS,PCAR,PKG,PLTR,PANW,PARA,PH,PAYX,PAYC,PYPL,PNR,PEP,PFE,PCG,PM,PSX,PNW,PNC,POOL,PPG,PPL,PFG,PG,PGR,PLD,PRU,PEG,PTC,PSA,PHM,PWR,QCOM,DGX,RL,RJF,RTX,O,REG,REGN,RF,RSG,RMD,RVTY,ROK,ROL,ROP,ROST,RCL,SPGI,CRM,SBAC,SLB,STX,SRE,NOW,SHW,SPG,SWKS,SJM,SW,SNA,SOLV,SO,LUV,SWK,SBUX,STT,STLD,STE,SYK,SMCI,SYF,SNPS,SYY,TMUS,TROW,TTWO,TPR,TRGP,TGT,TEL,TDY,TER,TSLA,TXN,TPL,TXT,TMO,TJX,TKO,TTD,TSCO,TT,TDG,TRV,TRMB,TFC,TYL,TSN,USB,UBER,UDR,ULTA,UNP,UAL,UPS,URI,UNH,UHS,VLO,VTR,VLTO,VRSN,VRSK,VZ,VRTX,VTRS,VICI,V,VST,VMC,WRB,GWW,WAB,WBA,WMT,DIS,WBD,WM,WAT,WEC,WFC,WELL,WST,WDC,WY,WSM,WMB,WTW,WDAY,WYNN,XEL,XYL,YUM,ZBRA,ZBH,ZTS";
		String hotTickers = "OKLO,IONQ,RGTI,CRCL,HOOD";
		String tickersToScan = sp500 + "," + hotTickers;
		
		tickersToScan = "SPY,QQQ,AAPL,TSLA,AMZN,META,MSFT,GOOG,NVDA";
		tickersToScan = "TSLA";
		
		// String[] tickers = tickersToScan.split(",");
		String[] tickers = tickerInput.split(",");
		
		int tradingDays = 67;
		int startDay = 267;
		
		List<Strategy> strategies = new ArrayList<>();
		
		
		for (String ticker: tickers) {
			System.out.println("==============\n" + ticker + "\n");
			
			double result = 0;
			Strategy finalStrategy = new Strategy();
			
			try {
				String[] dataArr = getRawDataArr(ticker);
		        if( dataArr== null || dataArr.length < startDay + 1) {
		            continue;
		        }
		        DayRecord[] drs = getDayRecordsFromRawArrs(dataArr);
		        
		        for (double buy = 0.95; buy < 1; buy=buy+0.005) {
					for (double sell = 1.005; sell < 1.25; sell=sell+0.005) {
						for (double trail = 0.02; trail < 0.2; trail=trail+0.005) {
							Strategy thisStrategy = new Strategy(0, buy, 0, trail, 0, sell, 0, 0, "", "", "", "", 0, tradingDays, startDay, 0.0);
							double thisResult = getStrategyPerformance(drs, thisStrategy);
							if (thisResult > result) {
								result = thisResult;
								finalStrategy = new Strategy(thisStrategy);
								
							}
						}
					}
				}
		        
		        finalStrategy.setTicker(ticker);
		        /* These are already done 
		        double finalResultPercentage = result / (drs[startDay].getOhlc()[3]);
		        double buyHoldResultPercentage = (drs[0].getOhlc()[3] - drs[startDay].getOhlc()[3])/drs[0].getOhlc()[3]; // no strategy performance
		        
		        finalStrategy.setResultPercentage(finalResultPercentage);
		        */
		        
		        if (true || finalStrategy.getResultPercentage() > finalStrategy.getBuyHoldResultPercentage() && 
		        		finalStrategy.getCountTransactions() > 9 && 
		        		finalStrategy.getResultPercentage() > 0.29) {
		        	 strategies.add(finalStrategy);
		        }
		       
		        strategies.sort(Comparator.comparingDouble(p -> p.getResultPercentage()));
			} catch (Exception e) {}
		}

		for (Strategy sg: strategies) {
        	// System.out.println(sg.getTicker() + " : " + sg.getResultPercentage());
        	printStrategy(sg);
        }
	}
	
	
	// return actual strategy profit amount in the period, assume buying one stock 
	public double getStrategyPerformance(DayRecord[] drs, Strategy strategy) {
	    List<Strategy> strategies = new ArrayList<>();
	    double finalResult = 0.0;
	    double finalResultPercentage = 0.0;
	    int countTrans = 0;
	    int startDay = strategy.getStartDay();
	    int tradingDays = strategy.getTradingDays();
	    
	    if (startDay <=0 || tradingDays <=0 || startDay < tradingDays) return 0.0;
	    
	    double buyHoldResultPercentage = 0.0;
	    
	    for (int i = startDay; i > startDay - tradingDays; i--) {
	        for (Strategy sg : strategies) {
	            if ("filled".equals(sg.getStatus())) {
	                double high = drs[i].getOhlc()[1];
	                double low = drs[i].getOhlc()[2];
	                
	                if (high > sg.getSell() && low < sg.getTrail()) {
	                	double thisResult = ((sg.getSell() + sg.getTrail()) / 2) - sg.getBuy();
	                    sg.setResult(thisResult);
	                    sg.setResultPercentage(thisResult/sg.getBuy());
	                    sg.setSellDate(drs[i].getDate());
	                    sg.setStatus("sold");
	                    continue;
	                }
	                if (high > sg.getSell()) {
	                	double thisResult = sg.getSell() - sg.getBuy();
	                    sg.setResult(thisResult);
	                    sg.setResultPercentage(thisResult/sg.getBuy());
	                    sg.setTrail(0.0); // take profit sell
	                    sg.setSellDate(drs[i].getDate());
	                    sg.setStatus("sold");
	                    continue;
	                }
	                if (low < sg.getTrail()) {
	                	double thisResult = sg.getTrail() - sg.getBuy();
	                    sg.setResult(thisResult);
	                    sg.setResultPercentage(thisResult/sg.getBuy());
	                    sg.setSellDate(drs[i].getDate());
	                    sg.setSell(0.0); // trailing stop sell
	                    sg.setStatus("sold");
	                    continue;
	                }

	                // Update trailing stop if not sold
	                if (high * sg.getTrailPercentage() > sg.getTrail()) {
	                    sg.setTrail(high * sg.getTrailPercentage());
	                }
	                
	                // hold - filled but not sold, still update current result
	                sg.setResult(drs[i].getOhlc()[3] - sg.getBuy());
	                sg.setResultPercentage((drs[i].getOhlc()[3] - sg.getBuy())/sg.getBuy());
	            }
	        }

	        // Check for buy
	        
	        if (!hasPosition(strategies) &&  drs[i].getOhlc()[2] <= strategy.getBuyPercentage() * drs[i + 1].getOhlc()[3]) {
	            Strategy filledStrategy = new Strategy(strategy); // deep copy required
	            filledStrategy.setStatus("filled");
	            double buyPrice = strategy.getBuyPercentage() * drs[i + 1].getOhlc()[3];
	            filledStrategy.setBuy(buyPrice);
	            filledStrategy.setSell(buyPrice * filledStrategy.getSellPercentage());
	            filledStrategy.setTrail(drs[i].getOhlc()[2] * (1- filledStrategy.getTrailPercentage()));
	            filledStrategy.setBuyDate(drs[i].getDate());
	            strategies.add(filledStrategy);
	        }
	    }

	    for (Strategy sg : strategies) {
	    	//printStrategy(sg);
	    	if (sg.getStatus() == "filled" || sg.getStatus() == "sold") {
	    		finalResult += sg.getResult();
	    		countTrans++;
	    	}
	        
	    }
	    
	    strategy.setCountTransactions(countTrans);
	    
	    finalResultPercentage = finalResult / (drs[startDay].getOhlc()[3]);
	    
	    strategy.setResultPercentage(finalResultPercentage);
	    
	    buyHoldResultPercentage = (drs[startDay - tradingDays].getOhlc()[3] - drs[startDay].getOhlc()[3])/drs[0].getOhlc()[3];
	    
	    strategy.setBuyHoldResultPercentage(buyHoldResultPercentage);
	    
	    //if (buyHoldResultPercentage > finalResultPercentage) System.out.println("This strategy does not beat buy hold");
	    //else System.out.println("This strategy BEAT buy hold!");
	    
	   // printStrategy(strategy);
	   // System.out.println("Strategy result: " + finalResult + " " + finalResultPercentage);
	   // System.out.println("Total filled or sold: " + countTrans);
	    return finalResult;
	}
	
	private boolean hasPosition(List<Strategy> strategies) {
		 for (Strategy sg : strategies) {
	    	if (sg.getStatus() == "filled" ) {
	    		return true;
	    	}
		}
		return false;
	}
	
	private void printStrategy(Strategy sg) {
		System.out.println(
				"----------------\nticker : " + sg.getTicker() + "\nstatus: " + 
				sg.getStatus() + "\nbuy: " + 
				sg.getBuyDate() + " " + 
				sg.getBuy() + " " + sg.getBuyPercentage() + "\nsell: "  + sg.getSellDate() + " " + 
				sg.getSell() + " " + sg.getSellPercentage() + "\ntrail:" + 
				sg.getTrail() + " " + sg.getTrailPercentage() + "\nresult: " +
				sg.getResult() + " " + sg.getResultPercentage() + "\nBuy & Hold: "+
				sg.getBuyHoldResultPercentage() + "\nCount Transactions: "+
				sg.getCountTransactions() + "\nStart Day: "+
				sg.getStartDay() + "\nTrading Days: "+
				sg.getTradingDays()
				
				);
	}
	
	public Map<String, Double> getOrderPrices(String ticker) {
		Map<String, Double> orderPrices = new HashMap<>(); //order, takeProfit, trail, performance
		List<Map<String, Double>> orderPricePercentageList = new ArrayList(); //order (% of previous close), takeProfit (% of order), trail, performance
		String[] dataArr;
		
		try {
			dataArr = getRawDataArr(ticker);
	        if( dataArr== null || dataArr.length < 61) {
	            return orderPrices;
	        }
	        DayRecord[] drs = getDayRecordsFromRawArrs(dataArr);
	        for (int i=0; i<60;i++) {
	        	//secondDayRises.add((drs[i].getOhlc()[1] - drs[i+1].getOhlc()[3]) / drs[i+1].getOhlc()[3]);
	        	
	        	
	        	
	        	
	        }
	        
	        
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return orderPrices;
		
	}
	

}
