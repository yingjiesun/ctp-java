package catchthepattern.com.models;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;

@Embeddable
public class DayRecord {
    String date;
    
    double[] ohlc;
    
    int vol;
    double ma10;
    double ma50;
    double ma250;
    double top; // candle top
    double bottom; // candle bottom
    double middle; // candle middle, (top + bottom)/2
    
    public DayRecord() {}
    
    public DayRecord(String date, double[] ohlc, int vol) {        
        this.date = date;
        this.ohlc = ohlc;
        this.vol = vol;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double[] getOhlc() {
        return ohlc;
    }

    public void setOhlc(double[] ohlc) {
        this.ohlc = ohlc;
    }

    public int getVol() {
        return vol;
    }

    public void setVol(int vol) {
        this.vol = vol;
    }

    public double getMa10() {
        return ma10;
    }

    public void setMa10(double ma10) {
        this.ma10 = ma10;
    }

    public double getMa50() {
        return ma50;
    }

    public void setMa50(double ma50) {
        this.ma50 = ma50;
    }

    public double getMa250() {
        return ma250;
    }

    public void setMa250(double ma250) {
        this.ma250 = ma250;
    }

    public double getTop() {
        return top;
    }

    public void setTop(double top) {
        this.top = top;
    }

    public double getBottom() {
        return bottom;
    }

    public void setBottom(double bottom) {
        this.bottom = bottom;
    }

    public double getMiddle() {
        return middle;
    }

    public void setMiddle(double middle) {
        this.middle = middle;
    }
        
}
