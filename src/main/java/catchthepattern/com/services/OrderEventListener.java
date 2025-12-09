package catchthepattern.com.services;

import catchthepattern.com.models.OrderStatusInfo;

public interface OrderEventListener {
    void onOrderUpdate(OrderStatusInfo info);
}
