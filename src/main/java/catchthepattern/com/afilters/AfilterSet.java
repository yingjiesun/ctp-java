package catchthepattern.com.afilters;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import catchthepattern.com.models.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity(name = "afiltersets")
@RequiredArgsConstructor
public class AfilterSet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable=false)
    private String name;
    
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name="userId", nullable=false)
    private User user;  
    
    @OneToMany(mappedBy="afilterSet", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  //  @OneToMany(mappedBy="afilterSet")
    Set<FilterA> filterAs;
    
    @OneToMany(mappedBy="afilterSet", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    Set<FilterB> filterBs;
    
    @JsonIgnore
    @ManyToMany(mappedBy = "afilterSets")
    private Set<TickerAfilterSet> tickerAfilterSets;  
    
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
    private boolean isValid;

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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Set<FilterA> getFilterAs() {
        return filterAs;
    }

    public void setFilterAs(Set<FilterA> filterAs) {
        this.filterAs = filterAs;
    }

    public Set<FilterB> getFilterBs() {
        return filterBs;
    }

    public void setFilterBs(Set<FilterB> filterBs) {
        this.filterBs = filterBs;
    }

    public Set<TickerAfilterSet> getTickerAfilterSets() {
        return tickerAfilterSets;
    }

    public void setTickerAfilterSets(Set<TickerAfilterSet> tickerAfilterSets) {
        this.tickerAfilterSets = tickerAfilterSets;
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

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }   
    
    
    
}
