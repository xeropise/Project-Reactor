package com.example.fluxandmono.fluxMonoTest;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Optional;

@SpringBootTest
public class sequenceTransForm {

    //@Test
    public void test_flux_map(){

        // map()은 1개의 데이터를 1대1 방식으로 변환해준다.
        Flux.just("a", "bc", "def", "wxyz")
                .map(str -> str.length()) // 문자열을 Integer 값으로 1:1 변환
                .subscribe(System.out::println);
    }

    //@Test
    public void test_flux_flatMap() {

        // flatMap()은 1개의 데이터로부터 시퀀스를 생성할 때 사용, 1대 다 방식의 변환을 처리한다.
        Flux<Integer> seq = Flux.just(1,2,3)
                .flatMap(i -> Flux.range(1, i)); // Integer를 Flux<Integer> 로 변환

        // 1 -> Flux.range(1,1)
        // 2 -> Flux.range(1,2)
        // 3 -> Flux.range(1,3)
        // fluxMap에 전달한 함수가 생성하는 각 Flux는 하나의 시퀀스처럼 연결된다.
        // Flux<Flux<Integer>> 가 아닌 Flux<Integer> 가 됨
        seq.subscribe(System.out::println);
    }

    //@Test
    public void test_flux_filter() {

        Flux.range(1, 10)
                .filter(num -> num % 2 == 0)
                .subscribe(x -> System.out.println(x + " -> "));
    }

    //@Test
    public void test_flux_defaultIfEmpty() {

        Flux<Integer> zero = Flux.empty();

        Flux<Integer> haveDefault = zero.defaultIfEmpty(0);
    }

    //@Test
    public void test_flux_startWith() {

        Flux<Integer> seq1 = Flux.just(1, 2, 3);

        Flux<Integer> seq2 = seq1.startWith(-1, 0);  // -1, 0, 1, 2, 3

        seq2.subscribe(System.out::println);
    }

    //@Test
    public void test_flux_concatWithValues() {

        Flux<Integer> seq = Flux.just(0).concatWithValues(100);

        seq.subscribe(System.out::println);
    }

    //@Test
    public void test_flux_concatWith() {


        Flux<Integer> seq1 = Flux.just(1,2,3);
        Flux<Integer> seq2 = Flux.just(4, 5, 6);
        Flux<Integer> seq3 = Flux.just(7,8,9);

        // 이전 시퀀스가 종료된 뒤에 구독 시작
        seq1.concatWith(seq2).concatWith(seq3);
    }

    @Test
    public void test_flux_mergeWith() {

        Flux<String> tick1 = Flux.interval(Duration.ofSeconds(1)).map(tick -> tick + "초틱");
        Flux<String> tick2 = Flux.interval(Duration.ofMillis(700)).map(tick -> tick + "밀리초틱");

        tick1.mergeWith(tick2).subscribe(System.out::println);
    }

    @Test
    public void test_flux_zipWith() {
        Flux<String> tick1 = Flux.interval(Duration.ofSeconds(1)).map(tick -> tick + "초틱");

        Flux<String> tick2 = Flux.interval(Duration.ofMillis(700)).map(tick -> tick + "밀리초틱");

        tick1.zipWith(tick2).subscribe(tup -> System.out.println(tup));
    }

    @Test
    public void test_flux_combineLatest() {
        Flux<String> tick1 = Flux.interval(Duration.ofSeconds(1)).map(tick -> tick + "초틱");

        Flux<String> tick2 = Flux.interval(Duration.ofMillis(700)).map(tick -> tick + "밀리초틱");

        Flux.combineLatest(tick1, tick2, (a, b) -> a + "\n" + b).subscribe(System.out::println);
    }

    @Test
    public void test_flux_take_takeLast() {
        /*

        Flux<Integer> seq1 = someSeq.take(10); // 최초 10개 데이터만 유지
        Flux<Integer> seq2 = someSeq.take(Duration.ofSeconds(10)); // 최초 10초 동안 데이터 유지
        Flux<Integer> seq3 = someSeq.takeLast(5); // 마지막 5개 데이터만 유지

         */
    }

    @Test
    public void test_flux_skip_skipUntil() {

        Flux.just(1,2,3).skip(0).skipUntil(i -> i==3).subscribe(System.out::println);
    }
}
