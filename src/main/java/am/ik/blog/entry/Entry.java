package am.ik.blog.entry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.time.Instant;
import org.jilt.Builder;
import org.jspecify.annotations.Nullable;

@Builder(toBuilder = "from")
public record Entry(@JsonUnwrapped EntryKey entryKey, FrontMatter frontMatter,
		@Nullable @JsonInclude(JsonInclude.Include.NON_NULL) String content, Author created, Author updated) {

	public String toMarkdown() {
		return """
				---
				title: %s
				tags: %s
				categories: %s%s%s
				---

				%s
				""".formatted(frontMatter.title(),
				frontMatter.tags().stream().map(t -> "\"%s\"".formatted(t.name())).toList(),
				frontMatter.categories().stream().map(c -> "\"%s\"".formatted(c.name())).toList(),
				created.date() == null ? "" : "%n%s: %s".formatted(FrontMatter.DATE_FIELD, created.date()),
				updated.date() == null ? "" : "%n%s: %s".formatted(FrontMatter.UPDATE_FIELD, updated.date()), content);
	}

	public static Long parseId(String fileName) {
		return Long.parseLong(fileName.replace(".md", "").replace(".markdown", ""));
	}

	public Long entryId() {
		return entryKey.entryId();
	}

	@JsonIgnore
	public String formatId() {
		return Entry.formatId(entryKey().entryId());
	}

	public static String formatId(Long entryId) {
		return "%05d".formatted(entryId);
	}

	@Nullable public Instant toCursor() {
		if (updated == null) {
			return null;
		}
		return updated.date();
	}
}
