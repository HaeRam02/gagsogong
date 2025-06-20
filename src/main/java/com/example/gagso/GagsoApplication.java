package com.example.gagso; // 본인의 패키지 경로에 맞게 수정하세요

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
// 만약 Spring Data JPA 리포지토리 스캔도 완전히 끄고 싶다면 아래 import도 추가
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;

@SpringBootApplication(exclude = {
		DataSourceAutoConfiguration.class,      // DataSource 자동 설정을 제외합니다.
		HibernateJpaAutoConfiguration.class     // Hibernate 및 JPA 자동 설정을 제외합니다.
		// JpaRepositoriesAutoConfiguration.class // Spring Data JPA 리포지토리 자동 설정을 제외합니다. (선택 사항)
})

public class  GagsoApplication {
	public static void main(String[] args) {
		SpringApplication.run(GagsoApplication.class, args);
	}
}
