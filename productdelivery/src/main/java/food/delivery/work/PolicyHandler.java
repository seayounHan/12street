package food.delivery.work;

import food.delivery.work.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PolicyHandler{
    @Autowired StockDeliveryRepository stockDeliveryRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaymentApproved_AcceptOrder(@Payload PaymentApproved paymentApproved){

        if(!paymentApproved.validate()) return;

        // delivery 객체 생성 //
         StockDelivery delivery = new StockDelivery();

         delivery.setOrderId(paymentApproved.getOrderId());
         delivery.setUserId(paymentApproved.getUserId());
         delivery.setOrderDate(paymentApproved.getOrderDate());
         delivery.setPhoneNo(paymentApproved.getPhoneNo());
         delivery.setProductId(paymentApproved.getProductId());
         delivery.setQty(paymentApproved.getQty()); 
         delivery.setDeliveryStatus("delivery Started");

         System.out.println("==================================");
         System.out.println(paymentApproved.getOrderId());
         System.out.println(paymentApproved.toJson());
         System.out.println("==================================");
         System.out.println(delivery.getOrderId());

         stockDeliveryRepository.save(delivery);

    }
    private Integer parseInt(String qty) {
        return null;
    }
    /*
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrderCanceled_CancleOrder(@Payload OrderCanceled orderCanceled){

        if(!orderCanceled.validate()) return;

        Long orderId =Long.valueOf(orderCanceled.getId());
        stockDeliveryRepository.deleteById(orderId); 
        
        stockDeliveryRepository.s

    }
    */
    
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverPaymentCanceled_CancleOrder(@Payload PaymentCanceled paymentCanceled) {
    	
    	if(!paymentCanceled.validate()) return;
    	System.out.println("\n\n========= wheneverPaymentCanceled_CancleOrder =============");
    	System.out.println("\n order id : "+paymentCanceled.getOrderId());
        List<StockDelivery> deliveryList = stockDeliveryRepository.findByOrderId(paymentCanceled.getOrderId());

        for (StockDelivery delivery:deliveryList)
        {
            delivery.setDeliveryStatus("delivery Canceled");
            stockDeliveryRepository.save(delivery);
        }

       
    }

  
}
