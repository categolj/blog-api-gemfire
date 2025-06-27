package am.ik.blog.entry.web;

import am.ik.blog.MockConfig;
import am.ik.blog.TestcontainersConfiguration;
import am.ik.blog.entry.AuthorBuilder;
import am.ik.blog.entry.Category;
import am.ik.blog.entry.Entry;
import am.ik.blog.entry.EntryBuilder;
import am.ik.blog.entry.EntryKey;
import am.ik.blog.entry.EntryRepository;
import am.ik.blog.entry.FrontMatter;
import am.ik.blog.entry.FrontMatterBuilder;
import am.ik.blog.entry.MockData;
import am.ik.blog.entry.Tag;
import am.ik.blog.entry.TagAndCount;
import am.ik.blog.entry.gemfire.EntryEntity;
import am.ik.blog.mockserver.MockServer;
import am.ik.blog.mockserver.MockServer.Response;
import am.ik.pagination.CursorPage;
import java.net.URI;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import org.apache.geode.cache.Region;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.client.RestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

import static am.ik.blog.entry.MockData.ENTRY1;
import static am.ik.blog.entry.MockData.ENTRY10;
import static am.ik.blog.entry.MockData.ENTRY2;
import static am.ik.blog.entry.MockData.ENTRY3;
import static am.ik.blog.entry.MockData.ENTRY4;
import static am.ik.blog.entry.MockData.ENTRY5;
import static am.ik.blog.entry.MockData.ENTRY6;
import static am.ik.blog.entry.MockData.ENTRY7;
import static am.ik.blog.entry.MockData.ENTRY8;
import static am.ik.blog.entry.MockData.ENTRY9;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@Import({ TestcontainersConfiguration.class, MockConfig.class })
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EntryControllerTest {

	RestClient restClient;

	@Autowired
	EntryRepository entryRepository;

	@Autowired
	MockServer mockServer;

	@Autowired
	@Qualifier("entryRegion")
	Region<String, EntryEntity> entryRegion;

	@LocalServerPort
	int port;

	@BeforeEach
	void setup(@Autowired RestClient.Builder restClientBuilder) {
		this.restClient = restClientBuilder.baseUrl("http://localhost:" + port)
			.defaultStatusHandler(__ -> true, (req, res) -> {
			})
			.build();
		this.mockServer.reset()
			.fallback(Response.builder()
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.status(404)
				.body("""
						{
							"message": "Entry not found"
						}
						""")
				.build());
	}

	@AfterEach
	void tearDown() {
		this.entryRepository.deleteAll();
	}

	static Entry withTenantId(Entry entry, String tenantId) {
		if (tenantId == null) {
			return entry;
		}
		return EntryBuilder.from(entry).entryKey(new EntryKey(entry.entryId(), tenantId)).build();
	}

	static Entry withTenantIdAndEmptyContent(Entry entry, String tenantId) {
		EntryBuilder builder = EntryBuilder.from(entry).content("");
		if (tenantId == null) {
			return builder.build();
		}
		return builder.entryKey(new EntryKey(entry.entryId(), tenantId)).build();
	}

	void prepareMockData(String tenantId) {
		if (tenantId == null) {
			this.entryRepository.saveAll(MockData.ALL_ENTRIES);
		}
		else {
			this.entryRepository
				.saveAll(MockData.ALL_ENTRIES.stream().map(entry -> withTenantId(entry, tenantId)).toList());
		}
	}

	@ParameterizedTest
	@CsvSource({ "/entries", "/tenants/t1/entries" })
	void getEntries(String path) {
		String tenantId = path.startsWith("/tenants/") ? path.split("/")[2] : null;
		prepareMockData(tenantId);
		Instant lastUpdatedDate;
		{
			// first batch
			var response = this.restClient.get()
				.uri(path, uriBuilder -> uriBuilder.queryParam("size", 4).build())
				.retrieve()
				.toEntity(new ParameterizedTypeReference<CursorPage<Entry, Instant>>() {
				});
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			CursorPage<Entry, Instant> page = response.getBody();
			assertThat(page).isNotNull();
			assertThat(page.size()).isEqualTo(4);
			assertThat(page.hasPrevious()).isFalse();
			assertThat(page.hasNext()).isTrue();
			assertThat(page.content()).containsExactly(withTenantIdAndEmptyContent(ENTRY10, tenantId),
					withTenantIdAndEmptyContent(ENTRY9, tenantId), withTenantIdAndEmptyContent(ENTRY8, tenantId),
					withTenantIdAndEmptyContent(ENTRY7, tenantId));
			lastUpdatedDate = ENTRY7.updated().date();
		}
		{
			// second batch
			Instant cursor = lastUpdatedDate;
			var response = this.restClient.get()
				.uri(path, uriBuilder -> uriBuilder.queryParam("size", 4).queryParam("cursor", cursor).build())
				.retrieve()
				.toEntity(new ParameterizedTypeReference<CursorPage<Entry, Instant>>() {
				});
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			CursorPage<Entry, Instant> page = response.getBody();
			assertThat(page).isNotNull();
			assertThat(page.size()).isEqualTo(4);
			assertThat(page.hasPrevious()).isTrue();
			assertThat(page.hasNext()).isTrue();
			assertThat(page.content()).containsExactly(withTenantIdAndEmptyContent(ENTRY6, tenantId),
					withTenantIdAndEmptyContent(ENTRY5, tenantId), withTenantIdAndEmptyContent(ENTRY4, tenantId),
					withTenantIdAndEmptyContent(ENTRY3, tenantId));
			lastUpdatedDate = ENTRY3.updated().date();
		}
		{
			// third batch
			Instant cursor = lastUpdatedDate;
			var response = this.restClient.get()
				.uri(path, uriBuilder -> uriBuilder.queryParam("size", 4).queryParam("cursor", cursor).build())
				.retrieve()
				.toEntity(new ParameterizedTypeReference<CursorPage<Entry, Instant>>() {
				});
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			CursorPage<Entry, Instant> page = response.getBody();
			assertThat(page).isNotNull();
			assertThat(page.size()).isEqualTo(4);
			assertThat(page.hasPrevious()).isTrue();
			assertThat(page.hasNext()).isFalse();
			assertThat(page.content()).containsExactly(withTenantIdAndEmptyContent(ENTRY2, tenantId),
					withTenantIdAndEmptyContent(ENTRY1, tenantId));
		}
	}

	@ParameterizedTest
	@CsvSource({ "/entries", "/tenants/t1/entries" })
	void getEntriesWithQuery(String path) {
		String tenantId = path.startsWith("/tenants/") ? path.split("/")[2] : null;
		prepareMockData(tenantId);
		var response = this.restClient.get()
			.uri(path, uriBuilder -> uriBuilder.queryParam("query", "Learn").build())
			.retrieve()
			.toEntity(new ParameterizedTypeReference<CursorPage<Entry, Instant>>() {
			});
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		CursorPage<Entry, Instant> page = response.getBody();
		assertThat(page).isNotNull();
		assertThat(page.size()).isEqualTo(20);
		assertThat(page.hasPrevious()).isFalse();
		assertThat(page.hasNext()).isFalse();
		assertThat(page.content()).containsExactly(withTenantIdAndEmptyContent(ENTRY6, tenantId),
				withTenantIdAndEmptyContent(ENTRY5, tenantId));
	}

	@ParameterizedTest
	@CsvSource({ "/entries", "/tenants/t1/entries" })
	void getEntriesWithAndQuery(String path) {
		String tenantId = path.startsWith("/tenants/") ? path.split("/")[2] : null;
		prepareMockData(tenantId);
		var response = this.restClient.get()
			.uri(path, uriBuilder -> uriBuilder.queryParam("query", "Learn python").build())
			.retrieve()
			.toEntity(new ParameterizedTypeReference<CursorPage<Entry, Instant>>() {
			});
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		CursorPage<Entry, Instant> page = response.getBody();
		assertThat(page).isNotNull();
		assertThat(page.size()).isEqualTo(20);
		assertThat(page.hasPrevious()).isFalse();
		assertThat(page.hasNext()).isFalse();
		assertThat(page.content()).containsExactly(withTenantIdAndEmptyContent(ENTRY6, tenantId));
	}

	@ParameterizedTest
	@CsvSource({ "/entries", "/tenants/t1/entries" })
	void getEntriesWithNotQuery(String path) {
		String tenantId = path.startsWith("/tenants/") ? path.split("/")[2] : null;
		prepareMockData(tenantId);
		var response = this.restClient.get()
			.uri(path, uriBuilder -> uriBuilder.queryParam("query", "Learn -python").build())
			.retrieve()
			.toEntity(new ParameterizedTypeReference<CursorPage<Entry, Instant>>() {
			});
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		CursorPage<Entry, Instant> page = response.getBody();
		assertThat(page).isNotNull();
		assertThat(page.size()).isEqualTo(20);
		assertThat(page.hasPrevious()).isFalse();
		assertThat(page.hasNext()).isFalse();
		assertThat(page.content()).containsExactly(withTenantIdAndEmptyContent(ENTRY5, tenantId));
	}

	@ParameterizedTest
	@CsvSource({ "/entries", "/tenants/t1/entries" })
	void getEntriesWithOrQuery(String path) {
		String tenantId = path.startsWith("/tenants/") ? path.split("/")[2] : null;
		prepareMockData(tenantId);
		var response = this.restClient.get()
			.uri(path, uriBuilder -> uriBuilder.queryParam("query", "Spring OR React").build())
			.retrieve()
			.toEntity(new ParameterizedTypeReference<CursorPage<Entry, Instant>>() {
			});
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		CursorPage<Entry, Instant> page = response.getBody();
		assertThat(page).isNotNull();
		assertThat(page.size()).isEqualTo(20);
		assertThat(page.hasPrevious()).isFalse();
		assertThat(page.hasNext()).isFalse();
		assertThat(page.content()).containsExactly(withTenantIdAndEmptyContent(ENTRY4, tenantId),
				withTenantIdAndEmptyContent(ENTRY1, tenantId));
	}

	@ParameterizedTest
	@CsvSource({ "/entries", "/tenants/t1/entries" })
	void getEntriesWithTag(String path) {
		String tenantId = path.startsWith("/tenants/") ? path.split("/")[2] : null;
		prepareMockData(tenantId);
		var response = this.restClient.get()
			.uri(path, uriBuilder -> uriBuilder.queryParam("tag", "rest-api").build())
			.retrieve()
			.toEntity(new ParameterizedTypeReference<CursorPage<Entry, Instant>>() {
			});
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		CursorPage<Entry, Instant> page = response.getBody();
		assertThat(page).isNotNull();
		assertThat(page.size()).isEqualTo(20);
		assertThat(page.hasPrevious()).isFalse();
		assertThat(page.hasNext()).isFalse();
		assertThat(page.content()).containsExactly(withTenantIdAndEmptyContent(ENTRY3, tenantId),
				withTenantIdAndEmptyContent(ENTRY1, tenantId));
	}

	@ParameterizedTest
	@CsvSource({ "/entries", "/tenants/t1/entries" })
	void getEntriesWithCategories(String path) {
		String tenantId = path.startsWith("/tenants/") ? path.split("/")[2] : null;
		prepareMockData(tenantId);
		var response = this.restClient.get()
			.uri(path, uriBuilder -> uriBuilder.queryParam("categories", "Programming,JavaScript").build())
			.retrieve()
			.toEntity(new ParameterizedTypeReference<CursorPage<Entry, Instant>>() {
			});
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		CursorPage<Entry, Instant> page = response.getBody();
		assertThat(page).isNotNull();
		assertThat(page.size()).isEqualTo(20);
		assertThat(page.hasPrevious()).isFalse();
		assertThat(page.hasNext()).isFalse();
		assertThat(page.content()).containsExactly(withTenantIdAndEmptyContent(ENTRY4, tenantId),
				withTenantIdAndEmptyContent(ENTRY3, tenantId));
	}

	@ParameterizedTest
	@CsvSource({ "/entries", "/tenants/t1/entries" })
	void getEntriesWithAllCriteria(String path) {
		String tenantId = path.startsWith("/tenants/") ? path.split("/")[2] : null;
		prepareMockData(tenantId);
		var response = this.restClient.get()
			.uri(path,
					uriBuilder -> uriBuilder.queryParam("query", "Express")
						.queryParam("tag", "rest-api")
						.queryParam("categories", "Programming,JavaScript")
						.build())
			.retrieve()
			.toEntity(new ParameterizedTypeReference<CursorPage<Entry, Instant>>() {
			});
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		CursorPage<Entry, Instant> page = response.getBody();
		assertThat(page).isNotNull();
		assertThat(page.size()).isEqualTo(20);
		assertThat(page.hasPrevious()).isFalse();
		assertThat(page.hasNext()).isFalse();
		assertThat(page.content()).containsExactly(withTenantIdAndEmptyContent(ENTRY3, tenantId));
	}

	@ParameterizedTest
	@CsvSource({ "/entries", "/tenants/t1/entries" })
	void getEntriesEmpty(String path) {
		var response = this.restClient.get()
			.uri(path, uriBuilder -> uriBuilder.queryParam("size", 4).build())
			.retrieve()
			.toEntity(new ParameterizedTypeReference<CursorPage<Entry, Instant>>() {
			});
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		CursorPage<Entry, Instant> page = response.getBody();
		assertThat(page).isNotNull();
		assertThat(page.size()).isEqualTo(4);
		assertThat(page.hasPrevious()).isFalse();
		assertThat(page.hasNext()).isFalse();
		assertThat(page.content()).isEmpty();
	}

	@ParameterizedTest
	@CsvSource({ "/entries", "/tenants/t1/entries" })
	void getEntriesWithIds(String path) {
		String tenantId = path.startsWith("/tenants/") ? path.split("/")[2] : null;
		prepareMockData(tenantId);
		var response = this.restClient.get()
			.uri(path,
					uriBuilder -> uriBuilder
						.queryParam("entryIds", List.of(ENTRY1.entryId(), ENTRY5.entryId(), ENTRY3.entryId()))
						.build())
			.retrieve()
			.toEntity(new ParameterizedTypeReference<List<Entry>>() {
			});
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody()).containsExactly(withTenantId(ENTRY1, tenantId), withTenantId(ENTRY3, tenantId),
				withTenantId(ENTRY5, tenantId));
	}

	@ParameterizedTest
	@CsvSource({ "/entries", "/tenants/t1/entries" })
	void getEntriesWithIdsWithMissingEntries(String path) {
		String tenantId = path.startsWith("/tenants/") ? path.split("/")[2] : null;
		prepareMockData(tenantId);
		var response = this.restClient.get()
			.uri(path,
					uriBuilder -> uriBuilder.queryParam("entryIds", List.of(ENTRY1.entryId(), ENTRY5.entryId(), 999L))
						.build())
			.retrieve()
			.toEntity(new ParameterizedTypeReference<List<Entry>>() {
			});
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody()).containsExactly(withTenantId(ENTRY1, tenantId), withTenantId(ENTRY5, tenantId));
	}

	@ParameterizedTest
	@CsvSource({ "/entries", "/tenants/t1/entries" })
	void getEntriesWithIdsWithEmptyEntries(String path) {
		var response = this.restClient.get()
			.uri(path,
					uriBuilder -> uriBuilder
						.queryParam("entryIds", List.of(ENTRY1.entryId(), ENTRY5.entryId(), ENTRY3.entryId()))
						.build())
			.retrieve()
			.toEntity(new ParameterizedTypeReference<List<Entry>>() {
			});
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody()).isEmpty();
	}

	@ParameterizedTest
	@CsvSource({ "/entries/{entryId}", "/tenants/t1/entries/{entryId}" })
	void getEntry(String path) {
		String tenantId = path.startsWith("/tenants/") ? path.split("/")[2] : null;
		prepareMockData(tenantId);
		Entry entry1 = withTenantId(ENTRY1, tenantId);
		var response = this.restClient.get().uri(path, entry1.entryId()).retrieve().toEntity(Entry.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEqualTo(entry1);
	}

	@ParameterizedTest
	@CsvSource({ "/entries/{entryId}", "/tenants/t1/entries/{entryId}" })
	void getEntryNotFound(String path) {
		String tenantId = path.startsWith("/tenants/") ? path.split("/")[2] : null;
		long nonExistentId = 99999L;
		var response = this.restClient.get().uri(path, nonExistentId).retrieve().toEntity(ProblemDetail.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getDetail())
			.isEqualTo("Entry not found: " + new EntryKey(nonExistentId, tenantId));
	}

	@ParameterizedTest
	@CsvSource({ "/entries/{entryId},public/blog", "/tenants/t1/entries/{entryId},private/blog" })
	void getEntryCacheAside(String path, String repo) {
		String tenantId = path.startsWith("/tenants/") ? path.split("/")[2] : null;
		long entryId = 200L;
		Entry entry = EntryBuilder.entry()
			.entryKey(new EntryKey(entryId, tenantId))
			.content("""
					# Cache Aside Entry
					This is a cache aside entry.
					""".trim())
			.frontMatter(FrontMatterBuilder.frontMatter()
				.title("Cache Aside Entry")
				.categories(List.of(new Category("Programming"), new Category("Java")))
				.tags(List.of(new Tag("cache"), new Tag("aside")))
				.build())
			.created(AuthorBuilder.author().build())
			.updated(AuthorBuilder.author().build())
			.build();
		this.mockServer.GET("/repos/%s/contents/content/00200.md".formatted(repo),
				req -> Response.builder().contentType(MediaType.APPLICATION_JSON_VALUE).status(200).body("""
						{"content":"%s"}
						""".formatted(Base64.getEncoder().encodeToString(entry.toMarkdown().getBytes(UTF_8)))).build())
			.route(req -> "/repos/%s/commits".formatted(repo).equals(req.path())
					&& "content/00200.md".equals(req.queryParam("path")),
					req -> Response.builder()
						.contentType(MediaType.APPLICATION_JSON_VALUE)
						.status(200)
						.body("""
								[{"commit":{"author":{"name":"Test User2","date":"2025-06-27T15:55:20Z"}}},{"commit":{"author":{"name":"Test User1","date":"2025-06-27T15:45:58Z"}}}]
								""")
						.build());
		var response = this.restClient.get().uri(path, entryId).retrieve().toEntity(Entry.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		Entry expected = EntryBuilder.from(entry)
			.created(AuthorBuilder.author().name("Test User1").date(Instant.parse("2025-06-27T15:45:58Z")).build())
			.updated(AuthorBuilder.author().name("Test User2").date(Instant.parse("2025-06-27T15:55:20Z")).build())
			.build();
		assertThat(response.getBody()).isEqualTo(expected);
		assertThat(this.entryRegion.get(new EntryKey(entryId, tenantId).toString()))
			.isEqualTo(EntryEntity.fromModel(expected));
	}

	@ParameterizedTest
	@CsvSource({ "/entries/{entryId},public/blog", "/tenants/t1/entries/{entryId},private/blog" })
	void getEntryCacheAsideWithDateOverwritten(String path, String repo) {
		String tenantId = path.startsWith("/tenants/") ? path.split("/")[2] : null;
		long entryId = 200L;
		Entry entry = EntryBuilder.entry()
			.entryKey(new EntryKey(entryId, tenantId))
			.content("""
					# Cache Aside Entry
					This is a cache aside entry.
					""".trim())
			.frontMatter(FrontMatterBuilder.frontMatter()
				.title("Cache Aside Entry")
				.categories(List.of(new Category("Programming"), new Category("Java")))
				.tags(List.of(new Tag("cache"), new Tag("aside")))
				.build())
			.created(AuthorBuilder.author().date(Instant.parse("2025-06-01T00:00:00Z")).build())
			.updated(AuthorBuilder.author().build())
			.build();
		this.mockServer.GET("/repos/%s/contents/content/00200.md".formatted(repo),
				req -> Response.builder().contentType(MediaType.APPLICATION_JSON_VALUE).status(200).body("""
						{"content":"%s"}
						""".formatted(Base64.getEncoder().encodeToString(entry.toMarkdown().getBytes(UTF_8)))).build())
			.route(req -> "/repos/%s/commits".formatted(repo).equals(req.path())
					&& "content/00200.md".equals(req.queryParam("path")),
					req -> Response.builder()
						.contentType(MediaType.APPLICATION_JSON_VALUE)
						.status(200)
						.body("""
								[{"commit":{"author":{"name":"Test User2","date":"2025-06-27T15:55:20Z"}}},{"commit":{"author":{"name":"Test User1","date":"2025-06-27T15:45:58Z"}}}]
								""")
						.build());
		var response = this.restClient.get().uri(path, entryId).retrieve().toEntity(Entry.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		Entry expected = EntryBuilder.from(entry)
			.created(AuthorBuilder.author().name("Test User1").date(Instant.parse("2025-06-01T00:00:00Z")).build())
			.updated(AuthorBuilder.author().name("Test User2").date(Instant.parse("2025-06-27T15:55:20Z")).build())
			.build();
		assertThat(response.getBody()).isEqualTo(expected);
		assertThat(this.entryRegion.get(new EntryKey(entryId, tenantId).toString()))
			.isEqualTo(EntryEntity.fromModel(expected));
	}

	@ParameterizedTest
	@CsvSource({ "/entries/{entryId},public/blog", "/tenants/t1/entries/{entryId},private/blog" })
	void getEntryCacheAsideWithUpdatedOverwritten(String path, String repo) {
		String tenantId = path.startsWith("/tenants/") ? path.split("/")[2] : null;
		long entryId = 200L;
		Entry entry = EntryBuilder.entry()
			.entryKey(new EntryKey(entryId, tenantId))
			.content("""
					# Cache Aside Entry
					This is a cache aside entry.
					""".trim())
			.frontMatter(FrontMatterBuilder.frontMatter()
				.title("Cache Aside Entry")
				.categories(List.of(new Category("Programming"), new Category("Java")))
				.tags(List.of(new Tag("cache"), new Tag("aside")))
				.build())
			.created(AuthorBuilder.author().build())
			.updated(AuthorBuilder.author().date(Instant.parse("2025-06-28T00:00:00Z")).build())
			.build();
		this.mockServer.GET("/repos/%s/contents/content/00200.md".formatted(repo),
				req -> Response.builder().contentType(MediaType.APPLICATION_JSON_VALUE).status(200).body("""
						{"content":"%s"}
						""".formatted(Base64.getEncoder().encodeToString(entry.toMarkdown().getBytes(UTF_8)))).build())
			.route(req -> "/repos/%s/commits".formatted(repo).equals(req.path())
					&& "content/00200.md".equals(req.queryParam("path")),
					req -> Response.builder()
						.contentType(MediaType.APPLICATION_JSON_VALUE)
						.status(200)
						.body("""
								[{"commit":{"author":{"name":"Test User2","date":"2025-06-27T15:55:20Z"}}},{"commit":{"author":{"name":"Test User1","date":"2025-06-27T15:45:58Z"}}}]
								""")
						.build());
		var response = this.restClient.get().uri(path, entryId).retrieve().toEntity(Entry.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		Entry expected = EntryBuilder.from(entry)
			.created(AuthorBuilder.author().name("Test User1").date(Instant.parse("2025-06-27T15:45:58Z")).build())
			.updated(AuthorBuilder.author().name("Test User2").date(Instant.parse("2025-06-28T00:00:00Z")).build())
			.build();
		assertThat(response.getBody()).isEqualTo(expected);
		assertThat(this.entryRegion.get(new EntryKey(entryId, tenantId).toString()))
			.isEqualTo(EntryEntity.fromModel(expected));
	}

	@ParameterizedTest
	@CsvSource({ "/entries/{entryId}.md", "/tenants/t1/entries/{entryId}.md" })
	void getEntryAsMarkdown(String path) {
		String tenantId = path.startsWith("/tenants/") ? path.split("/")[2] : null;
		prepareMockData(tenantId);
		Entry entry1 = withTenantId(ENTRY1, tenantId);
		var response = this.restClient.get().uri(path, entry1.entryId()).retrieve().toEntity(String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEqualToIgnoringNewLines(
				"""
						---
						title: Getting Started with Spring Boot
						tags: ["spring-boot", "tutorial", "rest-api"]
						categories: ["Programming", "Spring", "Java"]
						date: %s
						updated: %s
						---

						# Getting Started with Spring Boot

						Spring Boot makes it easy to create stand-alone, production-grade Spring-based applications.
						This tutorial covers the basics of setting up a new Spring Boot project and creating your first REST API.

						## Prerequisites

						- Java 17 or later
						- Maven 3.6 or later
						- IDE of your choice

						## Creating a New Project

						You can create a new Spring Boot project using Spring Initializr...
						"""
					.formatted(entry1.created().date(), entry1.updated().date()));
	}

	@ParameterizedTest
	@CsvSource({ "/entries/{entryId}.md", "/tenants/t1/entries/{entryId}.md" })
	void getEntryAsMarkdownNotFound(String path) {
		String tenantId = path.startsWith("/tenants/") ? path.split("/")[2] : null;
		long nonExistentId = 99999L;
		var response = this.restClient.get().uri(path, nonExistentId).retrieve().toEntity(ProblemDetail.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody().getDetail())
			.isEqualTo("Entry not found: " + new EntryKey(nonExistentId, tenantId));
	}

	@ParameterizedTest
	@CsvSource({ "/entries", "/tenants/t1/entries" })
	void postEntryFromMarkdown(String path) {
		String tenantId = path.startsWith("/tenants/") ? path.split("/")[2] : null;
		prepareMockData(tenantId);
		var response = this.restClient.post().uri(path).contentType(MediaType.TEXT_MARKDOWN).body("""
				---
				title: Sample Entry Title
				categories: ["c1", "c2", "c3"]
				tags: ["t1", "t2", "t3"]
				---
				Sample Entry
				Content
				""").retrieve().toEntity(Entry.class);
		long nextId = MockData.ALL_ENTRIES.getLast().entryId() + 1;
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getHeaders().getLocation())
			.isEqualTo(URI.create("http://localhost:%d%s/%d".formatted(port, path, nextId)));
		Entry entry = response.getBody();
		assertThat(entry).isNotNull();
		assertThat(entry.entryKey()).isEqualTo(new EntryKey(nextId, tenantId));
		assertThat(entry.content()).isEqualToIgnoringNewLines("""
				Sample Entry
				Content
				""");
		FrontMatter frontMatter = entry.frontMatter();
		assertThat(frontMatter).isNotNull();
		assertThat(frontMatter.title()).isEqualToIgnoringNewLines("Sample Entry Title");
		assertThat(frontMatter.categories()).extracting("name").containsExactly("c1", "c2", "c3");
		assertThat(frontMatter.tags()).extracting("name").containsExactly("t1", "t2", "t3");
		assertThat(this.entryRegion.get(new EntryKey(nextId, tenantId).toString()))
			.isEqualTo(EntryEntity.fromModel(entry));
	}

	@ParameterizedTest
	@CsvSource({ "/entries/{entryId}", "/tenants/t1/entries/{entryId}" })
	void putEntryFromMarkdown(String path) {
		String tenantId = path.startsWith("/tenants/") ? path.split("/")[2] : null;
		prepareMockData(tenantId);
		Entry entry1 = withTenantId(ENTRY1, tenantId);
		var response = this.restClient.put().uri(path, entry1.entryId()).contentType(MediaType.TEXT_MARKDOWN).body("""
				---
				title: Updated Entry Title
				categories: ["updated1", "updated2"]
				tags: ["tag1", "tag2"]
				---
				Updated Entry
				Content
				""").retrieve().toEntity(Entry.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		Entry entry = response.getBody();
		assertThat(entry).isNotNull();
		assertThat(entry.entryKey()).isEqualTo(entry1.entryKey());
		assertThat(entry.content()).isEqualToIgnoringNewLines("""
				Updated Entry
				Content
				""");
		FrontMatter frontMatter = entry.frontMatter();
		assertThat(frontMatter).isNotNull();
		assertThat(frontMatter.title()).isEqualToIgnoringNewLines("Updated Entry Title");
		assertThat(frontMatter.categories()).extracting("name").containsExactly("updated1", "updated2");
		assertThat(frontMatter.tags()).extracting("name").containsExactly("tag1", "tag2");
		assertThat(this.entryRegion.get(entry1.entryKey().toString())).isEqualTo(EntryEntity.fromModel(entry));
	}

	@ParameterizedTest
	@CsvSource({ "/entries/{entryId}", "/tenants/t1/entries/{entryId}" })
	void deleteEntry(String path) {
		String tenantId = path.startsWith("/tenants/") ? path.split("/")[2] : null;
		prepareMockData(tenantId);
		Entry entry1 = withTenantId(ENTRY1, tenantId);
		assertThat(this.entryRegion.containsKeyOnServer(entry1.entryKey().toString())).isTrue();
		assertThat(this.entryRepository.findById(entry1.entryKey())).isPresent();
		var response = this.restClient.delete().uri(path, entry1.entryId()).retrieve().toBodilessEntity();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		assertThat(this.entryRegion.containsKeyOnServer(entry1.entryKey().toString())).isFalse();
	}

	@ParameterizedTest
	@CsvSource({ "/categories", "/tenants/t1/categories" })
	void getCategories(String path) {
		String tenantId = path.startsWith("/tenants/") ? path.split("/")[2] : null;
		prepareMockData(tenantId);
		var response = this.restClient.get()
			.uri(path)
			.retrieve()
			.toEntity(new ParameterizedTypeReference<List<List<Category>>>() {
			});
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).containsExactly(
				List.of(new Category("Architecture"), new Category("Microservices"), new Category("Scalability")),
				List.of(new Category("Cloud Computing"), new Category("AWS"), new Category("DevOps")),
				List.of(new Category("Data Science"), new Category("Machine Learning"), new Category("Python")),
				List.of(new Category("Database"), new Category("Architecture")),
				List.of(new Category("DevOps"), new Category("Containerization")),
				List.of(new Category("DevOps"), new Category("Version Control")),
				List.of(new Category("Programming"), new Category("JavaScript"), new Category("Backend")),
				List.of(new Category("Programming"), new Category("JavaScript"), new Category("Frontend")),
				List.of(new Category("Programming"), new Category("Spring"), new Category("Java")),
				List.of(new Category("Security"), new Category("Programming")));
	}

	@ParameterizedTest
	@CsvSource({ "/tags", "/tenants/t1/tags" })
	void getTags(String path) {
		String tenantId = path.startsWith("/tenants/") ? path.split("/")[2] : null;
		prepareMockData(tenantId);
		var response = this.restClient.get()
			.uri(path)
			.retrieve()
			.toEntity(new ParameterizedTypeReference<List<TagAndCount>>() {
			});
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).containsExactly(new TagAndCount(new Tag("architecture"), 1),
				new TagAndCount(new Tag("aws"), 1), new TagAndCount(new Tag("cloud"), 1),
				new TagAndCount(new Tag("containers"), 1), new TagAndCount(new Tag("cybersecurity"), 1),
				new TagAndCount(new Tag("data-science"), 1), new TagAndCount(new Tag("database"), 1),
				new TagAndCount(new Tag("deployment"), 1), new TagAndCount(new Tag("design"), 1),
				new TagAndCount(new Tag("docker"), 1), new TagAndCount(new Tag("express"), 1),
				new TagAndCount(new Tag("frontend"), 1), new TagAndCount(new Tag("git"), 1),
				new TagAndCount(new Tag("hooks"), 1), new TagAndCount(new Tag("machine-learning"), 1),
				new TagAndCount(new Tag("microservices"), 1), new TagAndCount(new Tag("nodejs"), 1),
				new TagAndCount(new Tag("owasp"), 1), new TagAndCount(new Tag("python"), 1),
				new TagAndCount(new Tag("react"), 1), new TagAndCount(new Tag("rest-api"), 2),
				new TagAndCount(new Tag("scalability"), 1), new TagAndCount(new Tag("security"), 1),
				new TagAndCount(new Tag("serverless"), 1), new TagAndCount(new Tag("spring-boot"), 1),
				new TagAndCount(new Tag("sql"), 1), new TagAndCount(new Tag("tutorial"), 1),
				new TagAndCount(new Tag("version-control"), 1), new TagAndCount(new Tag("workflow"), 1));
	}

}