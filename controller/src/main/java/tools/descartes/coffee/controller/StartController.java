package tools.descartes.coffee.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StartController {
    private final TestExecutor testExecutor;

    public StartController(TestExecutor testExecutor) {
        this.testExecutor = testExecutor;
    }

    @GetMapping("/start")
    public String start() {
        Thread t = new Thread(testExecutor);
        t.start();
        return "Start successful";
    }
}