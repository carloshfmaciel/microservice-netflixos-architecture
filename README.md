# Arquitetura Microservices - Spring Cloud Netflix

Spring Cloud Config Server

Microserviço que centraliza todas as configurações(yml ou properties), de todos os microserviços.

Configurando

1 - adicione as dependência abaixo:

```
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-config-server</artifactId>
</dependency>

<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

Obs: Estamos adicionando o spring security, para que todo e qualquer microserviço que quiser consumir alguma configuração dele, tenha que se autenticar.

2 - Na classe anotada com @SpringBootApplication, adicione a anotação @EnableConfigServer.

```
@EnableConfigServer
@SpringBootApplication
public class ConfigServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConfigServerApplication.class, args);
	}
	
}
```

3 - Abaixo como deve ficar o application yml:

```yml
server:
  port: 8888

spring:
  application:
    name: config-server
  cloud:
    config:
      server:
        git:
          uri: https://github.com/carloshfmaciel/spring-cloud-config
          default-label: develop
          username: username
          password: password
          clone-on-start: true
          delete-untracked-branches: true
          refresh-rate: 600
      fail-fast: true
      
  security:
    user:
      name: config
      password: config
```
