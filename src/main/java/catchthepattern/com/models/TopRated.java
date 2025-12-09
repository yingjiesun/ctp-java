package catchthepattern.com.models;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
public class TopRated {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable=false)
    String ticker;
    
    @Column(name = "found_date")
    @Temporal(TemporalType.DATE)
    private Date foundDate;
    @Column(name = "sold_date")
    @Temporal(TemporalType.DATE)
    private Date soldDate;
    
    private String pattern;
    private String filterSet;
    private double boughtPrice;
    private double percentageChange;
    private double soldPercentageChange;
    private double highest;
    private double lowest;
    private String status; // pending, toBeRemoved, done

    public TopRated() {}
    
    public TopRated(String ticker, Date foundDate, String pattern, String filterSet, double percentageChange,
            double highest, double lowest) {
        super();
        this.ticker = ticker;
        this.foundDate = foundDate;
        this.pattern = pattern;
        this.filterSet = filterSet;
        this.percentageChange = percentageChange;
        this.highest = highest;
        this.lowest = lowest;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getFoundDate() {
        return foundDate;
    }

    public void setFoundDate(Date foundDate) {
        this.foundDate = foundDate;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getFilterSet() {
        return filterSet;
    }

    public void setFilterSet(String filterSet) {
        this.filterSet = filterSet;
    }

    public double getPercentageChange() {
        return percentageChange;
    }

    public void setPercentageChange(double percentageChange) {
        this.percentageChange = percentageChange;
    }

    public double getHighest() {
        return highest;
    }

    public void setHighest(double highest) {
        this.highest = highest;
    }

    public double getLowest() {
        return lowest;
    }

    public void setLowest(double lowest) {
        this.lowest = lowest;
    }

	public Date getSoldDate() {
		return soldDate;
	}

	public void setSoldDate(Date soldDate) {
		this.soldDate = soldDate;
	}

	public double getSoldPercentageChange() {
		return soldPercentageChange;
	}

	public void setSoldPercentageChange(double soldPercentageChange) {
		this.soldPercentageChange = soldPercentageChange;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
    
    public String printInfo() {
    	return this.getFoundDate() + " : " + this.getTicker();
    }

	public double getBoughtPrice() {
		return boughtPrice;
	}

	public void setBoughtPrice(double boughtPrice) {
		this.boughtPrice = boughtPrice;
	}
    
    

}
