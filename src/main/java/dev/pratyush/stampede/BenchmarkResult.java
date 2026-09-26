package dev.pratyush.stampede;

record BenchmarkResult(
        Strategy strategy,
        int readers,
        int cycles,
        long ttlMillis,
        long recomputeMillis,
        long originFetches,
        long maxOriginFetchesPerExpiry,
        long waitResponses,
        long earlyRefreshes
) {
    String csv() {
        return "%s,%d,%d,%d,%d,%d,%d,%d,%d".formatted(
                strategy.label(),
                readers,
                cycles,
                ttlMillis,
                recomputeMillis,
                originFetches,
                maxOriginFetchesPerExpiry,
                waitResponses,
                earlyRefreshes
        );
    }

    static String header() {
        return "strategy,readers,cycles,ttl_millis,recompute_millis,origin_fetches,"
                + "max_origin_fetches_per_expiry,wait_responses,early_refreshes";
    }
}
