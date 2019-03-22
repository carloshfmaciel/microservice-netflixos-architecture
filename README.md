# Arquitetura Microservices - Spring Cloud Netflix

# Spring Cloud Config Server

Microserviço que centraliza todas as configurações(yml ou properties), de todos os microserviços.

[Link para baixar o projeto](https://gitlab.com/s4bdigital/devops/microservices-poc.git)

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



1.  Accesse a url do config-server, seguido do nome de um dos arquivos presentes no repositório, acrescido do profile, no caso "/pre":
```
http://localhost:8888/order-service/pre
```


2.  Como adicionamos o spring security no config-server, será solicitado login e senha para acessar. Digite usuario: config e senha: config, conforme definido no yml do config-server.

![image](https://gitlab.com/s4bdigital/sites-team/kanban/uploads/f39c49f23dbbc1e661d9e2024e77ab39/login_config_server.PNG)


3.  Pronto! Conseguimos através de uma requisição GET, acessar o conteúdo do arquivo order-service.yml

![image](https://gitlab.com/s4bdigital/sites-team/kanban/uploads/a0bce95a7738bf6b30688b6540adc1e0/resultado_config_server.PNG)


# Spring Cloud Netflix Eureka - Service Registry / Service Discovery

Eureka implementa os patterns service registry/service discovery.

Em uma arquitetura básica, temos um ou mais servidores para registro, ou seja, onde os microserviços irão se registrar.

[Link para baixar o projeto](https://gitlab.com/s4bdigital/devops/microservices-poc.git)

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
Abaixo, arquivo bootstrap.yml:


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
*Como aqui estamos simulando alta disponibilidade, temos dois config-servers setados. Caso o primeiro falhe, ele busca a configuração no segundo. Também informamos usuário e senha definidos no config-server para o microserviço se autenticar.*

### Executando

1.  Na raiz do project eureka-server, build o projeto:
```
mvn clean install
```

2.  Execute o projeto (No exemplo abaixo, para simularmos alta disponibilidade, estaremos startando duas instâncias. Cada instância subirá em uma porta.):
```
java -jar -Dspring.profiles.active=pre1 target\eureka-server-1.0.0-SNAPSHOT.jar 
java -jar -Dspring.profiles.active=pre2 target\eureka-server-1.0.0-SNAPSHOT.jar 
```

### Testando

1.  Como definimos nos profiles, no arquivo eureka-server.yml, as portas 8761 e 8762, basta acessarmos no browser:
```
http://localhost:8761
http://localhost:8762
```

3.  Pronto! Percebemos que ambos os servers subiram e se auto registraram um no outro. Dessa forma, quando algum serviço se autoregistrar em alguma das instâncias eureka, o registro será replicado para todas as instâncias eureka.

![image](https://gitlab.com/s4bdigital/sites-team/kanban/uploads/e4fa16fb6d554d6d55c991e33d777981/eureka_screen.PNG)


# Spring Cloud Netflix Zuul - Api Gateway

Zuul implementa o pattern de API-Gateway. Funciona como um centralizador, interceptando as requisições e encaminhando as mesmas para os microserviços. Funciona integrado com o Eureka(Service Registry/Service Discovery), como também com o Ribbon(Load Balancer). 
Possui filtros implementáveis, com os quais podemos interceptar as requisições antes e depois do routing, possibilitando aplicar recursos de segurança e coleta de métricas.

[Link para baixar o projeto](https://gitlab.com/s4bdigital/devops/microservices-poc.git)

### Configurando

1.  Em projeto específico para api-gateway, que será um microserviço, adicione as dependência abaixo:

```xml
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-netflix-zuul</artifactId>
</dependency>
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-config</artifactId>
</dependency>
```

2.  Na classe anotada com **@SpringBootApplication** , adicione a anotação **@EnableZuulProxy**.

```java
@EnableZuulProxy
@SpringBootApplication
public class ApiGatewayApplication{

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

}
```

3.  Assim como o eureka, a configuração da api-gatway ficará no repositório git que será acessado e servido pelo config server anteriormente configurado. 
Ao invés de application.yml o arquivo deve possuir o nome da aplicação(server.application.name), no caso aqui será api-gateway.yml

Perceba que toda requisição que vier **\*/api/order/\*** será redirecionada para o microserviço registrado no eureka como **order-service-pre**.
Para que os redirecionamentos feito aos microserviços pelo zuul funcione em modo load balancer, habilitamos o **Ribbon**, através da propriedade **ribbon.eureka.enabled=true**.

```yml
server:
  port: 8080

spring:
  application:
    name: api-gateway-pre1
  profiles: pre1
eureka:
  instance:
    prefer-ip-address: true
    instance-id: ${spring.application.name}:${spring.application.instance_id:${random.value}}
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://eureka:eureka@127.0.0.1:8761/eureka,http://eureka:eureka@127.0.0.1:8762/eureka,http://eureka:eureka@127.0.0.1:8763/eureka
zuul:
  prefix: /api
  routes:
    order:
      path: /order/**
      serviceId: order-service-pre
    delivery:
      path: /delivery/**
      serviceId: delivery-service-pre
ribbon:
  eureka:
    enabled: true

---

server:
  port: 8081
spring:
  application:
    name: api-gateway-pre1
  profiles: pre2
eureka:
  instance:
    prefer-ip-address: true
    instance-id: ${spring.application.name}:${spring.application.instance_id:${random.value}}
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://eureka:eureka@127.0.0.1:8761/eureka,http://eureka:eureka@127.0.0.1:8762/eureka,http://eureka:eureka@127.0.0.1:8763/eureka
zuul:
  prefix: /api
  routes:
    order:
      path: /order/**
      serviceId: order-service-pre
    delivery:
      path: /delivery/**
      serviceId: delivery-service-pre
ribbon:
  eureka:
    enabled: true
```
*Obs: Como estamos rodando tudo no mesmo host, para evitar conflito de portas, temos dois profiles, simplesmente para subir com duas portas diferentes. Se estivéssemos rodando em hosts diferentes, poderíamos trabalhar apenas com um profile.*

4.  Dentro do projeto, teremos apenas um arquivo bootstrap.yml, que deverá ter o nome da aplicação(spring.application.name) e as configurações para acessar o config-server e obter sua configuração.
Abaixo, arquivo bootstrap.yml:


```yml
spring:
  application:
    name: api-gateway
  cloud:
    config:
      uri: http://localhost:8888,http://localhost:8889
      username: config
      password: config
      fail-fast: true
```
*Como aqui estamos simulando alta disponibilidade, temos dois config-servers setados. Caso o primeiro falhe, ele busca a configuração no segundo. Também informamos usuário e senha definidos no config-server para o microserviço se autenticar.*

### Executando

1.  Na raiz do project eureka-server, build o projeto:
```
mvn clean install
```

2.  Execute o projeto (No exemplo abaixo, para simularmos alta disponibilidade, estaremos startando duas instâncias. Cada instância subirá em uma porta.):
```
java -jar -Dspring.profiles.active=pre1 target\api-gateway-1.0.0-SNAPSHOT.jar 
java -jar -Dspring.profiles.active=pre2 target\api-gateway-1.0.0-SNAPSHOT.jar 
```
3.  Perceba que as duas instâncias se registraram no eureka-server.

![image](https://gitlab.com/s4bdigital/sites-team/kanban/uploads/88f11b7a5762ac8ad9f35c7b90e73ccd/eureka_api-gateway.PNG)

# Testando tudo

Para testarmos tudo, iremos utilizar um microserviço que:

* Carrega sua configuração através do config-server
* Se registra no Eureka, informando seu application-name e seu ip
* Disponibiliza um endpoint GET que retorna basicamente o hostname e a porta que o mesmo está rodando

Link: https://gitlab.com/s4bdigital/devops/microservices-poc/tree/master/order-service

Abaixo, configuração do microservico order-service.yml que deverá ficar no [repositório git](https://gitlab.com/s4bdigital/devops/microservices-config) acessado pelo config-server:

*Obs: Como estaremos rodando duas instâncias na mesma máquina e queremos simular o load balance entre as duas instâncias, é importante que elas possuam o mesmo nome. Por esta razão não estaremos usando profile. Para evitar conflito de portas, setamos como **0**, dessa forma o Spring verifica uma porta livre no host e atribui para o serviço.*
```yml
server:
  port: 0
  
spring:
  application:
    name: order-service-pre
  profiles: pre
eureka:
  client:
    service-url:
      defaultZone: http://eureka:eureka@127.0.0.1:8761/eureka,http://eureka:eureka@127.0.0.1:8762/eureka,http://eureka:eureka@127.0.0.1:8763/eureka
  instance:
    instance-id: ${spring.application.name}:${spring.application.instance_id:${random.value}}
    prefer-same-zone-eureka: true
management:
  endpoints:
    web:
      exposure:
        include: '*'
  endpoint:
    health:
      show-details: ALWAYS
```

4.  Dentro do projeto, teremos apenas um arquivo bootstrap.yml como abaixo:


```yml
spring:
  application:
    name: order-service
  cloud:
    config:
      uri: http://localhost:8888,http://localhost:8889
      username: config
      password: config
      fail-fast: true
```


5. Abaixo o Controller, que implementa uma API que retorna o hostname e a porta que a instância está rodando.  

```java
@RestController
public class Controller {

	@Autowired
	private ApplicationInfoManager applicationInfoManager;

	@GetMapping("/")
	public String get(HttpServletRequest request) {
		return "SERVICE: " + applicationInfoManager.getInfo().getAppName() + "\nPORT: "
				+ applicationInfoManager.getInfo().getPort();
	}

}
```

### Executando

1.  Na raiz do project eureka-server, build o projeto:
```
mvn clean install
```

2.  Execute o projeto (No exemplo abaixo, para simularmos alta disponibilidade, estaremos startando duas instâncias, com o mesmo profile, porém, cada instância subirá em uma porta.):
```
java -jar -Dspring.profiles.active=pre target\order-service-1.0.0-SNAPSHOT.jar 
java -jar -Dspring.profiles.active=pre target\order-service-1.0.0-SNAPSHOT.jar 
```

3.  Perceba que as duas instâncias se registraram no eureka-server. Como possuem o mesmo, temos uma hash no status que os diferencia.

![image](https://gitlab.com/s4bdigital/sites-team/kanban/uploads/8a77e50b9cb49ab05482a2151aa0f6c2/eureka_order-service.PNG)

### Testando a API-Gateway e o Load Balance

No browser digite:

*Perceba que estamos fazendo uma requisição para a api de order-service (/order) através da api-gateway (localhost:8080)*
```
http://localhost:8080/api/order/
```

**Resultado da primeira chamada:**

![image](https://gitlab.com/s4bdigital/sites-team/kanban/uploads/eb5b99a333ed0af0836372bece4a6b2f/load_balance1.PNG)


**Resultado da segunda chamada:**

![image](https://gitlab.com/s4bdigital/sites-team/kanban/uploads/1b8f891043a4d9ee3ee017b35d870331/load_balance2.PNG)