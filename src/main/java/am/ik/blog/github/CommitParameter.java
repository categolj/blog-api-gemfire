package am.ik.blog.github;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import org.jspecify.annotations.Nullable;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class CommitParameter {

	@Nullable private String sha;

	@Nullable private String path;

	@Nullable private String author;

	@Nullable private Instant since;

	@Nullable private Instant until;

	public CommitParameter() {

	}

	public CommitParameter sha(String sha) {
		this.sha = sha;
		return this;
	}

	public CommitParameter path(String path) {
		this.path = path;
		return this;
	}

	public CommitParameter author(String author) {
		this.author = author;
		return this;
	}

	public CommitParameter since(Instant since) {
		this.since = since;
		return this;
	}

	public CommitParameter until(Instant until) {
		this.until = until;
		return this;
	}

	public MultiValueMap<String, String> queryParams() {
		MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
		if (sha != null) {
			queryParams.add("sha", sha);
		}
		if (path != null) {
			queryParams.add("path", path);
		}
		if (author != null) {
			queryParams.add("author", author);
		}
		if (since != null) {
			queryParams.add("since", DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(since));
		}
		if (until != null) {
			queryParams.add("until", DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(until));
		}
		return queryParams;
	}

}
