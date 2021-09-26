package food.delivery.work.external;

import org.springframework.stereotype.Component;

import food.delivery.work.Promote;

@Component
public class PromoteServiceFallback implements PromoteService {
    @Override
    public boolean publishCoupon(Promote promote) {
        //do nothing if you want to forgive it

        System.out.println("Circuit breaker has been opened. Fallback returned instead.");
        return false;
    }
    
    @Override
    public boolean cancelCoupon(Promote promote) {
        //do nothing if you want to forgive it

        System.out.println("Circuit breaker has been opened. Fallback returned instead.");
        return false;
    }
}
