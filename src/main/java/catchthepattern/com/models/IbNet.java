package catchthepattern.com.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Date;

@Entity
public class IbNet {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable=false)
    Date date;
    
    @Column(nullable=false)
    double netLiquidation;
    
    double spy;
    

	public IbNet() {}

	public IbNet(Date date, double netLiquidation, double spy) {
		super();
		this.date = date;
		this.netLiquidation = netLiquidation;
		this.spy = spy;
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

	public double getNetLiquidation() {
		return netLiquidation;
	}

	public void setNetLiquidation(double netLiquidation) {
		this.netLiquidation = netLiquidation;
	}

	public double getSpy() {
		return spy;
	}

	public void setSpy(double spy) {
		this.spy = spy;
	}
    
    
}
