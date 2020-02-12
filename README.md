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
    langauge: java
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
    scripts: "./gradlew clean build"
    
    # 실행 완료시 알림
    notifications:
        slack: ##token##
    ```

1. 푸시하고 커밋한다
