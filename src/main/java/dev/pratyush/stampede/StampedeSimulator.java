package dev.pratyush.stampede;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

final class StampedeSimulator {
    private final BenchmarkConfig config;

    StampedeSimulator(BenchmarkConfig config) {
        this.config = config;
    }

    List<BenchmarkResult> runAll() {
        return List.of(simulateNaive(), simulateLease(), simulateXFetch());
    }

    BenchmarkResult simulateNaive() {
        long originFetches = (long) config.readers() * config.cycles();
        return new BenchmarkResult(Strategy.NAIVE, config.readers(), config.cycles(), config.ttlMillis(),
                config.recomputeMillis(), originFetches, config.readers(), 0, 0);
    }

    BenchmarkResult simulateLease() {
        long originFetches = config.cycles();
        long waitResponses = (long) (config.readers() - 1) * config.cycles();
        return new BenchmarkResult(Strategy.LEASE, config.readers(), config.cycles(), config.ttlMillis(),
                config.recomputeMillis(), originFetches, 1, waitResponses, 0);
    }

    BenchmarkResult simulateXFetch() {
        SplittableRandom random = new SplittableRandom(config.seed());
        long originFetches = 0;
        long maxPerExpiry = 0;
        long earlyRefreshes = 0;

        for (int cycle = 0; cycle < config.cycles(); cycle++) {
            long expiry = config.ttlMillis();
            List<Long> refreshStarts = new ArrayList<>();

            for (int reader = 0; reader < config.readers(); reader++) {
                long now = Math.floorDiv((long) reader * config.ttlMillis(), config.readers());
                double u = Math.max(random.nextDouble(), 0.000001);
                double gap = -config.recomputeMillis() * config.beta() * Math.log(u);
                boolean refreshInFlight = refreshStarts.stream()
                        .anyMatch(start -> now >= start && now < start + config.recomputeMillis());
                if (!refreshInFlight && now + gap >= expiry) {
                    refreshStarts.add(now);
                    earlyRefreshes++;
                }
            }

            if (refreshStarts.isEmpty()) {
                refreshStarts.add(expiry);
            }
            originFetches += refreshStarts.size();
            maxPerExpiry = Math.max(maxPerExpiry, refreshStarts.size());
        }

        return new BenchmarkResult(Strategy.XFETCH, config.readers(), config.cycles(), config.ttlMillis(),
                config.recomputeMillis(), originFetches, maxPerExpiry, 0, earlyRefreshes);
    }
}
