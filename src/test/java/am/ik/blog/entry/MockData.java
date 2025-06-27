package am.ik.blog.entry;

import java.time.Instant;
import java.util.List;

public class MockData {

	private static final Instant NOW = Instant.ofEpochMilli(Instant.now().toEpochMilli());

	public static final Entry ENTRY1 = EntryBuilder.entry()
		.entryKey(new EntryKey(1L))
		.content(
				"""
						# Getting Started with Spring Boot

						Spring Boot makes it easy to create stand-alone, production-grade Spring-based applications.
						This tutorial covers the basics of setting up a new Spring Boot project and creating your first REST API.

						## Prerequisites

						- Java 17 or later
						- Maven 3.6 or later
						- IDE of your choice

						## Creating a New Project

						You can create a new Spring Boot project using Spring Initializr...
						""")
		.frontMatter(FrontMatterBuilder.frontMatter()
			.title("Getting Started with Spring Boot")
			.categories(List.of(new Category("Programming"), new Category("Spring"), new Category("Java")))
			.tags(List.of(TagBuilder.tag().name("spring-boot").version("3.5").build(),
					TagBuilder.tag().name("tutorial").build(), TagBuilder.tag().name("rest-api").build()))
			.build())
		.created(AuthorBuilder.author().name("John Doe").date(NOW).build())
		.updated(AuthorBuilder.author().name("John Doe").date(NOW).build())
		.build();

	public static final Entry ENTRY2 = EntryBuilder.entry()
		.entryKey(new EntryKey(2L))
		.content("""
				# Introduction to Docker for Developers

				Docker has revolutionized how we develop, ship, and run applications.
				This comprehensive guide will walk you through Docker fundamentals and practical examples.

				## What is Docker?

				Docker is a containerization platform that allows you to package applications
				with their dependencies into lightweight, portable containers.

				## Getting Started

				First, install Docker Desktop from the official website...
				""")
		.frontMatter(FrontMatterBuilder.frontMatter()
			.title("Introduction to Docker for Developers")
			.categories(List.of(new Category("DevOps"), new Category("Containerization")))
			.tags(List.of(TagBuilder.tag().name("docker").build(), TagBuilder.tag().name("containers").build(),
					TagBuilder.tag().name("deployment").build()))
			.build())
		.created(AuthorBuilder.author().name("Jane Smith").date(NOW.plusSeconds(10)).build())
		.updated(AuthorBuilder.author().name("Jane Smith").date(NOW.plusSeconds(10)).build())
		.build();

	public static final Entry ENTRY3 = EntryBuilder.entry()
		.entryKey(new EntryKey(3L))
		.content("""
				# Building RESTful APIs with Node.js and Express

				Node.js and Express.js provide a powerful combination for building scalable REST APIs.
				This tutorial covers best practices and common patterns.

				## Setting Up the Project

				Start by creating a new Node.js project:

				```bash
				npm init -y
				npm install express
				```

				## Creating Your First Route

				Let's create a simple HTTP server with Express...
				""")
		.frontMatter(FrontMatterBuilder.frontMatter()
			.title("Building RESTful APIs with Node.js and Express")
			.categories(List.of(new Category("Programming"), new Category("JavaScript"), new Category("Backend")))
			.tags(List.of(TagBuilder.tag().name("nodejs").version("18").build(),
					TagBuilder.tag().name("express").build(), TagBuilder.tag().name("rest-api").build()))
			.build())
		.created(AuthorBuilder.author().name("Michael Johnson").date(NOW.plusSeconds(20)).build())
		.updated(AuthorBuilder.author().name("Michael Johnson").date(NOW.plusSeconds(20)).build())
		.build();

