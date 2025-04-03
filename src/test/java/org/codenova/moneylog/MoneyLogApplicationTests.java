package org.codenova.moneylog;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

@SpringBootTest
class MoneyLogApplicationTests {

    @Test
    void contextLoads() {
    }

    /*
        LocalDateTime 크기 비교 (과거, 현재, 미래) 비교 할 때 isAfter, isBefore ==> boolean
        t1.isAfter(t2)  ==> t1이 t2 이후인가?
        t2.isBefore(t3)  ==> t2이 t3 전인가?

     */
    @Test
    void compareLocalDateTime() {
        LocalDateTime t1 = LocalDateTime.of(2025, 1, 21, 1, 30);
        LocalDateTime t2 = LocalDateTime.of(2025, 1, 20, 13, 00);
        LocalDateTime t3 = LocalDateTime.of(2025, 2, 20, 00, 00);

        System.out.println(t1.isBefore(t2));  // T
        System.out.println(t3.isBefore(t2));  // T
        System.out.println(t1.isBefore(t3));  // F

    }
}
