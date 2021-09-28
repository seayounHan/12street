package food.delivery.work;

import food.delivery.work.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired PaymentRepository paymentRepository;
    
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverOrderCanceled_CancelPayment(@Payload OrderCanceled orderCanceled) {
    	
    	if(!orderCanceled.validate()) return;
    	
    	System.out.println("\n CancelPayment \n");
    	System.out.println("order id : "+orderCanceled.getId());
    	
        List<Payment> paymentList = paymentRepository.findByOrderId(orderCanceled.getId());
        System.out.println("\n payment count :"+paymentList.size() );
        
        for (Payment payment:paymentList)
        {
        	System.out.println("\n before cancel payment");
        	payment.setPayStatus("CANCEL");
        	paymentRepository.save(payment);
        }

       
    }
 
}