	public static final Entry ENTRY4 = EntryBuilder.entry()
		.entryKey(new EntryKey(4L))
		.content("""
				# Understanding React Hooks: A Complete Guide

				React Hooks have transformed how we write React components.
				This guide covers all the essential hooks and when to use them.

				## What are Hooks?

				Hooks are functions that let you use state and other React features in functional components.

				## useState Hook

				The most commonly used hook for managing component state:

				```javascript
				const [count, setCount] = useState(0);
				```
				""")
		.frontMatter(FrontMatterBuilder.frontMatter()
			.title("Understanding React Hooks: A Complete Guide")
			.categories(List.of(new Category("Programming"), new Category("JavaScript"), new Category("Frontend")))
			.tags(List.of(TagBuilder.tag().name("react").build(), TagBuilder.tag().name("hooks").build(),
					TagBuilder.tag().name("frontend").build()))
			.build())
		.created(AuthorBuilder.author().name("Sarah Wilson").date(NOW.plusSeconds(30)).build())
		.updated(AuthorBuilder.author().name("Sarah Wilson").date(NOW.plusSeconds(30)).build())
		.build();

	public static final Entry ENTRY5 = EntryBuilder.entry()
		.entryKey(new EntryKey(5L))
		.content("""
				# Database Design Best Practices

				Designing efficient and scalable databases is crucial for application performance.
				This article covers essential principles and patterns.

				## Normalization

				Normalization helps eliminate data redundancy and ensures data integrity.
				Learn about the different normal forms and when to apply them.

				## Indexing Strategies

				Proper indexing can dramatically improve query performance...
				""")
		.frontMatter(FrontMatterBuilder.frontMatter()
			.title("Database Design Best Practices")
			.categories(List.of(new Category("Database"), new Category("Architecture")))
			.tags(List.of(TagBuilder.tag().name("database").build(), TagBuilder.tag().name("sql").build(),
					TagBuilder.tag().name("design").build()))
			.build())
		.created(AuthorBuilder.author().name("David Chen").date(NOW.plusSeconds(40)).build())
		.updated(AuthorBuilder.author().name("David Chen").date(NOW.plusSeconds(40)).build())
		.build();

	public static final Entry ENTRY6 = EntryBuilder.entry()
		.entryKey(new EntryKey(6L))
		.content("""
				# Introduction to Machine Learning with Python

				Machine learning is transforming industries worldwide.
				This beginner-friendly guide introduces core concepts using Python and scikit-learn.

				## What is Machine Learning?

				Machine learning is a subset of artificial intelligence that enables computers
				to learn from data without explicit programming.

				## Setting Up Your Environment

				Install the required packages:

				```bash
				pip install numpy pandas scikit-learn matplotlib
				```
				""")
		.frontMatter(FrontMatterBuilder.frontMatter()
			.title("Introduction to Machine Learning with Python")
			.categories(List.of(new Category("Data Science"), new Category("Machine Learning"), new Category("Python")))
			.tags(List.of(TagBuilder.tag().name("machine-learning").build(), TagBuilder.tag().name("python").build(),
					TagBuilder.tag().name("data-science").build()))
			.build())
		.created(AuthorBuilder.author().name("Lisa Rodriguez").date(NOW.plusSeconds(50)).build())
		.updated(AuthorBuilder.author().name("Lisa Rodriguez").date(NOW.plusSeconds(50)).build())
		.build();

	public static final Entry ENTRY7 = EntryBuilder.entry()
		.entryKey(new EntryKey(7L))
		.content("""
				# Mastering Git: Advanced Techniques for Developers

				Git is an essential tool for every developer.
				This advanced guide covers techniques that will improve your workflow and collaboration.

				## Interactive Rebase

				Rebase allows you to rewrite commit history:

				```bash
				git rebase -i HEAD~3
				```

				## Cherry-picking Commits

				Sometimes you need to apply specific commits to different branches...
				""")
		.frontMatter(FrontMatterBuilder.frontMatter()
			.title("Mastering Git: Advanced Techniques for Developers")
			.categories(List.of(new Category("DevOps"), new Category("Version Control")))
			.tags(List.of(TagBuilder.tag().name("git").build(), TagBuilder.tag().name("version-control").build(),
					TagBuilder.tag().name("workflow").build()))
			.build())
		.created(AuthorBuilder.author().name("Tom Anderson").date(NOW.plusSeconds(60)).build())
		.updated(AuthorBuilder.author().name("Tom Anderson").date(NOW.plusSeconds(60)).build())
		.build();

