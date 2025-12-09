package catchthepattern.com.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class BestBullish {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable=false)
    String tickers;
    
    @Column(nullable=false)
    int score;
    
    public BestBullish( ) {}

    public BestBullish(String tickers, int score) {
        this.tickers = tickers;
        this.score = score;
    }

    public String getTicker() {
        return tickers;
    }

    public void setTicker(String tickers) {
        this.tickers = tickers;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

  
    
}
