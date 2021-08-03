package com.example.fluxandmono.fluxMonoTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

@SpringBootTest
public class FluxTest {

    //@Test
    public void test_flux_just_consumer() {
        List<String> names = new ArrayList<>();         // log() 를 추가하면 로그 확인이 가능하다.
        Flux<String> flux = Flux.just("xeropise", "xeropise2").log();

        // onSubscribe => request => onNext => onNext => onComplete

        /*
            flux.subscribe(new Consumer<String>() {
                @Override
                public void accept(String s) {

                }
            });
         */

        // subscribe 실행 전까지 Flux 에서는 아무것도 일어나지 않는다.
        // 구독이 되었을 경우에만 Publisher => Subscriber 로 데이터를 전달 한다.
        flux.subscribe(names::add);

        Assertions.assertEquals(names, Arrays.asList("xeropise", "xeropise2"));
    }

    //@Test
    public void test_flux_range() {
        List<Integer> list = new ArrayList<>();

        Flux<Integer> flux = Flux.range(1,5).log();

        flux.subscribe(list::add);

        Assertions.assertEquals(5, list.size());
        Assertions.assertEquals(1, (int) list.get(0));
        Assertions.assertNotEquals(5, (int) list.get(0));
    }

    //@Test
    public void test_flux_fromArray() {

        List<String> names = new ArrayList<>();

        Flux<String> flux =
                Flux.fromArray(new String[]{"xeropise", "xeropise1", "xeropise2"}).log();

        flux.subscribe(names::add);

        Assertions.assertEquals(names, Arrays.asList("xeropise", "xeropise1", "xeropise2"));
    }

    //@Test
    public void test_flux_fromIterable() {

        List<String> names = new ArrayList<>();

        Flux<String> flux =
                Flux.fromIterable(Arrays.asList("xeropise", "xeropise1", "xeropise2")).log();

        flux.subscribe(names::add);

        Assertions.assertEquals(names, Arrays.asList("xeropise", "xeropise1", "xeropise2"));
    }

    //@Test
    public void test_flux_fromStream() {
        List<String> names = new ArrayList<>();

        Flux<String> flux =
                Flux.fromStream(Stream.of("xeropise", "xeropise1", "xeropise2")).log();

        flux.subscribe(names::add);

        Assertions.assertEquals(names, Arrays.asList("xeropise", "xeropise1", "xeropise2"));

    }

    //@Test
    public void test_flux_empty() {

        List<String> names = new ArrayList<>();

        Flux<String> flux = Flux.empty();
        flux.subscribe(names::add);

        Assertions.assertEquals(names.size(),0 );

    }

    //@Test
    public void test_flux_error() {
        Flux<Integer> ints = Flux.range(1, 4)
                .map(i -> {
                    if (i <= 3) return i;
                    throw new RuntimeException("Got to 4");
                });
        ints.subscribe(i -> System.out.println(i),
                error -> System.err.println("Error: " + error));
    }

    @Test
    public void test_flux_errorAndComplete() {
        Flux<Integer> ints = Flux.range(1, 4);
        ints.subscribe(i -> System.out.println(i),
                error -> System.err.println("Error " + error),
                () -> System.out.println("Done"));
    }

    @Test
    public void test_flux_subscription() {
        Flux<Integer> ints = Flux.range(1, 4);
        ints.subscribe(i -> System.out.println(i),
                error -> System.err.println("Error " + error),
                () -> System.out.println("Done"),
                sub -> {System.out.println("call onNext"); sub.request(Long.MAX_VALUE);}); // onSubscribe
    }

