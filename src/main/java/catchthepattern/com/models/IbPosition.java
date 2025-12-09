package catchthepattern.com.models;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class IbPosition {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable=false)
    Date date;
    
    @Column(nullable=false)
    public String symbol;
    
    @Column(nullable=false)
    public double quantity;
    
    @Column(nullable=false)
    public double avgCost;
    
    public IbPosition() {}

	public IbPosition(Date date, String symbol, double quantity, double avgCost) {
		super();
		this.date = date;
		this.symbol = symbol;
		this.quantity = quantity;
		this.avgCost = avgCost;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public double getQuantity() {
		return quantity;
	}

	public void setQuantity(double quantity) {
		this.quantity = quantity;
	}

	public double getAvgCost() {
		return avgCost;
	}

	public void setAvgCost(double avgCost) {
		this.avgCost = avgCost;
	}
    
    
}