	public static final Entry ENTRY8 = EntryBuilder.entry()
		.entryKey(new EntryKey(8L))
		.content("""
				# Building Scalable Microservices Architecture

				Microservices architecture enables organizations to build and deploy applications at scale.
				This guide covers design principles and implementation strategies.

				## Microservices vs Monoliths

				Understand the trade-offs between different architectural approaches.

				## Service Communication

				Choose the right communication patterns for your services...
				""")
		.frontMatter(FrontMatterBuilder.frontMatter()
			.title("Building Scalable Microservices Architecture")
			.categories(
					List.of(new Category("Architecture"), new Category("Microservices"), new Category("Scalability")))
			.tags(List.of(TagBuilder.tag().name("microservices").build(), TagBuilder.tag().name("architecture").build(),
					TagBuilder.tag().name("scalability").build()))
			.build())
		.created(AuthorBuilder.author().name("Emily Davis").date(NOW.plusSeconds(70)).build())
		.updated(AuthorBuilder.author().name("Emily Davis").date(NOW.plusSeconds(70)).build())
		.build();

	public static final Entry ENTRY9 = EntryBuilder.entry()
		.entryKey(new EntryKey(9L))
		.content("""
				# Cybersecurity Fundamentals for Developers

				Security should be a top priority in every application.
				This guide covers essential security practices and common vulnerabilities.

				## The OWASP Top 10

				Understand the most critical security risks:

				1. Injection attacks
				2. Broken authentication
				3. Sensitive data exposure

				## Secure Coding Practices

				Implement security from the ground up...
				""")
		.frontMatter(FrontMatterBuilder.frontMatter()
			.title("Cybersecurity Fundamentals for Developers")
			.categories(List.of(new Category("Security"), new Category("Programming")))
			.tags(List.of(TagBuilder.tag().name("security").build(), TagBuilder.tag().name("cybersecurity").build(),
					TagBuilder.tag().name("owasp").build()))
			.build())
		.created(AuthorBuilder.author().name("Robert Kim").date(NOW.plusSeconds(80)).build())
		.updated(AuthorBuilder.author().name("Robert Kim").date(NOW.plusSeconds(80)).build())
		.build();

	public static final Entry ENTRY10 = EntryBuilder.entry()
		.entryKey(new EntryKey(10L))
		.content("""
				# Cloud Computing with AWS: A Practical Guide

				Amazon Web Services (AWS) is the leading cloud platform.
				This hands-on guide covers essential services and best practices.

				## Getting Started with AWS

				Create your AWS account and set up the CLI:

				```bash
				aws configure
				```

				## Essential AWS Services

				- EC2: Virtual servers in the cloud
				- S3: Object storage service
				- RDS: Managed database service
				- Lambda: Serverless computing
				""")
		.frontMatter(FrontMatterBuilder.frontMatter()
			.title("Cloud Computing with AWS: A Practical Guide")
			.categories(List.of(new Category("Cloud Computing"), new Category("AWS"), new Category("DevOps")))
			.tags(List.of(TagBuilder.tag().name("aws").build(), TagBuilder.tag().name("cloud").build(),
					TagBuilder.tag().name("serverless").build()))
			.build())
		.created(AuthorBuilder.author().name("Amanda Taylor").date(NOW.plusSeconds(90)).build())
		.updated(AuthorBuilder.author().name("Amanda Taylor").date(NOW.plusSeconds(90)).build())
		.build();

	public static final List<Entry> ALL_ENTRIES = List.of(ENTRY1, ENTRY2, ENTRY3, ENTRY4, ENTRY5, ENTRY6, ENTRY7,
			ENTRY8, ENTRY9, ENTRY10);

}