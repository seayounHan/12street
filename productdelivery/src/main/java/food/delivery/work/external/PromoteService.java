package food.delivery.work.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import food.delivery.work.Promote;

import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name="marketing", url = "${api.promote.url}", fallback = PromoteServiceFallback.class)
public interface PromoteService {
  
    @RequestMapping(method=RequestMethod.POST, path="/createPromoteInfo")
    public boolean publishCoupon(@RequestBody Promote promote);
    
    @RequestMapping(method=RequestMethod.POST, path="/cancelCoupon")
    public boolean cancelCoupon(@RequestBody Promote promote);
}
