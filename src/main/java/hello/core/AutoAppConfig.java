package hello.core;

import hello.core.member.MemberRepository;
import hello.core.member.MemoryMemberRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
// 컴포넌트 수캔은 @Component가 붙은 클래스를 스캔해서 스프링 빈으로 등록한다.
// 예제에 @Configuration이 있기때문에 일단 제외시킴 (@Configuration에는 @Component가 있기때문에 자동으로 스캔이 되서 중복문제가 발생)
@ComponentScan(
        // basePackages, basePackageClasses를 지정하지 않으면 현재 이 클래스가 속한 패키지와 그 하위 패키지를 스캔하는것이 디폴트 값이다.
        basePackages = "hello.core",
        basePackageClasses = AutoAppConfig.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Configuration.class)
)
public class AutoAppConfig {

//    @Bean(name = "memoryMemberRepository")
//    MemberRepository memberRepository() {
//        return new MemoryMemberRepository();
//    }
}
