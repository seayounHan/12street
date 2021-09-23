package food.delivery.work;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel="orderStatus", path="orderStatus")
public interface OrderStatusRepository extends CrudRepository<OrderStatus, Long> {
	
	List<OrderStatus> findByOrderId(Long orderId);

}