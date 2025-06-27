package am.ik.blog.entry;

import am.ik.pagination.CursorPage;
import am.ik.pagination.CursorPageRequest;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

public interface EntryRepository {

	Optional<Entry> findById(EntryKey entryKey);

	List<Entry> findAll(List<EntryKey> entryKeys);

	CursorPage<Entry, Instant> findOrderByUpdated(@Nullable String tenantId, SearchCriteria searchCriteria,
			CursorPageRequest<Instant> pageRequest);

	List<List<Category>> findAllCategories(@Nullable String tenantId);

	List<TagAndCount> findAllTags(@Nullable String tenantId);

	Entry save(Entry entry);

	Long nextId(@Nullable String tenantId);

	void saveAll(Entry... entries);

	void saveAll(List<Entry> entries);

	void deleteById(EntryKey entryKey);

	void deleteAll();

}
