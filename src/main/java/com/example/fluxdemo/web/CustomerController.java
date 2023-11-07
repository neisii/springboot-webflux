package com.example.fluxdemo.web;

import com.example.fluxdemo.domain.Customer;
import com.example.fluxdemo.domain.CustomerRepository;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
public class CustomerController {
    private final CustomerRepository customerRepository;

    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping("/flux")
    public Flux<Integer>flux() {
        return Flux.just(1,2,3,4,5).delayElements(Duration.ofSeconds(1)).log();
    }

    @GetMapping(value = "/fluxstream", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<Integer>fluxstream() {
        return Flux.just(1,2,3,4,5).delayElements(Duration.ofSeconds(1)).log();
    }

    @GetMapping("/customer")
    public Flux<Customer>findAll() { // 데이터가 여러개인 경우 Flux
        return customerRepository.findAll().log();
    }

    @GetMapping("/customer/{id}")
    public Mono<Customer> findById(@PathVariable Long id) { // 데이터가 1개인 경우 Mono
        return customerRepository.findById(id).log();
    }

}
