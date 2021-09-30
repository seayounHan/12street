package food.delivery.work;
import food.delivery.work.config.kafka.KafkaProcessor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.openfeign.EnableFeignClients;


@SpringBootApplication
@EnableBinding(KafkaProcessor.class)
@EnableFeignClients
public class PaymentApplication {
    protected static ApplicationContext applicationContext;
    
    @Value("${systeminfo.servertype}")
    protected static String serverType;
    
    public static void main(String[] args) {
    	
        System.out.println("================================================= \n");
        System.out.println("SERVER TYPE : "+serverType+"\n");
        System.out.println("================================================= \n");
    	
        applicationContext = SpringApplication.run(PaymentApplication.class, args);
                
    }
}