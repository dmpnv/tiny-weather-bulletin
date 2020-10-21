package dp.weather.bulletin.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Collections;
import java.util.List;

@Configuration
public class SwaggerConfig {

    ApiInfo apiInfo() {
        return new ApiInfoBuilder()
            .title("Tiny Weather Bulletin API")
            .description("This is an API of weather bulletin")
            .license("")
            .licenseUrl("http://unlicense.org")
            .termsOfServiceUrl("")
            .version("1.0.0")
            .contact(new Contact("","", ""))
            .build();
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("dp.weather.bulletin.api"))
                .paths(PathSelectors.any())
                .build()
                .directModelSubstitute(java.time.LocalDate.class, java.sql.Date.class)
                .directModelSubstitute(java.time.OffsetDateTime.class, java.util.Date.class)
                .apiInfo(apiInfo())
                .securityContexts(Collections.singletonList(xAuthTokenSecurityContext()))
                .securitySchemes(Collections.singletonList(apiKey()))
                /*.securitySchemes(Collections.singletonList(new BasicAuth("xBasic")))
                .securityContexts(Collections.singletonList(xBasicSecurityContext()))*/;
    }

    private SecurityContext xBasicSecurityContext() {
        return SecurityContext.builder()
                .securityReferences(Collections.singletonList(
                        new SecurityReference("xBasic",
                                new AuthorizationScope[0])))
                .build();
    }

    private SecurityContext xAuthTokenSecurityContext() {
        return SecurityContext.builder()
                .securityReferences(defaultAuth())
                .build();
    }

    private ApiKey apiKey() {
        return new ApiKey("xApiKey", "X-API-KEY", "header");
    }

    List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope
                = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return Collections.singletonList(
                new SecurityReference("xApiKey", authorizationScopes));
    }

}
