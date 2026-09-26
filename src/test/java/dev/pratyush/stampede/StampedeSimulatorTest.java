package dev.pratyush.stampede;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class StampedeSimulatorTest {
    private final BenchmarkConfig config = BenchmarkConfig.defaults();
    private final StampedeSimulator simulator = new StampedeSimulator(config);

    @Test
    void naiveCacheRegeneratesOncePerReaderAtTheExpiryBoundary() {
        BenchmarkResult result = simulator.simulateNaive();

        assertThat(result.originFetches()).isEqualTo((long) config.readers() * config.cycles());
        assertThat(result.maxOriginFetchesPerExpiry()).isEqualTo(config.readers());
    }

    @Test
    void leaseAllowsOnlyOneOriginFetchPerExpiryWindow() {
        BenchmarkResult result = simulator.simulateLease();

        assertThat(result.originFetches()).isEqualTo(config.cycles());
        assertThat(result.maxOriginFetchesPerExpiry()).isEqualTo(1);
        assertThat(result.waitResponses()).isEqualTo((long) (config.readers() - 1) * config.cycles());
    }

    @Test
    void xfetchMovesWorkBeforeTheCliffAndKeepsOriginWorkSmall() {
        BenchmarkResult result = simulator.simulateXFetch();

        assertThat(result.originFetches()).isLessThan(config.readers());
        assertThat(result.maxOriginFetchesPerExpiry()).isLessThan(10);
        assertThat(result.earlyRefreshes()).isEqualTo(result.originFetches());
    }

    @Test
    void benchmarkContainsOneCsvRowPerStrategy() {
        Map<Strategy, BenchmarkResult> results = simulator.runAll().stream()
                .collect(Collectors.toMap(BenchmarkResult::strategy, result -> result));

        assertThat(results).containsOnlyKeys(Strategy.NAIVE, Strategy.LEASE, Strategy.XFETCH);
    }

    @Test
    void slowerRecomputationWidensTheEarlyRefreshWindow() {
        BenchmarkResult fast = new StampedeSimulator(new BenchmarkConfig(1_000, 5, 1_000, 50, 1.0, 42L))
                .simulateXFetch();
        BenchmarkResult slow = new StampedeSimulator(new BenchmarkConfig(1_000, 5, 1_000, 200, 1.0, 42L))
                .simulateXFetch();

        assertThat(slow.originFetches()).isGreaterThanOrEqualTo(fast.originFetches());
    }
}
