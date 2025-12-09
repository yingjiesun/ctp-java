package catchthepattern.com.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import catchthepattern.com.models.IbNet;
import catchthepattern.com.models.IbPosition;
import catchthepattern.com.models.OrderStatusInfo;
import catchthepattern.com.repositories.IbNetRepository;
import catchthepattern.com.repositories.IbPositionRepository;
import catchthepattern.com.services.DataFormatService;
import catchthepattern.com.services.IbAutoTradeService;
import catchthepattern.com.services.IbGatewayService;
import catchthepattern.com.services.IbGatewayService.Position;
import catchthepattern.com.services.OrderManagerService;

@CrossOrigin
@RestController
public class IbController {

	@Autowired
	private IbGatewayService ibGatewayService;
	@Autowired
	private IbAutoTradeService ibAutoTradeService;
	@Autowired
	private OrderManagerService orderManagerService;
	@Autowired
	IbNetRepository ibNetRepository;
	@Autowired
    private IbPositionRepository ibPositionRepository;
	@Autowired
    private DataFormatService dataFormatService;
	
	@GetMapping("/open/connect")
    public void callIbGatewayConnect() {
		if (!ibGatewayService.isConnected()) ibGatewayService.connect();
    }
	
	@GetMapping("/open/ping")
    public String pingIbGateway() {
        return ibGatewayService.isConnected()
                ? "✅ IB Gateway is connected"
                : "❌ Not connected to IB Gateway";
    }

    @PostMapping("/open/account-summary")
    public ResponseEntity<Map<String, String>> getAccountSummary() {
        if (!ibGatewayService.isConnected()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                                 .body(Map.of("error", "Not connected to IB Gateway"));
        }
        Map<String, String> summary = ibGatewayService.requestAccountSummarySync();
        return ResponseEntity.ok(summary);
    }
    
    @PostMapping("/open/place-orders/{st}")
    public String placeOrders(@PathVariable("st") String st) {
    	if ("6355".equals(st)) {
    		ibAutoTradeService.placeOrders();
            return "Placed orders";
    	} else {
    		return "Rejected";
    	} 
    }
    
    
    @PostMapping("/open/update-ib-net")
    public String updateIbNet() {
    	ibGatewayService.updateIbNet();
        return "Updated Ib Net.";
    }
    
    @GetMapping("/open/{orderId}/status")
    public ResponseEntity<String> getOrderStatus(@PathVariable int orderId) {
        OrderStatusInfo info = orderManagerService.getOrderStatus(orderId);
        if (info != null) {
            return ResponseEntity.ok("OrderId " + orderId + " is " + info.getStatus());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("OrderId not found.");
        }
    }
    
    @GetMapping("/open/testStrategy3/{ticker}")
    public void testStrategyFromPreviousPeriod(@PathVariable String ticker) {
    	dataFormatService.testStrategyFromPreviousPeriod(ticker);
    }
    
    @GetMapping("/open/testStrategy2/{ticker}")
    public void testBestStrategyInDifferentPeriod(@PathVariable String ticker) {
    	dataFormatService.testBestStrategyInDifferentPeriod(ticker);
    }
    
    @GetMapping("/open/testStrategy/{ticker}")
    public void testStrategy(@PathVariable String ticker) {
    	dataFormatService.testStrategy(ticker);
    }
    /*
    @PostMapping("/open/bracket-trailing-order")
    public String placeBracketTrailingOrder(
            @RequestParam String symbol,
            @RequestParam int quantity,
            @RequestParam double entryPrice,
            @RequestParam double takeProfitPrice,
            @RequestParam double stopLossPercentage) {

    	ibGatewayService.placeBracketOrderTrailing(symbol, quantity, entryPrice, takeProfitPrice, stopLossPercentage);
        return "✅ Bracket trailing order submitted for " + symbol;
    }
    
    @PostMapping("/open/bracket-order")
    public String placeBracketOrder(
            @RequestParam String symbol,
            @RequestParam int quantity,
            @RequestParam double entryPrice,
            @RequestParam double takeProfitPrice,
            @RequestParam double stopLossPrice) {

    	ibGatewayService.placeBracketOrder(symbol, quantity, entryPrice, takeProfitPrice, stopLossPrice);
        return "✅ Bracket order submitted for " + symbol;
    }
    
    @GetMapping("/open/buy")
    public ResponseEntity<String> buy() {
        ibGatewayService.placeBuyOrder();
        return ResponseEntity.ok("Buy order sent.");
    }
    
    @PostMapping("/open/buy2")
    public ResponseEntity<String> buyStock(@RequestBody BuyRequest request) {
        try {
            ibGatewayService.buyStock(request.getSymbol(), request.getQuantity());
            return ResponseEntity.ok("Buy order placed for " + request.getQuantity() + " shares of " + request.getSymbol());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to place buy order: " + e.getMessage());
        }
    }
    
    // /buy/limit?symbol=AAPL&quantity=10&limitPrice=175.00"
    @PostMapping("/open/buy/limit")
    public String limitBuy(
            @RequestParam String symbol,
            @RequestParam int quantity,
            @RequestParam double limitPrice) {

        if (!ibGatewayService.isConnected()) {
            return "❌ Not connected to IB Gateway";
        }

        ibGatewayService.buyStockLimit(symbol, quantity, limitPrice);
        return "✅ Placed LIMIT BUY order for " + quantity + " shares of " + symbol + " @ $" + limitPrice;
    }
    
    // POST ../sell/trailing-stop/all?symbol=AAPL&trailingPercent=2.0
    
    @PostMapping("/open/sell/trailing-stop/all")
    public String sellAllTrailingStop(
            @RequestParam String symbol,
            @RequestParam double trailingPercent) {

        if (!ibGatewayService.isConnected()) {
            return "❌ Not connected to IB Gateway";
        }

        ibGatewayService.sellAllWithTrailingStop(symbol, trailingPercent);
        return "✅ Sent request to TRAILING STOP SELL all shares of " + symbol +
               " with trailing % " + trailingPercent;
    }
    */
    
