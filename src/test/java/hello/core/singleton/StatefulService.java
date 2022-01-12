package hello.core.singleton;

public class StatefulService {

    private int price; // 상태를 유지하는 필드 10000 -> 20000, 지역변수나 파라미터, ThreadLocal 등으로 사용해야한다.

    public void order(String name, int price) {
        System.out.println("name = " + name + " price = " + price);
        this.price = price;
    }

    public int getPrice() {
        return price;
    }
}
