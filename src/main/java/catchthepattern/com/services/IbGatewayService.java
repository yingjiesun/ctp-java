package catchthepattern.com.services;

import com.ib.client.*;
import com.ib.client.Types.SecType;

import catchthepattern.com.models.DayRecord;
import catchthepattern.com.models.IbNet;
import catchthepattern.com.repositories.IbNetRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

//import javax.annotation.PostConstruct;

@Service
public class IbGatewayService implements EWrapper {

	@Autowired
	private OrderManagerService orderManagerService;
	
    @Autowired
    private DataFormatService dataFormatService;
	
	@Autowired
	private IbNetRepository ibNetRepository;
	
	private String ibGatewayHost = "127.0.0.1"; // or gateway IP if running remotely
    //private String ibGatewayHost = "192.168.0.101";
	private int ibGatewayPort = 7497; // 7496 for TWS, 7497 for Gateway
	private int clientId = 5; //Only one client connection is allowed per clientId.
    private EClientSocket client;
    private final EReaderSignal signal = new EJavaSignal();
	private final AtomicInteger orderIdCounter = new AtomicInteger();
	
	private final List<OpenOrder> openOrders = new ArrayList<>();
	private final Map<Integer, StopGainCheck> pendingStopGainChecks = new HashMap<>();
	private final Map<String, String> accountSummary = new ConcurrentHashMap<>();
	private volatile CountDownLatch accountSummaryLatch = new CountDownLatch(1);
	private final AtomicInteger reqIdCounter = new AtomicInteger(9000);
	

    private final AtomicReference<PositionRequestContext> currentRequest = new AtomicReference<>();
	
	public EClientSocket getClient() {
	    return this.client;
	}
	
	public Map<String, String> requestAccountSummarySync() {
		accountSummary.clear();

	    // Cancel previous subscription if needed
	    int reqId = reqIdCounter.incrementAndGet();
	    client.cancelAccountSummary(reqId - 1); // assumes previous reqId was used just before

	    accountSummaryLatch = new CountDownLatch(1);
	    client.reqAccountSummary(reqId, "All", "NetLiquidation,TotalCashValue,CashBalance,AvailableFunds,BuyingPower");

	    try {
	        accountSummaryLatch.await(3, TimeUnit.SECONDS);
	    } catch (InterruptedException e) {
	        Thread.currentThread().interrupt();
	    }

	    client.cancelAccountSummary(reqId); // cleanup after done

	    return new HashMap<>(accountSummary);
	}
    
    private Contract createStockContract(String symbol) {
        Contract contract = new Contract();
        contract.symbol(symbol);
        contract.secType(SecType.STK);
        contract.exchange("SMART");
        contract.currency("USD");
        return contract;
    }
    
