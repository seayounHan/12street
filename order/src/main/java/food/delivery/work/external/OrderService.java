package food.delivery.work.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import food.delivery.work.Payment;

import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name="payment", url = "${api.payment.url}", fallback = OrderServiceFallback.class)
public interface OrderService {
  
    @RequestMapping(method=RequestMethod.POST, path="/requestPayment")
    public boolean requestPayment(@RequestBody Payment payment);

}
