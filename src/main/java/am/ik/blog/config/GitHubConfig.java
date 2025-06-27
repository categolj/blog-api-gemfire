package am.ik.blog.config;

import am.ik.blog.GitHubProps;
import am.ik.blog.github.Committer;
import am.ik.blog.github.GitCommit;
import am.ik.blog.github.GitCommitter;
import am.ik.blog.github.GitHubClient;
import am.ik.blog.github.GitHubUserContentClient;
import am.ik.blog.github.Parent;
import am.ik.blog.github.Tree;
import am.ik.spring.http.client.RetryableClientHttpRequestInterceptor;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.NoOpResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.support.RestTemplateAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.zalando.logbook.spring.LogbookClientHttpRequestInterceptor;

@Configuration(proxyBeanMethods = false)
@ImportRuntimeHints(GitHubConfig.RuntimeHints.class)
class GitHubConfig {

	@Bean
	RestTemplateCustomizer restTemplateCustomizer(GitHubProps props,
			LogbookClientHttpRequestInterceptor logbookClientHttpRequestInterceptor) {
		return restTemplate -> {
			restTemplate.setInterceptors(List.of(logbookClientHttpRequestInterceptor,
					new RetryableClientHttpRequestInterceptor(props.getBackOff())));
			restTemplate.setErrorHandler(new NoOpResponseErrorHandler());
		};
	}

	@Bean
	GitHubClient gitHubClient(GitHubProps props, RestTemplateBuilder restTemplateBuilder) {
		RestTemplate restTemplate = restTemplateBuilder //
			.rootUri(props.getApiUrl())
			.connectTimeout(props.getConnectTimeout())
			.readTimeout(props.getReadTimeout()) //
			.defaultHeader(HttpHeaders.AUTHORIZATION, "token %s".formatted(props.getAccessToken())) //
			.build();
		RestTemplateAdapter adapter = RestTemplateAdapter.create(restTemplate);
		HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
		return factory.createClient(GitHubClient.class);
	}

	@Bean
	Map<String, GitHubClient> tenantsGitHubClient(GitHubProps props, RestTemplateBuilder restTemplateBuilder) {
		return props.getTenants().entrySet().stream().collect(Collectors.toUnmodifiableMap(Entry::getKey, e -> {
			GitHubProps tenantProps = e.getValue();
			RestTemplate restTemplate = restTemplateBuilder //
				.rootUri(props.getApiUrl())
				.connectTimeout(tenantProps.getConnectTimeout())
				.readTimeout(tenantProps.getReadTimeout()) //
				.defaultHeader(HttpHeaders.AUTHORIZATION, "token %s".formatted(tenantProps.getAccessToken())) //
				.build();
			RestTemplateAdapter adapter = RestTemplateAdapter.create(restTemplate);
			HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
			return factory.createClient(GitHubClient.class);
		}));
	}

	@Bean
	GitHubUserContentClient gitHubUserContentClient(GitHubProps props, RestTemplateBuilder restTemplateBuilder) {
		RestTemplate restTemplate = restTemplateBuilder //
			.rootUri("https://raw.githubusercontent.com")
			.connectTimeout(props.getConnectTimeout())
			.readTimeout(props.getReadTimeout()) //
			.defaultHeader(HttpHeaders.AUTHORIZATION, "token %s".formatted(props.getAccessToken())) //
			.build();
		RestTemplateAdapter adapter = RestTemplateAdapter.create(restTemplate);
		HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
		return factory.createClient(GitHubUserContentClient.class);
	}

	static class RuntimeHints implements RuntimeHintsRegistrar {

		@Override
		public void registerHints(org.springframework.aot.hint.RuntimeHints hints, @Nullable ClassLoader classLoader) {
			hints.reflection()
				.registerConstructor(GitCommit.class.getDeclaredConstructors()[0], ExecutableMode.INVOKE)
				.registerConstructor(GitCommitter.class.getDeclaredConstructors()[0], ExecutableMode.INVOKE)
				.registerConstructor(Committer.class.getDeclaredConstructors()[0], ExecutableMode.INVOKE)
				.registerConstructor(Parent.class.getDeclaredConstructors()[0], ExecutableMode.INVOKE)
				.registerConstructor(Tree.class.getDeclaredConstructors()[0], ExecutableMode.INVOKE);
		}

	}

}
