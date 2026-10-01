package com.example.benchmark;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class BenchmarkResource {
    @GET
    @Path("health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    @GET
    @Path("json")
    public Map<String, String> json() {
        return Map.of("message", "Hello, World!");
    }

    @POST
    @Path("echo")
    @Consumes(MediaType.APPLICATION_JSON)
    public Object echo(Object body) {
        return body;
    }

    @GET
    @Path("items")
    public List<Item> items(@QueryParam("count") Integer requestedCount) {
        int count = requestedCount == null ? 100 : requestedCount;
        if (count < 0 || count > 10_000) {
            throw new BadRequestException("count must be between 0 and 10000");
        }
        return IntStream.range(0, count)
                .mapToObj(i -> new Item(i, "Item " + i))
                .toList();
    }

    public record Item(int id, String name) {}
}
