# 자동 배포
AWS Code Deploy를 통해 빌드 결과물을 EC2에 자동 배포한다

## Travis CI용 계정 생성 (IAM)
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
    

## 빌드 파일 보관소 생성 (S3)
1. S3 페이지 => `버킷 만들기` 버튼
1. 이름 및 지역 설정
    - 버킷 이름: `springboot-webservice-build-deploy` (임의 설정; 고유해야 함)
    - 리전: `아시아 태평양 (도쿄)` (임의 설정)
1. `생성` 버튼
    
## IAM 역할(Role) 추가
EC2와 CodeDeploy를 위한 [역할](https://docs.aws.amazon.com/ko_kr/IAM/latest/UserGuide/id_roles_terms-and-concepts.html)을 생성한다

### 첫번째 역할: CodeDeploy를 위한 EC2 역할
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

### 두번째 역할: CodeDeploy가 AWS 서비스를 부를 수 있게 허용하는 역할
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

### EC2에 첫번째 역할 부여
1. EC2 페이지 => 인스턴스
1. `작업` => `인스턴스 설정` => `IAM 역할 연결/바꾸기`
1. IAM 역할 연결/바꾸기
    - IAM 역할: `springboot-webservice-EC2CodeDeployRole` (설정한 이름)
1. `적용` 버튼

## EC2에 CodeDeploy Agent 설치
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

## Travis CI와 S3 연결
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

## Travis CI, S3 그리고 CodeDeploy 연결
CodeDeploy까지 연결하여 EC2로 배포한다

### 애플리케이션 생성
1. `CodeDeploy` 페이지 => 좌측 `애플리케이션`
1. `애플리케이션 생성` 버튼
1. `애플리케이션 구성`
    - 애플리케이션 이름: `springboot-webservice` (임의 설정)
    - 컴퓨팅 플랫폼: `EC2/온프레미스`
1. `애플리케이션 생성` 버튼

### 배포 그룹 설정
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

### EC2상에 S3로부터 다운로드
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

### CodeDeploy를 통한 배포 스크립트 실행
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

----
[Home](../README.md)
