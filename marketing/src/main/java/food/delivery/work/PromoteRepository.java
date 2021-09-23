package food.delivery.work;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="promotes", path="promotes")
public interface PromoteRepository extends PagingAndSortingRepository<Promote, Long>{

	List<Promote> findByOrderId(Long orderId);

}
