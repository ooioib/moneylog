package org.codenova.moneylog;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class LocalDateTests {

    @Test
    public void test() {

        LocalDate today = LocalDate.now();

        DayOfWeek dow = today.getDayOfWeek();   // 요일
        System.out.println(dow);

        int value = today.getDayOfWeek().getValue();   // 요일마다 값이 부여 돼 있음 (글 :5)

        System.out.println(value);
        System.out.println(today.plusDays(1).getDayOfWeek().getValue());
        System.out.println(today.plusDays(2).getDayOfWeek().getValue());
        System.out.println(today.plusDays(5).getDayOfWeek().getValue());

        LocalDate d = LocalDate.of(2025, 3, 18);


        // 우리에게 특정 LocalDate가 있다고 가정. 그 날짜 포함된 주의 시작일과 끝 일을 구하려면?
        // 한 주의 시작을 월~일

        // getDayOfWeek().getValue() ...if == 3 ==> +3(토) +4(일)


        // 한 주 시작을 월 ~ 일
        LocalDate firstDayOfWeek = d.minusDays(d.getDayOfWeek().getValue() - 1);
        LocalDate lastDayOfWeek = d.plusDays(7 - d.getDayOfWeek().getValue());

        System.out.println(firstDayOfWeek);
        System.out.println(lastDayOfWeek);

//        // 한 주 시작을 일 ~ 월
//        LocalDate firstDayOfWeek = d.minusDays(d.getDayOfWeek().getValue());
//        LocalDate lastDayOfWeek = d.plusDays(6 - d.getDayOfWeek().getValue());
//
//        System.out.println(firstDayOfWeek);
//        System.out.println(lastDayOfWeek);


    }

    @Test
    public void test2() {

        LocalDate today = LocalDate.now();
        System.out.println(today.getDayOfMonth());   // int 날짜

        // 이 상태에서 plus, minus 를 잘 시키면 이번달의 시작일과 마지막 일을 구할 수 있음

        today.plusDays(1);
        today.plusWeeks(1);
        today.plusMonths(1);

        today.minusDays(1);
        today.minusWeeks(1);
        today.minusMonths(1);
    }

    /*
        1. 특정 날이 있다고 가정하고 그날이 포함된 달의 시작일과 마지막 일을 구하려면?
        2. 이게 잘 구해졌다면, /expense/history 로 사용자가 접근시 모든 지출 기록이 다 나오는데
            이걸 이번 달의 데이터만 조회해서 출력되게 변경
     */




}
