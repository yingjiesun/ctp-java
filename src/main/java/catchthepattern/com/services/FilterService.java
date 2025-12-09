package catchthepattern.com.services;

import org.springframework.stereotype.Service;

import catchthepattern.com.models.DayRecord;
import catchthepattern.com.models.Filterset;
import catchthepattern.com.models.Filterset.stockTrend;

@Service
public class FilterService {
    
    public boolean passAllFilters(DayRecord[] drs, Filterset fs, int day) {         
        if ( fs.getMinPrice() != -1 &&  drs[day].getOhlc()[3] < fs.getMinPrice() ) return false; // closing price too low
        if ( fs.getMaxPrice() != -1 && drs[day].getOhlc()[3] > fs.getMaxPrice() ) return false; // closing price too high
        if ( fs.getMinVol()  != -1 && drs[day].getVol() < fs.getMinVol() ) return false; // volume too low 
        if ( !fs.getOneMonth().equalsIgnoreCase(stockTrend.ANY.name()) && !getTrend(drs, 22, day).equalsIgnoreCase(fs.getOneMonth())) return false;      
        if ( !fs.getThreeMonth().equalsIgnoreCase(stockTrend.ANY.name()) &&  !getTrend(drs, 66, day).equalsIgnoreCase(fs.getThreeMonth())) return false;               
        if ( !fs.getSixMonth().equalsIgnoreCase(stockTrend.ANY.name()) &&  !getTrend(drs,176, day).equalsIgnoreCase(fs.getSixMonth())) return false;        
        if ( !fs.getOneYear().equalsIgnoreCase(stockTrend.ANY.name()) &&  !getTrend(drs, 252, day).equalsIgnoreCase(fs.getOneYear())) return false;       
        return true;
    }    
    
    public String getTrend(DayRecord[] drs, int tradingDays, int startDate) {
        if (drs == null || drs.length < tradingDays + startDate + 1) return stockTrend.CHOPPY.name(); 
        int step = tradingDays/5;
        double v0 = drs[startDate].getOhlc()[3];
        double v1 = drs[startDate + step].getOhlc()[3];
        double v2 = drs[startDate + 2*step].getOhlc()[3];
        double v3 = drs[startDate + 3*step].getOhlc()[3];
        double v4 = drs[startDate + 4*step].getOhlc()[3];
        double avg = (v0 + v1 + v2 + v3+ v4)/5;
        
        double minimumChange = tradingDays * 0.002; // 20% 100 days
        if (minimumChange < 0.1) minimumChange = 0.1; // at least 10% to be bullish or bearish
        
        if ( Math.abs((v0 - avg)/avg) < 0.01 &&
                Math.abs((v1 - avg)/avg) < 0.01 &&
                Math.abs((v2 - avg)/avg) < 0.01 &&
                Math.abs((v3 - avg)/avg) < 0.01 &&
                Math.abs((v4 - avg)/avg) < 0.01
        ) {
            return stockTrend.FLAT.name();
        } else if (
                v0 > v1 &&
                v1 > v2 &&
                v2 > v3 &&
                v3 > v4 &&
                Math.abs((v0 - v4)/v4) > minimumChange
                ) {
            return stockTrend.BULLISH.name();
        } else if (
                v0 < v1 &&
                v1 < v2 &&
                v2 < v3 &&
                v3 < v4 &&
                Math.abs((v0 - v4)/v4) > minimumChange
                ) {
            return stockTrend.BEARISH.name();
        } else {
            return stockTrend.CHOPPY.name();
        }
        
    }
    
    public boolean isBigStock(DayRecord[] drs) {  
        if (drs == null || drs.length < 16) return false; 
        return drs[0].getOhlc()[3] * drs[0].getVol() > 7500000;
     }
    
    public boolean isBigStock2(DayRecord[] drs) {  
        if (drs == null || drs.length < 16) return false; 
        return drs[0].getOhlc()[3] * drs[0].getVol() > 500000000;
     }
        
    //BULLISH      
    
