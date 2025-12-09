package catchthepattern.com.models;

import catchthepattern.com.schedulers.StockScanTask.ListIndex;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Gainer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable=false)
    String ticker;
    
    @Column(nullable=false)
    double close;
    
    @Column(nullable=false)
    double day;
    
    @Column(nullable=false)
    double week;
    
    @Column(nullable=false)
    double month;
    
    @Column(nullable=false)
    double threeMonth;
    
    @Column(nullable=false)
    double sixMonth;
    
    @Column(nullable=false)
    double year;
    
    @Column(nullable=false)
    double pv;
    
    public Gainer() {}

    public Gainer(String ticker, double close, double day, double week, double month, double threeMonth,
            double sixMonth, double year, double pv) {
        super();
        this.ticker = ticker;
        this.close = close;
        this.day = day;
        this.week = week;
        this.month = month;
        this.threeMonth = threeMonth;
        this.sixMonth = sixMonth;
        this.year = year;
        this.pv = pv;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public double getClose() {
        return close;
    }

    public void setClose(double close) {
        this.close = close;
    }

    public double getDay() {
        return day;
    }

    public void setDay(double day) {
        this.day = day;
    }

    public double getWeek() {
        return week;
    }

    public void setWeek(double week) {
        this.week = week;
    }

    public double getMonth() {
        return month;
    }

    public void setMonth(double month) {
        this.month = month;
    }

    public double getThreeMonth() {
        return threeMonth;
    }

    public void setThreeMonth(double threeMonth) {
        this.threeMonth = threeMonth;
    }

    public double getSixMonth() {
        return sixMonth;
    }

    public void setSixMonth(double sixMonth) {
        this.sixMonth = sixMonth;
    }

    public double getYear() {
        return year;
    }

    public void setYear(double year) {
        this.year = year;
    }
    
    
    
    public double getPv() {
        return pv;
    }

    public void setPv(double pv) {
        this.pv = pv;
    }

    public double getValueByEnum(ListIndex index) {
        switch (index) {
            case DAY: return getDay(); 
            case WEEK: return getWeek(); 
            case MONTH: return getMonth(); 
            case MONTH3: return getThreeMonth(); 
            case MONTH6: return getSixMonth(); 
            case YEAR: return getYear();
            case PV: return getPv();
            default: return 0.0;
        }        
    }

}
