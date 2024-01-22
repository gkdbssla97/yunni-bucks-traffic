# ☕️ YUNNI-BUCKS-TRAFFIC
#### 이 프로젝트는 YUNNI-BUCKS에 백엔드 고도화를 위한 개선작업을 진행하고 있습니다.
#### [YUNNI-BUCKS 프로젝트 세부 사항](https://github.com/gkdbssla97/yunni-bucks)

1. 개발 기간 : 2023-07 ~ 2022-09 *(MVP기능 구현 완료)* </br>
2. 개발 기간 : 2022-10 ~ *(트래픽 상황 대처 프로젝트 고도화)*
--- 
### Architecture
<img width="864" alt="image" src="https://github.com/gkdbssla97/yunni-bucks-traffic/assets/55674664/b626e81c-e074-441d-9005-8b911df5d803">

| Local Server            |       Docker        |       Utility       |
  |:-------------------:|:-------------------:|:---:|
  | SpringBoot 2.7.14 |  MySQL 8.2.0 (M)	   | MySQL Exporter (M)  |
  | Java 17 |   MySQL 8.2.0 (S)   | MySQL Exporter (S)  |
  |        |   PostgreSQL 16.1   | Prometheus, Grafana 
  |        |     Redis 7.2.3     |    Flyway 8.4.4     |
---

### Traffic 개요
#### 대용량 데이터 검색 성능 개선
- PostgreSQL 검색 전용 DB로 역할 분배
  - Full Text Search
    - GIN INDEX
    - tsvector, tsquery
- Master-Slave Replication으로 MySQL Read/Write 역할 분리
  - Prometheus로 Metric 수집
    - Mysql Exporter
    - Spring Actuator
  - Grafana 활용하여 Prometheus Metric 시각화 처리
  
#### 동시성 처리
- 멀티 쓰레드 동작 중 발생 가능 문제점 해결 방안
  - 낙관 락(Optimistic Lock)
  - 비관 락(Pessimistic Lock)
  - 분산 락(Redisson, Distributed Lock)
#### Redis 활용 성능 개선
- Redis Caching
  - Write-Behind Caching
  - Time-To-Live (TTL) Caching
- Redis zSet
---
### 기능 개선 및 정합성 관리
- 메뉴 리뷰
  - PostgreSQL Full Text Search
  - Master-Slave Replication MySQL 이중화
- 메뉴 주문
  - Optimistic Lock
  - Pessimistic Lock
  - Distributed Lock
- 선착순 쿠폰 발급
  - .
- 조회
  - 전체 메뉴
    - Redis Cache 활용
      - Write-Behind Caching
      - Time-To-Live (TTL) Caching
  - 인기 메뉴
    - Redis zSet 활용
  
---
### 메뉴 리뷰
#### 1. 메뉴 리뷰 대용량 데이터(10만, 100만)일 경우 검색
- PostgreSQL 활용
  #### 구현 이유 
  1. **검색 정확도**: PostgreSQL의 tsvector는 텍스트를 토큰화하고, tsquery는 검색어를 토큰화하여 검색 정확도 향상, PostgreSQL의 어간 추출, 불용어 설정  
  2. **GIN 인덱스**: GIN 인덱스는 토큰에 대한 포인터를 저장하여, 특정 토큰을 가진 데이터를 빠르게 찾을 수 있다. 
  3. **다양한 RDBMS 활용**: 확장 모듈과 SQL 표준 준수로 인한 높은 호환성 제공으로 시스템의 확장성 기대

  #### 검색 기법
  
  1. **LIKE 연산자를 사용한 검색**: 가장 기본적인 문자열 검색 방식으로 와일드카드 검색 수행. <br>Full Table Scan은 테이블의 모든 행을 검사하기 때문에 데이터 양이 증가함에 따라 성능이 선형적으로 감소
  2. **ts_vector와 plainto_tsquery를 사용한 Full Text Search**: tsvector는 텍스트를 '단어'로 분할하고, 이를 정규화시킴. 이 단어들은 GIN 인덱스에 포인터를 저장하여 쿼리 시 각 단어를 효율적으로 검색 to_tsquery는 검색 쿼리를 tsvector 형식으로 변환하여, 인덱스에서 빠르게 검색
  
  | 구분(Menu Review)            | 100,000개 | 1,000,000개 |
  |---------|------------|---------|
  | Full Table Scan | 726 ms	 | 11.927 sec |
  | Full Text Search | 493 ms  | 4.264 sec  |
  | 처리속도 비교       | -233 ms | -7.663 sec |

