package am.ik.blog.config;

import java.util.Locale;
import java.util.Set;
import org.springframework.boot.actuate.endpoint.SanitizableData;
import org.springframework.boot.actuate.endpoint.SanitizingFunction;
import org.springframework.stereotype.Component;

@Component
public class SanitizingFunctionImpl implements SanitizingFunction {

	private final Set<String> keywords = Set.of("pass", "token", "secret", "key", "noop", "url");

	@Override
	public SanitizableData apply(SanitizableData data) {
		final String key = data.getKey().toLowerCase(Locale.US);
		final String value = data.getValue().toString().toLowerCase(Locale.US);
		for (String keyword : keywords) {
			if (key.contains(keyword) || value.contains(keyword)) {
				return data.withValue("[REDACTED]");
			}
		}
		return data;
	}

}