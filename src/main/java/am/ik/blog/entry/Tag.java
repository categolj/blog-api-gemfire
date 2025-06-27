package am.ik.blog.entry;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jilt.Builder;
import org.jspecify.annotations.Nullable;

@Builder
public record Tag(String name, @Nullable @JsonInclude(JsonInclude.Include.NON_EMPTY) String version) {
	public Tag(String name) {
		this(name, null);
	}
}
