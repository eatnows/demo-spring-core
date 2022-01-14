참고 강의 : [링크](https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-%ED%95%B5%EC%8B%AC-%EC%9B%90%EB%A6%AC-%EA%B8%B0%EB%B3%B8%ED%8E%B8)


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