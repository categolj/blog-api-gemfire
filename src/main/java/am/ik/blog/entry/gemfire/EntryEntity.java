package am.ik.blog.entry.gemfire;

import am.ik.blog.entry.Author;
import am.ik.blog.entry.AuthorBuilder;
import am.ik.blog.entry.Category;
import am.ik.blog.entry.Entry;
import am.ik.blog.entry.EntryBuilder;
import am.ik.blog.entry.EntryKey;
import am.ik.blog.entry.FrontMatter;
import am.ik.blog.entry.FrontMatterBuilder;
import am.ik.blog.entry.Tag;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jilt.Builder;

@Builder(toBuilder = "from")
public class EntryEntity {

	private String entryKey;

	private String title;

	private List<String> categories;

	private String joinedCategories;

	private Set<String> tags;

	private Set<String> tagWithVersions;

	private String content;

	private String createdBy;

	private long createdAt;

	private String updatedBy;

	private long updatedAt;

	private String tenantId;

	public EntryEntity() {
	}

	private static final String delimiter = "|";

	public static EntryEntity fromModel(Entry entry) {
		FrontMatter frontMatter = entry.frontMatter();
		Author created = entry.created();
		return new EntryEntity(toGemfireKey(entry.entryKey()), frontMatter.title(),
				// toList() throws `class java.util.ImmutableCollections$ListN are not
				// compatible with non-java PDX.`
				frontMatter.categories().stream().map(Category::name).collect(Collectors.toList()),
				String.join(delimiter, frontMatter.categories().stream().map(Category::name).toList()),
				frontMatter.tags().stream().map(Tag::name).collect(Collectors.toCollection(LinkedHashSet::new)),
				frontMatter.tags()
					.stream()
					.filter(tag -> tag.version() != null)
					.map(tag -> String.join(delimiter, tag.name(), tag.version()))
					.collect(Collectors.toCollection(LinkedHashSet::new)),
				entry.content(), created.name(), toDate(created), entry.updated().name(), toDate(entry.updated()),
				entry.entryKey().tenantId());
	}

	private static long toDate(Author author) {
		if (author.date() != null) {
			return author.date().toEpochMilli();
		}
		return 0;
	}

	static String toGemfireKey(EntryKey entryKey) {
		return entryKey.toString();
	}

	public Entry toModel() {
		List<Tag> mergedTags = new ArrayList<>();
		Map<String, Tag> tagVersions = tagWithVersions.stream().map(tag -> {
			List<String> strings = Arrays.asList(tag.split(Pattern.quote(delimiter)));
			if (strings.size() != 2) {
				throw new IllegalStateException("Invalid tag format: " + tag);
			}
			return new Tag(strings.get(0), strings.get(1));
		}).collect(Collectors.toMap(Tag::name, Function.identity()));
		for (String tag : tags) {
			if (tagVersions.containsKey(tag)) {
				mergedTags.add(tagVersions.get(tag));
			}
			else {
				mergedTags.add(new Tag(tag));
			}
		}
		return EntryBuilder.entry()
			.entryKey(EntryKey.valueOf(entryKey))
			.frontMatter(FrontMatterBuilder.frontMatter()
				.title(title)
				.categories(categories.stream().map(Category::new).toList())
				.tags(mergedTags)
				.build())
			.content(content)
			.created(AuthorBuilder.author().name(createdBy).date(Instant.ofEpochMilli(createdAt)).build())
			.updated(AuthorBuilder.author().name(updatedBy).date(Instant.ofEpochMilli(updatedAt)).build())
			.build();
	}

	public EntryEntity(String entryKey, String title, List<String> categories, String joinedCategories,
			Set<String> tags, Set<String> tagWithVersions, String content, String createdBy, long createdAt,
			String updatedBy, long updatedAt, String tenantId) {
		this.entryKey = entryKey;
		this.title = title;
		this.categories = categories;
		this.joinedCategories = joinedCategories;
		this.tags = tags;
		this.tagWithVersions = tagWithVersions;
		this.content = content;
		this.createdBy = createdBy;
		this.createdAt = createdAt;
		this.updatedBy = updatedBy;
		this.updatedAt = updatedAt;
		this.tenantId = tenantId;
	}

	public String getEntryKey() {
		return entryKey;
	}

	public void setEntryKey(String entryKey) {
		this.entryKey = entryKey;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<String> getCategories() {
		return categories;
	}

	public void setCategories(List<String> categories) {
		this.categories = categories;
	}

	public String getJoinedCategories() {
		return joinedCategories;
	}

	public void setJoinedCategories(String joinedCategories) {
		this.joinedCategories = joinedCategories;
	}

	public Set<String> getTags() {
		return tags;
	}

	public void setTags(Set<String> tags) {
		this.tags = tags;
	}

	public Set<String> getTagWithVersions() {
		return tagWithVersions;
	}

	public void setTagWithVersions(Set<String> tagWithVersions) {
		this.tagWithVersions = tagWithVersions;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public long getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(long createdAt) {
		this.createdAt = createdAt;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public long getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(long updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof EntryEntity entry))
			return false;
		return createdAt == entry.createdAt && updatedAt == entry.updatedAt && Objects.equals(entryKey, entry.entryKey)
				&& Objects.equals(title, entry.title) && Objects.equals(categories, entry.categories)
				&& Objects.equals(tags, entry.tags) && Objects.equals(tagWithVersions, entry.tagWithVersions)
				&& Objects.equals(content, entry.content) && Objects.equals(createdBy, entry.createdBy)
				&& Objects.equals(updatedBy, entry.updatedBy);
	}

	@Override
	public int hashCode() {
		return Objects.hash(entryKey, title, categories, tags, tagWithVersions, content, createdBy, createdAt,
				updatedBy, updatedAt);
	}

	@Override
	public String toString() {
		return "EntryEntity{" + "entryKey='" + entryKey + '\'' + ", title='" + title + '\'' + ", categories="
				+ categories + ", tags=" + tags + ", tagWithVersions=" + tagWithVersions + ", content='" + content
				+ '\'' + ", createdBy='" + createdBy + '\'' + ", createdAt=" + createdAt + ", updatedBy='" + updatedBy
				+ '\'' + ", updatedAt=" + updatedAt + '}';
	}

}
