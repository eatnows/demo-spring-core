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



#### 애너테이션 @PostConstruct, @PreDestroy
초기화, 소멸 메서드로 사용할 메서드위에 해당 애너테이션을 달아주면 된다.
```java
@PostConstruct
public void init() {
    System.out.println("NetworkClient.afterPropertiesSet");
    connect();
    call("초기화 연결 메시지");
}

@PreDestroy
public void close() {
    System.out.println("NetworkClient.destroy");
    disconnect();
}
```
- 최신 스프링에서 가장 권장하는 방법
- 패키지가 `javax.annotation.PostConstruct`이다. 스프링에 종속적인 기술이 아니라 JSR-250이라는 자바 표준이여서 스프링이 아닌 다른 컨테이너에서도 동작한다.
- 컴포넌트 스캔과도 잘 어울린다.
- 유일한 단점은 외부 라이브러리에 적용하지 못한다는 것이다. 이때는 `@Bean`의 기능을 활용해야한다.


### 빈 스코프
스코프는 번역 그대로 빈이 존재할 수 있는 범위를 뜻하며 스프링 빈은 기본적으로 싱글톤 스코프로 생성된다. <br> 
싱글톤 스코프는 스프링 컨테이너의 시작과 함께 생성되어 스프잉 컨테이너가 종료될때 까지 유지된다.

- 싱글톤: 기본 스코프, 스프링 컨테이너의 시작과 종료까지 유지되는 가장 넓은 범위의 스코프
- 프로토타입: 스프링 컨테이너는 프로토타입 빈의 생성과 의존관계 주입까지만 관여하고 더는 관리하지 않는 매울 짧은 범위의 스코프
- 웹 관련 스코프
  - request: 웹 요청이 들어오고 나갈때 까지 유지되는 스코프
  - session: 웹 세션이 생성되고 종료될 때 까지 유지되는 스코프
  - application: 웹의 서블릿 컨텍스트와 같은 범위로 유지되는 스코프

컴포넌트 스캔으로 등록과 설정에서 빈으로 수동 등록할 때 모두 `@Scope`를 사용한다
```java
@Scope("prototype")
@Component
public class Hello {}

@Scope("prototype")
@Bean
PrototypeBean Hello() {
    return new HelloBean();
} 
```

프로토타입은 스프링 컨테이너가 빈을 생성하고 의존관계 주입, 초기화까지만 처리를한다. <br> 
이후 클라이언트에 빈을 반환하고 더이상 생성된 프로토타입 빈을 관리하지 않는다. (매번 요청이 올때마다 새로 생성해서 반환)
프로토타입 빈을 관리할 책임은 반환받은 클라이언트에 있다. 그래서 `@PreDestroy`와 같은 종료메서드가 호출되지 않는다. (종료 메서드를 호출해야한다면 클라이언트에서 해야함)


#### 싱글톤 빈과 프로토타입 빈을 함께 사용시 문제점
스프링은 일반적으로 싱글톤 빈을 사용하므로, 싱글톤 빈이 프로토타입 빈을 사용하게 된다. <br>
그런데 싱글톤 빈은 생성 시점에만 의존관계 주입을 받기 떄문에, 프로토타입 빈이 새로 생성되기는 하지만 싱글톤 빈과 함께 계속 유지되는 것이 문제이다.

사용자는 프로토타입 빈을 주입 시점에만 새로 생성하는게 아니라, 사용할 때 마다 새로 생성해 사용하는 것을 원할 것이다.

이 문제를 해결하는 쉬운 방법은 `ApplicationContext`를 주입받아 직접 빈을 조회하는 것이다.
```java
@Autowired
private Applicationcontext ac;

public int logic() {
    PrototypeBean prototypeBean = ac.getBean(PrototypeBean.class);
    prototypeBean.addCount();
    int count = prototypeBean.getCount*();
    return count;
}
```
위 코드를 실행해보면 `ac.getBean()`을 통해 항상 새로운 프로토타입 빈이 생성되는 것을 확인할 수 있다. <br>
의존관계를 외부에서 주입(DI) 받는게 아니라 이렇게 직접 필요한 의존관계를 찾는 것을 Dependency Lookup (DL) 의존관계 조회(탐색) 이라고 한다.
하지만 이렇게 `ApplicationContext`를 주입받게되면 스프링 컨테이너에 종속적인 코드가 되고, 단위 테스트도 어려워진다. 
그래서 스프링에서는 지정한 프로토타입 빈을 찾아주는 DL의 기능을 제공해준다.

##### ObjectFactory, ObjectProvider
지정한 빈을 컨테이너에서 (ApplicationContext를 통해 직접 조회하지 않고)대신 찾아주는 DL서비스를 제공하는 것이 `ObjectProvider`이다. <br>
예전에는 `ObjectFactory`가 있었는데 `ObjectProvider`는 `ObjectFactory` 인터페이스를 상속받아 몇가지 기능을 더 추가한 것이다.

```java
@Autowired
private ObjectProvider<PrototypeBean> prototypeBeanProvider;

public int logic() {
    PrototypeBean prototypeBean = prototypeBeanProvider.getObject();

    prototypeBean.addCount();
    int count = prototypeBean.getCount();
    return count;
}
```
`ObjectProvider`의 `getObject()`를 호출하면 내부에서는 스프링 컨테이너를 통해 해당 빈을 찾아 반환한다. (Dependency Lookup)
<br> 스프링이 제공하는 기능이지만 기능이 단순하여 단위 테스트를 만들거나 mock 코드를 만들기는 훨씬 쉽다. `ObjectProvider`는 DL 정도의 기능만 제공한다. 프로토타입 빈과는 관련이 없다.

