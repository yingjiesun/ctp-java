package catchthepattern.com.models;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class StockData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable=false, unique=true)
    private String ticker;
    
    @ElementCollection
    @CollectionTable(name = "day_records")
    private DayRecord[] dayRecords;
    
    public StockData() {}

    public StockData(String ticker, DayRecord[] dayRecords) {
        super();
        this.ticker = ticker;
        this.dayRecords = dayRecords;
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

    public DayRecord[] getDayRecords() {
        return dayRecords;
    }

    public void setDayRecords(DayRecord[] dayRecords) {
        this.dayRecords = dayRecords;
    }
    
    

}
