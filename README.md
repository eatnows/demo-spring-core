[참고 강의](https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-%ED%95%B5%EC%8B%AC-%EC%9B%90%EB%A6%AC-%EA%B8%B0%EB%B3%B8%ED%8E%B8)


### FilterType 옵션
- **ANNOTATION**: 기본값, 애너테이션을 인식해서 동작한다.
- **ASSIGNABLE_TYPE**: 지정한 타입과 자식 타입을 인식해서 동작한다.
- **ASPECTJ**: AspectJ 패턴 사용
- **REGEX**: 정규 표현식
- **CUSTOM**: `TypeFilter`라는 인터페이스를 구현해서 처리

#### 자동 빈 등록 vs 수동 빈 등록
이 경우 수동 빈 등록이 우선권을 가진다. (수동 빈이 자동 빈을 오버라이딩 한다,)
```text
Overriding bean definition for bean 'memoryMemberRepository' with a different definition: replacing [Generic bean: class [hello.core.member.MemoryMemberRepository];
```
최근 스프링 부트에서는 오류가 나도록 기본값을 바꾸었다.
```text
Consider renaming one of the beans or enabling overriding by setting spring.main.allow-bean-definition-overriding=true
```


### 의존관계 주입 방법

#### 생성자 주입
생성자를 통해서 의존 관계를 주입 받는 방법이다.
 - 생성자 호출 시점에 딱 1번만 호출되는 것이 보장된다.
 - 불변, 필수 의존관계에 사용된다.


#### 수정자 주입 (setter)
setter라고 불리는 필드의 값을 변경하는 수정자 메서드를 통해서 의존관계를 주입하는 방법이다.
- 선택, 변경 가능성이 있는 의존관계에 사용
- 자바빈 프로퍼티 규약의 수정자 메서드 방식을 사용하는 방법
```java
// MemberRepository가 빈으로 등록되지 않아 선택적으로 사용하려면 required = false를 줘야한다.
@Autowired(required = false)
public void setMemberRepository(MemberRepository memberRepository) {
    this.memberRepository = memberRepository;
}

@Autowired
public void setDiscountPolicy(DiscountPolicy discountPolicy) {
    this.discountPolicy = discountPolicy;
}
```


#### 필드 주입
필드에 바로 주입하는 방식이다.
- 외부에서 변경이 불가능해서 테스트 하기 힘들다는 단점이 있다.
- DI 프레임워크가 없으면 아무것도 할 수 없다.
- 실제 코드와 관계없는 테스트 코드나 스프링 설정을 목적으로 하는 `@Configuration` 같은 곳에서만 특별한 용도로 사용할 것


#### 일반 메서드 주입
일반 메서드를 통해서 주입 받을 수 있다.
- 한번에 여러 필드를 주입 받을 수 있다.
- 일반적으로 잘 사용하지 않는다.
```java
public class OrderServiceImpl implements OrderService {
    
    private MemberRepository memberRepository;
    private DiscountPolicy discountPolicy;

    @Autowired
    public void init(MemberRepository memberRepository, DiscountPolicy discountPolicy) {
        this.memberRepository = memberRepository;
        this.discountPolicy = discountPolicy;
    }
}
```


### 생성자 주입을 권장하는 이유
#### 불변
- 대부분의 의존관계 주입은 한번 일어나면 애플리케이션이 종료시점까지 의존관계를 변경할 일이 없고, 보통 변경하면 안된다.
- 수정자 주입을 사용하면 setter 메서드를 public으로 열어야 한다. (누군가 실수로 변경할 여지가 있음) 

#### 누락
생성자 주입을 하여 객체를 생성할 경우 의존관계를 주입하지 않으면 객체 생성 단계에서 컴파일 오류가 발생하여 의존 관계를 누락시킬 일이 없다.
`final` 키워드를 사용하면 생성자에서 누락됐을 경우에 컴파일 에러가 발생한다.

