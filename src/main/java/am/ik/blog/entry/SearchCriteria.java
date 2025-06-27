package am.ik.blog.entry;

import java.util.List;

public record SearchCriteria(String query, List<String> categories, String tag) {
}
