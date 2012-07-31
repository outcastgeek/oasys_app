package com.outcastgeek.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@ImportResource("classpath*:META-INF/spring/*.xml")
@Configuration
public class JavaConfig {

	public static ApplicationContext getContext() {
		return new AnnotationConfigApplicationContext(JavaConfig.class);
	}
}
