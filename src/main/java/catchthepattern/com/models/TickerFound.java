package catchthepattern.com.models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;*/
import jakarta.persistence.*;

@Entity(name = "tickers")
public class TickerFound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable=false, unique=true)
    private String ticker;
    
    @ManyToMany(fetch = FetchType.EAGER) 
    @JoinTable(name = "ticker_pattern",
        joinColumns = { @JoinColumn(name = "tickerId") },
        inverseJoinColumns = { @JoinColumn(name = "patternId") })
    private Set<Pattern> patterns;
    
    private String patternDates; 
    
    private boolean isBigStock;
    
    public String getPatternDates() {
        return patternDates;
    }

    public void setPatternDates(String patternDates) {
        this.patternDates = patternDates;
    }

    public boolean isBigStock() {
        return isBigStock;
    }

    public void setBigStock(boolean isBigStock) {
        this.isBigStock = isBigStock;
    }

    public TickerFound() {}

    public TickerFound(Long id, String ticker, Set<Pattern> patterns, boolean isBigStock) {
        super();
        this.id = id;
        this.ticker = ticker;
        this.patterns = patterns;
        this.isBigStock = isBigStock;
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

    public Set<Pattern> getPatterns() {
        return patterns;
    }

    public void setPatterns(Set<Pattern> patterns) {
        this.patterns = patterns;
    }
    
    public void addPatterns(Pattern pattern) {
        if (this.patterns == null) this.patterns = new HashSet<Pattern>();
        this.patterns.add(pattern);
    }
    
    
}
