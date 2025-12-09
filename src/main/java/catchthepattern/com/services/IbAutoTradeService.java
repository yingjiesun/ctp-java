package catchthepattern.com.services;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import catchthepattern.com.models.DayRecord;
import catchthepattern.com.models.TopRated;
import catchthepattern.com.services.IbGatewayService.Position;
import jakarta.annotation.PostConstruct;

@Service
public class IbAutoTradeService {
	/* 
	 * 1. Check ib gateway connection
	 * 2. If ib gateway is not connected, call IbGatewayService.connect()
	 * 
	 * 1. Get account summary and positions
	 * 2. Do not use margin
	 * 3. Divide the TotalCashValue by 10 - assume holding 10 stocks and each use 10% of TotalCashValue
	 * 4. Get positions
	 * 5. Get top picks
	 * 6. Calculate how many new buys to submit according to top picks and AvailableFunds/BuyingPower, and submit new buys by placeBracketOrder- valid one day.
	 * 7. Sells are set in placeBracketOrder so no more handling logic needed.
	 * */
	
    @Autowired
    private IbGatewayService ibGatewayService;
    
    @Autowired
    private PatternService patternService;
    
    @Autowired
    DataFormatService dataFormatService;
	
	private Map<String, String> accountSummary; 
	
	private final double TRAILING_STOP = 8.4;
	//private final double STOP_GAIN = 1.017;
	//private final double ORDER_DEDUCT = 0.995;
	
	public IbAutoTradeService() {
		
	}
	
	@PostConstruct
	public void init() {
		if (!ibGatewayService.isConnected()) {
			ibGatewayService.connect();
			 try {
	                Thread.sleep(3000); // Brief pause to ensure connection is fully established
	            } catch (InterruptedException e) {
	                Thread.currentThread().interrupt();
	            }
			accountSummary = ibGatewayService.requestAccountSummarySync(); // NetLiquidation,TotalCashValue,CashBalance,AvailableFunds,BuyingPower
		} else {
			accountSummary = ibGatewayService.requestAccountSummarySync(); 
		}
		try { // wait for accountSummary
            Thread.sleep(2000); // Brief pause to ensure connection is fully established
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
	}
	
	public void placeOrders() {
		// always refresh account summary before submitting order
		init();
		
		if (accountSummary == null || accountSummary.isEmpty()) {
			System.err.println("❌ accountSummary is null or empty, orders not placed.");
		} else {
			List<TopRated> tps = patternService.getTopRated(true);
			List<Position> currentPositions = ibGatewayService.getPositions();
			
			int totalToBeOrders = 0;
			for (TopRated tp : tps) {
				if (tp.getStatus().equals("toBeOrdered")) {
					System.out.println("toBeOrdered: " + tp.getTicker() );
					totalToBeOrders++;
				}
			}
			System.out.println("totalToBeOrders: " + totalToBeOrders );
			
			if (totalToBeOrders > 0) {
				for (TopRated tp : tps) {
					if (tp.getStatus().equals("toBeOrdered")) {
						if (ibGatewayService.hasEnoughPositions(tp.getTicker(), currentPositions)) {
							System.out.println("hasEnoughPositions, skip: " + tp.getTicker() );
							tp.setStatus("done");
							continue; // skip is ticker is 
						}
						int quantity = getQuantity(accountSummary, tp.getBoughtPrice(), totalToBeOrders);
						System.out.println("To be odered: " + tp.getTicker() + " quantity: " + quantity);
						if (quantity > 0) {
							//double orderPrice = Math.round(tp.getBoughtPrice() * ORDER_DEDUCT * 100.0) / 100.0;
							//double takeProfitPrice = Math.round(orderPrice * STOP_GAIN * 100.0) / 100.0;
							
							double orderPrice = Math.round(dataFormatService.getBestOrderPrice_median(tp.getTicker()) * 100.0) / 100.0;
				        	// double takeProfitPrice = Math.round(dataFormatService.getBestSalePrice_median(tp.getTicker()) * 100.0) / 100.0;
				        	double takeProfitPrice = Math.round(dataFormatService.getBestSalePriceBasedOnOrderPrice(orderPrice, tp.getTicker()) * 100.0) / 100.0;
							
							System.out.printf("💬 Submitting bracket order for %s: qty=%d, entry=%.2f, TP=%.2f, trail=%.2f%%\n",
								    tp.getTicker(), quantity, orderPrice, takeProfitPrice, TRAILING_STOP);
							ibGatewayService.placeBracketOrderTrailing(tp.getTicker(), quantity, orderPrice, takeProfitPrice, TRAILING_STOP);
							try {
				                Thread.sleep(5000); // Make sure IB is done with previous oder before next order
				            } catch (InterruptedException e) {
				                Thread.currentThread().interrupt();
				            }
						}
						tp.setStatus("done");
					}
				}
				 patternService.deleteAllTopRated();
				 patternService.saveAllTopRated(tps);
			}
		}
	}
	
	private String getAccountValue(String tagName) {
        for (Map.Entry<String, String> entry : accountSummary.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(tagName)) {
                return entry.getValue(); // e.g., "1001049.89"
            }
        }
        return null; // or throw exception or return "0"
    }
    
    private double getAccountValueAsDouble(String tagName) {
        String value = getAccountValue(tagName);
        try {
            return value != null ? Double.parseDouble(value) : 0.0;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
	
    // IB allow 4x margin. Use maximum 2x margin only. Each order should less than 1/10 net liquidation.
	private int getQuantity(Map<String, String> accountSummary, double offerPrice, int totalToBeOrdered) {
		double netLiquidation = getAccountValueAsDouble("NetLiquidation");
		// double availableFunds  = getAccountValueAsDouble("AvailableFunds");
		double buyingPowerHalf  = getAccountValueAsDouble("BuyingPower") / 2;
		double buyAmount = -1;
	    if (netLiquidation != 0 && buyingPowerHalf != 0 && totalToBeOrdered != 0) {
	    	
	    	if ((netLiquidation/10)*totalToBeOrdered > buyingPowerHalf) { // buyingPowerHalf is not enough to buy 1/10 for each toBeOrdered
	    		buyAmount = buyingPowerHalf/totalToBeOrdered; 
			} else {
				buyAmount = buyingPowerHalf > (netLiquidation / 10)? (netLiquidation / 10) : buyingPowerHalf;
			}
        	return (int) (buyAmount / offerPrice);
        }
	    System.err.println("❌ netLiquidation or AvailableFunds is 0");
	    return -1;
	}

}
