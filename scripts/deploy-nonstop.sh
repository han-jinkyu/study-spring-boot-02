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

echo "> application.jar 교체"
IDLE_APPLICATION="$IDLE_PROFILE-springboot-webservice.jar"
IDLE_APPLICATION_PATH="$DEPLOY_PATH$IDLE_APPLICATION"

ln -Tfs $DEPLOY_PATH$JAR_NAME $IDLE_APPLICATION_PATH

echo "> $IDLE_PROFILE에서 구동중인 애플리케이션 pid 확인"
IDLE_PID=$(pgrep -f $IDLE_APPLICATION_PATH)

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