#### 정리 
- 생성자 주입 방식을 선택하면 프레임워크에 의존하지 않고, 순수한 자바 언어의 특징을 잘 살리는 방법이다.
- 생성자와 수정자 주입을 동시에 사용할 수 있기 때문에 기본으로 생성자 주입을 사용하고 필수 값이 아닌 경우에는 수정자 주입 방식을 옵션으로 부여하면 된다.


### Bean 중복등록 해결
조회 대상 빈이 2개 이상일 때 해결 방법
- @Autowired 필드명 매칭
- @Qualifier -> @Qualifier 끼리 매칭 -> 빈 이름 매칭
- @Primary 사용


#### @Autowired 필드명 매칭
`@Autowired`는 기본적으론 타입 매칭을 시도하는데, 여러 빈이 존재하면 필드이름 이나 파라미터 이름으로 빈 이름을 매칭한다.
```java
@Autowired
private DiscountPolicy rateDiscountPoilicy;
```


#### @Quailifier 사용
`Quilifier`는 추가 구분자를 붙여주는 방법이다. 주입시 추가적인 방법을 제공하는 것이지 빈 이름을 변경하는 것은 아니다.
1. 빈 등록시 @Qualifier를 붙여준다.
2. 주입시에 @Qualifier를 붙여주고 등록한 이름으로 적어준다.

```java
@Component
@Qualifier("mainDiscountPolicy")
public class RateDiscountPolicy implements DiscountPolicy{

    private int discountPercent = 10;

    @Override
    public int discount(Member member, int price) {
        if (member.getGrade() == Grade.VIP) {
            return price * discountPercent / 100;
        }
        return 0;
    }
}


public class OrderServiceImpl implements OrderService {

    private final MemberRepository memberRepository;
    private final DiscountPolicy discountPolicy;

    public OrderServiceImpl(MemberRepository memberRepository, @Qualifier("mainDiscountPolicy") DiscountPolicy discountPolicy) {
        this.memberRepository = memberRepository;
        this.discountPolicy = discountPolicy;
    }
}
```

만약 `@Qualifier`로 설정한 구분자를 찾지 못한다면, 해당 구분자라는 이름의 스프링 빈을 추가로 찾는다. (그래도 없을시 `NoSuchBeanDefinitionException` 발생)
<br> 하지만 개발시 혼란을 야기하기 때문에 `@Qualifier`를 찾는 용도로만 사용하는 것이 명확하고 좋다.


#### @Primary 사용
`@Primary`는 우선순위를 정하는 방법, 여러 빈이 매칭되면 `@Primary`가 우선권을 가진다.
```java
@Component
@Primary 
public class RateDiscountPolicy implements DiscountPolicy{

    private int discountPercent = 10;

    @Override
    public int discount(Member member, int price) {
        if (member.getGrade() == Grade.VIP) {
            return price * discountPercent / 100;
        }
        return 0;
    }
}

@Component
public class OrderServiceImpl implements OrderService {

    private final MemberRepository memberRepository;
    private final DiscountPolicy discountPolicy;

    public OrderServiceImpl(MemberRepository memberRepository, DiscountPolicy discountPolicy) {
        this.memberRepository = memberRepository;
        this.discountPolicy = discountPolicy;
    }
}
```

#### @Qualifier와 @Primary의 우선순위
*@Qualifier와 @Primary를 같이 사용하면 어떻게 될까?* <br>
스프링에서는 자동보다는 수덩이,
넓은 범위의 선택권 보다는 좁은 범위의 선택권이 우선 순위가 높다. 따라서 `@Qualifier`의 우선순위가 높다.



### 스프링 빈 LifeCycle
스프링은 의존관계 주입이 완료되면 스프링 빈에게 콜백 메서드를 통해서 초기화 시점을 알려주는 다양한 기능을 제공한다.
또한 스프링은 스프링 컨테이너가 종료되기 직전에 소멸 콜백을 준다. 따라서 안전하게 종료작업을 진행할 수 있다.

