package am.ik.blog.config;

import java.time.Clock;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.spring.LogbookClientHttpRequestInterceptor;

@Configuration(proxyBeanMethods = false)
class AppConfig {

	@Bean
	Clock clock() {
		return Clock.systemDefaultZone();
	}

	@Bean
	RestClientCustomizer restClientCustomizer(Logbook logbook) {
		return restClientBuilder -> restClientBuilder
			.requestInterceptor(new LogbookClientHttpRequestInterceptor(logbook));
	}

}