    public void placeBracketOrderTrailing(String symbol, int quantity, double entryPrice, double takeProfitPrice, double stopLossPercentage) {
    	
    	if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive.");
        if (takeProfitPrice <= entryPrice)
            throw new IllegalArgumentException("Take-profit price must be greater than entry price.");
        if (stopLossPercentage <= 0)
            throw new IllegalArgumentException("Trailing percent must be positive.");
        
    	// Auto-connect if needed
        if (client == null || !client.isConnected()) {
            connect(); // Reuse your @PostConstruct method or extract connection logic here
            try {
                Thread.sleep(1000); // Brief pause to ensure connection is fully established
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (!client.isConnected()) {
            System.out.println("❌ Failed to connect to IB Gateway. Cannot place order.");
            return;
        }
    	
    	int parentOrderId = orderIdCounter.getAndAdd(3); // Reserve 3 IDs for bracket order

        Contract contract = new Contract();
        contract.symbol(symbol);
        contract.secType(SecType.STK);
        contract.currency("USD");
        contract.exchange("SMART");

        String ocaGroup = "BRACKET_" + parentOrderId;

        // Parent BUY order
        Order parent = new Order();
        parent.orderId(parentOrderId);
        parent.action("BUY");
        parent.orderType("LMT");
        parent.totalQuantity(Decimal.get(quantity));
        parent.lmtPrice(entryPrice);
        parent.tif("DAY");
        parent.transmit(false);

        // Take-profit SELL order
        Order takeProfit = new Order();
        takeProfit.orderId(parentOrderId + 1);
        takeProfit.action("SELL");
        takeProfit.orderType("LMT");
        takeProfit.totalQuantity(Decimal.get(quantity));
        takeProfit.lmtPrice(takeProfitPrice);
        takeProfit.tif("GTC");
        takeProfit.parentId(parentOrderId);
        takeProfit.ocaGroup(ocaGroup);
        takeProfit.ocaType(1);
        takeProfit.transmit(false);

        // Trailing Stop SELL order (in %)
        Order trailingStop = new Order();
        trailingStop.orderId(parentOrderId + 2);
        trailingStop.action("SELL");
        trailingStop.orderType("TRAIL");
        trailingStop.totalQuantity(Decimal.get(quantity));
        trailingStop.trailingPercent(stopLossPercentage); // e.g., 2.0 means 2%
        trailingStop.tif("GTC");
        trailingStop.parentId(parentOrderId);
        trailingStop.ocaGroup(ocaGroup);
        trailingStop.ocaType(1);
        trailingStop.transmit(true);

        // Submit all orders
        client.placeOrder(parent.orderId(), contract, parent);
        client.placeOrder(takeProfit.orderId(), contract, takeProfit);
        client.placeOrder(trailingStop.orderId(), contract, trailingStop);

        System.out.println("✅ Bracket order with trailing stop submitted for " + symbol + " parentOrderId: " + parentOrderId);
    }
    
    public void placeBracketOrder(String symbol, int quantity, double entryPrice, double takeProfitPrice, double stopLossPrice) {
    	// Auto-connect if needed
        if (client == null || !client.isConnected()) {
            connect(); // Reuse your @PostConstruct method or extract connection logic here
            try {
                Thread.sleep(1000); // Brief pause to ensure connection is fully established
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (!client.isConnected()) {
            System.out.println("❌ Failed to connect to IB Gateway. Cannot place order.");
            return;
        }
    	
    	int parentOrderId = orderIdCounter.getAndAdd(3);

        Contract contract = new Contract();
        contract.symbol(symbol);
        contract.secType(SecType.STK);
        contract.currency("USD");
        contract.exchange("SMART");

        String ocaGroup = "BRACKET_" + parentOrderId;

        // Parent order
        Order parent = new Order();
        parent.orderId(parentOrderId);
        parent.action("BUY");
        parent.orderType("LMT");
        parent.totalQuantity(Decimal.get(quantity));
        parent.lmtPrice(entryPrice);
        parent.transmit(false);

        // Take-profit child order
        Order takeProfit = new Order();
        takeProfit.orderId(parentOrderId + 1);
        takeProfit.action("SELL");
        takeProfit.orderType("LMT");
        takeProfit.totalQuantity(Decimal.get(quantity));
        takeProfit.lmtPrice(takeProfitPrice);
        takeProfit.parentId(parentOrderId);
        takeProfit.ocaGroup(ocaGroup);
        takeProfit.ocaType(1); // 1 = Cancel all remaining when one order fills
        takeProfit.transmit(false);

        // Stop-loss child order
        Order stopLoss = new Order();
        stopLoss.orderId(parentOrderId + 2);
        stopLoss.action("SELL");
        stopLoss.orderType("STP");
        stopLoss.totalQuantity(Decimal.get(quantity));
        stopLoss.auxPrice(stopLossPrice);
        stopLoss.parentId(parentOrderId);
        stopLoss.ocaGroup(ocaGroup);
        stopLoss.ocaType(1);
        stopLoss.transmit(true); // Only the last order has transmit=true

        client.placeOrder(parentOrderId, contract, parent);
        client.placeOrder(parentOrderId + 1, contract, takeProfit);
        client.placeOrder(parentOrderId + 2, contract, stopLoss);

        System.out.println("✅ Bracket order placed for " + symbol);
    }
    
    private Order createOrder(String action, double quantity) {
        Order order = new Order();
        order.action(action); // "BUY" or "SELL"
        order.orderType("MKT"); // Market order
        order.totalQuantity(Decimal.get(quantity));
        return order;
    }
    
    public List<OpenOrder> getOpenOrders() {
        openOrders.clear();
        client.reqOpenOrders();  // Request open orders from IB Gateway
        try {
            Thread.sleep(3000); // Wait briefly for async callback (or use a latch if needed)
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return new ArrayList<>(openOrders);
    }
    
    public void placeBuyOrder() {
        Contract contract = createStockContract("AAPL");
        Order order = createOrder("BUY", 10.0); // Buy 10 shares

        client.placeOrder(orderIdCounter.getAndIncrement(), contract, order);
    }
    
    /*
    "DAY": Valid only during todays market session. (Default)
    "GTC": Good-Til-Canceled â€“ stays open until explicitly canceled or up to 90 days (depending on the exchange/broker).
    "IOC": Immediate or Cancel â€“ fill as much as possible immediately and cancel the rest.
    "FOK": Fill or Kill â€“ the order must be filled completely immediately or canceled.
    "GTD": Good-Til-Date â€“ valid until a specific date/time you set.
    */
    public void buyStockLimit(String symbol, int quantity, double limitPrice) {
        Contract contract = new Contract();
        contract.symbol(symbol);
        contract.secType(SecType.STK); // Stock
        contract.exchange("SMART");
        contract.currency("USD");

        Order order = new Order();
        order.action("BUY");
        order.orderType("LMT"); // Limit order
        order.lmtPrice(limitPrice); // Set the limit price
        order.totalQuantity(Decimal.get(quantity));
        order.tif("DAY"); // Time-in-force: valid for today only

        client.placeOrder(orderIdCounter.getAndIncrement(), contract, order);

        System.out.println("✅ Placed LIMIT BUY order: " + quantity + " shares of " + symbol + " @ $" + limitPrice);
    }
    
    public void buyStock(String symbol, int quantity) {
        Contract contract = new Contract();
        contract.symbol(symbol);
        contract.secType(SecType.STK); // Stock
        contract.exchange("SMART");
        contract.currency("USD");

        Order order = new Order();
        order.action("BUY");
        order.orderType("MKT");
        order.totalQuantity(Decimal.get(quantity));

        client.placeOrder(orderIdCounter.getAndIncrement(), contract, order);

        System.out.println("✅ Placed BUY order: " + quantity + " shares of " + symbol);
    }
    
    public void sellStockLimit(String symbol, int quantity, double limitPrice) {
        Contract contract = new Contract();
        contract.symbol(symbol);
        contract.secType(SecType.STK);
        contract.exchange("SMART");
        contract.currency("USD");

        Order order = new Order();
        order.action("SELL");
        order.orderType("LMT");
        order.totalQuantity(Decimal.get(quantity));
        order.lmtPrice(limitPrice);

        client.placeOrder(orderIdCounter.getAndIncrement(), contract, order);

        System.out.println("✅ Placed LIMIT SELL order: " + quantity + " shares of " + symbol + " @ $" + limitPrice);
    }
    
    public void sellStockTrailingStop(String symbol, double trailingPercent, int quantity) {
        Contract contract = new Contract();
        contract.symbol(symbol);
        contract.secType(SecType.STK);
        contract.exchange("SMART");
        contract.currency("USD");

        Order order = new Order();
        order.action("SELL");
        order.orderType("TRAIL");
        order.totalQuantity(Decimal.get(quantity));
        order.trailingPercent(trailingPercent);
        order.trailStopPrice(0.0); // Set to 0.0 so IB calculates it from market price

        client.placeOrder(orderIdCounter.getAndIncrement(), contract, order);

        System.out.println("✅ Placed TRAILING STOP SELL order: " + quantity +
                " shares of " + symbol + " with trailing stop %" + trailingPercent);
    }
    /*
    public void stopGainSell(String symbol, double stopPercent) {
        // Get current position
        Position matched = null;
        for (Position p : getPositions()) {
            if (p.symbol.equalsIgnoreCase(symbol) && !p.quantity.isZero()) {
                matched = p;
                break;
            }
        }

        if (matched == null) {
            System.out.println("❌ No position found for symbol: " + symbol);
            return;
        }

        double avgCost = matched.avgCost;
        Decimal quantity = matched.quantity;

        // Request market price
        int tickerId = new Random().nextInt(100000);
        Contract contract = createStockContract(symbol);

        // it’s subscribing to price updates, and IB Gateway pushes them as events.
        client.reqMktData(tickerId, contract, "", false, false, null);

        // Handle asynchronously in tickPrice()
        pendingStopGainChecks.put(tickerId, new StopGainCheck(symbol, avgCost, stopPercent, quantity));
    }
    
    public void sellAllWithTrailingStop(String symbol, double trailingPercent) {
        // Get current positions
        List<Position> currentPositions = getPositions();

        Position positionToSell = currentPositions.stream()
                .filter(p -> p.symbol.equalsIgnoreCase(symbol))
                .findFirst()
                .orElse(null);

        if (positionToSell == null || positionToSell.quantity.isZero()) {
            System.out.println("❌ No position found for symbol: " + symbol);
            return;
        }

        int quantity = (int) positionToSell.quantity.longValue();

        Contract contract = new Contract();
        contract.symbol(symbol);
        contract.secType(SecType.STK);
        contract.exchange("SMART");
        contract.currency("USD");

        Order order = new Order();
        order.action("SELL");
        order.orderType("TRAIL");
        order.totalQuantity(Decimal.get(quantity));
        order.trailingPercent(trailingPercent);
        order.trailStopPrice(0.0); // Let IB calculate based on market

        client.placeOrder(orderIdCounter.getAndIncrement(), contract, order);

        System.out.println("✅ Placed TRAILING STOP SELL for all shares: " + quantity +
                " of " + symbol + " with trailing % " + trailingPercent);
    }
    */
    public void sellStock(String symbol, int quantity) {
        Contract contract = new Contract();
        contract.symbol(symbol);
        contract.secType(SecType.STK); // Stock
        contract.exchange("SMART");
        contract.currency("USD");

        Order order = new Order();
        order.action("SELL");
        order.orderType("MKT");
        order.totalQuantity(Decimal.get(quantity));

        client.placeOrder(orderIdCounter.getAndIncrement(), contract, order);

        System.out.println("✅ Placed SELL order: " + quantity + " shares of " + symbol);
    }
    
    public void placeSellOrder() {
        Contract contract = createStockContract("AAPL");
        Order order = createOrder("SELL", 10); // Sell 10 shares

        client.placeOrder(orderIdCounter.getAndIncrement(), contract, order);
    }
/*
 * Old working function but have to call twice. 
    @jakarta.annotation.PostConstruct
    public void connect() {
        EWrapper wrapper = this;
        client = new EClientSocket(wrapper, signal);
        client.eConnect(ibGatewayHost, ibGatewayPort, clientId);

        if (client.isConnected()) {
            System.out.println("✅ Connected to IB Gateway");

            // Start a background thread to read messages
            new Thread(() -> {
                final EReader reader = new EReader(client, signal);
                reader.start();
                while (client.isConnected()) {
                    signal.waitForSignal();
                    try {
                        reader.processMsgs();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            client.reqIds(-1);
        } else {
            System.out.println("❌ Failed to connect to IB Gateway");
        }
    }
   */ 
    @jakarta.annotation.PostConstruct
    public void connect() {
        if (client != null && client.isConnected()) {
            //System.out.println("ℹ️ Check IB Gateway connection: Already connected.");
            return;
        }

        EWrapper wrapper = this;
        client = new EClientSocket(wrapper, signal);
        client.eConnect(ibGatewayHost, ibGatewayPort, clientId);

        // Start EReader BEFORE checking isConnected()
        final EReader reader = new EReader(client, signal);
        reader.start();

        new Thread(() -> {
            while (client.isConnected()) {
                signal.waitForSignal();
                try {
                    reader.processMsgs();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "IB-EReader-Thread").start();

        // Optionally wait briefly for handshake messages like nextValidId
        try {
            Thread.sleep(1000); // Give the EReader time to process initial messages
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (client.isConnected()) {
            System.out.println("✅ Connected to IB Gateway");
            client.reqIds(-1); // Optional: triggers nextValidId
        } else {
            System.out.println("❌ Failed to connect to IB Gateway");
        }
    }

    // ---- Required EWrapper Methods (add others as needed) ----
    
    public boolean isConnected() {
        return client != null && client.isConnected();
    }

    public void requestAccountSummary() {
    	connect();
        if (isConnected()) {
            int reqId = 9001; // unique request ID
            client.reqAccountSummary(reqId, "All", "NetLiquidation,TotalCashValue,CashBalance");
        } else {
            System.out.println("❌ Cannot request summary: Not connected.");
        }
    }
    
    public boolean hasEnoughPositions(String ticker, List<Position> currentPositions) {
    	for (Position p : currentPositions) {
    		if (p.symbol.equals(ticker)) {
    			System.out.println("isInPositions: " + ticker); 
    			return true;
    		}
    	}
    	return false;
    }
    
    public void updateIbNet() {
    	connect();
    	Map<String, String> acctSummary = requestAccountSummarySync();
    	String valueStr = acctSummary.get("NetLiquidation (USD)");
    	
    	String[] dataArr;
    	DayRecord[] drs;
    	double SPY_close = 0;
    	
    	try {
    		dataArr = dataFormatService.getRawDataArr("SPY");
    		drs = dataFormatService.getDayRecordsFromRawArrs(dataArr);
    		SPY_close = drs[0].getOhlc()[3];
    	} catch(Exception e) {}

        double totalCashValue = 0.0;
        if (valueStr != null && !valueStr.isEmpty()) {
             try {
                totalCashValue = Double.parseDouble(valueStr);
                LocalDate today = LocalDate.now();
              	Date updatedDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
             	
             	IbNet ibNet = new IbNet(updatedDate, totalCashValue, SPY_close);
             	ibNetRepository.save(ibNet);
                 
             } catch (NumberFormatException e) {
                 System.err.println("updateIbNet: " + e);
             }
         }
    }
    
    @Override
    public void accountSummary(int reqId, String account, String tag, String value, String currency) {
        String key = tag + (currency != null ? " (" + currency + ")" : "");
        accountSummary.put(key, value);
    }

    @Override
    public void accountSummaryEnd(int reqId) {
        accountSummaryLatch.countDown();
    }
    
    public List<Position> getPositions() {
    	connect();
        PositionRequestContext context = new PositionRequestContext();
        currentRequest.set(context);

        client.reqPositions();

        try {
            context.latch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            currentRequest.set(null);
        }

        return new ArrayList<>(context.positions);
    }
    
 /*

    @Override
    public void position(String account, Contract contract, Decimal pos, double avgCost) {
        if (pos != null && !pos.isZero()) {
            Position p = new Position(account, contract, pos.value().doubleValue(), avgCost);
            positions.add(p);
        }
    }

    @Override
    public void positionEnd() {
        positionLatch.countDown();
    }
    */
    @Override
    public void position(String account, Contract contract, Decimal pos, double avgCost) {
        PositionRequestContext context = currentRequest.get();
        if (context != null && !pos.isZero()) {
            context.positions.add(new Position(account, contract, pos, avgCost));
        }
    }

    @Override
    public void positionEnd() {
        PositionRequestContext context = currentRequest.getAndSet(null);
        if (context != null) {
            context.latch.countDown();
        }
    }

    // Inner class to hold position info
    public static class Position {
        public final String account;
        public final String symbol;
        public final double quantity;
        public final double avgCost;

        public Position(String account, Contract contract, Decimal quantityDecimal, double avgCost) {
            this.account = account;
            this.symbol = contract.symbol();
            this.quantity = quantityDecimal.value().doubleValue();
            this.avgCost = avgCost;
        }
    }
    
    private static class PositionRequestContext {
        final List<Position> positions = Collections.synchronizedList(new ArrayList<>());
        final CountDownLatch latch = new CountDownLatch(1);
    }
    
    

    @Override public void error(Exception e) { e.printStackTrace(); }
    @Override public void error(String str) { System.err.println(str); }
//    @Override public void error(int id, int errorCode, String errorMsg) {
//        System.err.printf("❌ Error [%d] %d: %s%n", id, errorCode, errorMsg);
//    }

    @Override public void connectionClosed() {
        System.out.println("🔌 Connection to IB Gateway closed.");
    }

    // Required empty overrides
    @Override
    public void tickPrice(int tickerId, int field, double price, TickAttrib attribs) {
        // 4 = LAST_PRICE
        if (field == 4 && pendingStopGainChecks.containsKey(tickerId)) {
            StopGainCheck check = pendingStopGainChecks.get(tickerId);
            double gain = ((price - check.avgCost) / check.avgCost) * 100;

            System.out.printf("📈 Current price: %.2f, Gain: %.2f%% (Target: %.2f%%)\n", price, gain, check.stopPercent);

            if (gain >= check.stopPercent) {
                // Sell all
                Order order = new Order();
                order.action("SELL");
                order.orderType("MKT");
                order.totalQuantity(check.quantity);

                Contract contract = createStockContract(check.symbol);
                client.placeOrder(orderIdCounter.getAndIncrement(), contract, order);

                System.out.println("✅ Triggered stop-gain SELL for " + check.symbol);
            }

            // Unsubscribe and clean up
            client.cancelMktData(tickerId);
            pendingStopGainChecks.remove(tickerId);
        }
    }
//    @Override public void tickSize(int tickerId, int field, int size) {}
//    @Override public void tickOptionComputation(int tickerId, int field, double impliedVol, double delta,
//                                                double optPrice, double pvDividend, double gamma,
//                                                double vega, double theta, double undPrice) {}
    @Override public void tickGeneric(int tickerId, int tickType, double value) {}
    @Override public void tickString(int tickerId, int tickType, String value) {}
    @Override public void tickEFP(int tickerId, int tickType, double basisPoints,
                                  String formattedBasisPoints, double totalDividends,
                                  int holdDays, String futureExpiry, double dividendImpact,
                                  double dividendsToExpiry) {}
    @Override public void nextValidId(int orderId) {
    	orderIdCounter.set(orderId);
        System.out.println("✅ Received next valid order ID: " + orderId);
    }
    @Override public void currentTime(long time) {}
    @Override public void managedAccounts(String accountsList) {}
    @Override public void updateAccountValue(String key, String value, String currency, String accountName) {}
//    @Override public void updatePortfolio(Contract contract, double position, double marketPrice,
//                                          double marketValue, double averageCost, double unrealizedPNL,
//                                          double realizedPNL, String accountName) {}
    @Override public void updateAccountTime(String timeStamp) {}
    @Override public void accountDownloadEnd(String accountName) {}
    @Override public void contractDetails(int reqId, ContractDetails contractDetails) {}
    @Override public void contractDetailsEnd(int reqId) {}
    @Override public void bondContractDetails(int reqId, ContractDetails contractDetails) {}
    @Override public void execDetails(int reqId, Contract contract, Execution execution) {}
    @Override public void execDetailsEnd(int reqId) {}
    @Override public void commissionReport(CommissionReport commissionReport) {}
    @Override public void fundamentalData(int reqId, String data) {}
    @Override public void historicalData(int reqId, Bar bar) {}
    @Override public void historicalDataEnd(int reqId, String startDateStr, String endDateStr) {}
    @Override public void marketDataType(int reqId, int marketDataType) {}
//    @Override public void updateMktDepth(int tickerId, int position, int operation, int side, double price, int size) {}
//    @Override public void updateMktDepthL2(int tickerId, int position, String marketMaker, int operation,
//                                           int side, double price, int size, boolean isSmartDepth) {}
    @Override public void updateNewsBulletin(int msgId, int msgType, String message, String origExchange) {}
//    @Override public void position(String account, Contract contract, double pos, double avgCost) {}
 //   @Override public void positionEnd() {}
//    @Override public void realtimeBar(int reqId, long time, double open, double high, double low,
//                                      double close, long volume, double wap, int count) {}
    @Override public void scannerParameters(String xml) {}
    @Override public void scannerData(int reqId, int rank, ContractDetails contractDetails,
                                      String distance, String benchmark, String projection, String legsStr) {}
    @Override public void scannerDataEnd(int reqId) {}
    @Override public void receiveFA(int faDataType, String xml) {}
    @Override public void historicalDataUpdate(int reqId, Bar bar) {}
    @Override public void marketRule(int marketRuleId, PriceIncrement[] priceIncrements) {}
    @Override public void pnl(int reqId, double dailyPnL, double unrealizedPnL, double realizedPnL) {}
//    @Override public void pnlSingle(int reqId, double pos, double dailyPnL, double unrealizedPnL,
//                                    double realizedPnL, double value) {}
//    @Override public void historicalTicks(int reqId, List<HistoricalTick> ticks, boolean done) {}
//    @Override public void historicalTicksBidAsk(int reqId, List<HistoricalTickBidAsk> ticks, boolean done) {}
//    @Override public void historicalTicksLast(int reqId, List<HistoricalTickLast> ticks, boolean done) {}
//    @Override public void tickByTickAllLast(int reqId, int tickType, long time, double price, int size,
//                                            TickAttribLast tickAttribLast, String exchange, String specialConditions) {}
//    @Override public void tickByTickBidAsk(int reqId, long time, double bidPrice, double askPrice, int bidSize,
//                                           int askSize, TickAttribBidAsk tickAttribBidAsk) {}
    @Override public void tickByTickMidPoint(int reqId, long time, double midPoint) {}
    @Override public void orderBound(long orderId, int apiClientId, int apiOrderId) {}
    @Override public void completedOrder(Contract contract, Order order, OrderState orderState) {}
    @Override public void completedOrdersEnd() {}

	@Override
	public void accountUpdateMulti(int arg0, String arg1, String arg2, String arg3, String arg4, String arg5) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void accountUpdateMultiEnd(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void connectAck() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deltaNeutralValidation(int arg0, DeltaNeutralContract arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void displayGroupList(int arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void displayGroupUpdated(int arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void error(int arg0, int arg1, String arg2, String arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void familyCodes(FamilyCode[] arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void headTimestamp(int arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void histogramData(int arg0, List<HistogramEntry> arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void historicalNews(int arg0, String arg1, String arg2, String arg3, String arg4) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void historicalNewsEnd(int arg0, boolean arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void historicalSchedule(int arg0, String arg1, String arg2, String arg3, List<HistoricalSession> arg4) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void historicalTicks(int arg0, List<HistoricalTick> arg1, boolean arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void historicalTicksBidAsk(int arg0, List<HistoricalTickBidAsk> arg1, boolean arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void historicalTicksLast(int arg0, List<HistoricalTickLast> arg1, boolean arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mktDepthExchanges(DepthMktDataDescription[] arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void newsArticle(int arg0, int arg1, String arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void newsProviders(NewsProvider[] arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void openOrder(int orderId, Contract contract, Order order, OrderState orderState) {
	    openOrders.add(new OpenOrder(
	        orderId,
	        order.action().toString(),
	        contract.symbol(),
	        order.totalQuantity().value().doubleValue(),
	        order.orderType().toString(),
	        order.lmtPrice()
	    ));
	    orderManagerService.trackOpenOrder(orderId, orderState.getStatus());
	}

	@Override
	public void openOrderEnd() {
		System.out.println("✅ Open orders retrieval complete.");	
	}

	@Override
	public void orderStatus(
			int orderId, 
			String status, 
			Decimal filled, 
			Decimal remaining,
            double avgFillPrice, 
            int permId, 
            int parentId,
            double lastFillPrice, 
            int clientId, 
            String whyHeld, 
            double mktCapPrice
            ) {
				orderManagerService.trackOrderStatus(orderId, status, filled, remaining);
	}

	@Override
	public void pnlSingle(int arg0, Decimal arg1, double arg2, double arg3, double arg4, double arg5) {
		// TODO Auto-generated method stub
		
	}

//	@Override
//	public void position(String arg0, Contract arg1, Decimal arg2, double arg3) {
//		// TODO Auto-generated method stub
//		
//	}

	@Override
	public void positionMulti(int arg0, String arg1, String arg2, Contract arg3, Decimal arg4, double arg5) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void positionMultiEnd(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void realtimeBar(int arg0, long arg1, double arg2, double arg3, double arg4, double arg5, Decimal arg6,
			Decimal arg7, int arg8) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void replaceFAEnd(int arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void rerouteMktDataReq(int arg0, int arg1, String arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void rerouteMktDepthReq(int arg0, int arg1, String arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void securityDefinitionOptionalParameter(int arg0, String arg1, int arg2, String arg3, String arg4,
			Set<String> arg5, Set<Double> arg6) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void securityDefinitionOptionalParameterEnd(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void smartComponents(int arg0, Map<Integer, Entry<String, Character>> arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void softDollarTiers(int arg0, SoftDollarTier[] arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void symbolSamples(int arg0, ContractDescription[] arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tickByTickAllLast(int arg0, int arg1, long arg2, double arg3, Decimal arg4, TickAttribLast arg5,
			String arg6, String arg7) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tickByTickBidAsk(int arg0, long arg1, double arg2, double arg3, Decimal arg4, Decimal arg5,
			TickAttribBidAsk arg6) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tickNews(int arg0, long arg1, String arg2, String arg3, String arg4, String arg5) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tickOptionComputation(int arg0, int arg1, int arg2, double arg3, double arg4, double arg5, double arg6,
			double arg7, double arg8, double arg9, double arg10) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tickReqParams(int arg0, double arg1, String arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tickSize(int arg0, int arg1, Decimal arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tickSnapshotEnd(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateMktDepth(int arg0, int arg1, int arg2, int arg3, double arg4, Decimal arg5) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateMktDepthL2(int arg0, int arg1, String arg2, int arg3, int arg4, double arg5, Decimal arg6,
			boolean arg7) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updatePortfolio(Contract arg0, Decimal arg1, double arg2, double arg3, double arg4, double arg5,
			double arg6, String arg7) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void userInfo(int arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void verifyAndAuthCompleted(boolean arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void verifyAndAuthMessageAPI(String arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void verifyCompleted(boolean arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void verifyMessageAPI(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void wshEventData(int arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void wshMetaData(int arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	// Inner class to represent open order info
	public static class OpenOrder {
	    public final int orderId;
	    public final String action;
	    public final String symbol;
	    public final double quantity;
	    public final String orderType;
	    public final double limitPrice;

	    public OpenOrder(int orderId, String action, String symbol, double quantity, String orderType, double limitPrice) {
	        this.orderId = orderId;
	        this.action = action;
	        this.symbol = symbol;
	        this.quantity = quantity;
	        this.orderType = orderType;
	        this.limitPrice = limitPrice;
	    }
	}
	
	private static class StopGainCheck {
	    String symbol;
	    double avgCost;
	    double stopPercent;
	    Decimal quantity;

	    public StopGainCheck(String symbol, double avgCost, double stopPercent, Decimal quantity) {
	        this.symbol = symbol;
	        this.avgCost = avgCost;
	        this.stopPercent = stopPercent;
	        this.quantity = quantity;
	    }
	}
	
}
