package am.ik.blog.entry;

import am.ik.blog.markdown.MarkdownToJson;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Date;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class EntryParser {

	private final ObjectMapper objectMapper;

	public EntryParser(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@SuppressWarnings("unchecked")
	public EntryBuilder fromMarkdown(EntryKey entryKey, String markdown, Author altCreated, Author altUpdated) {
		Map<String, Object> json = MarkdownToJson.convert(markdown);
		Map<String, Object> frontMatter = (Map<String, Object>) json.get("frontMatter");
		Assert.notNull(frontMatter, "FrontMatter must not be null");
		return EntryBuilder.from(this.objectMapper.convertValue(json, Entry.class))
			.entryKey(entryKey)
			.created(frontMatter.containsKey(FrontMatter.DATE_FIELD)
					? altCreated.withDate(((Date) frontMatter.get(FrontMatter.DATE_FIELD)).toInstant()) : altCreated)
			.updated(frontMatter.containsKey(FrontMatter.UPDATE_FIELD)
					? altUpdated.withDate(((Date) frontMatter.get(FrontMatter.UPDATE_FIELD)).toInstant()) : altUpdated);
	}

}
