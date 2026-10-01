package com.example.benchmark;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BenchmarkController.class)
class BenchmarkControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Test
    void healthReturnsOk() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"status\":\"ok\"}"));
    }

    @Test
    void jsonReturnsGreeting() throws Exception {
        mockMvc.perform(get("/json"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"Hello, World!\"}"));
    }

    @Test
    void echoReturnsRequestBody() throws Exception {
        String body = "{\"name\":\"test\",\"values\":[1,2]}";
        mockMvc.perform(post("/echo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().json(body));
    }

    @Test
    void itemsUsesDefaultCount() throws Exception {
        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(100));
    }

    @Test
    void itemsUsesRequestedCountAndDeterministicValues() throws Exception {
        mockMvc.perform(get("/items?count=2"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [{"id":0,"name":"Item 0"},{"id":1,"name":"Item 1"}]
                        """));
    }

    @Test
    void itemsRejectsInvalidCount() throws Exception {
        mockMvc.perform(get("/items?count=-1"))
                .andExpect(status().isBadRequest());
    }
}
