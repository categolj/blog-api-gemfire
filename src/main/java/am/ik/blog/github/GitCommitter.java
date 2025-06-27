package am.ik.blog.github;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

public record GitCommitter(String name, String email, Instant date) {

	@Override
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	public Instant date() {
		return date;
	}
}
