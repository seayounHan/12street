package food.delivery.work.external;

import org.springframework.stereotype.Component;

import food.delivery.work.Payment;

@Component
public class OrderServiceFallback implements OrderService {
    @Override
    public boolean requestPayment(Payment payment) {
        //do nothing if you want to forgive it

        System.out.println("Circuit breaker has been opened. Fallback returned instead.");
        return false;
    }

}