스프링 빈의 이벤트 라이프사이클 (싱글톤에 대한 예)
스프링 컨테이너 생성 -> 스프링 빈 생성 -> 의존관계 주입 -> 초기화 콜백 -> 사용 -> 소멸전 콜백 -> 스프링 종료

- 초기화 콜백 : 빈이 생성되고 빈의 의존관계 주입이 완료된 후 호출
- 소멸전 콜백 : 빈이 소멸되기 직전에 호출

#### 객체의 생성과 초기화를 분리하자
객체의 생성은 객체의 생성에만 집중해야한다. 객체의 초기화 작업은 객체가 동작하는 작업이기 때문에 작업을 분리하여,
객체의 생성은 객체가 메모리에 할당하는것 까지로 최대한 필요한 데이터만 세팅하는것 까지하고 실제 동작하는 행위는 별도의 초기화 메서드로 분리하는게 대부분의 경우에 좋다.
<br> 따라서 생성자안에서 무거운 초기화 작업을 함께 하는 것 보다는 객체를 생성하는 부분과 초기화 하는 부분을 명확하게 나누는 것이 유지보수 관점에서 좋다.


#### 스프링이 지원하는 3가지 빈 생명주기 콜백
- 인터페이스(InitializingBean, DisposableBean)
- 설정 정보에 초기화 메서드, 종료 메서드 지정
- @PostConstruct, @PreDestory 애너테이션 지원

#### 인터페이스 (InitializingBean, DisposableBean)
해당 인터페이스를 implements하여 메서드를 구현한다.
```java
public interface InitializingBean {
    // 의존관계 주입이 끝나면 호출된다.
	void afterPropertiesSet() throws Exception;
}
```
```java
public interface DisposableBean {
    
	void destroy() throws Exception;
}
```
인터페이스를 사용하는 방법의 단점
- 스프링 전용 인터페이스이기 때문에 해당 코드가 스프링 전용 인터페이스에 의존한다.
- 초기화, 소멸 메서드의 이름을 변경할 수 없다.
- 외부 라이브러리에 적용할 수 없다. (수정이 안되는 외부 라이브러리)

스프링 초창기에 나온 방법이여서 지금은 거의 사용하지 않고 다른 방법을 많이 사용한다.

#### 빈 등록 초기화, 소멸 메서드
빈으로 등록할 클래스의 초기화되면 실행할 메서드와 빈이 소멸됐을때 발생할 메서드를 작성후 
빈으로 등록하는 설정 정보에 `initMethod`와 `destroyMethod`옵션 값을 적어준다.
```java
@Configuration
static class LifeCycleConfig {
    // NetworkClient 클래스의 init 메서드와 close 메서드로 지정 
    @Bean(initMethod = "init", destroyMethod = "close")
    public NetworkClient networkClient() {
        NetworkClient networkClient = new NetworkClient();
        networkClient.setUrl("http://hello-spring.dev");
        return networkClient;
    }
}
```

**특징**
- 오버라이딩이 아니기 때문에 메서드 이름을 자유롭게 설정가능
- 빈으로 만들 클래스 자체가 스프링에 의존하지 않는다.
- 코드가 아니라 설정 정보를 사용하기 때문에 코드를 고칠 수 없는 외부 라이브러리에도 적용할 수 있다.

**소멸 메서드 추론**
`@Bean`의 `destroyMethod` 속성의 기본값은 `(inferred)`(추론)으로 등록되어있다. <br>
보통 라이브러리 대부분이 `close`, `shutdown`이라는 이름의 종료 메서드를 사용한다.
이 추론기능은 `close`, `shutdown`라는 이름의 메서드를 자동으로 호출해준다. (종료 메서드를 추론해서 호출하는 것)
<br> 따라서 직접 빈으로 등록하면 종료 메서드는 따로 적어주지 않아도 동작한다. 추론 기능을 사용하지 않으려면 `destroyMethod="""`처럼 빈 공백으로 지정하면 된다.

```java
String destroyMethod() default AbstractBeanDefinition.INFER_METHOD;


public static final String INFER_METHOD = "(inferred)";
```