    public boolean isBullish(DayRecord[] drs, int tradingDays, int startDate) {  
        if (drs == null || drs.length < tradingDays + startDate + 1) return false; 
        
        double startDayClose = drs[startDate].getOhlc()[3];
        double endDayClose = drs[tradingDays + startDate].getOhlc()[3];
        double middleDayClose = drs[(tradingDays + startDate)/2].getOhlc()[3];
        
        if (endDayClose == 0.0) return false; 
        
        if (startDayClose < middleDayClose) return false;
        if (middleDayClose < endDayClose) return false;
        double variance = (startDayClose - endDayClose)/endDayClose;
        
        if (tradingDays > 250 && variance < 0.30) return false;
        if (tradingDays > 115 && variance < 0.25) return false;
        if (tradingDays > 58 && variance < 0.18) return false;
        if (tradingDays > 18 && variance < 0.12) return false;
        if (variance < 0.08) return false;
        return true;               
    } 
    
    public boolean isBullishCorrection(DayRecord[] drs, int startDate) {  
        if (drs == null || drs.length < 100) return false; 
        
        double startDayClose = drs[startDate].getOhlc()[3];
        
        if (drs[startDate+1].getOhlc()[3] > startDayClose && drs[startDate+2].getOhlc()[3] > startDayClose) return false;
        
        double ma50_0 = drs[startDate].getMa50();
        double ma50_1 = drs[startDate + 22].getMa50();
        double ma50_2 = drs[startDate + 44].getMa50();
        double ma50_3 = drs[startDate + 66].getMa50();
        double ma50_4 = drs[startDate + 88].getMa50(); 
        
        if (ma50_0 == 0 || ma50_1 == 0 || ma50_2 == 0 || ma50_3 == 0 || ma50_4 == 0 ) return false;
        
        if ((ma50_0 - ma50_4)/ma50_4 < 0.20) return false;
        if ((ma50_2 - ma50_4)/ma50_4 < 0.10) return false;
        
        if ((ma50_0 - ma50_2)/ma50_2 < 0.9*(ma50_2 - ma50_4)/ma50_4 ) return false;
        
        
        if (ma50_0 < ma50_1 || ma50_1 < ma50_2 ||  ma50_2 < ma50_3 ||  ma50_3 < ma50_4) return false;
       // System.out.println("is: " + (startDayClose - ma50_0)/ma50_0);
        if (Math.abs((startDayClose - ma50_0)/ma50_0) < 0.011) return true;
 
        return false;               
    } 
    
    public int getBullishCorrectionScore(DayRecord[] drs, int startDate) {  
        if (drs == null || drs.length < 100) return 0; 
        
        int score = 0;
        double startDayClose = drs[startDate].getOhlc()[3];
        
        if (drs[startDate+1].getOhlc()[3] > startDayClose && drs[startDate+2].getOhlc()[3] > startDayClose) return 0;
        
        double ma50_0 = drs[startDate].getMa50();
        double ma50_1 = drs[startDate + 22].getMa50();
        double ma50_2 = drs[startDate + 44].getMa50();
        double ma50_3 = drs[startDate + 66].getMa50();
        double ma50_4 = drs[startDate + 88].getMa50(); 
        double avg20 = getAvg(drs, 0, 20);
        
        if (ma50_0 == 0 || ma50_1 == 0 || ma50_2 == 0 || ma50_3 == 0 || ma50_4 == 0 ) return 0;
        
        if ((ma50_0 - ma50_2)/ma50_2 < 0.10) return 0;
        if ((ma50_2 - ma50_4)/ma50_4 < 0.10) return 0;
        
        if (ma50_0 < ma50_1 || ma50_1 < ma50_2 ||  ma50_2 < ma50_3 ||  ma50_3 < ma50_4) return 0;
        
        if (Math.abs((startDayClose - ma50_0)/ma50_0) > 0.03) return 0;
       
        if (Math.abs((startDayClose - ma50_0)/ma50_0) < 0.02) score++;
        else if (Math.abs((avg20 - startDayClose)/avg20) < 0.01) score++;
        
       // System.out.println("is: " + score);
 
        return score;
    } 
    