> #### 시나리오
> 1. 사용자가 특정 메뉴의 리뷰를 keyword로 검색 (ex. 달콤한, 맛 없는, 푸짐한 양 ...)
> 2. 검색 요청이 들어오면, 먼저 keyword를 `plainto_tsquery` 함수를 이용해 `tsquery` 형식으로 변환. keyword는 공백 기준 단어로 분할되고, 각 단어는 정규화
> 3. 변환된 `tsquery`를 사용하여, `tsvector` 칼럼에 저장된 리뷰 텍스트와 매칭(`@@`), `GIN 인덱스`를 활용하여 효율적인 검색 수행
> 4. 매칭된 리뷰들을 반환. Full Text Search는 keyword가 포함된 리뷰를 빠르게 찾아내므로, 사용자는 원하는 리뷰 정보를 즉시 응답받음

- Master-Slave 구조 활용
  #### 구현 이유
  1. **데이터 안정성**: Master DB는 Write 작업을 처리하고, Slave DB는 Read 작업을 처리함으로써 부하 분산이 가능해질거라 판단
  2. **데이터 확장성**: Master DB에 문제가 발생한 경우, Slave DB를 Master로 승격시켜 서비스의 중단 없이 운영 (Failover & Failback)

  - Slave로 MySQL 선택한 이유
    - 일반적으로 Master-Slave 복제 방식은 같은 RDBMS 간에서만 가능
      - 각 RDBMS가 고유의 데이터 저장 방식과 통신 프로토콜을 가지고 있기 때문
  - Slave로 PostgreSQL 사용하지 않은 이유
    - MySQL에서 PostgreSQL로 데이터를 복제하려면 데이터 변환 및 동기화를 처리할 수 있는 도구가 필요하며, Debezium이나 Kafka Connect와 같은 CDC 기반의 도구를 사용 
      - 추가적 기술비용으로 인한 후순위 배치
    - tsvector를 이용한 전문검색 시 한국어를 지원하지 않음
    <br/>
- #### Grafana 
  - Network Traffic Monitoring <br>10만개 데이터 Write/Read 작업 시 Master-Slave를 통해 네트워크 트래픽 부하를 분산시켜서 읽기 작업을 효율적으로 처리하고 있는 것으로 판단</br><br>
  <img width="482" alt="image" src="https://github.com/gkdbssla97/yunni-bucks-traffic/assets/55674664/df23fc8b-3e67-456b-9469-8b80ca136485"> 

    |   DB   |   Write   | Master Read |  Slave Read |
    |:------:|:---------:|:-----------:|:-----------:|
    | Master | 2.10 MB/s | 260.36 kB/s |  3.71 kB/s  |
    |  Slave | 1.49 MB/s |  8.96 kB/s  | 269.74 kB/s |

  - QPS Monitoring
      
    Master Read &ensp; &emsp; &ensp; &emsp; &ensp; &emsp;&ensp; &emsp;&ensp; &emsp;&ensp; &emsp;&ensp; &emsp;&ensp; &emsp;&ensp; &emsp;&ensp; &emsp;&ensp; &emsp;&ensp; &emsp;&ensp; &emsp;&ensp;Slave Read

    <img width="486" alt="image" src="https://github.com/gkdbssla97/yunni-bucks-traffic/assets/55674664/43bc77de-3a60-4ade-98e5-19dd8e63af67">

    Master와 Slave의 QPS 차이는 각 서버가 처리하는 작업의 양, 즉 서버에 도착하는 트래픽을 대략적으로 나타내므로, 이를 통해 대략적인 부하 분산의 평가를 할 수 있다.<br>
    QPS는 단순히 쿼리 수를 측정하는 지표이기 때문에, 실제 작업의 처리 시간, 응답 시간 등과 같은 다른 요소들을 고려하지 못한다. 또한, 전체 interval에서의 평균 QPS를 측정하기 때문에 Write/Read 작업의 순간적인 QPS를 측정하기 어려움이 있다.

