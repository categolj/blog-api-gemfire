# Blog API GemFire

A drop-in replacement for [blog-api](https://github.com/categolj/blog-api) using VMware GemFire as
the backend.

## Prerequisites

### Maven Configuration

To access the Broadcom GemFire repository, add the following to your `~/.m2/settings.xml`:

```xml

<settings>
  <servers>
    <server>
      <id>gemfire-release-repo</id>
      <username>[YOUR_BC_SUPPORT_EMAIL]</username>
      <password>[YOUR_BC_ACCESS_TOKEN]</password>
    </server>
  </servers>
</settings>
```

## Quick Start

Clone and run with embedded GemFire using Testcontainers:

```bash
git clone https://github.com/categolj/blog-api-gemfire
cd blog-api-gemfire
./mvnw spring-boot:test-run
```

## Running with External GemFire

### Basic Configuration

Connect to an external GemFire locator:

```bash
./mvnw clean package -DskipTests
java -jar target/blog-api-gemfire-0.0.1-SNAPSHOT.jar --gemfire.locators=localhost:10334
```

### SNI Proxy Configuration

For environments using SNI proxy with SSL:

```bash
java -jar target/blog-api-gemfire-0.0.1-SNAPSHOT.jar \
  --gemfire.locators=demo-locator-0.demo-locator.demo.svc.cluster.local:10334,demo-locator-1.demo-locator.demo.svc.cluster.local:10334 \
  --gemfire.sni-proxy.host=[SNI_PROXY_IP] \
  --gemfire.sni-proxy.port=443 \
  --gemfire.properties.ssl-enabled-components=all \
  --gemfire.properties.ssl-endpoint-identification-enabled=true \
  --gemfire.properties.ssl-keystore=/certs/keystore.p12 \
  --gemfire.properties.ssl-keystore-password=${CERT_PASSWORD} \
  --gemfire.properties.ssl-truststore=/certs/truststore.p12 \
  --gemfire.properties.ssl-truststore-password=${CERT_PASSWORD}
```

## Running with Docker

A pre-built Docker image is available:

```bash
docker run --rm --name blog-api ghcr.io/categolj/blog-api-gemfire:jvm --gemfire.locators=LOCATOR_IP:10334
```
