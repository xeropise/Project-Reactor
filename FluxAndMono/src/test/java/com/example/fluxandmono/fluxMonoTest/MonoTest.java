package com.example.fluxandmono.fluxMonoTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class MonoTest {

    @Test
    public void test_mono_just() {

        List<Signal<Integer>> signals = new ArrayList<>(4);

        final Integer[] result = new Integer[1];

        Mono<Integer> mon = Mono.just(1).log()
                    .doOnEach(integerSignal -> {
                        signals.add(integerSignal);
                        System.out.println("Signal... : " + integerSignal);
                    });

        mon.subscribe(integer -> result[0] = integer);

        Assertions.assertEquals(signals.size(), 2);
        Assertions.assertEquals(signals.get(0).getType().name(), "ON_NEXT");
        Assertions.assertEquals(signals.get(1).getType().name(), "ON_COMPLETE");
        Assertions.assertEquals(result[0].intValue(), 1);


    }
}
