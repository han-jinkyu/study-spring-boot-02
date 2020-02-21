# AWS 설정

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

----
[Home](../README.md)
