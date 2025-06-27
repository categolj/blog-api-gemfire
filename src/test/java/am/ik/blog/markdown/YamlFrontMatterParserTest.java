package am.ik.blog.markdown;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class YamlFrontMatterParserTest {

	@Test
	@DisplayName("Parse markdown with front matter")
	void parseWithFrontMatter() {
		String markdown = """
				---
				title: Sample Post
				author: John Doe
				published: true
				views: 100
				rating: 4.5
				tags: [java, markdown, test]
				---
				# Hello World
				This is the content.
				""";

		YamlFrontMatterParser.ParseResult result = YamlFrontMatterParser.parse(markdown);

		assertThat(result.getFrontMatter()).containsEntry("title", "Sample Post")
			.containsEntry("author", "John Doe")
			.containsEntry("published", true)
			.containsEntry("views", 100)
			.containsEntry("rating", 4.5)
			.containsEntry("tags", List.of("java", "markdown", "test"));

		assertThat(result.getContent()).contains("# Hello World");
		assertThat(result.getContent()).contains("This is the content.");
	}

	@Test
	@DisplayName("Parse markdown without front matter")
	void parseWithoutFrontMatter() {
		String markdown = """
				# Hello World
				This is the content without front matter.
				""";

		YamlFrontMatterParser.ParseResult result = YamlFrontMatterParser.parse(markdown);

		assertThat(result.getFrontMatter()).isEmpty();
		assertThat(result.getContent()).contains("# Hello World");
		assertThat(result.getContent()).contains("This is the content without front matter.");
	}

	@Test
	@DisplayName("Parse markdown with empty front matter")
	void parseWithEmptyFrontMatter() {
		String markdown = """
				---
				---
				# Hello World
				This is the content.
				""";

		YamlFrontMatterParser.ParseResult result = YamlFrontMatterParser.parse(markdown);

		assertThat(result.getFrontMatter()).isEmpty();
		assertThat(result.getContent()).contains("# Hello World");
		assertThat(result.getContent()).contains("This is the content.");
	}

	@Test
	@DisplayName("Parse quoted strings")
	void parseQuotedStrings() {
		String markdown = """
				---
				title: "Quoted Title"
				description: 'Single quoted description'
				plain: Plain text
				---
				Content
				""";

		YamlFrontMatterParser.ParseResult result = YamlFrontMatterParser.parse(markdown);

		assertThat(result.getFrontMatter()).containsEntry("title", "Quoted Title")
			.containsEntry("description", "Single quoted description")
			.containsEntry("plain", "Plain text");
	}

	@Test
	@DisplayName("Parse numbers and booleans")
	void parseNumbersAndBooleans() {
		String markdown = """
				---
				integer: 42
				decimal: 3.14
				boolean_true: true
				boolean_false: false
				null_value: null
				---
				Content
				""";

		YamlFrontMatterParser.ParseResult result = YamlFrontMatterParser.parse(markdown);

		assertThat(result.getFrontMatter()).containsEntry("integer", 42)
			.containsEntry("decimal", 3.14)
			.containsEntry("boolean_true", true)
			.containsEntry("boolean_false", false)
			.containsEntry("null_value", null);
	}

	@Test
	@DisplayName("Parse arrays")
	void parseArrays() {
		String markdown = """
				---
				tags: [java, spring, test]
				numbers: [1, 2, 3]
				mixed: [hello, 42, true]
				empty: []
				---
				Content
				""";

		YamlFrontMatterParser.ParseResult result = YamlFrontMatterParser.parse(markdown);

		assertThat(result.getFrontMatter()).containsEntry("tags", List.of("java", "spring", "test"))
			.containsEntry("numbers", List.of(1, 2, 3))
			.containsEntry("mixed", List.of("hello", 42, true))
			.containsEntry("empty", List.of());
	}

	@Test
	@DisplayName("Ignore comment lines")
	void ignoreComments() {
		String markdown = """
				---
				# This is a comment
				title: Sample Post
				# Another comment
				author: John Doe
				---
				Content
				""";

		YamlFrontMatterParser.ParseResult result = YamlFrontMatterParser.parse(markdown);

		assertThat(result.getFrontMatter()).containsEntry("title", "Sample Post")
			.containsEntry("author", "John Doe")
			.doesNotContainKey("# This is a comment")
			.doesNotContainKey("# Another comment");
	}

	@Test
	@DisplayName("Ignore empty lines")
	void ignoreEmptyLines() {
		String markdown = """
				---

				title: Sample Post

				author: John Doe

				---
				Content
				""";

		YamlFrontMatterParser.ParseResult result = YamlFrontMatterParser.parse(markdown);

		assertThat(result.getFrontMatter()).containsEntry("title", "Sample Post")
			.containsEntry("author", "John Doe")
			.hasSize(2);
	}

	@Test
	@DisplayName("Parse with Windows line endings")
	void parseWithWindowsLineEndings() {
		String markdown = "---\r\ntitle: Sample Post\r\nauthor: John Doe\r\n---\r\nContent";

		YamlFrontMatterParser.ParseResult result = YamlFrontMatterParser.parse(markdown);

		assertThat(result.getFrontMatter()).containsEntry("title", "Sample Post").containsEntry("author", "John Doe");
		assertThat(result.getContent()).isEqualTo("Content");
	}

}