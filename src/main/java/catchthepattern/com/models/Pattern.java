package catchthepattern.com.models;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

@Entity(name = "patterns")
public class Pattern{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable=false)
	private String name;
	
	@JsonIgnore
	@ManyToOne
	@JoinColumn(name="userId", nullable=false)
	private User user;	
	
	@JsonIgnore
	@ManyToMany(mappedBy = "patterns")
    private Set<TickerFound> tickerFounds;  
	
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "defId", referencedColumnName = "id")
	@JsonManagedReference
	private PatternDef def;
	
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "filtersetId", referencedColumnName = "id")
	@JsonManagedReference
	private Filterset filterset;
	
	private double  closeAvg10;
	private double  closeAvg30;
	private double  highest10;
	private double  highest30;
	private double  lowest10;
	private double  lowest30;
	private int occurrence;
	private int num10PercentRiseIn30Days;	
	private int num10PercentLoseIn30Days;
	private int totalTickerScanned; 
	private boolean isValidPattern;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@JsonIgnore
	public User getUser() {
		return user;
	}
	
	public Set<TickerFound> getTickerFounds() {
        return tickerFounds;
    }
    public void setTickerFounds(Set<TickerFound> tickerFounds) {
        this.tickerFounds = tickerFounds;
    }
    public Filterset getFilterset() {
        return filterset;
    }
    public void setFilterset(Filterset filterset) {
        this.filterset = filterset;
    }
    public void setUser(User user) {
		this.user = user;
	}
	public PatternDef getDef() {
		return def;
	}
	public void setDef(PatternDef def) {
		this.def = def;
	}
	public double getCloseAvg10() {
		return closeAvg10;
	}
	public void setCloseAvg10(double closeAvg10) {
		this.closeAvg10 = closeAvg10;
	}
	public double getCloseAvg30() {
		return closeAvg30;
	}
	public void setCloseAvg30(double closeAvg30) {
		this.closeAvg30 = closeAvg30;
	}
	public double getHighest10() {
		return highest10;
	}
	public void setHighest10(double highest10) {
		this.highest10 = highest10;
	}
	public double getHighest30() {
		return highest30;
	}
	public void setHighest30(double highest30) {
		this.highest30 = highest30;
	}
	public double getLowest10() {
		return lowest10;
	}
	public void setLowest10(double lowest10) {
		this.lowest10 = lowest10;
	}
	public double getLowest30() {
		return lowest30;
	}
	public void setLowest30(double lowest30) {
		this.lowest30 = lowest30;
	}
    public int getOccurrence() {
        return occurrence;
    }
    public void setOccurrence(int occurrence) {
        this.occurrence = occurrence;
    }
    public int getTotalTickerScanned() {
        return totalTickerScanned;
    }
    public void setTotalTickerScanned(int totalTickerScanned) {
        this.totalTickerScanned = totalTickerScanned;
    }
    public boolean isValidPattern() {
        return isValidPattern;
    }
    public void setValidPattern(boolean isValidPattern) {
        this.isValidPattern = isValidPattern;
    }
    public int getNum10PercentRiseIn30Days() {
        return num10PercentRiseIn30Days;
    }
    public void setNum10PercentRiseIn30Days(int num10PercentRiseIn30Days) {
        this.num10PercentRiseIn30Days = num10PercentRiseIn30Days;
    }
    public int getNum10PercentLoseIn30Days() {
        return num10PercentLoseIn30Days;
    }
    public void setNum10PercentLoseIn30Days(int num10PercentLoseIn30Days) {
        this.num10PercentLoseIn30Days = num10PercentLoseIn30Days;
    }
   
	
	
}