    public double getAvg(DayRecord[] drs, int startDate, int days) {
        if (drs == null || drs.length < startDate + days || days == 0) return 0; 
        double sum = 0;
        for (int i=startDate; i<startDate + days; i++ ) {
            sum +=  drs[i].getOhlc()[3];
        }
        return sum/days;
     }

    
    // very flat in recent days - the deal price
    public boolean isAboutDelisted(DayRecord[] drs) {  
       if (drs == null || drs.length < 16) return false; 
       double highest = getHighest(drs, 5, 0);
       double lowest = getLowest(drs, 5, 0);
       if (lowest == 0 ) return true;
       return (highest - lowest)/lowest < 0.01;
    } 
    
      
    
    // 
    public boolean isBreakout(DayRecord[] drs, int tradingDays, int day) {  
        if (drs == null || tradingDays < 50 || drs.length < day + tradingDays) return false; 
        double high1 = getHighest(drs,3,day);
        double high2 = getHighest(drs,tradingDays-3,day+3);
        if (high1 < high2) return false;        
        int previousHighIndex = getHighestIndex(drs, tradingDays-3, day+3); // highest day not including recent 3 day
        if (previousHighIndex < day + 20) return false; // pre high must at least 20 days ago
        if (previousHighIndex > day + tradingDays - 40) return false; // 40 days before per high are lower
        if (drs[day].getOhlc()[1] < drs[previousHighIndex].getOhlc()[3]) return false; // this day low must higher than previous high close
        if (Math.abs((high1-high2)/high2) > 0.05) return false;
        double low1 = getLowest(drs, previousHighIndex-day, day); // lowest from this day to pre high
        if (Math.abs((high2-low1)/low1) < 0.15) return false;  
        /*
         * System.out.println("Found Breakout");
         * System.out.println("tradingDays: " + tradingDays);
         * System.out.println("previousHighIndex: " + previousHighIndex);
         * System.out.println("close: " + drs[previousHighIndex].getOhlc()[3]);
         */
        return true;
     }
    
    public boolean isDoubleBottom (DayRecord[] drs, int day) {
        if (drs.length < 250) return false;
        
        // eliminate long term bearish stock
        int oldDayIndex = Math.min(250, drs.length-1);
        int middleIndex = (oldDayIndex + day)/2;
        if (drs[day].getMa50() < drs[middleIndex].getMa50()) return false;
        
        int lowestDay = getLowestIndex(drs, 251, day+3); // lowest day exclude the recent 3 day
        if (lowestDay == -1) return false;
        if (lowestDay - day < 30) return false; // two bottoms at least 30 days in between
        if (lowestDay > 251 + day -30) return false; // lowest day should not in the first 30 days of range
        double previousLowest = drs[lowestDay].getOhlc()[2];
        double lowestIn3Days = getLowest(drs, 3, day); 
        double highestAfterLowest = getHighest(drs, lowestDay-day, day);
        if (Math.abs((highestAfterLowest - previousLowest)/previousLowest) < 0.15) return false;
        if (Math.abs((lowestIn3Days - previousLowest)/previousLowest) > 0.05) return false;
        if (drs[day].getOhlc()[3] < previousLowest && Math.abs((drs[day].getOhlc()[3] - previousLowest)/previousLowest) > 0.06) return false;
        
        
        /*
         * System.out.println("Found DoubleBottom");
         * System.out.println("lowestDay: " + lowestDay);
         * System.out.println("close: " + drs[lowestDay].getOhlc()[3]);
         */
        return true;
    }
    
    public boolean isBearish(DayRecord[] drs, int tradingDays, int startDate) {  
        if (drs == null || drs.length < tradingDays + startDate + 1) return false; 
        
        double startDayClose = drs[startDate].getOhlc()[3];
        double endDayClose = drs[tradingDays + startDate].getOhlc()[3];
        double middleDayClose = drs[(tradingDays + startDate)/2].getOhlc()[3];
        
        if (endDayClose == 0.0) return false; 
        
        if (startDayClose > middleDayClose) return false;
        if (middleDayClose > endDayClose) return false;
        double variance = (startDayClose - endDayClose)/endDayClose;
        
        if (tradingDays > 250 && variance > -0.30) return false;
        if (tradingDays > 115 && variance > -0.25) return false;
        if (tradingDays > 58 && variance > -0.18) return false;
        if (tradingDays > 18 && variance > -0.12) return false;
        if (variance > -0.08) return false;
        return true;               
    } 
  
