package am.ik.blog.entry.gemfire;

import am.ik.query.Node;
import am.ik.query.QueryParser;
import am.ik.query.RootNode;
import am.ik.query.TokenNode;
import am.ik.query.TokenType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

final class SearchCriteriaToOql {

	private static final String CONTENT_FIELD = "content.toLowerCase()";

	static QueryAndParams convertQuery(String query, int index) {
		return SearchCriteriaToOql.convertQuery(QueryParser.parseQuery(query), new AtomicInteger(index));
	}

	private static QueryAndParams convertQuery(RootNode root, AtomicInteger index) {
		StringBuilder builder = new StringBuilder();
		List<String> params = new ArrayList<>();
		Iterator<? extends Node> iterator = root.children().iterator();
		while (iterator.hasNext()) {
			Node node = iterator.next();
			if (Objects.requireNonNull(node) instanceof TokenNode(TokenType type, String value)) {
				if (!builder.isEmpty()) {
					if (Objects.requireNonNull(type) == TokenType.OR) {
						builder.append(" OR ");
					}
					else {
						builder.append(" AND ");
					}
				}
				String paramName = "$" + index.getAndIncrement();
				if (type == TokenType.OR) {
					if (iterator.hasNext()) {
						builder.append(CONTENT_FIELD).append(" LIKE ").append(paramName);
						params.add("%" + iterator.next().value() + "%");
					}
				}
				else if (type == TokenType.EXCLUDE) {
					builder.append("NOT (").append(CONTENT_FIELD).append(" LIKE ").append(paramName).append(")");
					params.add("%" + value + "%");
				}
				else {
					builder.append(CONTENT_FIELD).append(" LIKE ").append(paramName);
					params.add("%" + value + "%");
				}
			}
			else if (node instanceof RootNode nest) {
				if (!builder.isEmpty()) {
					builder.append(" AND ");
				}
				QueryAndParams queryAndParams = convertQuery(nest, index);
				builder.append("(").append(queryAndParams.query).append(")");
				params.addAll(queryAndParams.params);
			}
		}
		return new QueryAndParams(builder.toString(), params.stream().map(String::toLowerCase).toList());
	}

	static QueryAndParams convertTag(String tag, int index) {
		return new QueryAndParams("$" + index + " IN tags", List.of(tag));
	}

	static QueryAndParams convertCategories(List<String> categories, int index) {
		List<String> params = new ArrayList<>();
		StringBuilder categoriesQuery = new StringBuilder();
		for (int i = 0; i < categories.size(); i++) {
			String category = categories.get(i);
			if (i > 0) {
				categoriesQuery.append(" AND ");
			}
			categoriesQuery.append("categories[").append(i).append("] = $").append(index + i);
			params.add(category);
		}
		return new QueryAndParams(categoriesQuery.toString(), params);
	}

	record QueryAndParams(String query, List<String> params) {
	}

}
