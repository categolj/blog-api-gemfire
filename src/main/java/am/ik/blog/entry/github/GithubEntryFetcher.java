package am.ik.blog.entry.github;

import am.ik.blog.entry.Author;
import am.ik.blog.entry.Entry;
import am.ik.blog.entry.EntryFetcher;
import am.ik.blog.entry.EntryKey;
import am.ik.blog.entry.EntryParser;
import am.ik.blog.github.Commit;
import am.ik.blog.github.CommitParameter;
import am.ik.blog.github.File;
import am.ik.blog.github.GitCommitter;
import am.ik.blog.github.GitHubClient;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class GithubEntryFetcher implements EntryFetcher {

	private final EntryParser entryParser;

	private final GitHubClient gitHubClient;

	public final Map<String, GitHubClient> tenantsGitHubClient;

	public GithubEntryFetcher(EntryParser entryParser, GitHubClient gitHubClient,
			Map<String, GitHubClient> tenantsGitHubClient) {
		this.entryParser = entryParser;
		this.gitHubClient = gitHubClient;
		this.tenantsGitHubClient = tenantsGitHubClient;
	}

	@Override
	public Optional<Entry> fetch(@Nullable String tenantId, String owner, String repo, String path) {
		GitHubClient gitHubClient;
		if (tenantId == null) {
			gitHubClient = this.gitHubClient;
		}
		else {
			gitHubClient = this.tenantsGitHubClient.getOrDefault(tenantId, this.gitHubClient);
		}
		Long entryId = Entry.parseId(Paths.get(path).getFileName().toString());
		EntryKey entryKey = new EntryKey(entryId, tenantId);
		ResponseEntity<File> fileResponse = gitHubClient.getFile(owner, repo, path);
		if (!fileResponse.getStatusCode().is2xxSuccessful() || fileResponse.getBody() == null) {
			return Optional.empty();
		}
		File file = fileResponse.getBody();
		List<Commit> commits = gitHubClient.getCommits(owner, repo, new CommitParameter().path(path).queryParams());
		Author created = commits.isEmpty() ? null : toAuthor(commits.getLast());
		Author updated = commits.isEmpty() ? null : toAuthor(commits.getFirst());
		return Optional.of(this.entryParser.fromMarkdown(entryKey, file.decode(), created, updated).build());
	}

	private Author toAuthor(Commit commit) {
		GitCommitter committer = commit.commit().author();
		return new Author(committer.name(), committer.date());
	}

}