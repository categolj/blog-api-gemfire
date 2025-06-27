package am.ik.blog.markdown;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Converts markdown content with YAML front matter to a JSON-like map structure.
 */
public class MarkdownToJson {

	/**
	 * Converts markdown content to a map containing front matter and content.
	 * @param markdown the markdown content with optional YAML front matter
	 * @return a map with 'frontMatter' and 'content' keys
	 */
	public static Map<String, Object> convert(String markdown) {
		YamlFrontMatterParser.ParseResult parseResult = YamlFrontMatterParser.parse(markdown);

		Map<String, Object> jsonObject = new LinkedHashMap<>();

		Map<String, Object> frontMatter = parseResult.getFrontMatter();
		if (!frontMatter.isEmpty()) {
			jsonObject.put("frontMatter", frontMatter);
		}
		jsonObject.put("content", parseResult.getContent());

		return jsonObject;
	}

}