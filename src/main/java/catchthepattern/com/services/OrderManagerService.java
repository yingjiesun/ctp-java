package catchthepattern.com.services;

import catchthepattern.com.models.OrderStatusInfo;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import com.ib.client.Decimal;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Service
public class OrderManagerService {

    private final Map<Integer, OrderStatusInfo> orders = new ConcurrentHashMap<>();
    private final List<OrderEventListener> listeners = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService cleanerExecutor = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    public void init() {
        cleanerExecutor.scheduleAtFixedRate(this::cleanupCompletedOrders, 10, 10, TimeUnit.MINUTES);
    }

    @PreDestroy
    public void shutdown() {
        cleanerExecutor.shutdown();
    }

    public void registerListener(OrderEventListener listener) {
        listeners.add(listener);
    }

    public void trackOpenOrder(int orderId, String status) {
        orders.compute(orderId, (id, info) -> {
            if (info == null) info = new OrderStatusInfo(orderId);
            info.setStatus(status);
            notifyListeners(info);
            return info;
        });
    }

    public void trackOrderStatus(int orderId, String status, Decimal filled, Decimal remaining) {
        orders.compute(orderId, (id, info) -> {
            if (info == null) info = new OrderStatusInfo(orderId);
            info.setStatus(status);
            info.setFilled(filled);
            info.setRemaining(remaining);
            notifyListeners(info);
            return info;
        });
    }

    public void trackError(int orderId, String errorMessage) {
        orders.compute(orderId, (id, info) -> {
            if (info == null) info = new OrderStatusInfo(orderId);
            info.setStatus("Error");
            info.setErrorMessage(errorMessage);
            notifyListeners(info);
            return info;
        });
    }

    public OrderStatusInfo getOrderStatus(int orderId) {
        return orders.get(orderId);
    }

    private void notifyListeners(OrderStatusInfo info) {
        for (OrderEventListener listener : listeners) {
            listener.onOrderUpdate(info);
        }
    }

    private void cleanupCompletedOrders() {
        for (Iterator<Map.Entry<Integer, OrderStatusInfo>> it = orders.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Integer, OrderStatusInfo> entry = it.next();
            OrderStatusInfo info = entry.getValue();
            if (info.isCompleted() && Duration.between(info.getLastUpdatedAt(), Instant.now()).toMinutes() > 15) {
                it.remove();
                System.out.println("Cleaned up completed orderId=" + info.getOrderId());
            }
        }
    }
}
