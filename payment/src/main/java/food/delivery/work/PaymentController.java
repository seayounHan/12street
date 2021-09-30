package food.delivery.work;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
 public class PaymentController {
	 
	 @Autowired
	 PaymentRepository paymentRepository;
	 
	 @Autowired
	 ConfigMap configmap;
	
	 private String serverType = configmap.getServertype();
	 private String serverUser = configmap.getServeruser();
	 
     @PostMapping(value = "/requestPayment")
     public boolean requestPayment(@RequestBody Map<String, String> param) {
    	 
        System.out.println("================================================= \n");
        System.out.println("SERVER TYPE : "+serverType+"\n");
        System.out.println("SERVER USER : "+serverUser+"\n");
        System.out.println("================================================= \n"); 
    	 
        boolean result = false;
        
        Payment payment = new Payment();
        payment.setUsername(param.get("username"));
        payment.setPhoneNo(param.get("phoneNo")); 
        payment.setUserId(param.get("userId")); 
        payment.setAddress(param.get("address"));
        payment.setPhoneNo(param.get("phoneNo"));
        payment.setProductId(param.get("productId"));
        payment.setQty(Integer.parseInt((param.get("qty"))));
        payment.setPayStatus("Y");
        payment.setOrderId(Long.parseLong(param.get("orderId"))); 
        payment.setOrderStatus(param.get("orderStatus"));
        payment.setProductName(param.get("productName"));
        payment.setProductPrice(Long.parseLong(param.get("productPrice")));

        try {
            paymentRepository.save(payment);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
	 
 }