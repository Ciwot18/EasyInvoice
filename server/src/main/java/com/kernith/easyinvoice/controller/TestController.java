package com.kernith.easyinvoice.controller;

import com.kernith.easyinvoice.data.model.TestEntity;
import com.kernith.easyinvoice.data.repository.TestEntityRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
public class TestController {

    private final TestEntityRepository repository;

    public TestController(TestEntityRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/ping")
    public String ping() {
        return "OK";
    }

    @PostMapping
    public TestEntity create(@RequestParam String name) {
        TestEntity e = new TestEntity();
        e.setName(name);
        return repository.save(e);
    }
}