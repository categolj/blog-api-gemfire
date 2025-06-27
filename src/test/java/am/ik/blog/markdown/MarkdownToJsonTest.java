package am.ik.blog.markdown;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MarkdownToJsonTest {

	@Test
	@DisplayName("Convert markdown with front matter and content")
	void convertMarkdownWithFrontMatter() {
		String markdown = """
				---
				title: Sample Post
				author: John Doe
				published: true
				views: 100
				tags: [java, markdown]
				---
				# Hello World

				This is a sample blog post with **bold** text and *italic* text.

				## Section 2

				- Item 1
				- Item 2
				- Item 3
				""";

		Map<String, Object> result = MarkdownToJson.convert(markdown);

		assertThat(result).containsKey("frontMatter");
		assertThat(result).containsKey("content");

		@SuppressWarnings("unchecked")
		Map<String, Object> frontMatter = (Map<String, Object>) result.get("frontMatter");
		assertThat(frontMatter.get("title")).isEqualTo("Sample Post");
		assertThat(frontMatter.get("author")).isEqualTo("John Doe");
		assertThat(frontMatter.get("published")).isEqualTo(true);
		assertThat(frontMatter.get("views")).isEqualTo(100);
		assertThat(frontMatter.get("tags")).isEqualTo(List.of("java", "markdown"));
		assertThat(result.get("content")).isEqualTo(
				"# Hello World\n\nThis is a sample blog post with **bold** text and *italic* text.\n\n## Section 2\n\n- Item 1\n- Item 2\n- Item 3");
	}

	@Test
	@DisplayName("Convert markdown without front matter")
	void convertMarkdownWithoutFrontMatter() {
		String markdown = """
				# Simple Post

				This is a simple markdown post without front matter.

				Just plain content.
				""";

		Map<String, Object> result = MarkdownToJson.convert(markdown);

		assertThat(result).doesNotContainKey("frontMatter");
		assertThat(result).containsKey("content");
		assertThat(result.get("content"))
			.isEqualTo("# Simple Post\n\nThis is a simple markdown post without front matter.\n\nJust plain content.");
	}

	@Test
	@DisplayName("Convert markdown with empty front matter")
	void convertMarkdownWithEmptyFrontMatter() {
		String markdown = """
				---
				---
				# Post with Empty Front Matter

				Content here.
				""";

		Map<String, Object> result = MarkdownToJson.convert(markdown);

		assertThat(result).doesNotContainKey("frontMatter");
		assertThat(result).containsKey("content");
		assertThat(result.get("content")).asString().contains("# Post with Empty Front Matter");
		assertThat(result.get("content")).asString().contains("Content here.");
	}

	@Test
	@DisplayName("Convert markdown with complex front matter")
	@SuppressWarnings("unchecked")
	void convertMarkdownWithComplexFrontMatter() {
		String markdown = """
				---
				title: "Complex Post"
				date: 2023-12-01
				author:
				  name: John Doe
				  email: john@example.com
				categories: [tech, programming, java]
				featured: true
				rating: 4.5
				metadata:
				  seo_title: "SEO Title"
				  description: "Meta description"
				---
				# Complex Post

				This post has complex front matter structure.
				""";

		Map<String, Object> result = MarkdownToJson.convert(markdown);

		assertThat(result).containsKey("frontMatter");
		assertThat(result).containsKey("content");

		Map<String, Object> frontMatter = (Map<String, Object>) result.get("frontMatter");
		assertThat(frontMatter.get("title")).isEqualTo("Complex Post");
		Map<String, Object> author = (Map<String, Object>) frontMatter.get("author");
		assertThat(author.get("name")).isEqualTo("John Doe");
		assertThat(author.get("email")).isEqualTo("john@example.com");
		assertThat(new SimpleDateFormat("yyyy-MM-dd").format(frontMatter.get("date"))).isEqualTo("2023-12-01");
		assertThat(frontMatter.get("categories")).isEqualTo(List.of("tech", "programming", "java"));
		assertThat(frontMatter.get("featured")).isEqualTo(true);
		assertThat(frontMatter.get("rating")).isEqualTo(4.5);
		Map<String, Object> metadata = (Map<String, Object>) frontMatter.get("metadata");
		assertThat(metadata.get("seo_title")).isEqualTo("SEO Title");
		assertThat(metadata.get("description")).isEqualTo("Meta description");
		assertThat(result.get("content")).isEqualTo("# Complex Post\n\nThis post has complex front matter structure.");
	}

	@Test
	@DisplayName("Convert markdown with special characters")
	void convertMarkdownWithSpecialCharacters() {
		String markdown = """
				---
				title: "Post with \\"quotes\\" and special chars"
				description: 'Single quotes and\nnewlines'
				---
				# Special Characters

				This content has "quotes" and 'apostrophes'.

				Also has backslashes \\ and tabs\there.
				""";

		Map<String, Object> result = MarkdownToJson.convert(markdown);

		assertThat(result).containsKey("frontMatter");
		assertThat(result).containsKey("content");

		@SuppressWarnings("unchecked")
		Map<String, Object> frontMatter = (Map<String, Object>) result.get("frontMatter");
		assertThat(frontMatter.get("title")).isEqualTo("Post with \"quotes\" and special chars");
		assertThat(frontMatter.get("description")).asString().contains("Single quotes and");
		assertThat(result.get("content")).asString().contains("# Special Characters");
		assertThat(result.get("content")).asString().contains("quotes");
		assertThat(result.get("content")).asString().contains("apostrophes");
	}

	@Test
	@DisplayName("Convert empty markdown")
	void convertEmptyMarkdown() {
		String markdown = "";
		Map<String, Object> result = MarkdownToJson.convert(markdown);

		assertThat(result).doesNotContainKey("frontMatter");
		assertThat(result).containsKey("content");
		assertThat(result.get("content")).isEqualTo("");
	}

	@Test
	@DisplayName("Convert front matter only markdown")
	void convertFrontMatterOnly() {
		String markdown = """
				---
				title: Only Front Matter
				status: draft
				---
				""";

		Map<String, Object> result = MarkdownToJson.convert(markdown);

		assertThat(result).containsKey("frontMatter");
		assertThat(result).containsKey("content");

		@SuppressWarnings("unchecked")
		Map<String, Object> frontMatter = (Map<String, Object>) result.get("frontMatter");
		assertThat(frontMatter.get("title")).isEqualTo("Only Front Matter");
		assertThat(frontMatter.get("status")).isEqualTo("draft");
		assertThat(result.get("content")).isEqualTo("");
	}

	@Test
	@DisplayName("Convert front matter with numbers and booleans")
	void convertFrontMatterWithNumbersAndBooleans() {
		String markdown = """
				---
				id: 123
				price: 19.99
				published: true
				featured: false
				count: 0
				---
				Content
				""";

		Map<String, Object> result = MarkdownToJson.convert(markdown);

		assertThat(result).containsKey("frontMatter");
		assertThat(result).containsKey("content");

		@SuppressWarnings("unchecked")
		Map<String, Object> frontMatter = (Map<String, Object>) result.get("frontMatter");
		assertThat(frontMatter.get("id")).isEqualTo(123);
		assertThat(frontMatter.get("price")).isEqualTo(19.99);
		assertThat(frontMatter.get("published")).isEqualTo(true);
		assertThat(frontMatter.get("featured")).isEqualTo(false);
		assertThat(frontMatter.get("count")).isEqualTo(0);
		assertThat(result.get("content")).isEqualTo("Content");
	}

	@Test
	@DisplayName("Convert front matter with arrays")
	void convertFrontMatterWithArrays() {
		String markdown = """
				---
				tags: [java, spring, boot]
				numbers: [1, 2, 3]
				mixed: [hello, 42, true]
				empty: []
				---
				Array content
				""";

		Map<String, Object> result = MarkdownToJson.convert(markdown);

		assertThat(result).containsKey("frontMatter");
		assertThat(result).containsKey("content");

		@SuppressWarnings("unchecked")
		Map<String, Object> frontMatter = (Map<String, Object>) result.get("frontMatter");
		assertThat(frontMatter.get("tags")).isEqualTo(List.of("java", "spring", "boot"));
		assertThat(frontMatter.get("numbers")).isEqualTo(List.of(1, 2, 3));
		assertThat(frontMatter.get("mixed")).isEqualTo(List.of("hello", 42, true));
		assertThat(frontMatter.get("empty")).isEqualTo(List.of());
		assertThat(result.get("content")).isEqualTo("Array content");
	}

	@Test
	@DisplayName("Convert markdown with Windows line endings")
	void convertMarkdownWithWindowsLineEndings() {
		String markdown = "---\r\ntitle: Windows Post\r\n---\r\nWindows content\r\nWith CRLF";

		Map<String, Object> result = MarkdownToJson.convert(markdown);

		assertThat(result).containsKey("frontMatter");
		assertThat(result).containsKey("content");

		@SuppressWarnings("unchecked")
		Map<String, Object> frontMatter = (Map<String, Object>) result.get("frontMatter");
		assertThat(frontMatter.get("title")).isEqualTo("Windows Post");
		assertThat(result.get("content")).asString().contains("Windows content");
		assertThat(result.get("content")).asString().contains("With CRLF");
	}

	@Test
	@DisplayName("Convert markdown with long content")
	void convertMarkdownWithLongContent() {
		StringBuilder contentBuilder = new StringBuilder();
		contentBuilder.append("---\n");
		contentBuilder.append("title: Long Post\n");
		contentBuilder.append("---\n");
		contentBuilder.append("# Long Content\n\n");

		for (int i = 1; i <= 100; i++) {
			contentBuilder.append("This is paragraph ").append(i).append(". ");
			contentBuilder.append("It contains some text to make the content longer.\n\n");
		}

		String markdown = contentBuilder.toString();
		Map<String, Object> result = MarkdownToJson.convert(markdown);

		assertThat(result).containsKey("frontMatter");
		assertThat(result).containsKey("content");

		@SuppressWarnings("unchecked")
		Map<String, Object> frontMatter = (Map<String, Object>) result.get("frontMatter");
		assertThat(frontMatter.get("title")).isEqualTo("Long Post");
		assertThat(result.get("content")).asString().contains("# Long Content");
		assertThat(result.get("content")).asString().contains("This is paragraph 1.");
		assertThat(result.get("content")).asString().contains("This is paragraph 100.");
	}

	@Test
	void convertMarkdownWithDate() {
		String markdown = """
				---
				title: Post with Date
				date: 2023-10-01
				---
				Content with date.
				""";

		Map<String, Object> result = MarkdownToJson.convert(markdown);

		System.out.println(result);

	}

}