    @Test  // 동기 방식, pull, 한 번에 한개만 보냄
    public void test_flux_generate() {
        Flux<String> flux = Flux.generate(
                () -> 0, // (1)
                (state, sink) -> {
                    sink.next("3 x " + state + " = " + 3*state); // (2)
                    if (state == 10) sink.complete(); // (3)
                    return state + 1; // (4)
                });

        flux.subscribe(new BaseSubscriber<String>() {
            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                System.out.println("Subscribe#onSubScribe");
                System.out.println("=====================");
                request(1);
            }

            @Override
            protected void hookOnNext(String value) {
                System.out.println("Subscribe#onNext");
                System.out.println(value);
                request(1);
            }

            @Override
            protected void hookOnComplete() {
                System.out.println("====================");
                System.out.println("Subscribe#onComplete");
            }

            @Override
            protected void hookOnError(Throwable throwable) {
                super.hookOnError(throwable);
            }
        });
        /*
            3 x 0 = 0
            3 x 1 = 3
            3 x 2 = 6
            3 x 3 = 9
            3 x 4 = 12
            3 x 5 = 15
            3 x 6 = 18
            3 x 7 = 21
            3 x 8 = 24
            3 x 9 = 27
            3 x 10 = 30
         */
    }

    @Test  // 동기 방식, pull, 한 번에 한개만 보냄
    public void test_flux_generate2() {
        Flux<String> flux = Flux.generate(
                AtomicLong::new,
                (state, sink) -> {
                    long i = state.getAndIncrement();
                    sink.next("3 x " + i + " = " + 3*i);
                    if (i == 10) sink.complete();
                    return state;
                }
        );
    }

    @Test //비동기, pull, 한 개 이상의 next() 발생가능
    public void test_flux_create_pull() {
        Flux<Integer> flux = Flux.create( (FluxSink<Integer> sink) -> {
            sink.onRequest(request -> {
                for (int i=1; i<= request; i++){
                    sink.next(i);
                }
            });
        });
    }

    @Test //비동기, push, 한 개 이상의 next() 발생가능
    public void test_flux_create_push() {
/*
            DatePump pump = new DataPump();

            Flux<Integer> bridge = Flux.create((FluxSink<Integer> sink) -> {
                pump.setListener(new DataListener<Integer>() {

                    @Override
                    public void onData(List<Integer> chunk) {  // 데이터가 도착하면 이 메서드가 실행한다고 가정
                           chunk.forEach(s -> {
                                sink.next(s); // Subscriber의 요청에 상관없이 신호 발생
                           });
                    }

                    @Override
                    public void complete() {
                        logger.info("complete");
                        sink.complete();
                    }
                });
           });*/
    }

    @Test
    public void test_flux_create_backPressure() {
        Flux<Integer> flux = Flux.create( (FluxSink<Integer> sink) -> {
            sink.onRequest(request -> {
                for (int i=1; i<=request+3; i++) { // Subscriber가 요청한 것 보다 3개 더 발생
                    sink.next(i);
                }
            });
        });

        // Flux.create()로 생성한 Flux 는 초과로 발생한 데이터를 버퍼에 보관.
        // Subscriber의 다음 request 에 전달
        // 요청보다 발생한 데이터가 많을 경우, 선택할 수 있는 처리 방식은 다음과 같음

        /*
        IGNORE : Subscriber의 요청 무시하고 발생(Subscriber의 큐가 다 차면 IllegalStateException 발생)
        ERROR : 익셉션(IllegalStateException) 발생
        DROP : Subscriber가 데이터를 받을 준비가 안 되어 있으면 데이터 발생 누락
        LATEST : 마지막 신호만 Subscriber에 전달
        BUFFER : 버퍼에 저장했다가 Subscriber 요청시 전달. 버퍼 제한이 없으므로 OutOfMemoryError 발생 가능
        */

        //Flux.create(sink -> { ... }, FluxSink.OverflowStrategy.IGNORE);


    }

    //@Test
    public void test_flux_lazy() {

        Flux<Integer> flux = Flux.range(1, 9)
                .flatMap(n -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return Mono.just(3 * n);
                });

        System.out.println("구독을 하지 않아, 데이터를 전달하지 않는다.");

        flux.subscribe(value -> {
                    System.out.println(value);
                },
                null,
                () -> {
                    System.out.println("데이터 수신 완료");
                });
        System.out.println("전체 완료... ");
    }

    @Test
    public void test_flux_onErrorReturn() {

        Flux<Integer> seq = Flux.range(1, 10)
                    .map(x -> {
                        if(x == 5) throw new RuntimeException("exception");
                        else return x;
                    })
                .onErrorReturn(-1);

        seq.subscribe(System.out::println);
    }

    @Test
    public void test_flux_onErrorResume() {

        Random random = new Random();
        Flux<Integer> seq = Flux.range(1, 10)
                .map(x -> {
                    int rand = random.nextInt(8);
                    if (rand == 0) throw new IllegalArgumentException("illarg");
                    if (rand == 1) throw new IllegalStateException("illstate");
                    if (rand == 2) throw new RuntimeException("exception");
                    return x;
                })
                .onErrorResume(error -> {
                    if (error instanceof IllegalArgumentException) {
                        return Flux.just(21, 22);
                    }
                    if (error instanceof IllegalStateException) {
                        return Flux.just(31, 32);
                    }
                    return Flux.error(error);
                });

        seq.subscribe(System.out::println);
    }

    @Test
    public void test_flux_onErrorMap() {
        Flux<Integer> seq = Flux.just(1,2,3)
                                .map(i -> {
                                    if (i == 3) throw new IllegalArgumentException("error");
                                    return i;
                                }).onErrorMap(error -> new Exception(".."));
    }

    @Test
    public void text_flux_retry() {
        Flux.range(1, 5)
                .map(input -> {
                    if(input < 4) return "num " + input;
                    throw new RuntimeException("boom");
                })
                .retry(1) // 에러 신호 발생 시 1회 재시도
                .subscribe(System.out::println, System.err::println);
    }

    @Test  // 잘 이해가 안간다... 나중에 이해할 수 있도록 하자.
    public void test_flux_retryWhen() {
        Flux<Integer> seq = Flux.just(1, 2, 3)
                .map(i -> {
                    if (i < 3) return i;
                    else throw new IllegalStateException("force");
                })
                .retryWhen(errorsFlux -> errorsFlux.take(2)); // 2개의 데이터 발생

        seq.subscribe(
                System.out::println,
                err -> System.err.println("에러 발생: " + err),
                () -> System.out.println("compelte")
        );

        /*
                1.에러가 발생할 때마다 에러가 컴페니언 Flux로 전달된다.
                2.컴페니언 Flux가 뭐든 발생하면 재시도가 일어난다.
                3.컴페니언 Flux가 종료되면 재시도를 하지 않고 원본 시퀀스 역시 종료된다.
                4.컴페니언 Flux가 에러를 발생하면 재시도를 하지 않고 컴페니언 Flux가 발생한 에러를 전파한다.
         */
    }
 }
