package am.ik.blog.entry;

import java.util.Optional;
import org.jspecify.annotations.Nullable;

public interface EntryFetcher {

	Optional<Entry> fetch(@Nullable String tenantId, String owner, String repo, String path);

}
