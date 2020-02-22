# 운영환경 설정
운영환경에 필요한 설정을 준비한다

## 운영 DB
Profile 2개를 만들었기 때문에 무언가를 추가/수정하려면 2개를 한꺼번에 수정해야 된다. 이를 개선한다.

### 드라이버 의존성 추가
MaridDB 관련 드라이버 의존성을 추가한다

```groovy
dependencies {
    // [...]

    implementation 'org.mariadb.jdbc:mariadb-java-client'
    
    // [...]
}
```

### 운영 환경을 위한 환경설정 수정
1. `real-application.yml`에 있던 내용을 코드상의 `application.yml`로 옮긴다
    ```yaml
    ---
    # Local 환경 설정들...
   
    # Production 환경
    ---
    spring:
      profiles: set1
    server:
      port: 8081
    
    ---
    spring:
      profiles: set2
    server:
      port: 8082

    ```

1. `real-application.yml`에는 다음과 같은 DB 관련 설정만 남긴다
    ```yaml
    ---
    spring:
      profile: real-db
      data-source:
        url: jdbc:mariadb://[rds주소]:[포트명(default: 3306)]/[database명]
        username: ### DB계정 ###
        password: ### DB비밀번호 ###
        drive-class-name: org.mariadb.jdbc.Driver
    ```
   
    - [rds주소]: RDS 화면에 존재하는 엔드포인트
    - [포트명]: 기본 포트는 3306
    - [database명]: 접속하고자 하는 DB명. 이제까지 사용한 DB는 webservice
   
1. `application.yml`에 위의 `real-db` 프로파일을 설정한다
    ```yaml
    ---
    # Local 환경 설정들...
       
    # Production 환경
    ---
    spring.profiles: set1
    spring.profiles.include: real-db
   
    server:
      port: 8081
   
    ---
    spring.profiles: set2
    spring.profiles.include: real-db
   
    server:
      port: 8082
    ```
   
### 운영DB에 테이블 만들기
이제까지 한 설정을 로컬에서 그대로 실행하면 `java.sql.SQLException: Table 'webservice.posts' doesn't exist`라는 에러가 나올 것이다. 운영DB에 테이블을 생성하여 본다.

1. `src/test/resources/application.yml`을 수정한다
    ```yaml
    # Test
    spring:
      profiles:
        active: local   # 기본 환경 선택
      jpa:
        properties:
          hibernate:
            dialect: org.hibernate.dialect.MySQL5InnoDBDialect
    
    # local 환경
    ---
    spring:
      profiles: local
      jpa:
        show-sql: true
    ```
   
    - dialect는 [링크](https://www.mkyong.com/hibernate/hibernate-dialect-collection/)를 참조

1. `WebControllerTest`를 실행하여 테스트 중에 나오는 로그 가운데서 SQL을 확인한다
    ```text
    [...]
    2020-02-22 17:46:16.515  INFO 59352 --- [    Test worker] o.hibernate.annotations.common.Version   : HCANN000001: Hibernate Commons Annotations {5.1.0.Final}
    2020-02-22 17:46:16.617  INFO 59352 --- [    Test worker] org.hibernate.dialect.Dialect            : HHH000400: Using dialect: org.hibernate.dialect.MySQL5InnoDBDialect
    Hibernate: drop table if exists posts
    Hibernate: create table posts (id bigint not null auto_increment, created_date datetime, modified_date datetime, author varchar(255), content TEXT not null, title varchar(500) not null, primary key (id)) engine=InnoDB
    2020-02-22 17:46:17.198  WARN 59352 --- [    Test worker] o.h.t.s.i.ExceptionHandlerLoggedImpl     : GenerationTarget encountered exception accepting command : Error executing DDL "create table posts (id bigint not null auto_increment, created_date datetime, modified_date datetime, author varchar(255), content TEXT not null, title varchar(500) not null, primary key (id)) engine=InnoDB" via JDBC Statement
    [...]
    ``` 
   
1. 나오는 걸 확인했으므로 테스트가 아닌 경우에도 사용하기 위해 `src/main/resources/application.yml`에도 같은 설정 추가
    ```yaml
    spring:
      profiles:
        active: local   # 기본 환경 선택
      jpa:
        properties:
          hibernate:
            dialect: org.hibernate.dialect.MySQL5InnoDBDialect
    
    # 그 이외 설정...
    ```
   
1. 위에서 나온 SQL을 운영DB에 실행한다 
    ```sql
    create table posts (
        id bigint not null auto_increment, 
        created_date datetime, 
        modified_date datetime, 
        author varchar(255), 
        content TEXT not null, 
        title varchar(500) not null, 
        primary key (id)) engine=InnoDB
    ```

---
[Home](../README.md)