### 메뉴 주문
#### 1. 한 사용자가 여러 개의 주문을 동시에 요청
- Optimistic Lock 활용
    #### 구현 이유
- Optimistic Locking 선택한 이유 
  - 대부분의 상황에서 실제로 동일한 리소스에 대한 동시 요청이 드물게 발생하고, 이런 상황에서는 Optimistic Locking이 더 효율적
  - 낮은 비용으로 높은 동시성을 제공하며, 충돌 발생 시 재시도 로직을 통해 처리


- Pessimistic Lock 사용하지 않은 이유
  - 다중 사용자가 아닌 한 명의 사용자 이므로 충돌이 자주 발생하거나, 데이터 일관성을 보장이 중요한 작업이라 판단하지 않았음
#### 2. 여러 사용자가 음료 A를 주문을 동시에 요청 *(주문 메뉴 재고 감소 및 주문수 증가)*
- Pessimistic Lock 활용
  - Optimistic Lock과 성능 비교 시 비관적 락 우위
  - 사용자 수가 증가함에 따라 낙관적 락과 비관적 락 사이의 처리 속도 차이가 점점 더 벌어질 것으로 예상
- Distributed Lock, Redisson 활용
   lettuce는 계속 락 획득을 시도하는 반면에 redisson은 락 해제가 되었을 때 최소한의 시도를 하기 때문에 Redis의 부하를 줄여주게 된다.
  
    | 구분(Users)            | 100명       | 1000명      |
    |------------|------------|---------|
    | Optimistic Lock | 6.105 sec  | 24.529 sec |
    | Pessimistic Lock | 1.417 sec	 | 7.526 sec  |
    | 처리속도 비교       | -4.69 sec  | -17.00 sec |
    | Distributed Lock | 1.748 sec  | 8.955 sec  |
> #### 시나리오
> 1. 100명의 사용자가 예기치 못하게 동시에 같은 Menu(Beverage)를 주문
> 2. 주문 당 해당 메뉴 주문 수량만큼 재고 감소
> 3. Pessimistic lock을 통해 주문 중 다른 사용자의 주문(Thread) 접근 제한
> 4. Thread 순차적으로 1번 ~ 100번 사용자 주문
>    1. `재고 - 주문 수량 >= 0` 일 경우 주문 완료
>    2. `재고 - 주문 수량 < 0` 일 경우 ExceptionControl 예외처리

  #### 구현 이유
- Pessimistic Locking 선택한 이유
  - 주문 시스템에서는 동시에 여러 사용자가 같은 메뉴를 주문하는 경우, 그 메뉴의 재고 수량을 동시에 변경해야 하는 상황이 발생할 수 있다.
  - 비관적 락을 사용하면 한 번에 하나의 트랜잭션만 해당 메뉴의 재고를 변경할 수 있기 때문에 충돌을 방지할 수 있다.


- Optimistic Lock 사용하지 않은 이유
  - 낙관적 락은 충돌이 비교적 드물게 발생하는 상황에 유용하다. 
  - 주문 시스템의 경우 동시에 여러 사용자가 같은 메뉴를 주문하는 상황이 자주 발생하므로, 낙관적 락을 사용하면 충돌로 인한 롤백이 빈번하게 발생하여 오버헤드가 발생할거라 판단


- Distributed Locking 선택한 이유
  - redisson은 자신이 점유하고 있는 락을 해제할 때 pub/sub방식으로 채널에 메세지를 보내줌으로써 락을 획득해야 하는 스레드들에게 메세지를 전달
  - 단일 DB 환경에서도 사용할 수 있지만 분산 락은 여러 노드에 걸쳐 있는 데이터에 대한 동시성을 제어할 수 있어 분산 DB에서의 확장성 고려하여 테스트
  
