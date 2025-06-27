package am.ik.blog.entry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jilt.Builder;

@Builder(toBuilder = "from")
public record FrontMatter(String title, List<Category> categories, List<Tag> tags) {

	public static String DATE_FIELD = "date";

	public static String UPDATE_FIELD = "updated";

	public FrontMatter(String title, List<Category> categories, List<Tag> tags) {
		this.title = title;
		this.categories = Objects.requireNonNullElseGet(categories, ArrayList::new);
		this.tags = Objects.requireNonNullElseGet(tags, ArrayList::new);
	}
}
