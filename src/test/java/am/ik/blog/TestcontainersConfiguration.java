package am.ik.blog;

import com.vmware.gemfire.testcontainers.GemFireCluster;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

	@Bean
	GemFireCluster cluster() {
		GemFireCluster cluster = new GemFireCluster("gemfire/gemfire:10.1-jdk21", 1, 1);
		cluster.acceptLicense()
			.withCacheXml(GemFireCluster.SERVER_GLOB, "/test-cache.xml")
			.withClasspath(GemFireCluster.ALL_GLOB, "target/classes")
			.start();
		return cluster;
	}

	@Bean
	DynamicPropertyRegistrar dynamicPropertyRegistrar(GemFireCluster cluster) {
		return registry -> registry.add("gemfire.locators", () -> "127.0.0.1:%d".formatted(cluster.getLocatorPort()));
	}

}
