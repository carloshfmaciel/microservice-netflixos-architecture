# Arquitetura Microservices - Spring Cloud Netflix

# Spring Cloud Config Server

Microserviço que centraliza todas as configurações(yml ou properties), de todos os microserviços.

### Configurando


1.  Adicione as dependência abaixo:

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

### Executando


1.  Na raiz do project config-server, build o projeto:
```
mvn clean install
```

2.  Execute o projeto (No exemplo abaixo, para simularmos alta disponibilidade, estaremos startando duas instâncias):
```
java -jar target\config-server-1.0.0-SNAPSHOT.jar --server.port=8888
java -jar target\config-server-1.0.0-SNAPSHOT.jar --server.port=8889
```
Observação: No arquivo yml deixamos definido na propriedade port, a porta 8888. Estamos definindo a porta no start, pois estamos considerando que subiremos duas instâncias na mesma máquina. Caso esteja em máquinas diferentes, não é necessário passar usar o parâmetro --server.port.


### Testando o Config Server

Abaixo, o repositório com os arquivos de configuração que serão acessados pelo config-server:

![image](https://gitlab.com/s4bdigital/sites-team/kanban/uploads/a80038f1666964a9f07e85cae841c79a/git_config_files.PNG)



1.  Accesse a url do config-server, seguido do nome de um dos arquivos presentes no repositório, acrescido do profile, no caso "-pre":
```
http://localhost:8888/order-service-pre.yml
```


2.  Como adicionamos o spring security no config-server, será solicitado login e senha para acessar. Digite usuario: config e senha: config, conforme definido no yml do config-server.

![image](https://gitlab.com/s4bdigital/sites-team/kanban/uploads/f39c49f23dbbc1e661d9e2024e77ab39/login_config_server.PNG)


3.  Pronto! Conseguimos através de uma requisição GET, acessar o conteúdo do arquivo order-service.yml

![image](https://gitlab.com/s4bdigital/sites-team/kanban/uploads/a0bce95a7738bf6b30688b6540adc1e0/resultado_config_server.PNG)


# Spring Cloud Netflix Eureka - Service Registry / Service Discovery

Eureka implementa os patterns service registry/service discovery.

Em uma arquitetura básica, temos um ou mais servidores para registro, ou seja, onde os microserviços irão se registrar.

### Configurando Eureka Server

1.  Adicione as dependência abaixo:

```xml
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-config</artifactId>
</dependency>
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

2.  Na classe anotada com **@SpringBootApplication** , adicione a anotação **@EnableEurekaServer**.

```java
@EnableEurekaServer
@SpringBootApplication
public class EurekaServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EurekaServiceApplication.class, args);
	}

}
```


3.  A configuração do eureka, ficará no repositório git que será acessado e servido pelo config server anteriormente configurado. 
Ao invés de application.yml o arquivo deve possuir o nome da aplicação(server.application.name), no caso aqui será eureka-server.yml

Conteúdo do arquivo:

```yml
server:
  port: 8761
spring:
  application:
    name: eureka-server-pre1
  profiles: pre1
  security:
    user:
      name: eureka
      password: eureka
eureka:
  client:
    register-with-eureka: true
    fetch-registry: false
    service-url:
      defaultZone: http://eureka:eureka@127.0.0.1:8761/eureka,http://eureka:eureka@127.0.0.1:8762/eureka,http://eureka:eureka@127.0.0.1:8763/eureka
    healthcheck:
      enabled: true
  instance:
    instance-id: ${spring.application.name}:${spring.application.instance_id:${random.value}}
      
---

server:
  port: 8762
spring:
  application:
    name: eureka-server-pre2
  profiles: pre2
  security:
    user:
      name: eureka
      password: eureka
eureka:
  client:
    register-with-eureka: true
    fetch-registry: false
    service-url:
      defaultZone: http://eureka:eureka@127.0.0.1:8761/eureka,http://eureka:eureka@127.0.0.1:8762/eureka,http://eureka:eureka@127.0.0.1:8763/eureka
    healthcheck:
      enabled: true
  instance:
    instance-id: ${spring.application.name}:${spring.application.instance_id:${random.value}}

```
Obs: Como estamos rodando tudo no mesmo host, para evitar conflito de portas, temos dois profiles, simplesmente para subir com duas portas diferentes. Se estivéssemos rodando em hosts diferentes, poderíamos trabalhar apenas com um profile.


4.  Dentro do projeto, teremos apenas um arquivo bootstrap.yml, que deverá ter o nome da aplicação(spring.application.name) e as configurações para acessar o config-server e obter sua configuração.
Abaixo, arquivo boostrap.yml:

```yml
spring:
  application:
    name: eureka-server
  cloud:
    config:
      uri: http://localhost:8888,http://localhost:8889
      username: config
      password: config
      fail-fast: true
```