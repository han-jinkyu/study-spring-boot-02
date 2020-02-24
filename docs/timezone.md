# 타임존 설정
서버는 UTC로 설정되어 있기 때문에 9시간을 더해야 한다

## 현재 타임존 확인
1. EC2상의 시간을 확인해본다
    ```bash
    $ date # 현재 시간은 02.24. (월) 19:35:28 JST
    2020. 02. 24. (월) 10:33:28 UTC
    ```
   
1. RDS상의 시간도 확인해본다
    ```sql
    SELECT NOW(); 
    -- 현재 시간은 02.24. (월) 19:36:24 JST
    -- 2020-02-24 10:36:24
    ```

## EC2 타임존 변경
1. 다음 커맨드를 입력한다
    ```bash
    $ sudo rm /etc/localtime
    # 현재 Tokyo를 사용하므로 Asia/Tokyo로 링크 작성
    $ sudo ln -s /usr/share/zoneinfo/Asia/Tokyo /etc/localtime 
    ```
   
1. 시간을 확인한다
    ```bash
    $ date
    2020. 02. 24. (월) 19:42:12 JST
    ```
   
1. 확실하게 적용하기 위하여 인스턴스를 재부팅한다
    ```bash
    $ sudo reboot
    ```

## RDS 타임존 변경
1. AWS 관리페이지에서 다음으로 이동한다
    - `RDS` => `파라미터 그룹`

1. `springboot-webservice`를 선택하여 `파라미터 편집`을 누른다

1. `파라미터 필터링`란에 `time_zone`을 입력하여 필터하여 `time_zone`을 찾는다

1. `time_zone`을 누르고 우측 상단의 `파라미터 편집` 버튼을 누른다

1. 원하는 타임존을 선택한다
    - 나는 `Asia/Tokyo` 선택
    - 한국은 `Asia/Seoul`
    
1. 선택 후 우측 상단 `변경 사항 저장` 버튼을 누른다 

1. 변경됐는지 다시 한 번 확인한다
    ```sql
    SELECT NOW();
    -- 2020-02-24 19:54:29
    ```

---
[Home](../README.md)
