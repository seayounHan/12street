package food.delivery.work;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


 @RestController
 public class StockDeliveryController {

    @Autowired
    StockDeliveryRepository deliveryRepository;

    @RequestMapping(value = "/cancelDelivery", method=RequestMethod.DELETE)
    public boolean cancelDelivery(HttpServletRequest request, HttpServletResponse response) throws Exception{

        String id = request.getParameter("id");
        java.util.Optional<StockDelivery> optionalDelivery = deliveryRepository.findById(Long.valueOf(id));
        StockDelivery delivery = optionalDelivery.get();

        deliveryRepository.delete(delivery);
       
        return true;
    }

 }