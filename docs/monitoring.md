Monitoring (Prometheus & Grafana)

This document shows a minimal Prometheus scrape configuration and a simple Grafana dashboard example to get you started.

## Prometheus scrape config (example)

Add this job to your `prometheus.yml` to scrape the application's Prometheus metrics endpoint (`/actuator/prometheus`):

```yaml
scrape_configs:
  - job_name: 'book-buddy'
    static_configs:
      - targets: ['your-app-host:8080']
    metrics_path: /actuator/prometheus
    scheme: http
    # If using basic auth or TLS, configure accordingly
```

## Grafana dashboard (minimal)

Below are a couple of Prometheus queries you can use as panels in Grafana. They are intentionally simple; adjust labels and filters as needed.

1) JVM memory (used): Prometheus query

```promql
jvm_memory_used_bytes{area="heap"}
```

2) HTTP requests per second (example)

```promql
rate(http_server_requests_seconds_count[1m])
```

3) Importing a dashboard

- In Grafana: Dashboards → New → Import and paste JSON or use a template.
- Example panels:
  - JVM Heap Used (PromQL: `jvm_memory_used_bytes{area="heap"}`)
  - HTTP Requests/sec (PromQL: `rate(http_server_requests_seconds_count[1m])`)

Notes:
- Ensure `/actuator/prometheus` is exposed in `application-prod.yml` and you have `micrometer-registry-prometheus` on the classpath (already added).
- Secure access to the Prometheus endpoint (network-level controls or authentication). Do not publicly expose metrics endpoints.
