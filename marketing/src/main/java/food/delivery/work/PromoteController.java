package food.delivery.work;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;

 @RestController
 public class PromoteController {
	 
	 @Autowired
	 PromoteRepository promoteRepository;

     @PostMapping(value = "/createPromoteInfo")
     public boolean createPromoteInfo(@RequestBody Map<String, String> param) {

        boolean result = false;
        
        Promote promote = new Promote();
        promote.setPhoneNo(param.get("phoneNo")); 
        promote.setUserId(param.get("userId")); 
        promote.setUsername(param.get("userName")); 
        promote.setOrderId(Long.parseLong(param.get("orderId"))); 
        promote.setOrderStatus(param.get("orderStatus")); 
        promote.setProductId(param.get("productId"));
        promote.setCouponId("COUPON1");
        promote.setCouponKind("DISCOUNT");
        promote.setCouponUseYn("N");
        promoteRepository.save(promote);
        try {
            promote = promoteRepository.save(promote);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
     
     @PostMapping(value = "/cancelCoupon")
     public boolean cancelCoupon(@RequestBody Map<String, String> param) {

        boolean result = false;
        
        List<Promote> promoteList = promoteRepository.findByOrderId(Long.parseLong(param.get("orderId")));
        
        try {
	        for (Promote promote:promoteList)
	        {
	        	promote.setCouponId("");
	        	promote.setCouponKind("");
	            promote.setCouponUseYn("");
	            promoteRepository.save(promote);
	            result = true;
	        }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return result;

    }
	 
 }