# Arquitetura Microservices - Spring Cloud Netflix

# Spring Cloud Config Server

Microserviço que centraliza todas as configurações(yml ou properties), de todos os microserviços.

## Configurando


1.  adicione as dependência abaixo:

```xml
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


2.  Na classe anotada com **@SpringBootApplication** , adicione a anotação **@EnableConfigServer**.

```java
@EnableConfigServer
@SpringBootApplication
public class ConfigServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConfigServerApplication.class, args);
	}
	
}
```


3.  Abaixo como deve ficar o application yml:

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
          uri: https://gitlab.com/s4bdigital/devops/microservices-config.git
          default-label: master
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

## Executando


1.  Na raiz do project config-server, build o projeto:
```
mvn clean install
```

2.  Execute o projeto:
```
java -jar target\config-server-1.0.0-SNAPSHOT.jar --server.port=8888
java -jar target\config-server-1.0.0-SNAPSHOT.jar --server.port=8889
```
Observação: No arquivo yml deixamos definido na propriedade port, a porta 8888. Estamos definindo a porta no start, pois estamos considerando que subiremos duas instâncias na mesma máquina. Caso esteja em máquinas diferentes, não é necessário passar usar o parâmetro --server.port.


## Testando o Config Server

Abaixo, o repositório com os arquivos de configuração que serão acessados pelo config-server:

![image](https://gitlab.com/s4bdigital/sites-team/kanban/uploads/a80038f1666964a9f07e85cae841c79a/git_config_files.PNG)


Accesse a url do config-server, seguido do nome de um dos arquivos presentes no repositório:
```
http://localhost:8888/order-service-pre.yml
```
