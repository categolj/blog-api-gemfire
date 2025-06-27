package am.ik.blog.config;

import am.ik.blog.GitHubProps;
import am.ik.pagination.web.CursorPageRequestHandlerMethodArgumentResolver;
import am.ik.webhook.spring.WebhookVerifierRequestBodyAdvice;
import java.time.Instant;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
public class WebConfig implements WebMvcConfigurer {

	@Bean
	WebhookVerifierRequestBodyAdvice webhookVerifierRequestBodyAdvice(GitHubProps gitHubProps) {
		return WebhookVerifierRequestBodyAdvice.githubSha256(gitHubProps.getWebhookSecret());
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(
				new CursorPageRequestHandlerMethodArgumentResolver<>(Instant::parse, props -> props.withSizeMax(1024)));
	}

}
