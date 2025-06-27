package am.ik.blog;

import org.springframework.boot.SpringApplication;

public class TestBlogApiGemfireApplication {

	public static void main(String[] args) {
		SpringApplication.from(BlogApiGemfireApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
