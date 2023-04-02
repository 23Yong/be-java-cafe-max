package kr.codesqaud.cafe.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
public class MvcConfig implements WebMvcConfigurer {

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
		registry.addViewController("/user/form").setViewName("user/form");
		registry.addViewController("/user/login").setViewName("user/login");
		registry.addViewController("/question/form").setViewName("qna/form");
	}
}