package am.ik.blog.entry.web;

import am.ik.blog.entry.Author;
import am.ik.blog.entry.AuthorBuilder;
import am.ik.blog.entry.AuthorizedEntryService;
import am.ik.blog.entry.Category;
import am.ik.blog.entry.Entry;
import am.ik.blog.entry.EntryKey;
import am.ik.blog.entry.EntryParser;
import am.ik.blog.entry.SearchCriteria;
import am.ik.blog.entry.TagAndCount;
import am.ik.pagination.CursorPage;
import am.ik.pagination.CursorPageRequest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.util.UriComponentsBuilder;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
public class EntryController {

	private final AuthorizedEntryService entryService;

	private final EntryParser entryParser;

	private final Clock clock;

	public EntryController(AuthorizedEntryService entryService, EntryParser entryParser, Clock clock) {
		this.entryService = entryService;
		this.entryParser = entryParser;
		this.clock = clock;
	}

	@GetMapping(path = { "/entries", "/tenants/{tenantId}/entries" })
	public CursorPage<Entry, Instant> getEntries(@PathVariable(required = false) String tenantId,
			@ModelAttribute SearchCriteria criteria, CursorPageRequest<Instant> pageRequest) {
		return this.entryService.findOrderByUpdated(tenantId, criteria, pageRequest);
	}

	@GetMapping(path = { "/entries", "/tenants/{tenantId}/entries" }, params = "entryIds")
	public List<Entry> getEntriesWithIds(@PathVariable(required = false) String tenantId,
			@RequestParam List<Long> entryIds) {
		List<EntryKey> entryKeys = entryIds.stream().map(entryId -> new EntryKey(entryId, tenantId)).toList();
		return this.entryService.findAll(tenantId, entryKeys);
	}

	@GetMapping(path = { "/entries/{entryId:\\d+}", "/tenants/{tenantId}/entries/{entryId:\\d+}" })
	public ResponseEntity<?> getEntry(@PathVariable Long entryId, @PathVariable(required = false) String tenantId,
			WebRequest webRequest) {
		EntryKey entryKey = new EntryKey(entryId, tenantId);
		Optional<Entry> entry = this.entryService.findById(tenantId, entryKey);
		if (entry.isPresent()) {
			return checkNotModified(entry.get(), webRequest, Function.identity(), MediaType.APPLICATION_JSON);
		}
		else {
			return entryNotFound(entryKey);
		}
	}

	@GetMapping(path = { "/entries/{entryId:\\d+}.md", "/tenants/{tenantId}/entries/{entryId:\\d+}.md" })
	public ResponseEntity<?> getEntryAsMarkdown(@PathVariable Long entryId,
			@PathVariable(required = false) String tenantId, WebRequest webRequest) {
		EntryKey entryKey = new EntryKey(entryId, tenantId);
		Optional<Entry> entry = this.entryService.findById(tenantId, entryKey);
		if (entry.isPresent()) {
			return checkNotModified(entry.get(), webRequest, Entry::toMarkdown, MediaType.TEXT_MARKDOWN);
		}
		else {
			return entryNotFound(entryKey);
		}
	}

	@PostMapping(path = { "/entries", "/tenants/{tenantId}/entries" }, consumes = MediaType.TEXT_MARKDOWN_VALUE)
	public ResponseEntity<Entry> postEntryFromMarkdown(@PathVariable(required = false) String tenantId,
			@RequestBody String markdown, @AuthenticationPrincipal UserDetails userDetails,
			UriComponentsBuilder builder) {
		Instant now = this.clock.instant();
		Author created = AuthorBuilder.author().name(userDetails.getUsername()).date(now).build();
		Long entryId = this.entryService.nextId(tenantId);
		EntryKey entryKey = new EntryKey(entryId, tenantId);
		Entry entry = this.entryParser.fromMarkdown(entryKey, markdown, created, created).build();
		Entry saved = this.entryService.save(tenantId, entry);
		String path = tenantId == null ? "/entries/{entryId:\\d+}" : "/tenants/{tenantId}/entries/{entryId:\\d+}";
		return ResponseEntity
			.created(builder.path(path)
				.build(Map.of("entryId", entryId, "tenantId", Objects.requireNonNullElse(tenantId, ""))))
			.body(saved);
	}

	@PutMapping(path = { "/entries/{entryId:\\d+}", "/tenants/{tenantId}/entries/{entryId:\\d+}" },
			consumes = MediaType.TEXT_MARKDOWN_VALUE)
	public ResponseEntity<Entry> putEntryFromMarkdown(@PathVariable Long entryId,
			@PathVariable(required = false) String tenantId, @RequestBody String markdown,
			@AuthenticationPrincipal UserDetails userDetails) {
		EntryKey entryKey = new EntryKey(entryId, tenantId);
		Instant now = this.clock.instant();
		Author updated = AuthorBuilder.author().name(userDetails.getUsername()).date(now).build();
		Author created = this.entryService.findById(tenantId, entryKey).map(Entry::created).orElse(updated);
		Entry entry = this.entryParser.fromMarkdown(entryKey, markdown, created, updated).build();
		Entry saved = this.entryService.save(tenantId, entry);
		return ResponseEntity.ok(saved);
	}

	@DeleteMapping(path = { "/entries/{entryId:\\d+}", "/tenants/{tenantId}/entries/{entryId:\\d+}" })
	public ResponseEntity<Void> deleteEntry(@PathVariable Long entryId,
			@PathVariable(required = false) String tenantId) {
		EntryKey entryKey = new EntryKey(entryId, tenantId);
		this.entryService.deleteById(tenantId, entryKey);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping(path = { "/entries", "/tenants/{tenantId}/entries" })
	public ResponseEntity<Void> deleteAll(@PathVariable(required = false) String tenantId) {
		this.entryService.deleteAll(tenantId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping(path = { "/categories", "/tenants/{tenantId}/categories" })
	public List<List<Category>> getCategories(@PathVariable(required = false) String tenantId) {
		return this.entryService.findAllCategories(tenantId);
	}

	@GetMapping(path = { "/tags", "/tenants/{tenantId}/tags" })
	public List<TagAndCount> getTags(@PathVariable(required = false) String tenantId) {
		return this.entryService.findAllTags(tenantId);
	}

	private <T> ResponseEntity<T> checkNotModified(Entry entry, WebRequest webRequest, Function<Entry, T> mapper,
			MediaType mediaType) {
		Instant updated = entry.updated().date();
		if (updated != null) {
			long lastModified = updated.toEpochMilli();
			if (webRequest.checkNotModified(lastModified)) {
				return null;
			}
		}
		return ResponseEntity.ok()
			.cacheControl(CacheControl.maxAge(Duration.ofHours(1)))
			.contentType(mediaType)
			.body(mapper.apply(entry));
	}

	private ResponseEntity<?> entryNotFound(EntryKey entryKey) {
		return ResponseEntity.status(NOT_FOUND)
			.body(ProblemDetail.forStatusAndDetail(NOT_FOUND, "Entry not found: " + entryKey));
	}

}
