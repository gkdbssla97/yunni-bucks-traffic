# ☕️YUNNI-BUCKS

세종대학교 컴퓨터 공학과 19학번 2인 개발팀

개발 기간 : *2023-07 ~ 2022-09 (MVP 구현 완료)* 

## 목차
 - [프로젝트 소개](#프로젝트-소개)
 - [맴버 구성](#맴버-구성)
 - [개발 환경](#개발-환경)
 - [프로젝트 설명](#프로젝트-설명)
 - [구현기능 및 문서](#구현기능-및-문서)

## 프로젝트 소개
주문-결제-배달 온라인 카페 서비스

## 맴버 구성
|        | **윤광오(팀장)**                                                                                                                                                                                                                                               | 하윤(팀원)                                                                                                                                                                                                                                                                                                                                  |
|:------:|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|        |                                                                                                                                                                                                                                                           |                                                                                                                                                                                                                                                                                                                                         |
|  포지션   | Back-End Developer                                                                                                                                                                                                                                        | Back-End Developer                                                                                                                                                                                                                                                                                                                      |
| 담당 도메인 | 회원, 주문, 배달                                                                                                                                                                                                                                                | 결제, 카드, OCR(Optical Character Recognition)                                                                                                                                                                                                                                                                                              |
| 기술 스택  | Spring Boot, Redis, Query-Dsl, Spring Data JPA, JPA, JUnit, H2 Database, MariaDB, Rest Docs, Mockito, JWT                                                                                                                                                 | Spring Boot, Query-Dsl, Spring Data JPA, JPA, JUnit, H2 Database, MariaDB, Rest Docs, Mockito, Open API                                                                                                                                                                                                                                 |
|  한 일   | 설계 : ERD (DB), Domain Model, OOP, Layered Architecture<br/><br/> 구현: Java Reflection 활용한 Record Class 전용 CustomMapper, Fake Repository, Redis(NoSql) Fake Repository 구현, Scheduler 활용한 배달 상태 변경 구현, 자체 비밀번호 암호화 구현, 썸네일 파일 업/다운로드, JWT 활용한 Login, 페이지네이션, | 설계 : OOP, Layered Architecture(Pay, Card, OCR)<br/><br/> 구현: Clova OCR(https://clova.ai/ocr/), Toss Payments(https://docs.tosspayments.com/guides/index) develop API 연동하여 신용/체크카드 이미지 인식 및 자동 결제 시스템 개발</br>Fake Object, Test Container로 Fake Layer Architecture 구현 Unit/Integration Test, Pagination, Slack Error Log, API 요청 알림 WebHook 구현 

## 개발 환경
- Java 17
- Oracle OpenJDK 17.0.4
- IDE : IntelliJ IDEA
- DATABASE : H2, MariaDB
- ORM : JPA
- Framework: Spring Boot 2.7.14

## 프로젝트 설명


### Architecture

<img width="808" alt="image" src="https://github.com/gkdbssla97/yunni-bucks/assets/55674664/5340c5dc-103c-471b-b9bf-61919f35ab9b">

### Flow Chart

<img width="757" alt="image" src="https://github.com/gkdbssla97/yunni-bucks/assets/55674664/4a8f2a6d-4508-407b-a511-68374f6c3080">

### ERD

<img width="757" alt="image" src="https://github.com/gkdbssla97/yunni-bucks/assets/55674664/37452648-0c1b-4b6f-aa0a-42a53cbcc9ce">

### Domain Model
<img width="757" alt="image" src="https://github.com/gkdbssla97/yunni-bucks/assets/55674664/3f782d5f-0a01-4336-86da-55e0b13f373a">

### 프로젝트 구조

[프로젝트 디렉토리](https://github.com/gkdbssla97/yunni-bucks/blob/master/DIRECTORY-STRUCTURE.md)

### 1.SRP
주문, 결제, 할인, 배달은 각각의 기능만 가지며 책임을 수행한다.
[도메인 모델](#Domain-Model)

### 2.OCP
기존 구성요소는 수정이 일어나지 말아야 하며, 쉽게 확장해서 재사용을 할 수 있어야 하므로 구현보다는 인터페이스에 의존하도록 설계한다.
모듈별 인터페이스를 두어 코드 재사용이 용이하다. Unit Test 소형 테스트 진행에 수월하다.

<img width="359" alt="image" src="https://github.com/gkdbssla97/yunni-bucks/assets/55674664/c333f588-7561-4b27-afdd-a453af0d6e74">
&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp
<img width="359" alt="image" src="https://github.com/gkdbssla97/yunni-bucks/assets/55674664/dd6c7f4a-172a-4a97-b2b2-e3811f117073">

### 3.ISP
인터페이스의 단일책임을 강조하여 Service, Repository layer 계층 별 서로 다른 성격의 인터페이스를 명백히 분리한다.

<img width="539" alt="image" src="https://github.com/gkdbssla97/yunni-bucks/assets/55674664/706880be-4877-4a8d-a3db-27092e5bbf17">

### 4.DIP
Transitive Dependency가 발생했을 때 상위 레벨의 레이어가 하위 레벨의 레이어를 바로 의존하게 하지 않고 둘 사이에 존재하는 추상레벨을 통해 의존한다. 상위 레벨의 모듈은 하위 레벨의 모듈의 의존성을 벗어나 재사용 및 확장성을 보장받는다.

<img width="538" alt="image" src="https://github.com/gkdbssla97/yunni-bucks/assets/55674664/d7296b2a-496d-4487-b532-114976ecec9b">


### Unit / Integration Test 비교
#### Service Result (Fake Object 사용)

- *Card (기존 Test 대비 약 5배 단축)*   
<img width="339" alt="Untitled" src="https://github.com/gkdbssla97/yunni-bucks/assets/55674664/fe0c4374-1b7d-4929-8780-138ec6de4b5f">
<img width="329" alt="Untitled" src="https://github.com/gkdbssla97/yunni-bucks/assets/55674664/cbd263db-478f-45ac-a373-496aefbbaece">

- *Payments (기존 Test 대비 약 12배 단축)*  
<img width="364" alt="image" src="https://github.com/gkdbssla97/yunni-bucks/assets/55674664/d714ebeb-7c3a-482b-8c60-975b833077e6">
<img width="394" alt="image" src="https://github.com/gkdbssla97/yunni-bucks/assets/55674664/68744969-81d8-444c-9957-66f48d71a0a6">

- Fake Repository
1. atomicGeneratedId: AtomicLong을 사용하여 고유한 ID 값을 생성하기 위한 변수
2. data: CardPayment DB 대신 사용하는 ArrayList로 객체들을 저장하는 리스트
이렇게 In-Memory 방식으로 save 할 수 있다.
<img width="351" alt="image" src="https://github.com/gkdbssla97/yunni-bucks/assets/55674664/f938fadb-2003-4768-8302-4b4d6a3338cc">

3. 각 CRUD 메서드 구현, Stream & Lambda 식으로 작성하였고, Collections.toList() -> toList() Java17 기능 활용
<img width="399" alt="image" src="https://github.com/gkdbssla97/yunni-bucks/assets/55674664/67dcef06-6391-4844-9b3b-2c6ddeb99d98">

#### Controller Result (Fake Object, Test Container 사용)
- *Card (기존 Test 대비 약 5배 단축)*
<img width="364" alt="image" src="https://github.com/gkdbssla97/yunni-bucks/assets/55674664/3cd88a42-239d-47cc-a206-b5b39c689220">
<img width="374" alt="image" src="https://github.com/gkdbssla97/yunni-bucks/assets/55674664/17e38b84-aa0d-4316-9103-d2a3709b1ce4">

- *Payments (기존 Test 대비 10배 단축)*
<img width="364" alt="image" src="https://github.com/gkdbssla97/yunni-bucks/assets/55674664/f5fb1dbc-3d29-4530-b4fe-fcb0d75bcfec">
<img width="374" alt="image" src="https://github.com/gkdbssla97/yunni-bucks/assets/55674664/782e332b-19cd-4d1a-87c0-9ca6054fc2ed">

- Test Container
  
TestPayContainer 클래스에 FakePayRepository, FakeCardRepository 는실제 DB 대신 메모리 내에서 데이터를 저장하고 제공하는 가짜 객체이다.
또한, FakeUuidHolder, FakeTossApiService, FakeOcrApiService 등은 각각 UUID 생성, Toss API 호출, OCR API 호출과 같은 외부 서비스와의 상호작용을 가짜로 대체하는 객체다. 
이렇게 함으로써 DB 의존성 없이도 테스트를 수행하고, 외부 서비스와의 의존성 없이 원하는 결과를 반환하거나 동작을 검증해 독립적인 단위 테스트를 수행할 수 있다.

<img width="428" alt="image" src="https://github.com/gkdbssla97/yunni-bucks/assets/55674664/0e960804-2d9c-41ae-9853-03c66f67d1d9">

### 구현기능 및 문서
#### WebHook (Slack Notification)
- API 요청 알림 및 에러 로그 알림
<img width="276" alt="image" src="https://github.com/gkdbssla97/yunni-bucks/assets/55674664/c0b8dc39-354a-4984-916b-bf0293751ab7">
<img width="289" alt="image" src="https://github.com/gkdbssla97/yunni-bucks/assets/55674664/99eb62cb-8e7c-48dc-bed8-388e4bbbe688">
<img width="438" alt="image" src="https://github.com/gkdbssla97/yunni-bucks/assets/55674664/fd2265c2-ea12-42c2-9ae0-d42c7240c2f6">

- 동기식 / 비동기식 속도 비교

현재 요청 정보(HttpServletRequest)를 가져오기 위해 RequestContextHolder와 ServletRequestAttributes를 사용
threadPoolExecutor에서 비동기적으로 sendSlackMessage() 메서드를 실행
proceedingJoinPoint.proceed()를 호출하여 원래의 메서드 실행을 계속하고 결과값을 반환

<img width="500" alt="image" src="https://github.com/gkdbssla97/yunni-bucks/assets/55674664/4d2a1711-ef3f-4d35-8702-62adccc03e13">
<img width="500" alt="image" src="https://github.com/gkdbssla97/yunni-bucks/assets/55674664/01f7eace-f3ac-4ce6-81c2-a329387c6e45">

#### Spring Rest Docs
- Spring Rest Docs (Card, Payments Domain)

<img width="310" alt="image" src="https://github.com/gkdbssla97/yunni-bucks/assets/55674664/11f5dd23-20ba-4be5-840f-d95638fca8d1">
<img width="279" alt="image" src="https://github.com/gkdbssla97/yunni-bucks/assets/55674664/de35a375-b086-4a64-a505-89105a735892">
<img width="411" alt="image" src="https://github.com/gkdbssla97/yunni-bucks/assets/55674664/a0ff145a-c7d8-4907-92d4-e94f1a9ce285">

mockMvc.perform() 메서드가 반환하는 객체로, 이후의 검증과 문서화 작업을 위한 연산들을 체인 방식으로 호출
응답 결과를 기반으로 API 문서 스니펫을 생성 "card-create"는 생성될 스니펫 파일명
요청 헤더 중 'Authorization' 헤더에 대한 설명을 추가
요청/응답 본문의 필드들에 대한 설명을 추가
getCardRequests()/Responses() 메소드에서 FieldDescriptor 목록을 반환하도록 구현

### 플랜
1. 고민한 점: 아키텍처 설계 고민을 했다. 비즈니스 로직에 따른 테이블 구성 및 플로우 차트에 신경 썼다. 자바 OOP 패턴을 적용하는데에 초점을 두었다.
2. 개선할 점: 쿼리 성능 개선 및 예외처리의 사각지대가 있다. 확장성을 위해 단일 책임 원칙을 좀 더 세분화 해야한다.
3. 트래픽 부하 테스트, 최단거리 배달 가게 알고리즘 구현, Pay 충전 및 결제 수단 추가
