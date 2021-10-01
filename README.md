# 개인과제 - 12번가

![12번가](https://user-images.githubusercontent.com/88864433/133467597-709524b1-4613-4dab-bc57-948f433ad565.png)
---------------------------------

# Table of contents

- [개인과제 - 12번가 배송서비스](#---)
  - [서비스 시나리오](#서비스-시나리오)
  - [체크포인트](#체크포인트)
  - [분석/설계](#분석설계)
  - [구현:](#구현)
    - [DDD 의 적용](#DDD의-적용)
    - [동기식 호출과 Fallback 처리](#동기식-호출과-Fallback-처리) 
    - [비동기식 호출과 Eventual Consistency](#비동기식-호출과-Eventual-Consistency) 
    - [폴리글랏 퍼시스턴스](#폴리글랏-퍼시스턴스)
    - [API 게이트웨이](#API-게이트웨이)
  - [운영](#운영)
    - [Deploy/Pipeline](#deploypipeline)
    - [동기식 호출 / Circuit Breaker / 장애격리](#동기식-호출-circuit-breaker-장애격리)
    - [Autoscale (HPA)](#autoscale-(hpa))
    - [Zero-downtime deploy (Readiness Probe)](#zerodowntime-deploy-(readiness-Probe))
    - [Self-healing (Liveness Probe)](#self-healing-(liveness-probe))
    - [운영유연성](#운영유연성)



# 서비스 시나리오
	
[서비스 아키텍쳐]
주문팀, 상품배송팀, 마케팅팀, 결제팀

[서비스 시나리오]

가. 기능적 요구사항

1. [주문팀] 고객이 상품을 선택하여 주문 및 결제 한다.

2. [주문팀] 주문이 되면 결제팀에 요금 결제를 요청 한다. ( Req/Res )

3. [결제팀] 주문 정보를 바탕으로 결제를 진행하여, 주문팀에 알린다. ( Req/Res )

4. [결제팀] 결제가 완료가 되면, 결제가 완료됨을 상품배송팀에 전달한다.

5. [상품배송팀] 주문 및 결제 내역을 확인하고 쿠폰 발행을 요청한다. ( Req/Res )

6. [마케팅팀] 쿠폰을 발행하고 상품배송팀에 알린다. ( Req/Res )

7. [상품배송팀] 쿠폰이 발행이 완료되면(반드시), 배송을 출발한다.

##### CORRELATION #####

1. [주문팀] 고객이 주문을 취소한다.

2. [주문팀] 주문 취소를 결제팀에 알린다.

3. [결제팀] 주문 취소를 접수하고, 결제 취소를 진행한다.

4. [결제팀] 결제 취소 정보를 상품배송팀에 전달한다.

5. [상품배송팀] 주문 취소를 확인하고 쿠폰 취소를 요청한다. ( Req/Res )

6. [마케팅팀] 발행된 쿠폰을 취소하고 상품배송팀에 알린다. ( Req/Res )

7. [상품배송팀] 쿠폰이 발행이 취소되면(반드시), 배송을 취소한다.


나. 비기능적 요구사항

1. [설계/구현]Req/Resp : 주문을 요청하면, 결제를 완료해야 주문이 성립 된다. 
 
2. [설계/구현]CQRS : 고객이 결제 정보를 주문상태(orderstatus) 화면을 통해 확인이 가능하다.

3. [설계/구현]Correlation : 주문을 취소하면 -> 결제를 취소하고 -> 쿠폰을 취소하고 -> 배달을 취소 후 주문 상태 변경

4. [설계/구현]saga : 서비스(상품팀, 상품배송팀, 마케팅팀, 결제팀)는 단일 서비스 내의 데이터를 처리하고, 각자의 이벤트를 발행하면 연관된 서비스에서 이벤트에 반응하여 각자의 데이터를 변경시킨다.

5. [설계/구현/운영]circuit breaker : 결제 요청 건수가 임계치 이상 발생할 경우 Circuit Breaker 가 발동된다. 

다. 기타 

1. [설계/구현/운영]polyglot : 결제팀은 다른 DB를 사용하여 polyglot을 충족시킨다.


# 체크포인트

+ 분석 설계

	- 이벤트스토밍:
		- 스티커 색상별 객체의 의미를 제대로 이해하여 헥사고날 아키텍처와의 연계 설계에 적절히 반영하고 있는가?
		- 각 도메인 이벤트가 의미있는 수준으로 정의되었는가?
		- 어그리게잇: Command와 Event 들을 ACID 트랜잭션 단위의 Aggregate 로 제대로 묶었는가?
		- 기능적 요구사항과 비기능적 요구사항을 누락 없이 반영하였는가?
	- 서브 도메인, 바운디드 컨텍스트 분리
		- 팀별 KPI 와 관심사, 상이한 배포주기 등에 따른  Sub-domain 이나 Bounded Context 를 적절히 분리하였고 그 분리 기준의 합리성이 충분히 설명되는가?
			- 적어도 3개 이상 서비스 분리
		- 폴리글랏 설계: 각 마이크로 서비스들의 구현 목표와 기능 특성에 따른 각자의 기술 Stack 과 저장소 구조를 다양하게 채택하여 설계하였는가?
		- 서비스 시나리오 중 ACID 트랜잭션이 크리티컬한 Use 케이스에 대하여 무리하게 서비스가 과다하게 조밀히 분리되지 않았는가?
	- 컨텍스트 매핑 / 이벤트 드리븐 아키텍처
		- 업무 중요성과  도메인간 서열을 구분할 수 있는가? (Core, Supporting, General Domain)
		- Request-Response 방식과 이벤트 드리븐 방식을 구분하여 설계할 수 있는가?
		- 장애격리: 서포팅 서비스를 제거 하여도 기존 서비스에 영향이 없도록 설계하였는가?
		- 신규 서비스를 추가 하였을때 기존 서비스의 데이터베이스에 영향이 없도록 설계(열려있는 아키택처)할 수 있는가?
		- 이벤트와 폴리시를 연결하기 위한 Correlation-key 연결을 제대로 설계하였는가?
	- 헥사고날 아키텍처
		- 설계 결과에 따른 헥사고날 아키텍처 다이어그램을 제대로 그렸는가?

- 구현

	- [DDD] 분석단계에서의 스티커별 색상과 헥사고날 아키텍처에 따라 구현체가 매핑되게 개발되었는가?
		-Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 데이터 접근 어댑터를 개발하였는가
		- [헥사고날 아키텍처] REST Inbound adaptor 이외에 gRPC 등의 Inbound Adaptor 를 추가함에 있어서 도메인 모델의 손상을 주지 않고 새로운 프로토콜에 기존 구현체를 적응시킬 수 있는가?
		- 분석단계에서의 유비쿼터스 랭귀지 (업무현장에서 쓰는 용어) 를 사용하여 소스코드가 서술되었는가?
	- Request-Response 방식의 서비스 중심 아키텍처 구현
		- 마이크로 서비스간 Request-Response 호출에 있어 대상 서비스를 어떠한 방식으로 찾아서 호출 하였는가? (Service Discovery, REST, FeignClient)
		- 서킷브레이커를 통하여  장애를 격리시킬 수 있는가?
	- 이벤트 드리븐 아키텍처의 구현
		- 카프카를 이용하여 PubSub 으로 하나 이상의 서비스가 연동되었는가?
		- Correlation-key: 각 이벤트 건 (메시지)가 어떠한 폴리시를 처리할때 어떤 건에 연결된 처리건인지를 구별하기 위한 Correlation-key 연결을 제대로 구현 하였는가?
		- Message Consumer 마이크로서비스가 장애상황에서 수신받지 못했던 기존 이벤트들을 다시 수신받아 처리하는가?
		- Scaling-out: Message Consumer 마이크로서비스의 Replica 를 추가했을때 중복없이 이벤트를 수신할 수 있는가
		- CQRS: Materialized View 를 구현하여, 타 마이크로서비스의 데이터 원본에 접근없이(Composite 서비스나 조인SQL 등 없이) 도 내 서비스의 화면 구성과 잦은 조회가 가능한가?

	- 폴리글랏 플로그래밍

		- 각 마이크로 서비스들이 하나이상의 각자의 기술 Stack 으로 구성되었는가?
		- 각 마이크로 서비스들이 각자의 저장소 구조를 자율적으로 채택하고 각자의 저장소 유형 (RDB, NoSQL, File System 등)을 선택하여 구현하였는가?

	- API 게이트웨이

		- API GW를 통하여 마이크로 서비스들의 집입점을 통일할 수 있는가?
		- 게이트웨이와 인증서버(OAuth), JWT 토큰 인증을 통하여 마이크로서비스들을 보호할 수 있는가?

- 운영
	- SLA 준수
		- 셀프힐링: Liveness Probe 를 통하여 어떠한 서비스의 health 상태가 지속적으로 저하됨에 따라 어떠한 임계치에서 pod 가 재생되는 것을 증명할 수 있는가?
		- 서킷브레이커, 레이트리밋 등을 통한 장애격리와 성능효율을 높힐 수 있는가?
		- 오토스케일러 (HPA) 를 설정하여 확장적 운영이 가능한가?
		- 모니터링, 앨럿팅:

	- 무정지 운영 CI/CD (10)
		- Readiness Probe 의 설정과 Rolling update을 통하여 신규 버전이 완전히 서비스를 받을 수 있는 상태일때 신규버전의 서비스로 전환됨을 siege 등으로 증명
		- Contract Test : 자동화된 경계 테스트를 통하여 구현 오류나 API 계약위반를 미리 차단 가능한가?
---

# 분석/설계

## Event Stoming 결과

- MSAEz로 모델링한 이벤트스토밍 결과
https://www.msaez.io/#/storming/7znb05057kPWQo1TAWCkGM0O2LJ3/5843d1078a788a01aa837bc508a68029


### 이벤트 도출

![1](https://user-images.githubusercontent.com/88864433/133356420-db8f0cf8-a3f6-4d24-8242-e9e739401045.PNG)

```
1차적으로 필요하다고 생각되는 이벤트를 도출하였다 
``` 

### 부적격 이벤트 탈락

![2](https://user-images.githubusercontent.com/88864433/133356470-ee9c68e5-50c7-45b8-8bf2-15b9ee408036.PNG)

```
- 과정 중 도출된 잘못된 도메인 이벤트들을 걸러내는 작업을 수행함
- ‘재고가 충족됨’, ‘재고가 부족함’ 은 배송 이벤트를 수행하기 위한 내용에 가까우므로 이벤트에서 제외
- 주문과 결제는 동시에 이루어진다고 봐서 주문과 결제를 묶음 -> 개별 이벤트로 분리 하였다. 
```

![3](https://user-images.githubusercontent.com/62110109/135549360-b341220a-5359-4d91-b700-747d02ff7a10.png)

### 액터, 커맨드를 부착하여 읽기 좋게 

![4-3](https://user-images.githubusercontent.com/62110109/135549417-b1f6aaa7-903c-4050-b4dd-f3f768456733.png)

 
### 어그리게잇으로 묶기

![5-3](https://user-images.githubusercontent.com/62110109/135549457-c382a0e5-9171-44b6-84ba-92e0ac97e4e2.png)
 
``` 
- 고객의 주문후 배송팀의 배송관리, 마케팅의 쿠폰관리는 command와 event 들에 의하여 트랜잭션이 유지되어야 하는 단위로 묶어줌
```

### 바운디드 컨텍스트로 묶기

![6-3](https://user-images.githubusercontent.com/62110109/135549543-889f4b59-9a74-4d98-b839-d9f28fb0e571.png)
 
```
- 도메인 서열 분리 
    - Core Domain:  order, delivery : 없어서는 안될 핵심 서비스이며, 연간 Up-time SLA 수준을 99.999% 목표, 배포주기는 order의 경우 1주일 1회 미만, delivery의 경우 1개월 1회 미만
    - Supporting Domain:  marketing : 경쟁력을 내기위한 서비스이며, SLA 수준은 연간 60% 이상 uptime 목표, 배포주기는 각 팀의 자율이나 표준 스프린트 주기가 1주일 이므로 1주일 1회 이상을 기준으로 함
```
### 폴리시 부착


![7-3](https://user-images.githubusercontent.com/62110109/135549569-03f230e8-fa2b-4d4f-be03-cce36f0fbed7.png)
 

### 폴리시의 이동과 컨텍스트 맵핑 (점선은 Pub/Sub, 실선은 Req/Resp) 

![8-3](https://user-images.githubusercontent.com/62110109/135550000-b2599981-dcac-4da1-8312-40f62f5eba04.png)
 

### 완성된 모형

![모델](https://user-images.githubusercontent.com/62110109/135550039-2b5e8315-9419-427a-97f3-1c831490fd6b.png)
 
### 완성본에 대한 기능적/비기능적 요구사항을 커버하는지 검증

![주문완료검증](https://user-images.githubusercontent.com/62110109/135550086-bffd14af-31ff-4d34-917e-46a36549bb2a.png)

```
- 고객이 물건을 주문하고 결제요청한다. (ok)
- 결제팀에서 결제를 완료하고 주문팀에 알린다 (ok)
- 결제가 완료되면 주문/결제 내역이 배송팀에 전달된다 (ok)
- 마케팅팀에서 쿠폰을 발행한다 (ok) 
- 쿠폰이 발행된 것을 확인하고 배송을 시작한다 (ok)
```
![주문취소검증](https://user-images.githubusercontent.com/62110109/135550337-aa4a7bd3-c757-455d-aff4-e99cc11231e4.png)

``` 
- 고객이 주문을 취소한다 (ok)
- 주문 취소 후 결제 취소를 요청한다 (ok)
- 결제 취소가 완료되면 배송팀에 전달한다 (ok)
- 주문/결제 취소를 배송팀에서 확인후 마케팅팀에 쿠폰 취소요청 한다(ok)
- 마케팅팀에서 쿠폰발행을 취소한다 (ok)
- 쿠폰발행이 취소되면 배송팀에서 배송을 취소한다 (ok)
```

### 비기능 요구사항에 대한 검증 (5개가 맞는지 검토 필요)

![비기능적 요구사항2](https://user-images.githubusercontent.com/62110109/135550458-0ce802b9-0da2-46e8-bb76-1c41b708e6d6.png)

```
1. [설계/구현]Req/Resp : 주문 완료는 결제가 완료가 되어야 한다.
2. [설계/구현]CQRS : 고객이 결제 상태를 확인 가능해야한다.
3. [설계/구현]Correlation : 주문을 취소하면 -> 결제를 취소 -> 쿠폰을 취소하고 -> 배달을 취소 후 주문 상태 변경
4. [설계/구현]saga : 서비스(주문팀, 결제팀, 상품배송팀, 마케팅팀)는 단일 서비스 내의 데이터를 처리하고, 각자의 이벤트를 발행하면 연관된 서비스에서 이벤트에 반응하여 각자의 데이터를 변경시킨다.
5. [설계/구현/운영]circuit breaker : 결제 요청 건수가 임계치 이상 발생할 경우 Circuit Breaker 가 발동된다. 
``` 

### 헥사고날 아키텍처 다이어그램 도출 (그림 수정필요없는지 확인 필요)

![분산스트림2](https://user-images.githubusercontent.com/88864433/133557657-451e67e9-400a-477c-af09-2bfd56f9a659.PNG)
 

```
- Chris Richardson, MSA Patterns 참고하여 Inbound adaptor와 Outbound adaptor를 구분함
- 호출관계에서 PubSub 과 Req/Resp 를 구분함
- 서브 도메인과 바운디드 컨텍스트의 분리:  각 팀의 KPI 별로 아래와 같이 관심 구현 스토리를 나눠가짐
```

# 구현

- 분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 바운더리 컨텍스트 별로 대변되는 마이크로 서비스들을 스프링부트로 구현하였다. 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다 (각자의 포트넘버는 8081 ~ 808n 이다)

```
cd order
mvn spring-boot:run

cd payment
mvn spring-boot:run

cd productdelivery 
mvn spring-boot:run

cd marketing
mvn spring-boot:run 

cd orderstatus
mvn spring-boot:run 
```

# DDD의 적용
- Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 데이터 접근 어댑터를 개발하였는가? 

각 서비스 내에 도출된 핵심 Aggregate Root 객체를 Entity로 선언하였다. 
(주문(order), 결제(payment), 배송(productdelivery), 마케팅(marketing)) 

결제 Entity (Payment.java) 
```
@Entity
@Table(name="Payment_table")
public class Payment {

    
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
    private Long orderId;
    private String orderStatus;
    private Date orderDate;
    private String productName;
    private Long productPrice;
    
    @PrePersist
    public void onPrePersist(){

    	/*
        try {
            Thread.currentThread().sleep((long) (700 + Math.random() * 220));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        */
    }

    @PostPersist
    public void onPostPersist(){
    	/**/
        Logger logger = LoggerFactory.getLogger(this.getClass());

    	
        PaymentApproved paymentApproved = new PaymentApproved();
        BeanUtils.copyProperties(this, paymentApproved);
        paymentApproved.publishAfterCommit();
        System.out.println("\n\n##### paymentService : onPostPersist()" + "\n\n");
        System.out.println("\n\n##### paymentApproved : "+paymentApproved.toJson() + "\n\n");
        System.out.println("\n\n##### payStatus : "+this.payStatus + "\n\n");
        logger.debug("PaymentService");
    }

    @PostUpdate
    public void onPostUpdate() {
    	
    	PaymentCanceled paymentCanceled = new PaymentCanceled();
        BeanUtils.copyProperties(this, paymentCanceled);
        paymentCanceled.publishAfterCommit();
    }
    
....생략 

```

Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 다양한 데이터소스 유형 (RDB or NoSQL) 에 대한 별도의 처리가 없도록 하였고 데이터 접근 어댑터를 자동 생성하기 위하여 Spring Data REST 의 RestRepository 를 적용하였다

PaymentRepository.java

```
@RepositoryRestResource(collectionResourceRel="payments", path="payments")
public interface PaymentRepository extends PagingAndSortingRepository<Payment, Long>{
	
	List<Payment> findByOrderId(Long orderId);
	
}
```

배송팀의 StockDelivery.java

```
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
```
```
@Entity
@Table(name="StockDelivery_table")
public class StockDelivery {

     //Distance 삭제 및 Id auto로 변경
    
    private Long orderId;
    private String orderStatus;
    private String userName;
    private String address;
    private String productId;
    private Integer qty;
    private String storeName;
    private Date orderDate;
    private Date confirmDate;
    private String productName;
    private String phoneNo;
    private Long productPrice;
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String customerId;
    private String deliveryStatus;
    private Date deliveryDate;
    private String userId;
    
    private static final String DELIVERY_STARTED = "delivery Started";
    private static final String DELIVERY_CANCELED = "delivery Canceled";
... 생략 
```

마케팅의 promote.java 

``` 
@Entity
@Table(name="Promote_table")
public class Promote {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String phoneNo;
    private String username;
    private Long orderId;
    private String orderStatus;
    private String productId;
    private String payStatus;
    private String couponId;
    private String couponKind;
    private String couponUseYn;
    private String userId;

    @PostPersist
    public void onPostPersist(){
        CouponPublished couponPublished = new CouponPublished();
        BeanUtils.copyProperties(this, couponPublished);
        couponPublished.publishAfterCommit();

    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPhoneNo() {
		return phoneNo;
	}
.... 생략 

```

PromoteRepository.java

```
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
public interface PromoteRepository extends PagingAndSortingRepository<Promote, Long>{

	List<Promote> findByOrderId(Long orderId);

}
```

- 분석단계에서의 유비쿼터스 랭귀지 (업무현장에서 쓰는 용어) 를 사용하여 소스코드가 서술되었는가?
가능한 현업에서 사용하는 언어를 모델링 및 구현시 그대로 사용하려고 노력하였다. 

- 적용 후 Rest API의 테스트
주문 결제 후 productdelivery 주문 접수하기 POST

```
[시나리오 1]
http POST http://aedb7e1cae2d84953b471cb6b57ed58f-1249713815.ap-southeast-1.elb.amazonaws.com:8080/orders address=“Seoul” productId=“1001" payStatus=“Y” phoneNo=“01011110000" productName=“Mac” productPrice=3000000 qty=1 userId=“goodman” username=“John”
http POST http://aedb7e1cae2d84953b471cb6b57ed58f-1249713815.ap-southeast-1.elb.amazonaws.com:8080/orders address=“England” productId=“2001” payStatus=“Y” phoneNo=“0102220000” productName=“gram” productPrice=9000000 qty=1 userId=“gentleman” username=“John”
http POST http://aedb7e1cae2d84953b471cb6b57ed58f-1249713815.ap-southeast-1.elb.amazonaws.com:8080/orders address=“USA” productId=“3001" payStatus=“Y” phoneNo=“01030000" productName=“Mac” productPrice=3000000 qty=1 userId=“goodman” username=“John”
http POST http://aedb7e1cae2d84953b471cb6b57ed58f-1249713815.ap-southeast-1.elb.amazonaws.com:8080/orders address=“USA” productId=“3001” payStatus=“Y” phoneNo=“01030000” productName=“Mac” productPrice=3000000 qty=1 userId=“last test” username=“last test”
[시나리오 2]
http PATCH http://aedb7e1cae2d84953b471cb6b57ed58f-1249713815.ap-southeast-1.elb.amazonaws.com:8080/orders/1 orderStatus=“Order Canceled”
http PATCH http://aedb7e1cae2d84953b471cb6b57ed58f-1249713815.ap-southeast-1.elb.amazonaws.com:8080/orders/3 orderStatus=“Order Canceled”
http PATCH http://aedb7e1cae2d84953b471cb6b57ed58f-1249713815.ap-southeast-1.elb.amazonaws.com:8080/orders/5 orderStatus=“Order Canceled”
[체크]
http GET http://aedb7e1cae2d84953b471cb6b57ed58f-1249713815.ap-southeast-1.elb.amazonaws.com:8080/orders
http GET http://aedb7e1cae2d84953b471cb6b57ed58f-1249713815.ap-southeast-1.elb.amazonaws.com:8080/payments
http GET http://aedb7e1cae2d84953b471cb6b57ed58f-1249713815.ap-southeast-1.elb.amazonaws.com:8080/orderStatus
http GET http://aedb7e1cae2d84953b471cb6b57ed58f-1249713815.ap-southeast-1.elb.amazonaws.com:8080/stockDeliveries
http GET http://aedb7e1cae2d84953b471cb6b57ed58f-1249713815.ap-southeast-1.elb.amazonaws.com:8080/promotes
```


# 동기식 호출과 Fallback 처리

(Request-Response 방식의 서비스 중심 아키텍처 구현)

- 마이크로 서비스간 Request-Response 호출에 있어 대상 서비스를 어떠한 방식으로 찾아서 호출 하였는가? (Service Discovery, REST, FeignClient)

요구사항대로 주문은 결제가 완료된 이후 주문완료됨 이벤트를 발송한다.

- Order.java

```
    @PostPersist
    public void onPostPersist(){
    	
        //Logger logger = LoggerFactory.getLogger(this.getClass());
        
        Payment payment = new Payment(); 
        payment.setUsername(this.username);
        payment.setPhoneNo(this.phoneNo); 
        payment.setUserId(this.userId); 
        payment.setAddress(this.address);
        payment.setPhoneNo(this.phoneNo);
        payment.setProductId(this.productId);
        payment.setQty(this.qty);
        payment.setPayStatus("N");
        payment.setOrderId(this.id);
        payment.setOrderStatus(this.orderStatus);
        payment.setProductName(this.productName);
        payment.setProductPrice(this.productPrice);
        
        System.out.println("\n\n##### OrderService : before req/res");
    	boolean result = (boolean) OrderApplication.applicationContext.getBean(food.delivery.work.external.OrderService.class).requestPayment(payment);
    	System.out.println("\n\n##### OrderService : after req/res");
        
    	if(result){
    		// 문제없이 결제가 되었기 때문에 payStatus update
    		this.payStatus = "Y";
    		
	        OrderPlaced orderPlaced = new OrderPlaced();
	        BeanUtils.copyProperties(this, orderPlaced);
	        orderPlaced.publishAfterCommit();
	        System.out.println("\n\n##### OrderService : onPostPersist()" + "\n\n");
	        System.out.println("\n\n##### orderplace : "+orderPlaced.toJson() + "\n\n");
	        System.out.println("\n\n##### productid : "+this.productId + "\n\n");
	        //logger.debug("OrderService");
    	}else {
        	throw new RollbackException("Failed during request payment");
        }
    }
    
```

##### 동기식 호출은 Order서비스에 OrderService 클래스를 두어 FeignClient 를 이용하여 결제서비스 호출하도록 하였다.

- OrderService.java

```

@FeignClient(name="payment", url = "${api.payment.url}", fallback = OrderServiceFallback.class)
public interface OrderService {
  
    @RequestMapping(method=RequestMethod.POST, path="/requestPayment")
    public boolean requestPayment(@RequestBody Payment payment);

}
```

- OrderServiceFallback.java

```
  
@Component
public class OrderServiceFallback implements OrderService {
    @Override
    public boolean requestPayment(Payment payment) {
        //do nothing if you want to forgive it

        System.out.println("Circuit breaker has been opened. Fallback returned instead.");
        return false;
    }

}

```


# 비동기식 호출과 Eventual Consistency (작성완료)

(이벤트 드리븐 아키텍처)

- 카프카를 이용하여 PubSub 으로 하나 이상의 서비스가 연동되었는가?

주문/주문취소 후에 이를 결제팀에 알려주는 트랜잭션은 Pub/Sub 관계로 구현하였다.
아래는 주문/주문취소 이벤트를 통해 kafka를 통해 배송팀 서비스에 연계받는 코드 내용이다. 

- Order.java

```
    @PostUpdate
    public void onPostUpdate() {
    	
    	OrderCanceled orderCanceled = new OrderCanceled();
        BeanUtils.copyProperties(this, orderCanceled);
        orderCanceled.publishAfterCommit();
    }
    
```
- 결제팀에서는 주문/주문취소 접수 이벤트에 대해 이를 수신하여 자신의 정책을 처리하도록 PolicyHandler를 구현한다. 

```
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
```


# SAGA 패턴

상품배송팀의 기능을 수행할 수 없더라도 결제는 항상 받을 수 있게끔 설계하였다. 

payment 서비스가  고객으로 결제(order and pay) 요청을 받고
[payment 서비스]
payment aggegate의 값들을 추가한 이후 결제승인됨(PaymentApproved) 이벤트를 발행한다. - 첫번째 

- Payment.java

```
    @PostPersist
    public void onPostPersist(){
    	/**/
        Logger logger = LoggerFactory.getLogger(this.getClass());

    	
        PaymentApproved paymentApproved = new PaymentApproved();
        BeanUtils.copyProperties(this, paymentApproved);
        paymentApproved.publishAfterCommit();
        System.out.println("\n\n##### paymentService : onPostPersist()" + "\n\n");
        System.out.println("\n\n##### paymentApproved : "+paymentApproved.toJson() + "\n\n");
        System.out.println("\n\n##### payStatus : "+this.payStatus + "\n\n");
        logger.debug("PaymentService");
    }
```

서비스의 트랜젝션 완료

[product delivery 서비스]
결제승인됨(PaymentApproved) 이벤트가 발행되면 상품배송 서비스에서 해당 이벤트를 확인한다.
재고배송(stockdelivery) 정보를 추가 한다. - 두번째 서비스의 트렌젝션 완료

- PolicyHandler.java

```
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
```

# CQRS
- CQRS: Materialized View 를 구현하여, 타 마이크로서비스의 데이터 원본에 접근없이(Composite 서비스나 조인SQL 등 없이) 도 내 서비스의 화면 구성과 잦은 조회가 가능한가?

결제상태가 바뀔 때마다 고객이 현재 상태를 확인할 수 있어야 한다는 요구사항에 따라 주문 서비스 내에 OrderStatus View를 모델링하였다.
해당 뷰에 payStatus 정보를 받아 보여주도록 구현하였다.

OrderStatus.java 

```
@Entity
@Table(name="OrderStatus_table")
public class OrderStatus {

        @Id
        @GeneratedValue(strategy=GenerationType.AUTO)
        private Long id;
        private String username;
        private String userId;
        private Long orderId;
        private String orderStatus;
        private String productId;
        private String productName;
        private Long productPrice;
        private int qty; 
        private String couponId;
        private String couponKind;
        private String couponUseYn;
        private String payStatus;
.... 생략 
```

결제 완료/취소시 받아보는 View Handler 구현
- OrderStatusViewHandler.java
  
```
    @StreamListener(KafkaProcessor.INPUT)
    public void whenPaymentApproved_then_CREATE_1 (@Payload PaymentApproved paymentApproved) {
        try {

            if (!paymentApproved.validate()) return;

            // view 객체 생성
            OrderStatus orderStatus = new OrderStatus();
            orderStatus.setUsername(paymentApproved.getUsername());
            orderStatus.setUserId(paymentApproved.getUserId());
            orderStatus.setOrderId(paymentApproved.getOrderId());
            orderStatus.setOrderStatus("Payment Approved");
            orderStatus.setProductId(paymentApproved.getProductId());
            orderStatus.setProductName(paymentApproved.getProductName());
            orderStatus.setProductPrice(paymentApproved.getProductPrice());
            orderStatus.setQty(paymentApproved.getQty());
            orderStatus.setPayStatus(paymentApproved.getPayStatus());
            
            orderStatusRepository.save(orderStatus);
            
            System.out.println("\n\n##### OrderStatus : whenPaymentApproved_then_CREATE_1" + "\n\n");
            
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    
    @StreamListener(KafkaProcessor.INPUT)
    public void whenPaymentCanceled_then_UPDATE_1 (@Payload PaymentCanceled paymentCanceled) {
    	try {

            if (!paymentCanceled.validate()) return;

            List<OrderStatus> orderStatusList = orderStatusRepository.findByOrderId(paymentCanceled.getOrderId());
            
            for(OrderStatus orderStatus: orderStatusList) {
            	orderStatus.setOrderStatus("Payment Canceled");
            	orderStatus.setPayStatus(paymentCanceled.getPayStatus());
            	orderStatusRepository.save(orderStatus);
            	
            	System.out.println("\n\n##### OrderStatus : whenPaymentCanceled_then_UPDATE_1" + "\n\n");
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
```

주문에 대한 결제완료(PayStatus) 시 orderId를 키값으로 OrderStatus 데이터도 생성되며

"결제완료, 주문완료, 배송시작, 주문취소, 결제취소"의 이벤트에 따라 주문상태가 업데이트되도록 모델링하였다.



- CQRS 테스트 

![CQRS](https://user-images.githubusercontent.com/62110109/135557782-73ff455e-e109-4435-844a-39d13fdde4b7.png)




# 폴리글랏 퍼시스턴스
- payment 서비스 pom.xml
```
		<dependency> 
			<groupId>mysql</groupId> 
			<artifactId>mysql-connector-java</artifactId> 
		</dependency>
```

- payment 서비스 application.yml
```

spring:
  profiles: docker

  datasource:
    url: jdbc:mysql://street12db.cf4uv8oilikl.ap-northeast-2.rds.amazonaws.com:3306/street12db
    username: admin
    password: 12street
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    database: mysql
    properties:
      hibernate:
        hbm2ddl:
          auto: create
        show_sql: true
        format_sql: true
        ddl-auto: create
```

- db 조회 결과 화면
![mysql](https://user-images.githubusercontent.com/62110109/135553846-3c109201-52aa-4371-88c5-de85e2503fdb.png)



- 각 마이크로 서비스들이 각자의 저장소 구조를 자율적으로 채택하고 각자의 저장소 유형 (RDB, NoSQL, File System 등)을 선택하여 구현하였는가?

H2 DB의 경우 휘발성 데이터의 단점이 있는데, productdelivery 서비스의 경우 타 서비스들의 비해 중요하다고 생각하였다.
payment 서비스는 주문과 쿠폰발행/취소를 중간에서 모두 파악하여 처리해야 되기 때문에 백업,복원기능과 안정성이 장점이 있는 mysql을 선택하여 구현하였다.


# API 게이트웨이
- API GW를 통하여 마이크로 서비스들의 진입점을 통일할 수 있는가?
- payment 서비스에 대한 서비스 진입점을 추가 하였다

- application.yml
```
spring:
  profiles: docker
  cloud:
    gateway:
      routes:
        - id: productdelivery
          uri: http://productdelivery:8080
          predicates:
            - Path=/stockDeliveries/** 
        - id: order
          uri: http://order:8080
          predicates:
            - Path=/orders/**
        - id: orderstatus
          uri: http://orderstatus:8080
          predicates:
            - Path=/orderStatus/**
        - id: marketing
          uri: http://marketing:8080
          predicates:
            - Path=/promotes/**
        - id: payment
          uri: http://payment:8080
          predicates:
            - Path=/payments/**   
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "*"
            allowedMethods:
              - "*"
            allowedHeaders:
              - "*"
            allowCredentials: true

server:
  port: 8080
```

Gateway의 application.yml이며, 마이크로서비스들의 진입점을 세팅하여 URI Path에 따라서 각 마이크로서비스로 라우팅되도록 설정되었다.

# 운영
--
# Deploy/Pipeline

- (CI/CD 설정) BuildSpec.yml 사용 각 MSA 구현물은 git의 source repository 에 구성되었고, AWS의 CodeBuild를 활용하여 무정지 CI/CD를 설정하였다.
- 신규 추가된 Payment 서비스

- Repository 화면 캡쳐 

![CICD](https://user-images.githubusercontent.com/62110109/135554336-115a5b14-fa7a-4a08-8791-cb0324c746c6.png)

- CodeBuild 설정

![CODEBUILD1](https://user-images.githubusercontent.com/62110109/135554541-adb8714a-6ffd-4607-9157-6d5dae6ccd4f.png)


- 빌드 완료

![codebuild_완료](https://user-images.githubusercontent.com/62110109/135554628-df7244db-ac51-4bd4-b515-6d8e31659579.png)


- buildspec.yml

```

version: 0.2

env:
  variables:
    CODEBUILD_RESOLVED_SOURCE_VERSION: "latest"

phases:
  install:
    runtime-versions:
      java: corretto11
      docker: 20
  pre_build:
    commands:
      - echo Logging in to Amazon ECR....
      - echo $IMAGE_REPO_NAME
      - echo $AWS_ACCOUNT_ID
      - echo $AWS_DEFAULT_REGION
      - echo $CODEBUILD_RESOLVED_SOURCE_VERSION
      - echo start command
      - $(aws ecr get-login --no-include-email --region ap-southeast-2)
  build:
    commands:
      - echo Build started on `date`
      - echo Building the Docker image...
      - mvn package -Dmaven.test.skip=true
      - docker build -t $AWS_ACCOUNT_ID.dkr.ecr.ap-southeast-2.amazonaws.com/user20-payment:$CODEBUILD_RESOLVED_SOURCE_VERSION  .
  post_build:
    commands:
      - echo Build completed on `date`
      - echo Pushing the Docker image...
      - docker push $AWS_ACCOUNT_ID.dkr.ecr.ap-southeast-2.amazonaws.com/user20-payment:$CODEBUILD_RESOLVED_SOURCE_VERSION

cache:
  paths:
    - '/root/.m2/**/*'
```

# 동기식 호출 / Circuit Breaker / 장애격리
결제 요청이 과도할 경우 서킷 브레이크를 통해 장애 격리를 하려고 한다.

- 부하테스터 siege툴을 통한 Circuit Breaker 동작 확인 : 
- 동시사용자 50명
- 30초간 실시
- payment 서비스의 req/res 호출 후 저장전 sleep 을 진행한다.

```
siege -c50 -t30S -r10 -v --content-type "application/json" 'http://localhost:8081/orders POST { "orderStatus": "test", "userName": "test", "qty": 10, "deliveryStatus": "delivery Started"}'
```

- application.yml

![ciruit1](https://user-images.githubusercontent.com/62110109/135555058-155f0b95-e17c-46f0-8194-38abbcb1a67a.png)

- Payment.java

![circuit2](https://user-images.githubusercontent.com/62110109/135555111-bff2f851-5f13-4b27-8415-cd27db5d9a94.png)

- 실행결과

![circuit3](https://user-images.githubusercontent.com/62110109/135555147-2f150617-2d37-4d73-b157-1d61e21eb99a.png)



# Autoscale(HPA) : payment에 적용 테스트
앞서 CB 는 시스템을 안정되게 운영할 수 있게 해줬지만 사용자의 요청을 100% 받아들여주지 못했기 때문에 이에 대한 보완책으로 자동화된 확장 기능을 적용하고자 한다.

deployment.yml, Payment.java 수정

![hpa1](https://user-images.githubusercontent.com/62110109/135563742-3801fa64-e128-4dad-9824-5a126d13caff.png)

![hpa2](https://user-images.githubusercontent.com/62110109/135563761-49022655-2664-4d72-bd6d-a76712adcfbf.png)


Autoscale 설정 명령어 실행
```
kubectl autoscale deploy payment --min=1 --max=10 --cpu-percent=15
```


siege 명령어 실행
```
siege -c50 -t30S -r10 -v --content-type "application/json" 'http://payment:80/payments POST { "orderStatus": "test", "userName": "test", "qty": 10, "deliveryStatus": "delivery Started"}'
```

Autoscale 적용되어, payment 서비스의 pod가 10개로 증가

![hpa4](https://user-images.githubusercontent.com/62110109/135564121-e06f62f1-9f14-41f4-9f4c-93490ede0b26.png)

siege 결과 Availablity 100%

![hpa5](https://user-images.githubusercontent.com/62110109/135564149-b989d7ff-f857-476f-ad43-cf2e26f9bec4.png)



# Zero-downtime deploy (Readiness Probe) 
(무정지 배포) 

- deployment.yml : readinessProbe 옵션 존재
- deployment_v2.yml : readinessProbe 옵션 미존재

- deployment.yml 설정된 readinessProbe

![HPA8](https://user-images.githubusercontent.com/62110109/135555458-bfd69ec8-703a-492d-b7ed-0aab65643209.png)

- CASE : readinessProbe 옵션 없이 배포
![readness1](https://user-images.githubusercontent.com/62110109/135560493-6371be54-ce54-4e6f-9ec9-863e86e5cbd9.png)
![readness2](https://user-images.githubusercontent.com/62110109/135555805-f3ac950f-d0bb-4ccd-90fa-39bcaae44bba.png)

- CASE : readinessProbe 옵션 배포
![readness3](https://user-images.githubusercontent.com/62110109/135555872-3585ae2d-a60f-42ac-885d-bfc14259f7ce.png)

서비스의 끊김없이 무정지 배포가 실행됨을 확인하였다. 


# Self-healing (Liveness Probe)

- 결재 서비스의 port 및 정보를 잘못된 값으로 변경하여 yml 적용

![liveness1](https://user-images.githubusercontent.com/62110109/135556266-0abcb6aa-60a1-4d53-b2cb-b47000b38f34.png)

- 결재 서비스 yml을 배포

![liveness2](https://user-images.githubusercontent.com/62110109/135556292-4b187552-e097-4113-a8c8-bc64f87cf209.png)

- 잘못된 포트여서 kubelet이 자동으로 POD 재시작하였다. 

![LIVENESS3](https://user-images.githubusercontent.com/62110109/135556344-b58722ae-d2c6-40a3-90ea-ca35753ead84.png)



# 운영유연성
- 결재 서비스에 Configmap 정보 활용,
- 운영/테스트 서버 정보의 정보를 Configmap으로 받아와 사용 가능하도록 설정함

- Configmap 생성
![config1](https://user-images.githubusercontent.com/62110109/135557487-ed35f22e-a68c-4425-8ba4-7e7c0d09e091.png)


- Deployment.yml env 설정

![config2](https://user-images.githubusercontent.com/62110109/135557621-830c9fab-7380-4f74-9b33-0f65b621756f.png)


- application.yml 설정

![config2](https://user-images.githubusercontent.com/62110109/135557719-60a39fce-b487-4b74-9578-55367814e571.png)
