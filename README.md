
# 1. 소개
Java를 이용해 만든 FreeChat 입니다.

디폴트 포트번호는 9000으로 셋팅되어 있습니다.

관리자 계정같은 경우

ID : admin 

Password : 1234

로 셋팅되어 있습니다.

# 2. 구상도
<div>
  <img src="https://user-images.githubusercontent.com/15880397/98195279-acba1900-1f64-11eb-9162-f397de1e414c.PNG" width="90%"></img>
</div>

# 3. 실행방법 (Window 기준)
1. JavaSetting.exe 실행(C로 짯으며 바탕화면 경로를 가져오는 실행파일 입니다.)

2. 그럼 ServerSet.bat, ClientSet.bat 파일이 나옵니다. 이걸 실행시켜주세요.

3. 그 후 ServerStart와 ClientStart를 실행시키면 됩니다.

# 4. 쳇 로그
채팅 기록은 ChatContents폴더에 날짜별로 이용자들의 채팅기록이 남습니다.

# 5. 접속자 로그
접속자 기록은 Log폴더에 날짜별로 기록되어 저장됩니다.

# 6. Admin 정보
현 채팅프로그램은 DB를 사용하지 않고 직렬화를 통해 객체를 .ser파일로 저장합니다.

.ser 파일은 AdminRegister 폴더에 저장됩니다.