#### JSR-330 Provider
마지막으로 `javax.inject.Provider`라는 JSR-330 자바 표준을 사용하는 방법이다. 
하지만 이 방법을 사용하려면 `javax.inject:javax.inject:1` 라이브러리를 `gradle`에 추가해야 한다.
```groovy
implementation 'javax.inject:javax.inject:1'
```

```java
package javax.inject;

public interface Provider<T> {
    T get();
}
```

`provider`의 `get()`을 호출하면 내부에서는 스프링 컨테이너를 통해 해당 빈을 찾아서 반환한다.(`DL`)
<br> 자바 표준이기 때문에 스프링이 아닌 다른 컨테이너에서도 사용할 수 있다.
기능이 단순하여 단위테스트를 만들거나 mock 코드를 만들기가 쉽다. `Provider`는 DL 정도의 기능만 제공한다.

`ObjectProvider`, `JSR330 Provder`등은 프로토타입 뿐만 아니라 `DL`이 필요한 경우는 언제든지 사용이 가능하다.
<br> 참고로 스프링이 제공하는 메서드에 `@Lookup` 애너테이션을 사용하는 방법도 있다.


### 웹 스코프
웹 스코프는 웹 환경에서만 동작한다. 웹 스코프는 프로토타입과는 다르게 스프링이 해당 스코프의 종료시점까지 관리를 해서 종료 메서드가 호출된다.

#### 웹 스코프 종류
- request: HTTP 요청 하나가 들어오고 나갈때 까지 유지되는 스코프, 각각의 HTTP 요청마다 별도의 빈 인스턴스가 생성되고 관리된다.
- session: HTTP Session과 동일한 생명주기를 가지는 스코프
- application: 서블릿 컨텍스트 (`ServletContext`)와 동일한 생명주기를 가지는 스코프
- websocket: 웹 소켓과 동일한 생명주기를 가지는 스코프

웹 스코프는 웹 환경에서만 동작하므로 라이브러리를 추가해야한다.
```groovy
implementation 'org.springframework.boot:spring-boot-starter-web'
```

스프링 부트는 웹 라이브러리가 없으면 우리가 지금까지 학습한 `AnnotationConfigApplicationContext`를 기반으로 애플리케이션을 구동한다.
웹 라이브러리가 추가되면 웹과 관련된 추가 설정과 환경들이 필요하므로 `AnnotationConfigServletWebServerApplicationContext`를 기반으로 애플리케이션을 구동한다.

웹 스코프를 이용해서 요청온 데이터의 로그를 찍는 로직을 만든다고 할때 로그를 찍는 로거 클래스를 만들 수 있다.
```java
@Component
@Scope(value = "request")
public class MyLogger {

    private String uuid;
    private String requestURL;

    public void setRequestURL(String requestURL) {
        this.requestURL = requestURL;
    }

    public void log(String message) {
        System.out.println("[" + uuid + "]" + "[" + requestURL + "] " + message);
    }

    @PostConstruct
    public void init() {
        uuid = UUID.randomUUID().toString();
        System.out.println("[" + uuid + "] request scope bean create: " + this);
    }

    @PreDestroy
    public void close() {
        System.out.println("[" + uuid + "] request scope bean close: " + this);
    }
}
```

문제는 웹 스코프 빈을 이용하여 웹 서버를 시작하면 에러가 발생한다.
```text
Error creating bean with name 'myLogger': Scope 'request' is not active for the current thread; consider defining a scoped proxy for this bean if you intend to refer to it from a singleton; nested exception is java.lang.IllegalStateException: No thread-bound request found: Are you referring to request attributes outside of an actual web request, or processing a request outside of the originally receiving thread? If you are actually operating within a web request and still receive this message, your code is probably running outside of DispatcherServlet: In this case, use RequestContextListener or RequestContextFilter to expose the current request.
```

웹 스코프 request 빈은 해당 요청이 스프링 컨테이너까지 왔을때 빈이 생성이되는데 애플리케이션을 시작할 때
의존관계를 주입하려고 하니 웹 스코프 빈이 생성되지 않아서 발생한 문제다. 


#### 스코프와 Provider
웹 스코프를 주입받아야하는 빈에 ObjectProvider로 주입을 받는다.
```java
@Controller
@RequiredArgsConstructor
public class LogDemoController {

  private final LogDemoService logDemoService;
  private final ObjectProvider<MyLogger> myLoggerProvider;

  @RequestMapping("log-demo")
  @ResponseBody
  public String logDemo(HttpServletRequest request) {
    String requestURL = request.getRequestURL().toString();
    MyLogger myLogger = myLoggerProvider.getObject();
    myLogger.setRequestURL(requestURL);

    myLogger.log("controller test");
    logDemoService.logic("testId");
    return "OK";
  }
}
```

`ObjectProvider` 덕분에 `ObjectProvider.getObject()`를 호출하는 시점까지 request scope 빈의 생성을 하는 스프링 컨테이너요청을 지연할 수 있다.
`ObjectProvider.getObject()`를 호출하는 시점에는 HTTP 요청이 진행중이기 때문에 request scope 빈이 정상적으로 처리된다.
