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

---
[Home](../README.md)
