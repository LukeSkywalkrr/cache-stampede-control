package dev.pratyush.stampede;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

final class BenchmarkRunner {
    private final StampedeSimulator simulator;
    private final BenchmarkConfig config;

    BenchmarkRunner(StampedeSimulator simulator, BenchmarkConfig config) {
        this.simulator = simulator;
        this.config = config;
    }

    void run() {
        List<BenchmarkResult> results = simulator.runAll();
        Path csv = Path.of("benchmark.csv");
        String output = toCsv(results);
        try {
            Files.writeString(csv, output);
        } catch (IOException e) {
            throw new IllegalStateException("could not write benchmark.csv", e);
        }

        System.out.println("Cache stampede benchmark");
        System.out.println("readers=" + config.readers()
                + " cycles=" + config.cycles()
                + " ttl_ms=" + config.ttlMillis()
                + " recompute_ms=" + config.recomputeMillis()
                + " beta=" + config.beta());
        System.out.print(output);
    }

    private static String toCsv(List<BenchmarkResult> results) {
        StringBuilder csv = new StringBuilder(BenchmarkResult.header()).append(System.lineSeparator());
        for (BenchmarkResult result : results) {
            csv.append(result.csv()).append(System.lineSeparator());
        }
        return csv.toString();
    }
}
