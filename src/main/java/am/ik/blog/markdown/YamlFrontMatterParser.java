package am.ik.blog.markdown;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.yaml.snakeyaml.Yaml;

/**
 * Parses markdown content with YAML front matter.
 */
class YamlFrontMatterParser {

	private static final Pattern FRONTMATTER_PATTERN = Pattern.compile("^---\\r?\\n(.*?)\\r?\\n---\\r?\\n(.*?)$",
			Pattern.DOTALL);

	/**
	 * Result of parsing markdown with YAML front matter.
	 */
	public static class ParseResult {

		private final Map<String, Object> frontMatter;

		private final String content;

		public ParseResult(Map<String, Object> frontMatter, String content) {
			this.frontMatter = frontMatter;
			this.content = content;
		}

		public Map<String, Object> getFrontMatter() {
			return frontMatter;
		}

		public String getContent() {
			return content;
		}

	}

	/**
	 * Parses markdown content and extracts YAML front matter.
	 * @param markdown the markdown content with optional YAML front matter
	 * @return parse result containing front matter and content
	 */
	public static ParseResult parse(String markdown) {
		Matcher matcher = FRONTMATTER_PATTERN.matcher(markdown);

		if (matcher.matches()) {
			String yamlContent = matcher.group(1);
			String markdownContent = matcher.group(2);

			// Normalize line endings and trim
			markdownContent = markdownContent.replaceAll("\\r\\n", "\\n").trim();

			Map<String, Object> frontMatter = parseYaml(yamlContent);
			return new ParseResult(frontMatter, markdownContent);
		}
		else {
			return new ParseResult(new HashMap<>(), markdown.trim());
		}
	}

	private static Map<String, Object> parseYaml(String yaml) {
		return new Yaml().load(yaml);
	}

}