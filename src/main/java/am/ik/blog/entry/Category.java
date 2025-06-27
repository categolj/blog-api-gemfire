package am.ik.blog.entry;

public record Category(String name) {

	// Used for deserialization in Jackson
	public static Category valueOf(String category) {
		return new Category(category);
	}
}
