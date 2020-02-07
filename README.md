# study-spring-boot-02
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
