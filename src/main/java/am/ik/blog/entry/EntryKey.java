package am.ik.blog.entry;

import am.ik.csv.Csv;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import org.jspecify.annotations.Nullable;

public record EntryKey(Long entryId, @Nullable @JsonInclude(JsonInclude.Include.NON_EMPTY) String tenantId) {
	private static final Csv csv = Csv.builder().delimiter("|").build();

	public EntryKey(Long entryId) {
		this(entryId, null);
	}

	@Override
	public String toString() {
		return tenantId == null ? Entry.formatId(entryId) : csv.joinLine(Entry.formatId(entryId), tenantId);
	}

	public static EntryKey valueOf(String value) {
		List<String> strings = csv.splitLine(value);
		try {
			if (strings.size() == 1) {
				return new EntryKey(Long.parseLong(strings.getFirst()), null);
			}
			else if (strings.size() == 2) {
				return new EntryKey(Long.parseLong(strings.get(0)), strings.get(1));
			}
			else {
				throw new IllegalArgumentException("Invalid EntryKey format: " + value);
			}
		}
		catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid EntryKey format: " + value, e);
		}
	}

}
