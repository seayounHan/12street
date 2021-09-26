package food.delivery.work;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel="stockDeliveries", path="stockDeliveries")
public interface StockDeliveryRepository extends CrudRepository<StockDelivery, Long>{

	 List<StockDelivery> findByOrderId(Long orderId);

}
