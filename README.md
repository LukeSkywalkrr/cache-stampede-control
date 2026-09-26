# Cache Stampede Control

This Java 21 Spring Boot benchmark models the lease mechanism described in Facebook's NSDI 2013 memcache paper and the XFetch early-expiration rule from Vattani, Chierichetti, and Lowenstein.

## The problem

A hot key expires at one instant. With 1,000 concurrent readers, a normal cache-aside miss can send 1,000 equivalent requests to the origin before any reader has a fresh value to reuse.

## Run it

```bash
./gradlew bootRun
```

The command prints the benchmark and writes `benchmark.csv`.

```csv
strategy,readers,cycles,ttl_millis,recompute_millis,origin_fetches,max_origin_fetches_per_expiry,wait_responses,early_refreshes
naive,1000,5,1000,100,5000,1000,0,0
lease,1000,5,1000,100,5,1,4995,0
xfetch,1000,5,1000,100,25,6,0,25
```

## What it shows

Naive cache-aside turns one expired hot key into one origin fetch per reader. A lease turns the same boundary into one origin fetch per expiry window while the other readers wait. XFetch moves a small amount of refresh work before expiry, so the sharp boundary is gone before the herd arrives.

## Test

```bash
./gradlew test
```

The tests assert the stampede properties: naive origin work scales with reader count, leases allow one origin fetch per expiry, XFetch stays far below the naive case, the CSV has one row per strategy, and slower recomputation widens the early-refresh window.

## Limits

This is an in-memory simulator, not a memcached server. It does not model packet loss, multi-key fan-out, cache-server failover, stale serving policy, or database latency. The local numbers demonstrate the mechanism under fixed inputs; they are not claims about Facebook production traffic.