    public double getHighest(DayRecord[] drs, int tradingDays, int startDate) {  
        if (drs == null || drs.length < (tradingDays + startDate + 1)) return 0.0; 
        double max = drs[0].getOhlc()[1];
        for (int i = startDate; i < startDate + tradingDays; i++)
            if (drs[i].getOhlc()[1] > max) max = drs[i].getOhlc()[1];         
        return max;
     }
    
    public double getLowest(DayRecord[] drs, int tradingDays, int startDate) {  
        if (drs == null || drs.length < tradingDays + startDate + 1) return 0.0; 
        double min = drs[startDate].getOhlc()[2];
        for (int i = startDate; i < startDate + tradingDays; i++)
            if (drs[i].getOhlc()[2] < min) min = drs[i].getOhlc()[2];         
        return min;
     }
    
 // find highest index 
    public int getHighestIndex(DayRecord[] drs, int tradingDays, int startDate) {  
        if (drs == null || drs.length < tradingDays + startDate + 1) return -1; 
        double max = drs[startDate].getOhlc()[1];
        int index = 0;
        for (int i = startDate; i < startDate + tradingDays; i++) {
            if (drs[i].getOhlc()[1] > max) {
                max = drs[i].getOhlc()[1]; 
                index = i;
            }            
        }            
        return index;
     }
    
    // find lowest index in 252 days
    public int getLowestIndex(DayRecord[] drs, int tradingDays, int startDate) {  
        if (drs == null || drs.length < tradingDays + startDate + 1) return -1; 
        double min = drs[startDate].getOhlc()[2];
        int index = 0;
        for (int i = startDate; i < startDate + tradingDays; i++) {
            if (drs[i].getOhlc()[2] < min) {
                min = drs[i].getOhlc()[2]; 
                index = i;
            }            
        }            
        return index;
     }
    
    public double getHighestClose(DayRecord[] drs, int tradingDays, int startDate) {  
        if (drs == null || drs.length < tradingDays + startDate + 1) return 0.0; 
        double max = drs[0].getOhlc()[3];
        for (int i = startDate; i < startDate + tradingDays; i++)
            if (drs[i].getOhlc()[3] > max) max = drs[i].getOhlc()[3];         
        return max;
     }
    
    public double getLowestClose(DayRecord[] drs, int tradingDays, int startDate) {  
        if (drs == null || drs.length < tradingDays + startDate + 1) return 0.0; 
        double min = drs[0].getOhlc()[3];
        for (int i = startDate; i < startDate + tradingDays; i++)
            if (drs[i].getOhlc()[3] < min) min = drs[i].getOhlc()[3];         
        return min;
     }
    
    // 20 days before day vol avg exclusive day
    public double getAvgVolBefore(DayRecord[] drs, int days, int day) {
        if (days == 0 || drs == null || drs.length < day + days ) return -1;
        double sum = 0.0;
        for (int i=day+1; i< day+1+days; i++) {
            sum += drs[i].getVol();
        }
        return sum/days;
    }
    
    // 20 days before day vol avg inclusive day
    public double getAvgVolAfter(DayRecord[] drs, int day) {
        if ( drs == null || drs.length < day  ) return -1;
        double sum = 0.0;
        for (int i=0; i<= day; i++) {
            sum += drs[i].getVol();
        }
        return sum/(day+1);
    }
    
