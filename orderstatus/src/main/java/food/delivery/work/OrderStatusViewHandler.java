package food.delivery.work;

import food.delivery.work.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class OrderStatusViewHandler {


    @Autowired
    private OrderStatusRepository orderStatusRepository;
    
    @StreamListener(KafkaProcessor.INPUT)
    public void whenOrderPlaced_then_CREATE_1 (@Payload OrderPlaced orderPlaced) {
        try {

            if (!orderPlaced.validate()) return;

            // view 객체 생성
            OrderStatus orderStatus = new OrderStatus();
            orderStatus.setUsername(orderPlaced.getUsername());
            orderStatus.setUserId(orderPlaced.getUserId());
            orderStatus.setOrderId(orderPlaced.getId());
            orderStatus.setOrderStatus("OrderPlaced");
            orderStatus.setProductId(orderPlaced.getProductId());
            orderStatus.setProductName(orderPlaced.getProductName());
            orderStatus.setProductPrice(orderPlaced.getProductPrice());
            orderStatus.setQty(orderPlaced.getQty());
           
            orderStatusRepository.save(orderStatus);
            
            System.out.println("\n\n##### OrderStatus : whenOrderPlaced_then_CREATE_1" + "\n\n");

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenDeliveryStarted_then_UPDATE_1 (@Payload DeliveryStarted deliveryStarted) {
        try {

            if (!deliveryStarted.validate()) return;

            List<OrderStatus> orderStatusList = orderStatusRepository.findByOrderId(deliveryStarted.getOrderId());
            
            for(OrderStatus orderStatus: orderStatusList) {
            	orderStatus.setOrderStatus("DeliveryStarted");
            	orderStatusRepository.save(orderStatus);
            	
            	System.out.println("\n\n##### OrderStatus : whenDeliveryStarted_then_UPDATE_1" + "\n\n");
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    
    @StreamListener(KafkaProcessor.INPUT)
    public void whenDeliveryCanceled_then_UPDATE_1 (@Payload DeliveryCanceled deliveryCanceled) {
        try {

            if (!deliveryCanceled.validate()) return;

            List<OrderStatus> orderStatusList = orderStatusRepository.findByOrderId(deliveryCanceled.getOrderId());
            
            for(OrderStatus orderStatus: orderStatusList) {
            	orderStatus.setOrderStatus("DeliveryCanceled");
            	orderStatusRepository.save(orderStatus);
            	
            	System.out.println("\n\n##### OrderStatus : whenDeliveryCanceled_then_UPDATE_1" + "\n\n");
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    
    @StreamListener(KafkaProcessor.INPUT)
    public void whenCouponPublished_then_UPDATE_1 (@Payload CouponPublished couponPublished) {
        try {

            if (!couponPublished.validate()) return;

            List<OrderStatus> orderStatusList = orderStatusRepository.findByOrderId(couponPublished.getOrderId());
            
            for(OrderStatus orderStatus: orderStatusList) {
            	orderStatus.setOrderStatus("DeliveryStarted and CouponPublished");
            	orderStatus.setCouponId(couponPublished.getCouponId());
            	orderStatus.setCouponKind(couponPublished.getCouponKind());
            	orderStatus.setCouponUseYn(couponPublished.getCouponUseYn());
            	orderStatusRepository.save(orderStatus);
            	
            	System.out.println("\n\n##### OrderStatus : whenCouponPublished_then_UPDATE_1" + "\n\n");
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    
    @StreamListener(KafkaProcessor.INPUT)
    public void whenCouponCanceled_then_UPDATE_1 (@Payload CouponCanceled couponCanceled) {
        try {

            if (!couponCanceled.validate()) return;

            List<OrderStatus> orderStatusList = orderStatusRepository.findByOrderId(couponCanceled.getOrderId());
            
            for(OrderStatus orderStatus: orderStatusList) {
            	orderStatus.setOrderStatus("DeliveryCanceled and CouponCanceled");
            	orderStatus.setCouponId(couponCanceled.getCouponId());
            	orderStatus.setCouponKind(couponCanceled.getCouponKind());
            	orderStatus.setCouponUseYn(couponCanceled.getCouponUseYn());
            	orderStatusRepository.save(orderStatus);
            	
            	System.out.println("\n\n##### OrderStatus : whenCouponCanceled_then_UPDATE_1" + "\n\n");
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }



}

