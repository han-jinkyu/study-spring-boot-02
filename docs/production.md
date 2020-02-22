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
   
---
[Home](../README.md)