    public boolean skipPenny(DayRecord[] drs) {
        
        if (drs == null || drs.length < 31) return true;
        for (int i=0;i<30;i++) {
            if (drs[i].getVol() < 250000) return true; 
            if (drs[i].getOhlc()[3] < 2) return true; 
            if (drs[i].getOhlc()[3] * drs[i].getVol()  < 500000) return true; 
        }        
        return false;
    }

    
    // return the multiplicative inverse of the variance
    public double getCloseVariance(DayRecord[] drs, int days) {
        if (days == 0) return 0.0;
        if (drs.length < days) return 0.0;
        
        double[] closeArr = new double[days];
        double[] percentageVarArr = new double[days];
        for (int i = 0; i < days; i++) {
            closeArr[i] = drs[i].getOhlc()[3]; 
        }
        double avg = Utils.average(closeArr);
        for (int i = 0; i < days; i++) {
            percentageVarArr[i] = (drs[i].getOhlc()[3] - avg)/avg; 
        }        
        return Utils.variance(percentageVarArr);
    }
    
    
    public int getVolSpikeScore(DayRecord[] drs) {       
        
        if (drs == null || drs.length < 100) return 0;
        if (drs[0].getVol() < 250000) return 0; 
        if (drs[1].getVol() < 250000) return 0; 
        if (drs[2].getVol() < 250000) return 0; 
        int score = 0;
        
        int volDoubleDate = -1;
        double sum1 = 0.0; // after volDoubleDate
        double sum2 = 0.0; // before volDoubleDate
        double avg1 = 0.0; // after volDoubleDate
        double avg2 = 0.0; // before volDoubleDate
        for (int i=10; i>0; i--) {
            final double rate = getAvgVolAfter(drs, i) / getAvgVolBefore(drs, 20, i);
            if (rate > 2.0) {
                volDoubleDate = i;
                score = (int) (10 * rate);
                break;
            }
        }
        
        if (volDoubleDate == -1) return 0; // no vol double in recent 10 days
        if (volDoubleDate == 0 && drs[0].getVol() < drs[1].getVol()*3) return 0; // one day spike requires triple vol 
      
       // if (score > 0) System.out.println("getVolSpikeScore score: " + score);
        
        return score;
    }
    
    public int getVolSpikeScore_old(DayRecord[] drs) {
        if (drs == null || drs.length < 16) return 0;
        if (drs[0].getVol() < 2000000) return 0; 
        if (drs[1].getVol() < 2000000) return 0; 
        if (drs[2].getVol() < 2000000) return 0; 
        if (isFallingKnife(drs, 0)) return 0; // eliminate big price drop
            
        int score = 0;
        
        int volDoubleDate = -1;
        double sum1 = 0.0; // after volDoubleDate
        double sum2 = 0.0; // before volDoubleDate
        double avg1 = 0.0; // after volDoubleDate
        double avg2 = 0.0; // before volDoubleDate
        for (int i=0; i<10; i++) {
            if (drs[i].getVol() > 2* getAvgVolBefore(drs, 20, i)) {
                volDoubleDate = i;
                break;
            }
        }
        
        if (volDoubleDate == -1) return 0; // no vol double in recent 10 days
        if (volDoubleDate == 0 && drs[0].getVol() < drs[1].getVol()*3) return 0; // one day spike requires triple vol
        
        for (int i=0; i<=volDoubleDate; i++) {
            sum1 += drs[i].getVol();
        }
        for (int i=volDoubleDate+1; i<volDoubleDate + 21; i++) {
            sum2 += drs[i].getVol();
        }
        avg1 = sum1/(volDoubleDate+1);
        avg2 = sum2/20;
        if (avg1 < 1.8 * avg2) return 0;
        if (sum2 == 0) return 0; // avoid divide 0
     
        score = (int) (avg1/avg2);
        if (isBigStock(drs)) score++;
        if (isBigStock2(drs)) score++;
        return score;
    }    
    
    public double get10DaysVar(DayRecord[] drs, int startDate) {
        if (drs == null || drs.length < 11 + startDate) return 10; 
        double min = getLowest(drs,10,0);
        if (min == 0.0) return 10;
        double max = getHighest(drs,10,0);
        return (max - min)/min;
    }
    
    public double get10DaysVarClose(DayRecord[] drs, int startDate) {
        if (drs == null || drs.length < 11 + startDate) return 10; 
        double min = getLowestClose(drs,10,0);
        if (min == 0.0) return 10;
        double max = getHighestClose(drs,10,0);
        return (max - min)/min;
    }
    
    //sharp drop
    public boolean isFallingKnife(DayRecord[] drs, int startDate) {
        if (drs == null || drs.length < 11 + startDate) return true; 
        double avg = (drs[startDate + 8].getOhlc()[3] + drs[startDate + 9].getOhlc()[3] + drs[startDate + 10].getOhlc()[3])/3;
        
       if ((drs[startDate].getOhlc()[3] - avg)/avg < -0.2) return true;
        return false;
    }  

}
