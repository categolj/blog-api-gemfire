package am.ik.blog.entry;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public record TagAndCount(@JsonUnwrapped Tag tag, int count) {
}