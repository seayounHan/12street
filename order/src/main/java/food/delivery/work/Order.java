package food.delivery.work;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table(name="Order_table")
public class Order {

    
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String username;
    private String address;
    private String phoneNo;
    private String productId;
    private int qty; //type change
    private String payStatus;
    private String userId;
    private String orderStatus;
    private Date orderDate;
    private String productName;
    private Long productPrice;
    private String couponId;
    private String couponKind;
    private String couponUseYn;

    @PostPersist
    public void onPostPersist(){
    	
         Logger logger = LoggerFactory.getLogger(this.getClass());

    	
        OrderPlaced orderPlaced = new OrderPlaced();
        BeanUtils.copyProperties(this, orderPlaced);
        orderPlaced.publishAfterCommit();
        System.out.println("\n\n##### OrderService : onPostPersist()" + "\n\n");
        System.out.println("\n\n##### orderplace : "+orderPlaced.toJson() + "\n\n");
        System.out.println("\n\n##### productid : "+this.productId + "\n\n");
        logger.debug("OrderService");
    }

    @PostUpdate
    public void onPostUpdate() {
    	
    	OrderCanceled orderCanceled = new OrderCanceled();
        BeanUtils.copyProperties(this, orderCanceled);
        orderCanceled.publishAfterCommit();
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    public String getPhoNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }
    public String getProductId() {
        return productId;
    }

    public void setProductid(String productId) {
        this.productId = productId;
    }
    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }
    public String getPayStatus() {
        return payStatus;
    }

    public void setPayStatus(String payStatus) {
        this.payStatus = payStatus;
    }
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }
    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }
    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
    public Long getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(Long productPrice) {
        this.productPrice = productPrice;
    }

	public String getCouponId() {
		return couponId;
	}

	public void setCouponId(String couponId) {
		this.couponId = couponId;
	}

	public String getCouponKind() {
		return couponKind;
	}

	public void setCouponKind(String couponKind) {
		this.couponKind = couponKind;
	}

	public String getCouponUseYn() {
		return couponUseYn;
	}

	public void setCouponUseYn(String couponUseYn) {
		this.couponUseYn = couponUseYn;
	}

	public String getPhoneNo() {
		return phoneNo;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

}