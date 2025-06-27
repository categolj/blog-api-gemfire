package am.ik.blog.entry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.annotation.Nullable;
import java.time.Instant;
import org.jilt.Builder;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

@Builder
public record Author(String name, @Nullable Instant date) {
	@JsonIgnore
	public String rfc1123DateTime() {
		if (this.date == null) {
			return "";
		}
		return RFC_1123_DATE_TIME.format(this.date);
	}

	public Author withDate(Instant date) {
		return new Author(this.name, date);
	}
}
