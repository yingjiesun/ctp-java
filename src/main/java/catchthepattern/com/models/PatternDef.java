package catchthepattern.com.models;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity(name = "patternDefs")
public class PatternDef{	
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@OneToOne(mappedBy = "def")
	@JsonBackReference
	private Pattern pattern;

	private boolean openClose;
	private boolean highLow;
	private boolean vol;	
	private double[][] ohlc;
	private double[] vols;	

	public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public boolean isOpenClose() {
		return openClose;
	}
	public void setOpenClose(boolean openClose) {
		this.openClose = openClose;
	}
	public boolean isHighLow() {
		return highLow;
	}
	public void setHighLow(boolean highLow) {
		this.highLow = highLow;
	}
	public boolean isVol() {
		return vol;
	}
	public void setVol(boolean vol) {
		this.vol = vol;
	}
	public double[][] getOhlc() {
		return ohlc;
	}
	public void setOhlc(double[][] ohlc) {
		this.ohlc = ohlc;
	}
	public double[] getVols() {
		return vols;
	}
	public void setVols(double[] vols) {
		this.vols = vols;
	}
	
	
}