#### 생각해 보아야 할 점
- 나머지 스레드(사용자 별 주문 요청)들은 락이 해제될 때까지 대기 상태에 머무른다.
- 이 방식은 동시성 문제를 방지할 수 있지만, 대기 시간이 길어질 수 있다는 단점
- 최대 사용자는 몇 명까지인지 부하테스트 필요 (사용자가 늘어날수록 시간도 기하급수적 증가)
    
  | 구분 | 응답시간      |
  |-----------|-----------|
    | 10명 | 564 ms    |
    | 100명 | 1.417 sec | 
    | 1000명 | 7.526 sec |
  | 1억명 | ? sec     |
---

### 메뉴 조회
#### 1. 사용자가 전체 메뉴를 조회한다.
- Redis Caching 활용
  - @Cacheable, Look-Aside Caching 전략
#### 구현 이유
- Redis Caching 선택한 이유
  - 높은 트래픽을 효율적으로 처리: 사용자가 전체 메뉴를 조회하는 경우, DB에 직접 접근하지 않고 Redis에 캐싱된 데이터를 사용하면, 응답 시간을 크게 단축시키고 DB에 가해지는 부하를 줄일 수 있다.
  - 일관된 사용자 경험 제공: 메뉴 정보는 자주 변경되지 않는 데이터라고 판단했다. Redis Caching을 사용하면, 사용자가 매번 동일한 데이터를 조회할 때 일관된 정보를 빠르게 제공할 수 있다.
  

- DB Lock 사용하지 않은 이유
  - 단순히 데이터를 조회하는 경우 (예: 메뉴 조회)와 같이 데이터의 변경이 없는 상황에서는 DB Lock 없이 Redis Caching만으로도 충분히 빠른 응답 시간과 효율적인 서버 운영

#### 적용 결과
<img src="https://github.com/gkdbssla97/yunni-bucks-traffic/assets/55674664/4f547d8a-2e8c-482c-a081-f02d71273be0" width="500" height="300">
<img src="https://github.com/gkdbssla97/yunni-bucks-traffic/assets/55674664/e5d4887e-4cd3-439b-bd86-c69859c9a99b" width="500" height="300">

*아래 값은 작업 시작 시간 에서 종료 시간까지의 평균 값으로 산출*

| 구분             | TPS        | 응답시간(ms) |
|----------------|------------|---------|
| 레디스 캐싱 전략 사용 전 | 1247.75	   | 520.58  |
| 레디스 캐싱 전략 사용 후 | 2494.0     | 184.26  |
| 속도 개선 증가       | 2.0배 (TPS) | 2.83배   |

 - caching miss 했을 때 응답시간 193ms, caching hit 했을 때 응답시간 175ms
 - caching hit 시 응답시간이 약 9.3% 단축

#### 2. 사용자가 인기 메뉴를 조회한다.
- Redis zSet 활용
> #### 시나리오
> 0. 특정 메뉴를 조회하면 조회수가 1만큼 증가한다. 
> 1. 주문 시 주문 메뉴의 주문수를 1만큼 증가한다. 
> 2. 정렬 기준에 따라 인기 메뉴를 조회한다. (최상위 3개)
>    1. 조회수가 가장 높은 순으로 메뉴 3개를 내림차순 정렬
>    2. 조회수가 같을 경우 주문수가 가장 높은 순으로 내림차순 정렬
>    3. 주문수가 같을 경우 key(menuTitle)를 사전순 정렬

