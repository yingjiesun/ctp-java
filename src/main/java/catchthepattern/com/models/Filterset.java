package catchthepattern.com.models;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity(name = "filtersets")
public class Filterset {
    
    /*
     * BULLISH: day4 > 3 > 2 > 1, 8% 15% 25% 35%
     * BEARISH: reverse of BULLISH
     * CHOPPY: None of above and Hign/Low > 10%
     * FLAT: None of above
     * */
    public enum stockTrend {
         BULLISH, BEARISH, FLAT, CHOPPY, ANY
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(mappedBy = "filterset")
    @JsonBackReference
    private Pattern pattern;
    
    private double minPrice;
    private double maxPrice;
    private double minVol;
    private String oneMonth;
    private String threeMonth;
    private String sixMonth;
    private String oneYear;
    
    public Filterset() {}
    
    public Filterset(Long id, double minPrice, double maxPrice, double minVol, String oneMonth, String threeMonth,
            String sixMonth, String oneYear) {        
        this.id = id;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.minVol = minVol;
        this.oneMonth = oneMonth;
        this.threeMonth = threeMonth;
        this.sixMonth = sixMonth;
        this.oneYear = oneYear;
    }
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public double getMinPrice() {
        return minPrice;
    }
    public void setMinPrice(double minPrice) {
        this.minPrice = minPrice;
    }
    public double getMaxPrice() {
        return maxPrice;
    }
    public void setMaxPrice(double maxPrice) {
        this.maxPrice = maxPrice;
    }
    public double getMinVol() {
        return minVol;
    }
    public void setMinVol(double minVol) {
        this.minVol = minVol;
    }
    public String getOneMonth() {
        return oneMonth;
    }
    public void setOneMonth(String oneMonth) {
        this.oneMonth = oneMonth;
    }
    public String getThreeMonth() {
        return threeMonth;
    }
    public void setThreeMonth(String threeMonth) {
        this.threeMonth = threeMonth;
    }
    public String getSixMonth() {
        return sixMonth;
    }
    public void setSixMonth(String sixMonth) {
        this.sixMonth = sixMonth;
    }
    public String getOneYear() {
        return oneYear;
    }
    public void setOneYear(String oneYear) {
        this.oneYear = oneYear;
    }
    
    
    
}
