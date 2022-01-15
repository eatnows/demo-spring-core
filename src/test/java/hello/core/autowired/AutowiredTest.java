package hello.core.autowired;

import hello.core.member.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.lang.Nullable;

import java.util.Optional;

public class AutowiredTest {

    @Test
    void AutowiredOption() {
        ApplicationContext ac = new AnnotationConfigApplicationContext(TestBean.class);
    }

    static class TestBean {

        // Member는 스프링 빈이 아니다.

        @Autowired(required = false)
        public void setNoBean1(Member member) {
            // true일 경우 에러 발생. 의존관계가 없으면 호출이 아예 안됨
            System.out.println("noBean1 = " + member);
        }

        @Autowired // @Nullable 때문에 호출은 되지만 null로 나옴
        public void setNoBean2(@Nullable Member member) {
            // null
            System.out.println("noBean2 = " + member);
        }

        @Autowired
        public void setNoBean3(Optional<Member> member) {
            // Optional.empty
            System.out.println("noBean3 = " + member);
        }
    }
}