#### 구현 이유
- Redis zSet 선택한 이유
  - 실시간 처리: Redis는 실시간으로 데이터를 처리한다. 메뉴의 조회수가 변경될 때마다 즉시 ZSET의 스코어를 업데이트할 수 있다.
  - 정렬 기능: zSet은 스코어에 따라 자동으로 메뉴를 정렬한다. 조회수를 Score로 사용하면, 인기 메뉴를 스코어가 높은 순서로 쉽게 조회할 수 있다고 판단했다.
  - 동시성 처리: Redis는 단일 스레드 모델을 사용하며, atomic operations를 지원한다. 따라서, 여러 사용자가 동시에 인기 메뉴를 조회하거나, 조회수를 업데이트하더라도 데이터의 일관성을 유지할 수 있다.
  

#### 생각해 보아야 할 점
- Redis zSet은 하나의 스코어를 기준으로 정렬하는 것이 일반적이다. 하지만 주문량과 조회수와 같은 두 가지 지표를 모두 고려하는 것이 메뉴의 인기도를 판단하는 데 더욱 정확할 것이라 판단했다.
- 여러 지표를 조합하면, 단일 지표를 사용할 때보다 성능이 저하될 수 있다. 그러나 이런 성능 저하는 레디스를 통해 메뉴 정보를 캐싱함으로써 최소화할 수 있다.
- 성능 저하는 Redis에 메뉴의 주문량과 조회수를 모두 캐싱해 인기도를 계산했다.

#### 인기메뉴 조회 (상위 3개 메뉴)
>  *총 4개의 메뉴 중 조회수, 주문량을 종합해 인기메뉴 3개를 조회한다.*
> <img width="639" alt="image" src="https://github.com/gkdbssla97/yunni-bucks-traffic/assets/55674664/7a725c55-9382-4637-aa68-f01b658584b9">
<br>

<img width="403" height="360" alt="image" src="https://github.com/gkdbssla97/yunni-bucks-traffic/assets/55674664/174bc528-6ba0-4a23-851b-2c40f50233c7"/>

- 가장 높은 조회수(*5*)를 기록한 `빵1` 최상위 1번에 위치
- 조회수 동점을 이룬 `빵2`와 `빵3`중 주문량이 높은 `빵3`이 2번 위치
- 전체 메뉴 중 상위 2개를 제외한 `빵2`가 그 다음 3번 위치

<img width="403" alt="image" src="https://github.com/gkdbssla97/yunni-bucks-traffic/assets/55674664/8d5b3889-2e7e-4aa1-b29c-b4f324aec544"/>

redis-cli → ranking 이름의 Sorted Set(ZSET)에서, Score(조회수)가 0에서 10 사이인 요소들을 내림차순 조회

---
### 인기메뉴 데이터 관리
- 일일 인기메뉴 기준
### 1. Redis의 Score를 기준으로 인기순위를 정한다.
- 기본적인 로직은 Redis에서 제공하는 opsForZSet() 메서드는 Sorted Set 자료구조를 활용하여 데이터를 저장한다.
- 점수(score)를 기준으로 데이터의 순위 정보를 관리하므로, 인기 메뉴의 순위를 레디스에 저장하고 관리하는 데 적합하다.

### 2. 과거 메뉴 정보는 RDB에 write-back하고, 당일 메뉴 정보는 Redis에 저장한다.
- 이전 데이터를 RDB에서 가져와 캐싱하고, 당일 데이터를 Redis에 보관하며 검색할 때마다 score를 1씩 증가한다.
- 동점일 경우, 메뉴 조회시 캐싱된 메뉴 주문수량 내림차순 기준으로 인기메뉴를 정렬한다.
> 당일 데이터 Redis 사용법

1. 매번 주문을 할 때마다 Redis에 zSetOperations의 incremetScore 진행
2. 오늘이 끝날 때에 RDB에 write back 값 저장
3. Redis 비우기 `redisTemplate.delete("menu::*");`

> 스케쥴러를 사용해 당일(00시 00분)이 됐을 때  RDB, Redis 순차적 업데이트 과정
```java
  @Scheduled(cron = "0 0 0 * * *") // 매일 00시 00분에 실행
  @Transactional
  public void refreshPopularMenusInRedis() {...}
  ```
  
