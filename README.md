# Aroundog (Dog Walking Application)
현대 사회에서 반려동물과 함께하는 생활 및 애완동물을 기르는 사람이 증가 하고 있는 추세입니다. 그에 따른 산책 중 다른 견종과의 사고 또한 증가했습니다. 친구들과 어울리게 해주기 위해 다른 강아지들을 만나고 싶은 사람도 있는 반면, 무서워하거나 위협을 느껴 다른 강아지들을 피하고 싶은 사람도 있습니다. 

#### 이를 모두 해결할 수 있는 산책 앱

---
### 환경 및 언어  
**Language** - Android(Kotlin), Java(Spring), Python(자동화 스크립트)  
**Development Environment** - Android Studio, Eclipse, Pycharm, MYSQL
  
---
### 시스템 구성도
![image](https://user-images.githubusercontent.com/54983139/203083353-ee904d3a-2d46-490e-a448-78365437400e.png)

---

## 주요 기능

- 산책 경로 트래킹
- 선호하지 않는 강아지 알림
- 월별 산책 정보 확인
- 산책정보 상세 확인
- 추천 산책경로 확인
- 추천 산책 경로 중복 제거
- 로그인 / 회원가입
- 아이디 / 비밀번호 찾기

## 서버

![Untitled](https://user-images.githubusercontent.com/54983139/193743481-d3762456-f786-4eb8-908a-70202afb49cd.png)
- Spring boot 사용
- API

## 데이터베이스
![aroundog](https://user-images.githubusercontent.com/58110946/204068160-ab257cfc-eb57-4d8e-a67c-941de43777a9.png)
| walk | 산책 후 저장되는 산책에 대한 정보 |
| --- | --- |
| walk_deduplication | 중복이 제거된 산책 정보 |
| user | 사용자 정보 |
| coordinate | 사용자별 마지막 산책 위치, 산책 여부 |
| user_dog | 사용자의 반려견 정보 |
| dog_img | 강아지 사진, 사진이 저장된 경로 |
| dog | 강아지 종 |
| good | 유저가 좋아요를 누른 산책 |
| bad | 유저가 싫어요를 누른 산책 |
