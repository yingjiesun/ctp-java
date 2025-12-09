package catchthepattern.com.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity(name = "stockInfo")
public class StockInfo {
    
    // https://www.alphavantage.co/query?function=OVERVIEW&symbol=IBM&apikey=demo
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable=false, unique=true)
    @JsonProperty("Symbol")
    private String symbol;
    
    @JsonProperty("Name")
    private String name; // company name
    
    @Column(length = 65535)
    @JsonProperty("Description")
    private String description;
    
    @JsonProperty("Sector")
    private String sector;
    
    @JsonProperty("Exchange")
    private String exchange; // NYSE
    
    @JsonProperty("Country")
    private String country;
    
    @JsonProperty("Currency")
    private String currency; // USD
    
    @JsonProperty("Industry")
    private String industry;
    
    @JsonProperty("MarketCapitalization")
    private String cap;
    
    @JsonProperty("PERatio")
    private String pe;
    
    @JsonProperty("PEGRatio")
    private String peg;
    
    @JsonProperty("DividendPerShare")
    private String dividendPerShare;
    
    @JsonProperty("DividendYield")
    private String dividendYield;
    
    @JsonProperty("EPS")
    private String EPS;
    
    @JsonProperty("Beta")
    private String beta;
    
    public StockInfo() {}
    
    public StockInfo(String symbol, String name, String description, String sector, String exchange, String country,
            String currency, String industry, String cap, String pe, String peg, String dividendPerShare,
            String dividendYield, String ePS, String beta) {
        super();
        this.symbol = symbol;
        this.name = name;
        this.description = description;
        this.sector = sector;
        this.exchange = exchange;
        this.country = country;
        this.currency = currency;
        this.industry = industry;
        this.cap = cap;
        this.pe = pe;
        this.peg = peg;
        this.dividendPerShare = dividendPerShare;
        this.dividendYield = dividendYield;
        EPS = ePS;
        this.beta = beta;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return description;
    }

    public void setDesc(String description) {
        this.description = description;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getCap() {
        return cap;
    }

    public void setCap(String cap) {
        this.cap = cap;
    }

    public String getPe() {
        return pe;
    }

    public void setPe(String pe) {
        this.pe = pe;
    }

    public String getPeg() {
        return peg;
    }

    public void setPeg(String peg) {
        this.peg = peg;
    }

    public String getDividendPerShare() {
        return dividendPerShare;
    }

    public void setDividendPerShare(String dividendPerShare) {
        this.dividendPerShare = dividendPerShare;
    }

    public String getDividendYield() {
        return dividendYield;
    }

    public void setDividendYield(String dividendYield) {
        this.dividendYield = dividendYield;
    }

    public String getEPS() {
        return EPS;
    }

    public void setEPS(String ePS) {
        EPS = ePS;
    }

    public String getBeta() {
        return beta;
    }

    public void setBeta(String beta) {
        this.beta = beta;
    }
    
    

}
