package com.example.benchmark;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class BenchmarkController {
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    @GetMapping(value = "/json", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> json() {
        return Map.of("message", "Hello, World!");
    }

    @PostMapping(
            value = "/echo",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Object echo(@RequestBody Object body) {
        return body;
    }

    @GetMapping(value = "/items", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Item> items(@RequestParam(defaultValue = "100") int count) {
        if (count < 0 || count > 10_000) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "count must be between 0 and 10000");
        }
        return IntStream.range(0, count)
                .mapToObj(i -> new Item(i, "Item " + i))
                .toList();
    }

    public record Item(int id, String name) {}
}
