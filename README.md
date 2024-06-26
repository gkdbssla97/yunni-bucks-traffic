# ☕️ YUNNI-BUCKS-TRAFFIC
#### 이 프로젝트는 YUNNI-BUCKS에 백엔드 고도화를 위한 개선작업을 진행하고 있습니다.
#### [YUNNI-BUCKS 프로젝트 세부 사항](https://github.com/gkdbssla97/yunni-bucks)

1. 개발 기간 : 2023-07 ~ 2023-09 *(MVP기능 구현 완료)* </br>
2. 개발 기간 : 2023-10 ~ *(트래픽 상황 대처 프로젝트 고도화)*
--- 
### Architecture
<img width="864" alt="image" src="https://github.com/gkdbssla97/yunni-bucks-traffic/assets/55674664/89814746-866b-4dff-81d9-92468357888c">

[//]: # (<img width="864" alt="image" src="https://github.com/gkdbssla97/yunni-bucks-traffic/assets/55674664/77f364b3-985f-4094-96ef-c96170b3c482">)
[//]: # (<img width="864" alt="image" src="https://github.com/gkdbssla97/yunni-bucks-traffic/assets/55674664/a2cdb608-eb14-4a6c-b10e-620abb49f499">)
[//]: # (<img width="864" alt="image" src="https://github.com/gkdbssla97/yunni-bucks-traffic/assets/55674664/6b549854-5534-4fbb-8608-a14a49eeaed1">)
[//]: # (<img width="864" alt="image" src="https://github.com/gkdbssla97/yunni-bucks-traffic/assets/55674664/d3a9049e-b005-4c48-a925-aeb0b3a2882d">)
[//]: # (<img width="864" alt="image" src="https://github.com/gkdbssla97/yunni-bucks-traffic/assets/55674664/b626e81c-e074-441d-9005-8b911df5d803">)

#### System Infrastructure Details
| Local Server |  NCP (Docker)   |     AWS EC2     |      Utility       | Monitoring & Testing |
|:---:|:---------------:|:---------------:|:------------------:|:--------------------:|
| SpringBoot 2.7.14 | MySQL 8.2.0 (M) | Jenkins 2.440.1 | MySQL Exporter (M) | Prometheus, Grafana  
| Java 17 | MySQL 8.2.0 (S) |  Tomcat 9.0.87  | MySQL Exporter (S) |       VisualVM       |
|  | PostgreSQL 16.1 |  Vault 1.15.6   |    Flyway 8.4.4    |       nGrinder       |
|  |   Redis 7.2.3   |  Nginx 1.24.0   |             |      SonarQube       |

---
### CI / CD 
#### Pipeline
- 소스코드 관리 및 변경 감지
  - Git Webhook
  - SonarQube
- 자동화된 빌드 및 테스트
  - Jenkins (AWS EC2)
    - Build (.WAR file)
    - Unit Test
- 자동 배포
  - Tomcat (AWS EC2)
  - Nginx 무중단 배포 (예정)
- 데이터베이스 관리
  - Docker (NCP)
#### Nginx
 - Reverse Proxy
   - Load Balancing
   - Caching
   - Scale-Up & Scale-Out
#### Credentials
- HashiCorp Vault
---
### Traffic 개요
#### 대용량 데이터 검색 성능 개선
- PostgreSQL 검색 전용 DB로 역할 분배
  - Full Text Search
    - GIN INDEX
    - tsvector, tsquery
- Master-Slave Replication으로 MySQL Read/Write 역할 분리
  - Slave DB Scale-Out
    - Round-Robin 
    - MHA Failover (예정) 
  - Prometheus로 Metric 수집
    - MySQL Exporter
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
### Integration & Deployment 
| **GitHub** | **Git Webhook** | **Jenkins**  |        **SonarQube**         | **WAR Deployment**  |     **Tomcat**      |  **Monitoring**   |
|:---:|:---:|:------------:|:----------------------------:|:-------------------:|:-------------------:|:-----------------:|
| Code Push | Webhook Trigger | Build & Test | Code Quality Analysis Report | Deploy WAR via HTTP | Unzip & Compile WAR | Grafana, VisualVM |
#### Continuous Integration   
- Jenkins 활용
  #### 사용 이유
  1. **확장성**: Jenkins는 Plugin을 통해 대부분 종류의 개발, 테스트, 배포 작업을 자동화할 수 있다. GitHub, GitLab과 같은 다양한 소스 코드 관리 도구와 통합할 수 있으며, Slack, 이메일 등을 통한 알림 설정 가능
     1. Trigger: Git Webhook이 Jenkins에 Push 알림
  2. **Pipeline**: Jenkins의 파이프라인은 Groovy 기반의 스크립트로 정의될 수 있으며, 빌드, 테스트, 배포 등의 작업을 세밀하게 제어할 수 있다.
  #### Trouble Shooting
  1. **메모리 부족 오류**: Jenkins는 빌드 프로세스 중에 복잡한 프로젝트나 동시에 여러 빌드를 실행할 경우, 메모리 부족으로 인해 빌드 실패 <br>(Free Tier → t2.small scale-up)
  2. **Jenkins Pipeline에서 Tomcat 서버에 접속하기 위한 쉘 스크립트 실행 시도 시, `Permission denied` 오류 발생**:
     Tomcat 서버의 `~/.ssh/authorized_keys`에 Tomcat rsa.pub 공개키 추가,
     Jenkins에 Tomcat 서버용 RSA 개인키를 Credential로 추가하고, 해당 CredentialID를 Pipeline에서 사용
#### Continuous Deployment
- Tomcat 활용
  #### 사용 이유
  1. **분리된 환경**: WAR 파일을 외부 Tomcat에 배포하여 App과 서버 환경을 분리
     1. **자원 할당 최적화**: 외부 Tomcat을 사용하면, 서버의 자원(CPU, 메모리 등) 할당과 관리 자유도 높음 (내장 Tomcat은 JVM 설정에 의존적)
     2. **로드 밸런싱** : 여러 외부 Tomcat 인스턴스를 운영함으로써 트래픽이 급증하는 상황에서도 안정적인 서비스를 제공하는 데 기여
  2. **보안**: 외부 Tomcat 서버를 사용하면, 서버의 보안 설정을 App과 독립적으로 관리할 수 있다. (접근 제어, SSH/SSL 등을 App 변경 없이 수행가능)
  #### Trouble Shooting
     1. **AWS → NCP에 설치된 Docker 안의 DB Container 접근 문제**
        - JSch를 이용한 SSH 터널링: Spring Boot에서 JSch 라이브러리를 사용하여 NCP 서버에 SSH 접속 설정
        - 포트 포워딩 설정: NCP 서버에서 Docker 컨테이너로 포트 포워딩을 설정하여, 특정 포트를 통해 DB 컨테이너에 접근

          | MySQL(Master) | MySQL(Slave1) | MySQL(Slave2) | PostgreSQL | Redis |
          |:-------------:|:-------------:|:---:|:---:|:---:|
          | 3306:3306 |   3307:3306   |   3308:3306   | 5432:5432 | 6379:6379 |
     2. **Tomcat WAR 파일 최대 업로드 크기 문제**
        - server.xml 수정: conf/server.xml 수정하여 \<Connector> 태그 내의 maxPostSize 속성 값을 _52428800(50MB)_ 에서 _157286400(150MB)_ 로 변경 (배포 WAR file 80.1MB 용량 초과)
     3. **Tomcat App 실행 중 자동 배포**
        > #### 시나리오
        > 1. **Tomcat 프로세스 확인**: 배포 script는 실행 중인 Tomcat App의 PID 확인 (`TOMCAT_PID=$(ps -ef | grep tomcat | grep -v grep | awk '{print $2}')`)
        > 2. **프로세스 종료**: 실행 중인 Tomcat이 있을 경우 `kill -15 $TOMCAT_PID`로 프로세스에게 종료 요청 후 프로세스가 완전히 종료될 때까지 대기 <br>(`kill -9`는 프로세스가 SIGTERM에 반응하지 않거나 강제 종료가 필요한 경우에만 사용)
        > 3. **Tomcat 재시작**: 프로세스 종료 후, `./bin/startup.sh`를 실행하여 Tomcat를 재시작하여 새로운 배포 적용
#### Nginx 
- Reverse Proxy 활용
    #### 구현 이유
  1. **Load Balancing**: Nginx의 로드 밸런싱 알고리즘을 활용하여 톰캣 서버 간에 트래픽을 효율적으로 분산시켜 성능을 최적화할 수 있다고 판단
      - Weighted Round Robin
          1. Tomcat-1 : t2.medium(2vCPU 4GB) weight=2
          2. Tomcat-2 : t2.small(1vCPU 2GB) weight=1
  2. **Caching**: 정적 또는 동적 컨텐츠의 일부를 Nginx에서 캐싱함으로써, 반복적인 요청에 대해 빠른 응답 제공, 백엔드 서버의 부하 감소 및 응답 시간 단축

    #### Scale-Up & Scale-Out
  1. **목표**: 최대 1000명까지 안정적인 서버 운영
  2. **과정**: nGrinder로 vUser 수 점진적으로 올리면서 Scale-Up과 Scale-Out의 스케일 조정 
     1. t2.micro(1vCPU 1GB) 단일 Tomcat 100명 Test 실행, Read Time Out 발생 → t2.small(1vCPU 2GB) Scale-Up
     2. t2.small 단일 톰캣 400명 Test 실행, nGrinder CPU 70%, Tomcat CPU 65% 사용 → 1000명 Test 실행, nGrinder/Tomcat CPU Usage 100% 초과 Read Time Out 발생
     3. t2.medium(2vCPU 4GB) 단일 Tomcat 1000명 Test 실행, Nginx의 CPU 사용량은 62%, Tomcat CPU 사용량은 130~140% 유지, 200% 모두 사용하지 못 함
        1. nGrinder가 WAS에 트래픽 부하를 걸지 못한다고 판단 → 사용자 수 2천명 고려하여 nGrinder 4vCPU 8GB Scale-Up
      
        <img width="600" alt="image" src="https://github.com/gkdbssla97/yunni-bucks-traffic/assets/55674664/a066ff5c-3cf7-47b7-b618-c6856f0a896f">
     4. tomcat-2 (t2.small) 증설하여 로드 밸런싱 설정 (weight=2:1비율)<br> Tomcat 서버 2대 모두 CPU 사용량이 191%, 98%로 최대 사용량에 근접했고, Nginx CPU 사용량 역시 85%로 높은 사용량을 보이고 있다.<br>
        <img width="600" alt="image" src="https://github.com/gkdbssla97/yunni-bucks-traffic/assets/55674664/265f8f52-b6d5-4162-a1cf-44d820573d94">
     5. Scale 유지하여 2000명 Test 실행, vUser 대비 TPS가 기대치만큼 나오지 않음
        1. nGrinder CPU 사용량이 약 70%로, WAS 서버를 3대로 증설 또는 Tomcat-2의 Scale-Up 시 2000명도 충분히 트래픽을 버틸거라 판단
    #### vUser별 WAS 서버 스펙 및 TPS 결과
    |   vUser   | 40 | 400 | 1000 | 1000 |   2000    |
   |:--------:|:---:|:---:|:---:|:---------:|:---:|
   | Tomcat-1 | t2.micro | t2.small | t2.medium | t2.medium | t2.medium |
   | Tomcat-2 | X | X | X | t2.small | t2.small  |
   |   TPS    | 84.3 | 1637.4 | 2678.1 | 4116.6 |  3838.2   |
    #### VisualVM CPU, Thread Metric
    <img width="516" alt="image" src="https://github.com/gkdbssla97/yunni-bucks-traffic/assets/55674664/e60918e5-a867-4a8e-9769-1c21c9875190">
    <br>로드 밸런싱을 적용한 후, 빨간선을 기준으로 병목 현상이 감소하였으며, 이는 CPU의 최대 사용률 지속 시간이 늘어난 것으로 확인된다. </br> 또한, 최대치까지 활용된 live thread 수치는 시스템이 높은 요청 처리량을 효율적으로 소화할 수 있음을 시사한다. Heap Size의 증감률과 낮은 GC activity는 현재 메모리 관리가 비교적 잘 이루어지고 있다고 판단

    #### 고민할 점
  - Weighted Round Robin 대신 Least Response Time Method 사용 시 성능 비교
  - 지속적 Scale-Up의 한계
#### Code Quality Analysis
- SonarQube
  #### 사용 이유
  1. **코드 품질 개선**: 개발 과정에서 자동으로 코드 스멜(Code Smells), 버그, 취약점 등을 식별하여 코드의 문제점을 인식하고 개선하여 리포팅할 수 있다.
  2. **팀워크와 코드 품질 문화 증진**: 팀 내에서 코드 리뷰를 촉진하고, 모든 팀원이 코드 리포트를 볼 수 있어 코드 품질에 대한 인식을 높여 협업 증진
#### Credential
- Vault
  #### 사용 이유
  1. **안전한 비밀 관리**: 민감한 정보 (API 토큰, DB 접속 정보, 비밀 키 등)를 암호화하여 저장하고, 권한에 따라 안전하게 접근 제어할 수 있다.<br>Vault로 `.yml`에 공개된 정보의 노출 리스크를 줄일 수 있다고 판단
  2. **중앙화된 비밀 관리**: 모든 정보를 한 곳에서 관리함으로써, 설정 변경이 필요할 때마다 애플리케이션을 재배포해야 하는 불편함 해소 가능

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
  2. **ts_vector와 ts_query를 사용한 Full Text Search**: tsvector는 텍스트를 '단어'로 분할하고, 이를 정규화시킴. 이 단어들은 GIN 인덱스에 포인터를 저장하여 쿼리 시 각 단어를 효율적으로 검색 tsquery는 검색 쿼리를 tsvector 형식으로 변환하여, 인덱스에서 빠르게 검색
  
  | 구분 (MenuReview)            | 100,000개  |  1,000,000개  |
  |:---------:|:------------:|:---------:|
  | Full Table Scan |  726 ms	  |  11.927 sec  |
  | Full Text Search |  493 ms   |  4.264 sec   |
  | 처리속도 비교       |  -233 ms  |  -7.663 sec  |

> #### 시나리오
> 1. 사용자가 특정 메뉴의 리뷰를 keyword로 검색 (ex. 달콤한, 맛 없는, 푸짐한 양 ...)
> 2. 검색 요청이 들어오면, 먼저 keyword를 `plainto_tsquery` 함수를 이용해 `tsquery` 형식으로 변환. keyword는 공백 기준 단어로 분할되고, 각 단어는 정규화
> 3. 변환된 `tsquery`를 사용하여, `tsvector` 칼럼에 저장된 리뷰 텍스트와 매칭(`@@`), `GIN 인덱스`를 활용하여 효율적인 검색 수행
> 4. 매칭된 리뷰들을 반환. Full Text Search는 keyword가 포함된 리뷰를 빠르게 찾아내므로, 사용자는 원하는 리뷰 정보를 즉시 응답받음

- Master-Slave 구조 활용
  #### DB 클러스터링(동기 복제) vs Replication(비동기 복제)
  1. **동기 복제 방식**: 동기 복제는 모든 노드에 데이터 변경이 반영되어야만 트랜잭션이 커밋되는 방식이라 정합성을 보장하지만 전체 시스템의 성능 저하 우려
  2. **비동기 복제 방식**: Master 노드에서 변경된 데이터를 지연 없이 빠르게 Slave 노드로 복제한다. <br>실시간으로 대용량 읽기 작업이 많은 메뉴 리뷰는 비동기 복제가 적합하다 판단
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
  
  #### Slave DB Scale-Out
  1. **Slave 서버 증설**: 웹 서버의 트래픽 증가로 인해 Read 작업의 부하를 감당하기 어려워질 수 있다. Scale-Out으로 각 서버가 처리하는 트래픽을 줄여 성능 향상과 데이터를 분산 저장해 안정성을 높일 수 있다고 판단
     1. Round-Robin: 각 Slave 데이터 소스를 공평하게 사용하여, 특정 데이터 소스에 과도한 부하를 방지한다.
  2. **Failover**: Slave 서버는 최소 2대 이상이어야 하며, Master 서버에 장애가 발생했을 때, Slave 서버 중 하나를 새로운 Master로 승격시키고, 나머지 Slave 서버들이 새로운 Master를 참조할 수 있어야 한다. (예정)
  <br/>
- #### Grafana 
  - Network Traffic Monitoring <br>10만개 데이터 Write/Read 작업 시 Master-Slave를 통해 네트워크 트래픽 부하를 분산시켜서 읽기 작업을 효율적으로 처리하고 있는 것으로 판단</br><br>
  <img width="482" alt="image" src="https://github.com/gkdbssla97/yunni-bucks-traffic/assets/55674664/df23fc8b-3e67-456b-9469-8b80ca136485"> 

    |   DB   |   Write   | Master Read |  Slave Read |
    |:------:|:---------:|:-----------:|:-----------:|
    | Master | 2.10 MB/s | 260.36 kB/s |  3.71 kB/s  |
    |  Slave | 1.49 MB/s |  8.96 kB/s  | 269.74 kB/s |

  - QPS Monitoring
      
    Master Read &ensp; &emsp; &ensp; &emsp; &ensp; &emsp;&ensp; &emsp;&ensp; &emsp;&ensp; Slave Read

    <img width="486" alt="image" src="https://github.com/gkdbssla97/yunni-bucks-traffic/assets/55674664/43bc77de-3a60-4ade-98e5-19dd8e63af67">

    Master-Slave 구조에서 Master와 Slave의 QPS가 각각 2.0과 3.2로 분산되었는데, Master는 쓰기 작업에 더 많은 부하를 갖고 있고, Slave는 읽기 작업에 더 많은 부하를 갖고 있다. 이를 통해 읽기 및 쓰기 작업이 적절하게 분산되었다고 보여진다.<br>
    QPS는 단순히 쿼리 수를 측정하는 지표이기 때문에, 실제 작업의 처리 시간, 응답 시간 등과 같은 다른 요소들을 고려하지 못한다. 또한, 전체 interval에서의 평균 QPS를 측정하기 때문에 Write/Read 작업의 순간적인 QPS를 측정하기 어려움이 있다.
---
### 메뉴 주문
#### 1. 한 사용자가 여러 개의 주문을 동시에 요청
- Optimistic Lock 활용
    #### 구현 이유
- Optimistic Lock 선택한 이유 
  - 대부분의 상황에서 실제로 동일한 리소스에 대한 동시 요청이 드물게 발생하고, 이런 상황에서는 Optimistic Locking이 더 효율적
  - 낮은 비용으로 높은 동시성을 제공하며, 충돌 발생 시 재시도 로직을 통해 처리


- Pessimistic Lock 사용하지 않은 이유
  - 다중 사용자가 아닌 한 명의 사용자 이므로 충돌이 자주 발생하거나, 데이터 일관성을 보장이 중요한 작업이라 판단하지 않았음
#### 2. 여러 사용자가 음료 A를 주문을 동시에 요청 *(주문 메뉴 재고 감소 및 주문수 증가)*
- Pessimistic Lock 활용
  - Optimistic Lock과 성능 비교 시 비관적 락 우위
  - 사용자 수가 증가함에 따라 낙관적 락과 비관적 락 사이의 처리 속도 차이가 점점 더 벌어질 것으로 예상
- Distributed Lock, Redisson 활용
   Lettuce는 계속 락 획득을 시도하는 반면에 Redisson은 락 해제가 되었을 때 최소한의 시도를 하기 때문에 Redis의 부하를 줄여주게 된다.
  
    | 구분 (Users)            |     100명     |    1000명     |
    |:------------:|:------------:|:---------:|
    | Optimistic Lock |  6.105 sec   |  24.529 sec  |
    | Pessimistic Lock |  1.417 sec	  |  7.526 sec   |
    | 처리속도 비교       |  -4.69 sec   |  -17.00 sec  |
    | Distributed Lock |  1.748 sec   |  8.955 sec   |
> #### 시나리오
> 1. 100명의 사용자가 예기치 못하게 동시에 같은 Menu(Beverage)를 주문
> 2. 주문 당 해당 메뉴 주문 수량만큼 재고 감소
> 3. Pessimistic Lock을 통해 주문 중 다른 사용자의 주문(Transaction) 접근 제한
> 4. Thread 순차적으로 1번 ~ 100번 사용자 주문
>    1. `재고 - 주문 수량 >= 0` 일 경우 주문 완료
>    2. `재고 - 주문 수량 < 0` 일 경우 `ExceptionHandler` 예외처리

  #### 구현 이유
- Pessimistic Lock 선택한 이유
  - 주문 시스템에서는 동시에 여러 사용자가 같은 메뉴를 주문하는 경우, 그 메뉴의 재고 수량을 동시에 변경해야 하는 상황이 발생할 수 있다.
  - 비관적 락을 사용하면 한 번에 하나의 트랜잭션만 해당 메뉴의 재고를 변경할 수 있기 때문에 충돌을 방지할 수 있다.


- Optimistic Lock 사용하지 않은 이유
  - 낙관적 락은 충돌이 비교적 드물게 발생하는 상황에 유용하다. 
  - 주문 시스템의 경우 동시에 여러 사용자가 같은 메뉴를 주문하는 상황이 자주 발생하므로, 낙관적 락을 사용하면 충돌로 인한 롤백이 빈번하게 발생하여 오버헤드가 발생할거라 판단


- Distributed Lock 선택한 이유
  - Redisson은 자신이 점유하고 있는 락을 해제할 때 Pub/Sub방식으로 채널에 메세지를 보내줌으로써 락을 획득해야 하는 쓰레드들에게 메세지를 전달
  - 단일 DB 환경에서도 사용할 수 있지만 분산 락은 여러 노드에 걸쳐 있는 데이터에 대한 동시성을 제어할 수 있어 분산 환경 확장성 고려하여 테스트
  
#### 생각해 보아야 할 점
- 나머지 쓰레드(사용자 별 주문 요청)들은 락이 해제될 때까지 대기 상태에 머무른다.
- 이 방식은 동시성 문제를 방지할 수 있지만, 대기 시간이 길어질 수 있다는 단점
- 최대 사용자는 몇 명까지인지 부하테스트 필요 (사용자가 늘어날수록 시간도 기하급수적 증가)
    
  | Users |    응답시간     |
  |:-----------:|:-----------:|
    | 10명 |   564 ms    |
    | 100명 |  1.417 sec  | 
    | 1000명 |  7.526 sec  |
  | 1억명 |    ? sec    |
---

### 메뉴 조회
#### 1. 사용자가 전체 메뉴를 조회한다.
- Redis Caching 활용
  - @Cacheable, Look-Aside Caching 전략
  - 최신 메뉴 등록 기준 5Page 이하만 Caching 처리
    - 유저들이 최신 메뉴를 우선적으로 볼 것이라 판단
#### 구현 이유
- Redis Caching 선택한 이유
  - 높은 트래픽을 효율적으로 처리: 사용자가 전체 메뉴를 조회하는 경우, DB에 직접 접근하지 않고 Redis에 캐싱된 데이터를 사용하면, 응답 시간을 크게 단축시키고 DB에 가해지는 부하를 줄일 수 있다.
  - 일관된 사용자 경험 제공: 메뉴 정보는 자주 변경되지 않는 데이터라고 판단했다. Redis Caching을 사용하면, 사용자가 매번 동일한 데이터를 조회할 때 일관된 정보를 빠르게 제공할 수 있다.
  

- DB Lock 사용하지 않은 이유
  - 단순히 데이터를 조회하는 경우 (예: 메뉴 조회)와 같이 데이터의 변경이 없는 상황에서는 DB Lock 없이 Redis Caching만으로도 충분히 빠른 응답 시간과 효율적인 서버 운영

#### 적용 결과
 _Caching 적용 전_
<img width="1089" alt="image" src="https://github.com/gkdbssla97/yunni-bucks-traffic/assets/55674664/0bcb2f4e-e83f-4de3-82b0-e14d99678325"><br>
 _Caching 적용 후_
<img width="1085" alt="image" src="https://github.com/gkdbssla97/yunni-bucks-traffic/assets/55674664/37fd5ace-0d7d-42ec-abf0-03709894c9b5">
*아래 값은 작업 시작 시간 에서 종료 시간까지의 평균 값으로 산출 (nGrinder 1분 측정)*

|        구 분        |   TPS    |  응답시간(ms)  |
|:----------------:|:--------:|:----------:|
|  레디스 캐싱 전략 사용 전  |  171.4	  |   55.67    |
|  레디스 캐싱 전략 사용 후  |  339.2   |   28.15    |
|     속도 개선 증가     |  2.0 배   |   2.0 배    |

- (Caching Miss 시 응답시간 - Caching Hit 시 응답시간) / Caching Miss 시 응답시간 * 100% → 캐싱 적용 후 응답시간이 캐싱 적용 전의 약 **49.4%** 단축

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
  - 동시성 처리: Redis는 단일 쓰레드 모델을 사용하며, atomic operations를 지원한다. 따라서, 여러 사용자가 동시에 인기 메뉴를 조회하거나, 조회수를 업데이트하더라도 데이터의 일관성을 유지할 수 있다.

#### 생각해 보아야 할 점
- Redis zSet은 하나의 스코어를 기준으로 정렬하는 것이 일반적이다. 하지만 주문량과 조회수와 같은 두 가지 지표를 모두 고려하는 것이 메뉴의 인기도를 판단하는 데 더욱 정확할 것이라 판단했다.
- 여러 지표를 조합하면, 단일 지표를 사용할 때보다 성능이 저하될 수 있다. 그러나 이런 성능 저하는 레디스를 통해 메뉴 정보를 캐싱함으로써 최소화할 수 있다.
- 성능 저하는 Redis에 메뉴의 주문량과 조회수를 모두 캐싱해 인기도를 계산했다.

#### 인기메뉴 조회 시 RDB
>  *총 4개의 메뉴 중 조회수, 주문량을 종합해 인기메뉴 3개를 조회한다.*
> <img width="639" alt="image" src="https://github.com/gkdbssla97/yunni-bucks-traffic/assets/55674664/7a725c55-9382-4637-aa68-f01b658584b9">

#### 인기메뉴 조회 시 Response Body

<img width="298" alt="image" src="https://github.com/gkdbssla97/yunni-bucks-traffic/assets/55674664/15cfcb0d-65b5-4d31-a620-7fb037282737">

- 가장 높은 조회수(*5*)를 기록한 `빵1` 최상위 1번에 위치
- 조회수 동점을 이룬 `빵2`와 `빵3`중 주문량이 높은 `빵3`이 2번 위치
- 전체 메뉴 중 상위 2개를 제외한 `빵2`가 그 다음 3번 위치
#### 인기메뉴 조회 시 Redis (score만 반영)
<img width="403" alt="image" src="https://github.com/gkdbssla97/yunni-bucks-traffic/assets/55674664/8d5b3889-2e7e-4aa1-b29c-b4f324aec544"/>

redis-cli → ranking 이름의 Sorted Set(ZSET)에서, Score(조회수)가 0에서 10 사이인 요소들을 내림차순 조회

---
### 인기메뉴 데이터 관리
- 일일 인기메뉴 기준
#### 1. Redis의 Score를 기준으로 인기순위를 정한다.
- 기본적인 로직은 Redis에서 제공하는 opsForZSet() 메서드는 Sorted Set 자료구조를 활용하여 데이터를 저장한다.
- 점수(score)를 기준으로 데이터의 순위 정보를 관리하므로, 인기 메뉴의 순위를 레디스에 저장하고 관리하는 데 적합하다.

#### 2. 과거 메뉴 정보는 RDB에 write-back하고, 당일 메뉴 정보는 Redis에 저장한다.
- 이전 데이터를 RDB에서 가져와 캐싱하고, 당일 데이터를 Redis에 보관하며 검색할 때마다 score를 1씩 증가한다.
- 동점일 경우, 메뉴 조회시 캐싱된 메뉴 주문수량 내림차순 기준으로 인기메뉴를 정렬한다.
> **당일 데이터 Redis 사용법**
> 1. 매번 주문을 할 때마다 Redis에 zSetOperations의 `ZINCRBY` 명령어로 `score` 증가 _(Atomic Operation)_
> 2. 오늘이 끝날 때(자정)에 RDB에 `Write-Back` 값 저장
> 3. Redis Data 비우기, `redisTemplate.delete("AllMenus::*");`

**스케쥴러를 사용해 자정(00:00:00)이 됐을 때  Write-Back Caching**
```java
@Scheduled(cron = "0 0 0 * * *")
public void refreshPopularMenusInRedis() {
    ScanOptions options = ScanOptions.scanOptions().match("AllMenus::*").count(500).build();
    RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
    
    try {
        Cursor<byte[]> cursor = connection.scan(options);
    
        while (cursor.hasNext()) {
            String key = new String(cursor.next());
            CompletableFuture.runAsync(() -> {
                  Optional.ofNullable((Menu) objectRedisTemplate.opsForValue().get(key))
                      ...
          }, threadPoolExecutor).exceptionally(ex -> 
               log.error("Failed to process key: {}", key, ex); ...)
        } cursor.close();
    }
} // 동기식: for-each loop {...})
```
#### 성능 측정 (_Postman_)
![image](https://github.com/gkdbssla97/yunni-bucks-traffic/assets/55674664/5d336b7e-e9e1-4223-9311-e0577e4e6b34)

|      구 분      |  Sync |  Async   | Async+Scan |
|:-------------:|:-----:|:--------:|:----------:|
|     응답시간      | 3.73s | 263.16ms |  88.75ms   |
| 성능개선 (Sync대비) |   -   |  14.2 배  |   42.0 배   |

>**Synchronized → Asynchronized<br>**
I/O 작업을 동기적으로 처리하면, 작업이 완료될 때까지 쓰레드가 대기 상태가 되어야 하므로, 쓰레드의 CPU 사용률이 낮아진다.<br>
> - `CompletableFuture` 비동기 처리 사용 이유
>   1. **효율성**: 메인 쓰레드가 별도의 작업 쓰레드의 완료를 기다리지 않고 다음 작업을 계속 진행하여 쓰레드의 CPU 사용률을 높일 수 있다고 판단하여 적용
>   2. **에러 처리**: 비동기 처리가 실패한 경우 감지할 수 있게 예외처리 가능<br>
> - `ThreadPoolExecutor` 사용 이유
>   1. **커스텀 설정**: ThreadPoolExecutor의 설정을 직접 관리함으로써, 어플리케이션의 특성에 맞게 ThreadPool의 동작 제어
>   2. **공유 리소스 관리**: commonPool에서 쓰레드를 과도하게 사용하여 시스템 전체의 성능이 저하되는 것을 방지하기 위해 특정 작업에 대해 별도의 ThreadPool을 사용
> - `scan 명령어` 사용 이유
>   1. **Blocking 최소화**: Redis는 Single Thread 구조로 동작하고, keys 명령어는 모든 키를 찾을 때까지 Redis를 Blocking 한다. 이는 다른 클라이언트의 요청 처리가 지연될 수 있다.<br>`scan 명령어`는 일정량(count)의 키만 반환하여 Timeout 발생 할 확률 낮춤 
>
> _`parallelStream()`을 사용하더라도 병렬 쓰레드는 I/O 작업 대기시간을 없앨 수 없기에 사용 X_

#### RDB
1. 매일 자정이 되면, Redis에서 `AllMenus::`로 시작하는 모든 키를 찾는다. 이 키들은 인기 메뉴 데이터를 나타낸다.
2. 이 키들을 찾은 후, 각 키에 해당하는 값을 가져온다. 값은 메뉴 score로, 인기 메뉴의 정보를 담고 있다.
3. 각 인기 메뉴에 대해 해당 메뉴의 제목을 기반으로 RDB에서 같은 메뉴를 찾는다. 동시에, Redis의 Sorted Set에서 해당 메뉴의 인기 점수(score)를 가져온다.
4. 만약 RDB에서 메뉴를 찾고, 그 메뉴의 인기 점수를 Redis에서 성공적으로 가져왔다면, RDB의 메뉴 정보에 업데이트한다.
   1. RDB에서 해당 메뉴를 찾지 못하거나 인기 점수를 가져오지 못한 경우에는, Redis에서 가져온 인기 메뉴 정보를 그대로 RDB에 저장한다.

#### Redis
1. 매일 자정이 되면, Redis에서 `AllMenus::`로 시작하는 모든 키를 찾아 삭제하여 새로운 일일 데이터를 위한 공간을 만든다.
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
