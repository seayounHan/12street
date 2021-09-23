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
    @Autowired OrderRepository orderRepository;
    /*
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverDeliveryStarted_UpdateOrderStatus(@Payload DeliveryStarted deliveryStarted){

        if(!deliveryStarted.validate()) return;

        System.out.println("\n\n##### listener UpdateOrderStatus : " + deliveryStarted.toJson() + "\n\n");

        Optional<Order> orderResponse = orderRepository.findById(deliveryStarted.getOrderId());
        
        Order order = orderResponse.get();
        
        //deliveryStatus 보고 확인하여  변경해주
    	order.setOrderStatus("deliveryStarted");
    	orderRepository.save(order);

    }
    
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverDeliveryCancled_UpdateOrderStatus(@Payload DeliveryCanceled deliveryCanceled){

        if(!deliveryCanceled.validate()) return;

        System.out.println("\n\n##### listener UpdateOrderStatus : " + deliveryCanceled.toJson() + "\n\n");

        Optional<Order> orderResponse = orderRepository.findById(deliveryCanceled.getOrderId());
        
        Order order = orderResponse.get();
    	order.setOrderStatus("deliveryCanceled");
    	orderRepository.save(order);

    }
    
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverCouponPublished_UpdateCouponStatus(@Payload CouponPublished couponpublished){

        if(!couponpublished.validate()) return;

        System.out.println("\n\n##### listener UpdateOrderStatus : " + couponpublished.toJson() + "\n\n");

        Optional<Order> orderResponse = orderRepository.findById(couponpublished.getOrderId());
        
        Order order = orderResponse.get();
    	order.setCouponId(couponpublished.getCouponId());
    	order.setCouponKind(couponpublished.getCouponKind());
    	order.setCouponUseYn(couponpublished.getCouponUseYn());
    	orderRepository.save(order);

    }
    
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverCouponCanceled_UpdateCouponStatus(@Payload CouponCanceled couponcanceled){

        if(!couponcanceled.validate()) return;

        System.out.println("\n\n##### listener UpdateOrderStatus : " + couponcanceled.toJson() + "\n\n");

        Optional<Order> orderResponse = orderRepository.findById(couponcanceled.getOrderId());
        
        Order order = orderResponse.get();
    	order.setCouponId(couponcanceled.getCouponId());
    	order.setCouponKind(couponcanceled.getCouponKind());
    	order.setCouponUseYn(couponcanceled.getCouponUseYn());
    	orderRepository.save(order);

    }
    */
}
