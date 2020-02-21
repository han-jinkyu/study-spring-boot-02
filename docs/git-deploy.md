# 배포
EC2에 애플리케이션을 배포한다

## Java 8 설치
1. Java 8 설치
    ```bash
    # JDK는 devel
    $ sudo yum install -y java-1.8.0-openjdk-devel.x86_64
    ```

1. Git 설치
    ```bash
    $ sudo yum install -y git
    ```
   
1. 프로젝트 클론
    ```bash
    $ mkdir app && mkdir app/git
    $ cd ~/app/git
    $ git clone [레포지터리 URI]
    ```
   
1. 프로젝트 테스트 수행
    ```bash
    # /gradle 폴더가 존재해야 됨...
    $ ./gradlew test
    ```
    순서대로 했다면 테스트 실패!
    
## 테스트 관련 수정
- 테스트를 위한 환경설정 파일 작성 (src/test/resource/application.yml)
    ```yaml
    # Test
    spring:
      profiles:
        active: local # 기본 환경 선택
    
    # local 환경
    ---
    spring:
      profiles: local
      jpa:
        show-sql: true
    ```

## 스크립트
1. 아래 스크립트를 작성한다
    ```shell script
    #!/bin/bash
    
    # 레포지터리 경로를 변수에 설정
    REPOSITORY=/home/ec2-user/app/git
    REPOSITORY_NAME=study-spring-boot-02
    APP_NAME=demo
    
    # 레포지터리로 이동
    cd "$REPOSITORY/$REPOSITORY_NAME/"
    
    # 리모트 레포지토리로부터 갱신한다
    echo "> git pull"
    git pull
    
    # 프로젝트 빌드 시작
    echo "> 프로젝트 빌드 시작"
    ./gradlew build
    
    # 빌드된 파일을 레포지터리 루트 폴더로 이동한다
    cp ./build/libs/*.jar $REPOSITORY/
    
    # 현재 구동 중인 프로세스 ID를 확인하여 중지
    echo "> 현재 구동 중인 애플리케이션 PID 확인"
    CURRENT_PID=$(pgrep -f "$APP_NAME")
    echo "CURRENT_PID: $CURRENT_PID"
    
    # -z: Returns true if the string is null
    if [ -z "$CURRENT_PID" ]; then
      echo "> 현재 구동 중인 애플리케이이 없으므로 종료하지 않습니다"
    else
      echo "> kill -15 $CURRENT_PID"
      kill -15 "$CURRENT_PID"
      sleep 5
    fi
    
    # 새로운 애플리케이션 배포
    echo "> 새 애플리케이션 배포"
    
    cd "$REPOSITORY/"
    JAR_NAME=$(ls $REPOSITORY/ | grep "$APP_NAME" | tail -n 1)
    echo "> JAR Name: $JAR_NAME"
    
    # nohup은 세션이 끊겨도 계속 동작하도록 하는 커맨드
    # &은 백그라운드를 의미
    nohup java -jar "$REPOSITORY/$JAR_NAME" &
    ```

1. 스크립트를 실행하기 위하여 권한을 준다
    ```bash
    $ chmod 755 deploy.sh
    ```
   
----
[Home](../README.md)
