package kata.ratelimiter.webfluxapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class ApiRestController {

    @GetMapping("/api/resource")
    public Mono<Void> getResource() {
        return Mono.empty();
    }
}
