package am.ik.blog.entry;

import am.ik.blog.security.Authorized;
import am.ik.blog.security.Privilege;
import am.ik.pagination.CursorPage;
import am.ik.pagination.CursorPageRequest;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

@Service
public class AuthorizedEntryService {

	private final EntryRepository entryRepository;

	public AuthorizedEntryService(EntryRepository entryRepository) {
		this.entryRepository = entryRepository;
	}

	@Authorized(resource = "entry", requiredPrivileges = Privilege.GET)
	public Optional<Entry> findById(@Nullable @P("tenantId") String tenantId, EntryKey entryKey) {
		return entryRepository.findById(entryKey);
	}

	@Authorized(resource = "entry", requiredPrivileges = Privilege.LIST)
	public List<Entry> findAll(@Nullable @P("tenantId") String tenantId, List<EntryKey> entryKeys) {
		return entryRepository.findAll(entryKeys);
	}

	@Authorized(resource = "entry", requiredPrivileges = Privilege.LIST)
	public CursorPage<Entry, Instant> findOrderByUpdated(@Nullable @P("tenantId") String tenantId,
			SearchCriteria searchCriteria, CursorPageRequest<Instant> pageRequest) {
		return entryRepository.findOrderByUpdated(tenantId, searchCriteria, pageRequest);
	}

	@Authorized(resource = "entry", requiredPrivileges = Privilege.LIST)
	public List<List<Category>> findAllCategories(@Nullable @P("tenantId") String tenantId) {
		return entryRepository.findAllCategories(tenantId);
	}

	@Authorized(resource = "entry", requiredPrivileges = Privilege.LIST)
	public List<TagAndCount> findAllTags(@Nullable @P("tenantId") String tenantId) {
		return entryRepository.findAllTags(tenantId);
	}

	@Authorized(resource = "entry", requiredPrivileges = Privilege.EDIT)
	public Entry save(@Nullable @P("tenantId") String tenantId, Entry entry) {
		return entryRepository.save(entry);
	}

	@Authorized(resource = "entry", requiredPrivileges = Privilege.EDIT)
	public Long nextId(@Nullable @P("tenantId") String tenantId) {
		return entryRepository.nextId(tenantId);
	}

	@Authorized(resource = "entry", requiredPrivileges = Privilege.EDIT)
	public void saveAll(@Nullable @P("tenantId") String tenantId, Entry... entries) {
		entryRepository.saveAll(entries);
	}

	@Authorized(resource = "entry", requiredPrivileges = Privilege.EDIT)
	public void saveAll(@Nullable @P("tenantId") String tenantId, List<Entry> entries) {
		entryRepository.saveAll(entries);
	}

	@Authorized(resource = "entry", requiredPrivileges = Privilege.DELETE)
	public void deleteById(@Nullable @P("tenantId") String tenantId, EntryKey entryKey) {
		entryRepository.deleteById(entryKey);
	}

	@Authorized(resource = "entry", requiredPrivileges = Privilege.ADMIN)
	public void deleteAll(@Nullable @P("tenantId") String tenantId) {
		entryRepository.deleteAll();
	}

}
