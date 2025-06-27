package am.ik.blog;

import am.ik.blog.mockserver.MockServer;
import java.time.Clock;
import java.time.Instant;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.springframework.test.util.TestSocketUtils;

@TestConfiguration(proxyBeanMethods = false)
public class MockConfig {

	// truncate to milliseconds
	public static final Instant NOW = Instant.ofEpochMilli(Instant.now().toEpochMilli()).plusSeconds(200);

	@Primary
	@Bean
	Clock testClock() {
		return Clock.fixed(NOW, Clock.systemDefaultZone().getZone());
	}

	@Bean
	MockServer mockServer() {
		int availableTcpPort = TestSocketUtils.findAvailableTcpPort();
		MockServer mockServer = new MockServer(availableTcpPort);
		mockServer.run();
		return mockServer;
	}

	@Bean
	DynamicPropertyRegistrar gitDynamicPropertyRegistrar(MockServer mockServer) {
		return registry -> {
			int port = mockServer.port();
			registry.add("blog.github.api-url", () -> "http://127.0.0.1:%d".formatted(port));
			registry.add("blog.github.content-owner", () -> "public");
			registry.add("blog.github.content-repo", () -> "blog");
			registry.add("blog.github.access-token", () -> "important");
			registry.add("blog.github.tenants.t1.api-url", () -> "http://127.0.0.1:%d".formatted(port));
			registry.add("blog.github.tenants.t1.content-owner", () -> "private");
			registry.add("blog.github.tenants.t1.content-repo", () -> "blog");
			registry.add("blog.github.tenants.t1.access-token", () -> "secret");
		};
	}

}
