package catchthepattern.com.models;

public class Strategy {
	double buy; // actual buy price, get value when filled.
	double buyPercentage; //percentage of previous close
	double trail; // trailing stop
	double trailPercentage;
	double sell; //take profit
	double sellPercentage;
	double result; //the profit after some days, either sold or hold
	double resultPercentage;
	String buyDate;
	String sellDate;
	String status; //notFilled, filled, sold
	String ticker;
	int countTransactions;
	int tradingDays; // how many trading days to calculate the performance of this strategy.
	int startDay;
	double buyHoldResultPercentage;
	
	
	
	public Strategy() {
		super();
	}

	public Strategy(double buy, double buyPercentage, double trail, double trailPercentage, double sell,
			double sellPercentage, double result, double resultPercentage, String buyDate, String sellDate, String status, String ticker, int countTransactions, int tradingDays, int startDay, double buyHoldResultPercentage) {
		super();
		this.buy = buy;
		this.buyPercentage = buyPercentage;
		this.trail = trail;
		this.trailPercentage = trailPercentage;
		this.sell = sell;
		this.sellPercentage = sellPercentage;
		this.result = result;
		this.resultPercentage = resultPercentage;
		this.buyDate = buyDate;
		this.sellDate = sellDate;
		this.status = status;
		this.countTransactions = countTransactions;
		this.ticker = ticker;
		this.tradingDays = tradingDays;
		this.startDay = startDay;
		this.buyHoldResultPercentage = buyHoldResultPercentage;
	}

	// Copy constructor
    public Strategy(Strategy other) {
        this.buy = other.buy;
        this.buyPercentage = other.buyPercentage;
        this.trail = other.trail;
        this.trailPercentage = other.trailPercentage;
        this.sell = other.sell;
        this.sellPercentage = other.sellPercentage;
        this.result = other.result;
        this.resultPercentage = other.resultPercentage;
        this.buyDate = other.buyDate;
		this.sellDate = other.sellDate;
        this.status = other.status; // String is immutable, so safe to copy by reference
        this.ticker = other.ticker;
        this.countTransactions = other.countTransactions;
        this.tradingDays = other.tradingDays;
        this.startDay = other.startDay;
        this.buyHoldResultPercentage = other.buyHoldResultPercentage;
    }
    
	public double getBuy() {
		return buy;
	}
	public void setBuy(double buy) {
		this.buy = buy;
	}
	public double getTrail() {
		return trail;
	}
	public void setTrail(double trail) {
		this.trail = trail;
	}
	public double getSell() {
		return sell;
	}
	public void setSell(double sell) {
		this.sell = sell;
	}
	public double getResult() {
		return result;
	}
	public void setResult(double result) {
		this.result = result;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}

	public double getBuyPercentage() {
		return buyPercentage;
	}

	public void setBuyPercentage(double buyPercentage) {
		this.buyPercentage = buyPercentage;
	}

	public double getTrailPercentage() {
		return trailPercentage;
	}

	public void setTrailPercentage(double trailPercentage) {
		this.trailPercentage = trailPercentage;
	}

	public double getSellPercentage() {
		return sellPercentage;
	}

	public void setSellPercentage(double sellPercentage) {
		this.sellPercentage = sellPercentage;
	}

	public double getResultPercentage() {
		return resultPercentage;
	}
	

	public String getBuyDate() {
		return buyDate;
	}

	public void setBuyDate(String buyDate) {
		this.buyDate = buyDate;
	}

	public String getSellDate() {
		return sellDate;
	}

	public void setSellDate(String sellDate) {
		this.sellDate = sellDate;
	}

	public void setResultPercentage(double resultPercentage) {
		this.resultPercentage = resultPercentage;
	}

	public int getCountTransactions() {
		return countTransactions;
	}

	public void setCountTransactions(int countTransactions) {
		this.countTransactions = countTransactions;
	}

	public String getTicker() {
		return ticker;
	}

	public void setTicker(String ticker) {
		this.ticker = ticker;
	}

	public int getTradingDays() {
		return tradingDays;
	}

	public void setTradingDays(int tradingDays) {
		this.tradingDays = tradingDays;
	}

	public double getBuyHoldResultPercentage() {
		return buyHoldResultPercentage;
	}

	
	public int getStartDay() {
		return startDay;
	}

	public void setStartDay(int startDay) {
		this.startDay = startDay;
	}

	public void setBuyHoldResultPercentage(double buyHoldResultPercentage) {
		this.buyHoldResultPercentage = buyHoldResultPercentage;
	}
	
	
	
	
}
