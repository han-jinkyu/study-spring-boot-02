# 무중단 배포
상기 자동배포는 서비스가 잠시 멈추는 점과 배포시 문제가 발생하면 롤백하기 어려우므로 무중단 배포를 통하여 중단하지 않고 배포를 진행해본다.

## Nginx 설치 및 실행
1. yum을 이용하여 설치
    ```bash
    $ sudo yum install -y nginx
    ```
   
    - nginx를 못 찾고 에러가 난다면 에러문의 이 명령어를 실행한다
        ```bash
        $ sudo amazon-linux-extras install nginx1        
        ```
   
1. Nginx를 실행한다
    ```bash
    $ sudo service nginx start
   
    # nginx 프로세스를 확인한다
    $ ps -ef | grep nginx
    ```

1. AWS 콘솔에서 EC2 인스턴스의 `퍼블릭 DNS (IPv4)`를 복사하여 브라우저에서 실행해본다
    - nginx 페이지가 뜬다면 성공
    
## 리버스 프록시 (Reverse Proxy) 설정
1. nginx 설정 파일을 연다
    ```bash
    $ sudo vi /etc/nginx/nginx.conf
    ```
   
1. 다음을 설정한다
    ```
    [...]
   
    http {
        [...]
   
        server {
            [...]
   
            location / {
                proxy_pass http://localhost:8080;
                proxy_set_header X-Real-IP $remote_addr;
                proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
                proxy_set_header Host $http_host;
            }
        }
   
        [...]
    }
    ```

    - proxy_pass: 리퀘스트를 `http://localhost:8080`으로 전달한다
    - proxy_set_header [HEADER] [VARIABLE]: 프록시로(현재는 nginx)부터 보내는 요청의 HEADER에 VARIABLE를 설정한다
    
1. `:wq`로 저장하고 nginx를 재시작한다
    ```bash
    $ sudo service nginx restart 
    ```

## Profile 생성
환경별로 사용할 Profile을 생성한다

