package com.example.gagso; // 본인의 패키지 경로에 맞게 수정하세요

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
//
 import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;

@SpringBootApplication
public class GagsoApplication {
	public static void main(String[] args) {
		SpringApplication.run(GagsoApplication.class, args);
	}
}

