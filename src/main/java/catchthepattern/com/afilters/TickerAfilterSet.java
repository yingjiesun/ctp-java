package catchthepattern.com.afilters;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import catchthepattern.com.models.TickerFound;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity(name = "tickerAfiltersets")
@RequiredArgsConstructor
public class TickerAfilterSet {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique=true)
    @NonNull
    private String ticker;
    
    @ManyToMany(fetch = FetchType.EAGER) 
    @JoinTable(name = "ticker_afilterset",
        joinColumns = { @JoinColumn(name = "tickerId") },
        inverseJoinColumns = { @JoinColumn(name = "afiltersetId") })
    private Set<AfilterSet> afilterSets = new HashSet<AfilterSet>();
    
    private String afilterSetDates; 
    
    private boolean isBigStock;
    
    public TickerAfilterSet() {}

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

    public Set<AfilterSet> getAfilterSets() {
        return afilterSets;
    }

    public void setAfilterSets(Set<AfilterSet> afilterSets) {
        this.afilterSets = afilterSets;
    }

    public String getAfilterSetDates() {
        return afilterSetDates;
    }

    public void setAfilterSetDates(String afilterSetDates) {
        this.afilterSetDates = afilterSetDates;
    }

    public boolean isBigStock() {
        return isBigStock;
    }

    public void setBigStock(boolean isBigStock) {
        this.isBigStock = isBigStock;
    }
    

}
