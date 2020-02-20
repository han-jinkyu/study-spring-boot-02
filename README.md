# study-spring-boot-02 [![Build Status](https://travis-ci.com/han-jinkyu/study-spring-boot-02.svg?branch=master)](https://travis-ci.com/han-jinkyu/study-spring-boot-02)
스프링 부트 공부를 위한 두번째 레포지터리.
[jojoldu님의 블로그](https://jojoldu.tistory.com/250)를 참고하였습니다.

# AWS

## EC2
클라우드 상의 가상 컴퓨터 인스턴스를 생성한다

### 인스턴스 작성
1. `인스턴스 시작` 버튼
1. AMI 선택
    - Amazon Linux 선택
1. 인스턴스 스펙
    - t2.micro (free tier)
1. 인스턴스 세부 정보
    - 디폴트
1. 스토리지 
    - 30GB (free tier)
1. 태그 추가
    - Key:Value로 인스턴스를 나타내는 값
    - Name:springboot-webservice로 작성
1. 보안 그룹
    - SSH(22)는 `내 IP`로 선택하여 외부 접속 막기 
    - HTTP(80), HTTPS(443)은 사용자 지정 `0.0.0.0/0, ::/0`으로 설정
1. 키 페어 생성하여 보존 (*.pem)

### Elastic IP (고정 IP)
1. EC2 페이지 좌측의 `네트워크 및 보안` => `탄력적 IP`
1. `새 주소 할당`
1. 할당된 주소에 `주소 연결`
    - 리소스 유형: 인스턴스
    - 인스턴스: 생성한 인스턴스 연결
    - 프라이빗 IP: 탄력적 IP 설정

### ssh 설정
1. 인스턴스 작성시에 마지막에 취득한 키 페어 파일(*.pem)의 모드를 변경한다
    ```bash
    $ chmod 600 springboot-webservice.pem
    ```
   
1. `~/.ssh`에 들어가서 config 파일을 작성한다
    ```bash
    $ vi config
    ```
   
1. config 파일 내용은 다음과 같이 적는다
    ```text
    # springboot-webservice
    Host [연결할 때 쓸 이름]
         HostName [탄력적 IP]
         User ec2-user
         IdentityFile [pem 파일 존재 위치]
    ```
   
1. 저장하고 ssh를 이용해 연결해본다
    ```bash
    $ ssh [연결할 때 쓸 이름]
    ```

## RDS
관계형 데이터베이스를 생성한다

### 인스턴스 생성
1. `데이터베이스 생성` 버튼 누르기
1. 이하의 선택사항을 확인하여 고른다. (free tier 사용)
    - 데이터베이스 생성 방식 선택: `표준 생성`
    - 엔진 옵션: `MariaDB:10.2.21` (임의 선택)
    - 템플릿: `프리 티어`
    - 설정
        - DB 인스턴스 식별자: `springbootwebservice` (임의)
    - 인스턴스 크기: `버스터블 클래스` (db.t2.micro)
    - 스토리지
        - 스토리지 유형: `범용(SSD)`
        - 할당된 스토리지: `20 GiB` (free tier)
        - 스토리지 자동조정: `끄기` (free tier를 위해 끔)
    - 연결
        - VPC: `Default VPC`
        - 추가 연결 구성
            - VPC 보안 그룹: `새로 생성`
            - 새 VPC 보안 그룹 이름: `springboot-webservice-rds`
    - 추가 구성
        - 데이터베이스 옵션
            - 초기 데이터베이스 이름: `webservice` (임의)

1. 새로 생성한 VPC 보안 그룹의 설정 변경
    1. 연결하고자 하는 `EC2의 보안 그룹 ID`를 복사
    1. RDS와 함께 만든 보안 그룹에 `인바운드 규칙`을 수정
        1. MySQL/Aurora:TCP:3306:`사용자 지정/EC2의 보안 그룹 ID`
        1. MySQL/Aurora:TCP:3306:`내 IP` 

1. RDS 인스턴스 수정
    - 네트워크 및 보안
        - 퍼블릭 액세스 가능성: `예`

### RDS 내부 설정 변경
1. 문자열 변수 확인 (UTF8인지 확인하기 위해)
    ```sql
    SHOW VARIALBES LIKE 'c%';
    ```
   
1. 좌측 메뉴 `파라미터 그룹`
    - default 설정은 변경 불가하므로 새로운 `파라미터 그룹 생성`
        - 그룹 이름, 설명: `springboot-webervice`
    - 다음 사항을 UTF8으로 변경
        - character_set_client
        - character_set_connection
        - character_set_database
        - character_set_filesystem
        - character_set_results
        - character_set_server
        - collation_connection: utf8_general_ci
        - collation_server: utf8_general_ci
    
1. 인스턴스에 새로 생성한 그룹 적용
    - `수정` 버튼
    - 데이터베이스 옵션
        - DB 파라미터 그룹: `springboot-webervice`
    - `재부팅`

1. 파라미터 그룹으로 변경이 안 되는 파라미터는 직접 변경
    ```sql
    ALTER DATABASE webservice
    CHARACTER SET = 'utf8'
    COLLATE = 'utf8_general_ci';
    ```

## 배포
EC2에 애플리케이션을 배포한다

### Java 8 설치
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
    
### 테스트 관련 수정
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

### 스크립트
- scripts/deploy.sh를 확인한다
- 스크립트를 실행하기 위하여 권한을 준다
    ```bash
    $ chmod 755 deploy.sh
    ```


## CI
Travis CI를 통해 CI 환경을 구축해본다

### Travis CI 활성화
1. [Travis CI](https://travis-ci.com/)에 들어가 Github을 이용하여 로그인
1. Dashboard에서 프로젝트를 찾아서 메인페이지에 들어간다

### 프로젝트 설정
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


## 자동 배포
AWS Code Deploy를 통해 빌드 결과물을 EC2에 자동 배포한다

### Travis CI용 계정 생성 (IAM)
1. IAM 페이지 => 엑세스 관리 => 사용자
1. 상단의 `사용자 추가` 버튼
1. 세부 정보 설정
    - 사용자 이름: `springboot-webservice-deploy` (임의 설정)
    - 엑세스 유형: `프로그래밍 방식 엑세스`
1. 정책(Policy) 설정
    - `기존 정책 연결`: 계정에 바로 정책 설정
    - 설정할 정책 목록
        - `AmazonS3FullAccess`: S3에 대한 모든 접근 권한
        - `AWSCodeDeployFullAccess`: Code Deploy에 대한 모든 접근 권한
1. 태그 설정
    - 이번엔 설정 안 함
1. 검토 페이지
    - 확인하고 `사용자 추가` 버튼
1. 사용자 추가
    - `.csv 다운로드` 버튼을 눌러 `엑세스 키 ID`, `비밀 엑세스 키`를 다운로드 받아놓는다
    

### 빌드 파일 보관소 생성 (S3)
1. S3 페이지 => `버킷 만들기` 버튼
1. 이름 및 지역 설정
    - 버킷 이름: `springboot-webservice-build-deploy` (임의 설정; 고유해야 함)
    - 리전: `아시아 태평양 (도쿄)` (임의 설정)
1. `생성` 버튼
    
### IAM 역할(Role) 추가
EC2와 CodeDeploy를 위한 [역할](https://docs.aws.amazon.com/ko_kr/IAM/latest/UserGuide/id_roles_terms-and-concepts.html)을 생성한다

#### 첫번째 역할: CodeDeploy를 위한 EC2 역할
1. IAM 페이지 => 액세스 관리 => 역할
1. `역할 만들기` 버튼
1. 신뢰할 수 있는 유형의 개체 선택
    - AWS 서비스
    - 사용 사례 선택: `EC2` => `EC2`
1. 역할 만들기
    - `AmazonEC2RoleforAWSCodeDeploy` 선택
1. 태그 추가
    - 이번엔 설정 안 함
1. 검토 페이지
    - 역할 이름: `springboot-webservice-EC2CodeDeployRole` (임의 설정)
    - 정책: `AmazonEC2RoleforAWSCodeDeploy`
1. `역할 만들기` 버튼

#### 두번째 역할: CodeDeploy가 AWS 서비스를 부를 수 있게 허용하는 역할
1. IAM 페이지 => 액세스 관리 => 역할
1. `역할 만들기` 버튼
1. 신뢰할 수 있는 유형의 개체 선택
    - AWS 서비스
    - 사용 사례 선택: `CodeDeploy` => `CodeDeploy`
1. 역할 만들기
    - `AWSCodeDeployRole` 선택
1. 태그 추가
    - 이번엔 설정 안 함
1. 검토 페이지
    - 역할 이름: `springboot-webservice-EC2CodeDeployRole` (임의 설정)
    - 정책: `AmazonEC2RoleforAWSCodeDeploy`
1. `역할 만들기` 버튼
    - 역할 이름: `springboot-webservice-CodeDeployRole` (임의 설정)
    - 정책: `AmazonEC2RoleforAWSCodeDeploy`
1. `역할 만들기` 버튼

#### EC2에 첫번째 역할 부여
1. EC2 페이지 => 인스턴스
1. `작업` => `인스턴스 설정` => `IAM 역할 연결/바꾸기`
1. IAM 역할 연결/바꾸기
    - IAM 역할: `springboot-webservice-EC2CodeDeployRole` (설정한 이름)
1. `적용` 버튼

### EC2에 CodeDeploy Agent 설치
1. EC2 인스턴스에 접속
1. AWS Client 설치
    ```bash
    $ sudo yum -y update
    $ sudo yum install -y aws-cli
    ```
1. 적당한 폴더로 이동하여 AWS 설정을 진행
    ```bash
    $ cd /home/ec2-user
    $ sudo aws configure
    AWS Access Key ID [None]: ###ACCESS_KEY_ID###
    AWS Secret Access Key [None]: ###SECRET_ACCESS_KEY###
    Default region name [None]: ap-northeast-1
    Default output format [None]: json
    ```
   
    - Region name
        - ap-northeast-2 = 서울, ap-northeast-1 = 도쿄

1. EC2로 CodeDeploy 설치 파일 다운로드
    ```bash
    $ aws s3 cp s3://aws-codedeploy-ap-northeast-1/latest/install . --region ap-northeast-1
    ```
    
    - aws: AWS Client
    - s3: S3 서비스
    - cp `src` `dest`: COPY 명령어

1. 다운로드된 파일에 권한 추가하고 설치
    ```bash
    $ chmod +x ./install
    $ sudo ./install auto
    ```
   
    - +x: X (Executable) 권한을 준다
    - 혹시 `sudo ./install auto`가 ruby를 찾는다면 ruby를 설치
        ```bash
        $ sudo yum install -y ruby
        ```
1. CodeDeploy Agent가 실행 중인지 확인
    ```bash
    $ sudo service codedeploy-agent status
    The AWS CodeDeploy agent is running as PID xxxx
    ```
   
1. CodeDeploy Agent가 EC2 부팅시 자동 실행하게 스크립트 작성 
    ```bash
    $ sudo vi /etc/init.d/codedeploy-startup.sh
    ```
   
    ```shell script
    #!bin/bash
   
    echo 'Starting codedeploy-agent'
    sudo service codedeploy-agent start
    ```

### Travis CI와 S3 연결
빌드 결과물을 저장할 수 있게 연결한다

1. `.travis.yml`에 추가한다
    ```yaml
    deploy:
     - provider: s3
       access_key_id: $AWS_ACCESS_KEY      # Travis CI 상에서 설정
       secret_access_key: $AWS_SECRET_KEY  # Travis CI 상에서 설정
       bucket: springboot-webservice-build-deploy  # 보존할 버킷명
       region: ap-northeast-1  # Tokyo
       skip_cleanup: true
       acl: public_read
       on:
         repo: han-jinkyu/study-spring-boot-02 # github repo 이름
         branch: master
    ```

    - 다음과 같은 메시지가 로그에서 나오면서 실패하면 ACL(Access Control List) 설정을 바꿔야 하는 경우가 있다
        ```
        Oops, It looks like you tried to write to a bucket that isn't yours or doesn't exist yet. Please create the bucket before trying to write to it.
        ```
        
        1. 생성한 버킷 클릭
        1. `권한` 탭
        1. `퍼블릭 액세스 차단` 설정
        1. `새 ACL(액세스 제어 목록)을 통해 부여된 버킷 및 객체에 대한 퍼블릭 액세스 차단`를 비활성화
        
1. 빌드된 파일을 압축하여 보낼 수 있게 `.travis.yml`을 더 수정한다
    ```yaml
    ...
    before_deploy:
     - zip -r study-spring-boot-02 * # github repo명을 쓴다
     - mkdir -p deploy
     - mv study-spring-boot-02.zip deploy/study-spring-boot-02.zip
   
    deploy:
     - provider: s3
       ...
       local_dir: deploy # before_deploy에서 생성한 폴더명
       ...
    ```

### Travis CI, S3 그리고 CodeDeploy 연결
CodeDeploy까지 연결하여 EC2로 배포한다

#### 애플리케이션 생성
1. `CodeDeploy` 페이지 => 좌측 `애플리케이션`
1. `애플리케이션 생성` 버튼
1. `애플리케이션 구성`
    - 애플리케이션 이름: `springboot-webservice` (임의 설정)
    - 컴퓨팅 플랫폼: `EC2/온프레미스`
1. `애플리케이션 생성` 버튼

#### 배포 그룹 설정
1. 생성한 애플리케이션 선택
1. `배포 그룹` 탭 => `배포 그룹 생성` 버튼
    - 배포 그룹 이름: `springboot-webservice-group` (임의 설정)
    - 서비스 역할 입력: 생성해두었던 `CodeDeployRole`
    - 배포 유형: `현재 위치`
    - 환경 구성
        - `Amazon EC2 인스턴스`
        - 태그
            - 키: `Name`
            - 값: `springboot-webservice`
    - 배포 구성: `CodeDeployDefault.AllAtOnce`
    - 로드 밸런서: `비활성화` (로드 밸런서 설정이 없기 때문)
1. `배포 그룹 생성` 버튼

#### EC2상에 S3로부터 다운로드
1. S3로부터 zip 파일을 다운로드 받아놓을 폴더를 생성한다
    ```bash
    $ mkdir -p /home/ec2-user/app/travis/build
    ```
1. AWS CodeDeploy를 위한 설정 `appspec.yml` 생성
    ```yaml
    version: 0.0   # CodeDeploy의 버전을 뜻함. 0.0을 사용할 것.   
    os: linux
    files: 
     - source: /   # S3 버킷에서 복사할 파일 위치
       destination: /home/ec2-user/app/travis/build/
    ```
1. Travis CI에서 CodeDeploy를 실행하도록 `.travis.yml` 수정
    ```yaml
    deploy:
        ...
        - provider: codedeploy
            access_key_id: $AWS_ACCESS_KEY
            secret_access_key: $AWS_SECRET_KEY
            bucket: springboot-webservice-build-deploy
            key: study-spring-boot-02.zip  # S3에 저장된 zip 파일을 EC2로 배포
            build_type: zip
            application: springboot-webservice # 콘솔에서 등록한 CodeDeploy 애플리케이션
            deployment_group: springboot-webservice-group # 콘솔에서 등록한 CodeDeploy 애플리케이션의 배포 그룹
            region: ap-northeast-1
            on:
              repo: han-jinkyu/study-spring-boot-02
              branch: master
        ...
    ```
1. Commit하고 Push하여 성공하면 `app/travis/build` 폴더에 애플리케이션 확인 가능

#### CodeDeploy를 통한 배포 스크립트 실행
1. jar 파일을 모아둘 폴더 생성
    ```bash
    $ mkdir -p /home/ec2-user/app/travis/jar
    ```
   
1. `applictaion.jar`를 실행할 스크립트 작성
    ```bash
    $ vi /home/ec2-user/app/travis/deploy.sh
    ```
   
    ```shell script
    #!/bin/bash
    
    REPOSITORY=/home/ec2-user/app/travis
    
    echo "> 현재 구동 중인 애플리케이션 pid 확인"
    
    CURRENT_PID=$(pgrep -f demo)
    
    echo "$CURRENT_PID"
    
    if [ -z $CURRENT_PID ]; then
        echo "> 현재 구동 중인 애플리케이션이 존재하지 않습니다"
    else
        echo "> kill -15 $CURRENT_PID"
        kill -15 $CURRENT_PID
        sleep 5
    fi
    
    echo "> 새 애플리케이션 배포"
    
    echo "> build 파일 복사"
    
    cp $REPOSITORY/build/build/libs/*.jar $REPOSITORY/jar/
    
    JAR_NAME=$(ls $REPOSITORY/jar/ | grep 'demo' | tail -n 1)
    
    echo "> JAR NAME: $JAR_NAME"
    
    nohup java -jar $REPOSITORY/jar/$JAR_NAME &
    ```

1. `appspec.yml`을 갱신하여 스크립트를 실행하도록 한다
    ```yaml
    files:
      ...
   
    hooks:
      AfterInstall:  # 배포 후 아래 명령어 실행
        - location: scripts/execute-deploy.sh  # 코드 내부의 파일
          timeout: 180
    ```
   
1. `appspec.yml`에 작성한 `execute-deploy.sh`를 작성한다
    ```shell script
    #!/bin/bash
    
    # 백그라운드(&) 실행 뒤 로그 등을 남기지 않도록(> /dev/null) 한다
    sh /home/ec2-user/app/travis/deploy.sh > /dev/null 2> /dev/null < /dev/null &
    ```
    - `sh` 커맨드를 안 넣으면 `Permission denied` 당하는 경우가 존재하는 걸로 생각된다

## 무중단 배포
상기 자동배포는 서비스가 잠시 멈추는 점과 배포시 문제가 발생하면 롤백하기 어려우므로 무중단 배포를 통하여 중단하지 않고 배포를 진행해본다.

### Nginx 설치 및 실행
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
    
### 리버스 프록시 (Reverse Proxy) 설정
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

### Profile 생성
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

### 배포 스크립트 작성
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
   
    echo "> 파일 복사"
    DEPLOY_PATH=$BASE_PATH/jar/
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

### Nginx 동적 프록시 생성
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

### Nginx 스크립트 작성
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
