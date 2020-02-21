# CI
Travis CI를 통해 CI 환경을 구축해본다

## Travis CI 활성화
1. [Travis CI](https://travis-ci.com/)에 들어가 Github을 이용하여 로그인
1. Dashboard에서 프로젝트를 찾아서 메인페이지에 들어간다

## 프로젝트 설정
1. `.travis.yml`을 레포지터리 루트에 작성한다. ([참고](https://docs.travis-ci.com/user/tutorial/))
    ```yaml
    language: java
    jdk:
        - openjdk8
    
    # 오직 master에 커밋될 시에만 움직인다
    branches:
        only:
          - master
    
    # 이하의 폴더를 캐시하여 다음 번 빌드시에 또 다운로드하지 않게 함
    cache:
        directories:
          - '$HOME/.m2/repository'
          - '$HOME/.gradle'
    
    # 푸시되었을 때 사용될 스크립트
    script: "./gradlew clean build"
    
    # 실행 완료시 알림
    notifications:
        slack:
          rooms:
            - secure: ##token##
    ```

1. 푸시하고 커밋한다

----
[Home](../README.md)