1. 외부에 두고 불러들일 `/app/config/springboot-webservice/real-application.yml` 파일을 생성한다 (파일명 임의 설정 가능)
    ```yaml
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

1. 애플리케이션의 main이 존재하는 클래스를 수정한다
    ```java
    @EnableJpaAuditing
    @SpringBootApplication
    public class DemoApplication {
    
        public static final String APPLICATION_LOCATIONS = "spring.config.location=" +
            "classpath:application.yml," +
            "/app/config/springboot-webservice/real-application.yml";
    
        /**
         * Main
         * @param args arguments
         */
        public static void main(String[] args) {
            new SpringApplicationBuilder(DemoApplication.class)
                .properties(APPLICATION_LOCATIONS)
                .run(args);
        }
    }
    ```
   
    - APPLICATION_LOCATIONS: `application.yml`과 방금 생성한 `real-application.yml`의 경로를 써놓은 상수
    - SpringApplicationBuilder를 통해 properties를 불러 사용한다

## 배포 스크립트 작성
무중단 배포를 위한 스크립트를 작성한다

1. 폴더를 작성한다
    ```bash
    $ mkdir -p /home/ec2-user/app/nonstop
    $ cd /home/ec2-user/app/nonstop
    ```
   
1. 스크립트 작동 확인을 위하여 기존 jar 파일을 복사한다
    ```bash
    $ mkdir -p /home/ec2-user/app/nonstop/springboot-webservice/build/libs
    $ cp /home/ec2-user/app/travis/build/build/libs/*.jar /home/ec2-user/nonstop/springboot-webservice/build/libs/
    ```

1. jar 파일을 모아둘 디렉터리를 생성한다
    ```bash
    $ mkdir -p /home/ec2-user/app/nonstop/jar
    ```

1. 스크립트를 작성한다
    ```bash
    $ vi /home/ec2-user/app/nonstop/deploy.sh
    ```
   
    ```shell script
    #!/bin/bash
   
    BASE_PATH=/home/ec2-user/app/nonstop
    BUILD_PATH=$(ls $BASE_PATH/springboot-webservice/build/libs/*.jar)
    JAR_NAME=$(basename $BUILD_PATH)
    echo "> build 파일명: $JAR_NAME"
   
    echo "> 기존 파일 삭제"
    APPLICATION_NAME=demo
    DEPLOY_PATH=$BASE_PATH/jar/
    ls $DEPLOY_PATH | grep $APPLICATION_NAME | xargs rm
   
    echo "> 파일 복사"
    cp $BUILD_PATH $DEPLOY_PATH
   
    echo "> 현재 구동중인 Set 확인"
    CURRENT_PROFILE=$(curl -s http://localhost/profile)
    echo "> $CURRENT_PROFILE"
    
    # 현재 사용중이 아닌 Profile
    if [ $CURRENT_PROFILE == set1 ]
    then 
      IDLE_PROFILE=set2
      IDLE_PORT=8082
    elif [ $CURRENT_PROFILE == set2 ]
    then
      IDLE_PROFILE=set1
      IDLE_PORT=8081
    else
      echo "> 일치하는 profile이 없습니다. Profile: $CURRENT_PROFILE"
      echo "> set1을 할당합니다. IDLE_PROFILE: set1"
      IDLE_PROFILE=set1
      IDLE_PORT=8081
    fi
   
    # link(ln)를 만들어준다
    echo "> application.jar 교체"
    IDLE_APPLICATION="$IDLE_PROFILE-springboot-webservice.jar"
    IDLE_APPLICATION_PATH="$DEPLOY_PATH$IDLE_APPLICATION"
   
    ln -Tfs $DEPLOY_PATH$JAR_NAME $IDLE_IDLE_APPLICATION_PATH
   
    echo "> $IDLE_PROFILE에서 구동중인 애플리케이션 pid 확인"
    IDLE_PID=$(pgrep -f $IDLE_IDLE_APPLICATION)
   
    if [ -z $IDLE_PID ]
    then
      echo "> 현재 구동중인 애플리케이션이 없으므로 종료하지 않습니다"
    else
      echo "> kill -15 $IDLE_PID"
      kill -15 $IDLE_PID
      sleep 5
    fi
   
    echo "> $IDLE_PROFILE 배포"
    nohup java -jar -Dspring.profiles.active=$IDLE_PROFILE $IDLE_APPLICATION_PATH &
   
    echo "> $IDLE_PROFILE 10초 후 health check 시작"
    echo "> curl -s http://localhost:$IDLE_PORT/actuator/health"
    sleep 10
   
    for retry_count in {1..10}
    do
      response=$(curl -s http://localhost:$IDLE_PORT/actuator/health)
      up_count=$(echo $response | grep 'UP' | wc -l)
   
      if [ $up_count -ge 1 ]
      then # $up_count >= 1 ("UP" 문자열이 있는지 검증)
        echo "> health check 성공"
        break
      else
        echo "> health check의 응답을 알 수 없거나 status가 UP이 아닙니다"
        echo "> health check: ${response}"
      fi
   
      if [ $retry_count -eq 10 ]
      then
        echo "> health check 실패"
        echo "> nginx에 연결하지 않고 배포를 종료합니다"
        exit 1
      fi
   
      echo "> health check 연결 실패. 재시도..."
      sleep 10
    done
    ```
   
    - `http://loalhost:$PORT/actuator/health`는 `spring-boot-starter-actuator`를 사용하고 있다면 자동으로 디플로이 된다

## Nginx 동적 프록시 생성
배포 후 nginx가 기존에 보고 있던 profile의 다른 편을 바라보도록 변경한다

1. nginx가 동적으로 profile을 변경하도록 설정 파일을 수정한다
    ```bash
    $ sudo vi /etc/nginx/nginx.conf 
    ```

    ```
    [...]
       
    http {
        [...]
   
        server {
            [...]
   
            include /etc/nginx/conf.d/service-url.inc;
   
            location / {
                proxy_pass $service_url;
                [...]
            }
        }
   
        [...]
    }
    ```
    
    - `service-url.inc`를 포함하여 내부에 선언된 변수를 사용한다
    - `$service_url` 변수를 통해 proxy pass를 실행한다
    
1. `service-url.inc`를 생성한다
    ```bash
    $ sudo vi /etc/nginx/conf.d/service-url.inc
    ```
   
    ```shell script
    set $service_url http://127.0.0.1:8081;
    ```
   
1. 저장 후 nginx를 재시작한다
    ```bash
    $ sudo service nginx restart
    ```

1. profile이 잘 변경되었는지 확인한다
    ```bash
    $ curl -s localhost/profile
    ```

## Nginx 스크립트 작성
배포 시점에 자동으로 profile을 변경하도록 한다

1. 스크립트를 생성한다
    ```bash
    $ vi /home/ec2-user/app/nonstop/switch.sh
    ```
   
    ```shell script
    #!/bin/bash
    
    echo "> 현재 구동중인 Port 확인"
    CURRENT_PROFILE=$(curl -s http://localhost/profile)
    
    if [ $CURRENT_PROFILE == set1 ]
    then
      IDLE_PORT=8082
    elif [ $CURRENT_PROFILE == set2 ]
    then
      IDLE_PORT=8081
    else
      echo "> 일치하는 Port가 없습니다. Profile: $CURRENT_PROFILE"
      echo "> 8081을 할당합니다"
      IDLE_PORT=8081
    fi
    
    echo "> 전환할 포트: $IDLE_PORT"
    echo "> Port 전환"
    echo "set \$service_url http://127.0.0.1:${IDLE_PORT};" | sudo tee /etc/nginx/conf.d/service-url.inc
    
    PROXY_PORT=$(curl -s http://localhost/profile)
    echo "> Nginx 현재 Proxy Port: $PROXY_PORT"
    
    echo "> Nginx 재시작"
    sudo service nginx reload
    ```

    - 현재 구동중인 프로파일을 확인하여 쉬고 있는 포트를 설정하고 전환한다
    - `tee`는 화면 출력과 동시에 파일에 쓰는 커맨드
    - `service nginx reload`를 통해 설정만 다시 불러온다 (`restart`는 프로그램 재시작)

1. `switch.sh`에 실행권한을 준다
    ```bash
    $ sudo chmod +x ~/app/nonstop/switch.sh
    ```

1. `profile=set2`를 실행한다
    ```bash
    $ sh /home/ec2-user/app/nonstop/deploy.sh
    ```
   
1. 생성한 `switch.sh` 스크립트를 실행하여 본다
    ```bash
    $ sh /home/ec2-user/app/nonstop/switch.sh
    ```
   
1. 잘 구동되었는지 확인하여 본다
    ```bash
    $ curl -s http://localhost/profile
    set2    # set2가 나오면 성공
    ```
   
1. `deploy.sh` 스크립트 맨 끝에 `switch.sh`를 실행하도록 추가
    ```bash
    $ vi /home/ec2-user/app/nonstop/deploy.sh
    ```

    ```shell script
    echo "> 스위칭"
    sleep 10
    sh /home/ec2-user/app/nonstop/switch.sh
    ```

## 실제 배포 적용
1. 기존 빌드 삭제
    ```bash
    $ rm ~/app/nonstop/springboot-webservice/build/libs/*.jar
    ```

1. 기존에 `/travis`로 적용되어 있는 파일을 전부 바꾼다
    
    - `execute-deploy.sh`
        ```shell script
        #!/bin/bash
        
        sh /home/ec2-user/app/nonstop/deploy.sh > /dev/null 2> /dev/null < /dev/null &
        ```
    
    - `appspec.yml`
        ```yaml
        ...
        files:
          - source: /
            destination: /home/ec2-user/app/nonstop/springboot-webservice/
        ...  
        ```
        
        - `nonstop/springboot-webservice/`인 것을 주의하자

----
[Home](../README.md)