#### RDB
1. 매일 자정이 되면, Redis에서 `menu::`로 시작하는 모든 키를 찾는다. 이 키들은 인기 메뉴 데이터를 나타낸다.
2. 이 키들을 찾은 후, 각 키에 해당하는 값을 가져온다. 값은 메뉴 score로, 인기 메뉴의 정보를 담고 있다.
3. 각 인기 메뉴에 대해 해당 메뉴의 제목을 기반으로 RDB에서 같은 메뉴를 찾는다. 동시에, Redis의 Sorted Set에서 해당 메뉴의 인기 점수(score)를 가져온다.
4. 만약 RDB에서 메뉴를 찾고, 그 메뉴의 인기 점수를 Redis에서 성공적으로 가져왔다면, RDB의 메뉴 정보에 업데이트한다.
   1. RDB에서 해당 메뉴를 찾지 못하거나 인기 점수를 가져오지 못한 경우에는, Redis에서 가져온 인기 메뉴 정보를 그대로 RDB에 저장한다.

#### Redis
1. 매일 자정이 되면, Redis에서 `menu::`로 시작하는 모든 키를 찾아 삭제하여 새로운 일일 데이터를 위한 공간을 만든다.
2. 모든 메뉴의 score는 0으로 초기화된다.
- 장점
  - 자주 접근되는 데이터나 실시간성이 중요한 데이터를 Redis에 저장하면 전반적인 시스템 성능을 크게 향상
    - Redis의 순위 정보는 검색 또는 주문 시마다 변경 가능
  - 주기적으로 Redis의 데이터를 RDBMS에 백업(write-back)하는 과정으로 비상 상황 발생 시 RDBMS에서 데이터를 복구할 수 있다.
  - TTL(Time-To-Live)로 오래된 데이터가 자동으로 삭제되면서 Redis의 메모리 사용량을 효율적으로 관리

[//]: # (* 단점)

[//]: # ()
[//]: # (  * 성능 상의 문제)

[//]: # ()
[//]: # (    * `7일전` ~ `1일전` 까지의 값들을 하나하나 가져와서 zSetOperations에 캐싱)

[//]: # ()
[//]: # (      * O&#40;N&#41; * 6)

[//]: # ()
[//]: # (    * `당일`의 데이터와 `이전` 데이터를 zSetOperations에서 더함)

[//]: # ()
[//]: # (      * O&#40;N&#41;)

[//]: # ()
[//]: # (    * 즉 &#40;O&#40;N&#41; * 날짜수&#41; 가 발생할 것 같았다.)

[//]: # ()
[//]: # (      * Redis는 O&#40;N&#41;을 지양.)

[//]: # ()
[//]: # (    * **대규모 트래픽에 더 맞는 방식이 필요하지 않을까?**)

####  고민할 점
- 일일 인기메뉴는 write-back, TTL(1일)로 해결되지만, 주별, 월별 인기메뉴 조회는 어떻게 처리할까?
---

### 쿠폰 관리
- 신규 회원 가입 시 제공되는 선착순 쿠폰 100장 이벤트를 진행
    - Pessimistic Lock 활용

[//]: # (### 구현 이유)

[//]: # (* PessimisticLock을 통한 비관적 lock 진행)

[//]: # (    * Redis를 사용하지 않은 이유)

[//]: # (        * 한 사람의 포인트에 관한 내용이기 때문에 대량의 트래픽이 걸리거나 많은 충돌이 발생하지 않을 것이라 판단하였기 때문이다.)

[//]: # (        * 한번에 한사람이 자신의 포인트 충전을 하는 것이기 때문.)

[//]: # (    * OptimisticLock을 사용하지 않은 이유)

[//]: # (        * Pessimistic Lock은 작업 도중에 Lock을 걸어 다른 쓰레드의 접근 자체를 차단하기 때문에 versioning을 통해 정합성을 맞추는 Optimistic Lock에 비해 데이터 정합성을 더 잘 보장할 수 있다고 생각했기 때문이다.)

[//]: # (            * 포인트의 경우는 실제 돈과 연결되기 때문에 정합성이 중요하다 판단하였다.)