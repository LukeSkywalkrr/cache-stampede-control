package dev.pratyush.stampede;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StampedeApplication implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(StampedeApplication.class, args);
    }

    @Override
    public void run(String... args) {
        BenchmarkConfig config = BenchmarkConfig.defaults();
        BenchmarkRunner runner = new BenchmarkRunner(new StampedeSimulator(config), config);
        runner.run();
    }
}
