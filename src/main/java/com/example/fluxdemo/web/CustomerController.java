package com.example.fluxdemo.web;

import com.example.fluxdemo.domain.Customer;
import com.example.fluxdemo.domain.CustomerRepository;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;

@RestController
public class CustomerController {
    private final CustomerRepository customerRepository;

    private final Sinks.Many<Customer> sink; // 각 요청의 Stream을 합하는 역할
    // A 요청 -> Flux -> Stream
    // B 요청 -> Flux -> Stream
    // -> Flux.merge -> sink

    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
        sink = Sinks.many().multicast().onBackpressureBuffer(); //
    }

    @GetMapping("/flux")
    public Flux<Integer>flux() {
        return Flux.just(1,2,3,4,5).delayElements(Duration.ofSeconds(1)).log();
    }

    @GetMapping(value = "/fluxstream", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<Integer>fluxstream() {
        return Flux.just(1,2,3,4,5).delayElements(Duration.ofSeconds(1)).log();
    }

    @GetMapping(value = "/customer", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<Customer>findAll() { // Flux : 데이터가 여러개인 경우 Flux
        return customerRepository.findAll().log().delayElements(Duration.ofSeconds(1)).log();
    }

    @GetMapping("/customer/{id}")
    public Mono<Customer> findById(@PathVariable Long id) { // Mono : 데이터가 1개인 경우 Mono
        return customerRepository.findById(id).log();
    }

    @GetMapping(value = "/customer/sse") // 생략가능 , produces = MediaType.TEXT_EVENT_STREAM_VALUE
    public Flux<ServerSentEvent<Customer>> findAllSSE() {
        return sink.asFlux().map(customer -> ServerSentEvent.builder(customer).build()).doOnCancel(() -> {
            sink.asFlux().blockLast(); // blockLast : 마지막 데이터가 들어오면 onComplete 수행
        });
    }

    @PostMapping("/customer")
    public Mono<Customer> save() { // tryEmitNext : 데이터를 푸시한다
        return customerRepository.save(new Customer("gildong", "Hong")).doOnNext(customer -> {
            sink.tryEmitNext(customer);
        });
    }

}
