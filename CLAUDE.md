# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this
repository.

## Build and Development Commands

This is a Spring Boot 3.x application using Maven with Java 21.
Code formatting is enforced using the Spring Java Format plugin.

**Build Commands:**

```bash
./mvnw clean spring-javaformat:apply package                    # Build application
./mvnw spring-boot:run                                          # Run application locally
./mvnw spring-javaformat:apply test                             # Run all tests
```

## Architecture Overview

### Core Structure

- **Package**: `am.ik.blog` - Main application package
- **Domain**: `am.ik.blog.entry` - Blog entry domain model and repository
- **Config**: `am.ik.blog.config` - GemFire and application configuration
- **Markdown**: `am.ik.blog.markdown` - Markdown processing with YAML front matter
- **Web**: `am.ik.blog.entry.web` - REST API controllers

### Domain Model

- **Entry**: Main blog entry entity with ID, title, content, categories, tags, frontMatter
- **Author**: Author information with name, URL, GitHub
- **Category/Tag**: Simple categorization entities
- **FrontMatter**: YAML metadata container

## Key Dependencies and Patterns

### Technology Stack

- **Spring Boot 3.5**
- **VMware GemFire 10.1**
- **Java 21**
- **Jilt** for builder pattern generation

### Design Patterns

- **Repository Pattern**: `EntryRepository` with dual-source strategy (cache + external API)
- **Builder Pattern**: Generated via `@Builder` annotation from Jilt
- **Record Classes**: Used for configuration properties (`GemfireProps`)
- **Cache-Aside**: GemFire acts as cache with fallback to external API

## Development Requirements

### Prerequisites

- Java 21 runtime

### Code Standards

- Spring Java Format enforced via Maven plugin
- All code must pass formatting validation before commit
- PDX serialization classes must be registered in `GemfireConfig`

### Testing Strategy

- JUnit 5 with AssertJ
- Integration tests using Testcontainers and embedded Tomcat

### After Task completion

- Ensure all code is formatted using `./mvnw spring-javaformat:apply`
- For each task, notify that the task is complete and ready for review by the following command:

```
osascript -e 'display notification "<Message Body>" with title "<Message Title>"’
```