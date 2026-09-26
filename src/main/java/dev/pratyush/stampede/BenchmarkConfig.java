package dev.pratyush.stampede;

record BenchmarkConfig(
        int readers,
        int cycles,
        long ttlMillis,
        long recomputeMillis,
        double beta,
        long seed
) {
    static BenchmarkConfig defaults() {
        return new BenchmarkConfig(1_000, 5, 1_000, 100, 1.0, 42L);
    }
}