    /* 
    // /sell/trailing-stop?symbol=AAPL&trailingPercent=2.5&quantity=10
    @PostMapping("/open/sell/trailing-stop")
    public String trailingStopSell(
            @RequestParam String symbol,
            @RequestParam double trailingPercent,
            @RequestParam int quantity) {

        if (!ibGatewayService.isConnected()) {
            return "❌ Not connected to IB Gateway";
        }

        ibGatewayService.sellStockTrailingStop(symbol, trailingPercent, quantity);
        return "✅ Placed TRAILING STOP SELL order for " + quantity + " shares of " + symbol +
               " with trailing %" + trailingPercent;
    }
    
    // /sell/limit?symbol=AAPL&quantity=10&limitPrice=185.00"
    @PostMapping("/open/sell/limit")
    public String limitSell(
            @RequestParam String symbol,
            @RequestParam int quantity,
            @RequestParam double limitPrice) {

        if (!ibGatewayService.isConnected()) {
            return "❌ Not connected to IB Gateway";
        }

        ibGatewayService.sellStockLimit(symbol, quantity, limitPrice);
        return "✅ Placed LIMIT SELL order for " + quantity + " shares of " + symbol + " @ $" + limitPrice;
    }
  
    @PostMapping("/open/sell")
    public ResponseEntity<String> sellStock(@RequestBody BuyRequest request) {
        try {
            ibGatewayService.sellStock(request.getSymbol(), request.getQuantity());
            return ResponseEntity.ok("Sell order placed for " + request.getQuantity() + " shares of " + request.getSymbol());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to place sell order: " + e.getMessage());
        }
    }
    */
    
    @GetMapping("/open/positions")
    public List<Position> getPositions() {
    	return ibGatewayService.getPositions();
    }
    
    @GetMapping("/open/positionHistory")
    public List<IbPosition> getPositionHistory() {
    	return  (List<IbPosition>) ibPositionRepository.findAll();
    }
    
    @GetMapping("/open/orders/open")
    public List<IbGatewayService.OpenOrder> getOpenOrders() {
        if (!ibGatewayService.isConnected()) {
            return List.of(); // Return empty if not connected
        }
        return ibGatewayService.getOpenOrders();
    }
    
    @GetMapping("/open/testBestOffer")
    public void testBestOffer() {
        String[] tickers = {"QBTS","BBD","AAPL","TDUP","STNE","TSLA","NVDA","BA","PLTR","SPOT","DG","PM","APPS","OKTA"};
        for (String ticker : tickers) {
        	System.out.println("---------------------------------");
        	double orderPrice = Math.round(dataFormatService.getBestOrderPrice(ticker) * 100.0) / 100.0;
        	double salePrice = Math.round(dataFormatService.getBestSalePrice(ticker) * 100.0) / 100.0;
        	double profit = Math.round((((salePrice - orderPrice)/orderPrice) * 10000.0))/100.0;
        	
        	System.out.println(ticker + ": " + orderPrice + " " + salePrice + " " + profit + "%");
        	
        	double orderPrice_m = Math.round(dataFormatService.getBestOrderPrice_median(ticker) * 100.0) / 100.0;
        	double salePrice_m = Math.round(dataFormatService.getBestSalePrice_median(ticker) * 100.0) / 100.0;
        	
        	double profit_m = Math.round((((salePrice_m - orderPrice_m)/orderPrice_m) * 10000.0))/100.0;
        	
        	System.out.println(ticker + ": " + orderPrice_m + " " + salePrice_m + " " + profit_m + "%");
        	
        	System.out.println("---------------------------------");
        }
    }
    
    @GetMapping("/open/ib-net-history")
    public List<IbNet> getIbNetHistory() {
        return  (List<IbNet>) ibNetRepository.findAll();
    }
	
}
