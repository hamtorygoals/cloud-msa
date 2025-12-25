# 🎫 Cloud-based Ticketing System (MSA)

본 프로젝트는 대규모 동시 접속 환경에서의 좌석 예매 문제를 가정하여,  
클라우드 환경에서 **마이크로서비스 아키텍처(MSA)** 기반으로 설계·구현한 티켓팅 시스템이다.

Azure VM 환경 위에서 Docker 기반으로 서비스를 구성하였으며,  
Redis를 활용한 **대기열 관리 및 좌석 Lock**,  
PostgreSQL을 통한 **데이터 영속성**,  
Nginx를 이용한 **API Gateway 역할**을 직접 구현하였다.

---

## 1. 전체 시스템 아키텍처 개요

본 시스템은 단일 애플리케이션이 아닌,  
**역할별 서비스 분리(MSA)** 구조로 구성된다.

### 구성 요소
- **Nginx**  
  - API Gateway 및 정적 파일 제공
- **ticketing-core**  
  - 좌석 조회, 좌석 홀드, 예약 확정, 인증 코드 발급
- **queue-service**  
  - Redis 기반 대기열 관리
- **staff-api**  
  - 보안요원 전용 예약 조회 API
- **PostgreSQL**  
  - 예약 및 좌석 정보 저장
- **Redis**  
  - 대기열, 좌석 홀드, 인증 코드 관리

모든 서비스는 **Docker 컨테이너**로 구성되며,  
Azure VM 내에서 `docker-compose`를 통해 실행된다.

---

## 2. Azure 실구축 환경

### 2.1 리소스 그룹 구성

- **cloud-msa-rg**
  - 메인 서비스 실행용 VM
  - 네트워크, 보안 그룹(NSG), 디스크 포함

- **locust-vm-group**
  - 부하 테스트 전용 VM
  - Locust 실행 환경 분리

👉 서비스 VM과 부하 테스트 VM을 분리하여  
실제 운영 환경과 유사한 테스트 구조를 구성하였다.

---

## 3. Docker 기반 서비스 구성

**실행 중인 컨테이너**
- nginx
- ticketing-core
- queue-service
- staff-api
- postgres
- redis

| 컨테이너        | 역할                                   |
|-----------------|----------------------------------------|
| nginx           | API Gateway, HTTPS 처리, 정적 파일 제공 |
| ticketing-core  | 좌석 조회 / 홀드 / 예약 확정            |
| queue-service   | Redis 기반 대기열 관리                  |
| staff-api       | 보안요원용 예약 조회                    |
| postgres        | 예약 및 좌석 데이터 저장                |
| redis           | 대기열, 좌석 Lock, 인증 코드 관리       |

모든 서비스는 독립적으로 빌드된 Docker Image로 실행되며,
Docker Hub에 이미지를 배포한 뒤 서버에서 pull하여 실행한다.

<img width="724" height="464" alt="image" src="https://github.com/user-attachments/assets/bade54d4-e1ac-4885-9eac-75a51245036c" />
<img width="1047" height="630" alt="image" src="https://github.com/user-attachments/assets/7e919aeb-49bb-4afd-b5cd-da868d5a8fa5" />
<img width="1180" height="778" alt="image" src="https://github.com/user-attachments/assets/ad9fbce8-4d72-448c-bd0c-7c1bf4482da8" />
<img width="865" height="516" alt="image" src="https://github.com/user-attachments/assets/576aa448-788d-4967-b6f6-47805b710ca9" />
<img width="1084" height="690" alt="image" src="https://github.com/user-attachments/assets/3e7c9721-2977-4b52-a0e0-048f975d8591" />
<img width="861" height="557" alt="image" src="https://github.com/user-attachments/assets/d3a216c9-0cb9-4920-b498-f263097caa77" />
<img width="1129" height="686" alt="image" src="https://github.com/user-attachments/assets/c4b8ea1c-d7c2-48ca-bd27-8e1300f4a5d1" />
<img width="932" height="470" alt="image" src="https://github.com/user-attachments/assets/f248c9e1-e85b-4abf-b501-9f4c5e3d16a7" />

## 4. 서비스 흐름 및 주요 기능 구현

### 4.1 대기열 시스템 (queue-service)

- 사용자는 `/api/queue/enter`를 통해 대기열에 진입
- Redis Sorted Set을 사용하여 입장 순서 관리
- 허용 인원 수만큼만 좌석 선택 가능 상태로 전환
- 주기적 상태 조회(`/status`)를 통해 대기 상태 갱신

👉 대규모 동시 접속 상황에서 서버 과부하 및 좌석 충돌 방지

---

### 4.2 좌석 홀드 및 예약 확정 (ticketing-core)

#### 좌석 홀드
- 좌석 선택 시 Redis에 TTL 기반 홀드 키 생성
- 동일 좌석에 대한 중복 홀드 방지
- 홀드 시간 초과 시 자동 해제

#### 예약 확정
- 홀드 토큰 검증 후 PostgreSQL에 예약 정보 저장
- 4자리 인증 코드 랜덤 발급
- Redis `setIfAbsent`를 사용해 인증 코드 중복 방지

---

### 4.3 보안요원 조회 기능 (staff-api)

- 이벤트 ID + 4자리 인증 코드 입력
- 내부 API를 통해 예약 좌석 조회
- 행사 운영 시 오프라인 검증 수단으로 활용 가능

---

## 5. Nginx 기반 API Gateway

Nginx는 다음 역할을 수행한다.

- HTTPS 처리 (Let’s Encrypt)
- 서비스별 라우팅
  - `/api` → ticketing-core
  - `/api/queue` → queue-service
  - `/staff` → staff-api
- 정적 파일 제공 (프론트엔드)

👉 별도의 API Gateway 서비스를 사용하지 않고,  
Nginx로 직접 Gateway 역할을 구현

---

## 6. CI/CD 파이프라인

### CI (Pull Request 기준)
- 코드 스타일 검사 (Spotless)
- 멀티 모듈 Gradle 빌드

### CD (main 브랜치)
- Docker Image 빌드
- Docker Hub에 Push
- 서버에서 Image Pull
- `docker-compose` 기반 자동 배포

👉 코드 변경 → 자동 빌드 → 자동 배포 흐름을 경험

---

## 7. 부하 테스트 (Locust)

- Locust 전용 VM에서 테스트 수행
- 수천 명의 동시 접속 시나리오 시뮬레이션
- 주요 API 응답 시간 및 실패율 분석

### 테스트 결과 요약
- 평균 응답 시간: 수십 ms 수준
- 실패율: 0%
- 대기열 API 정상 동작 확인

👉 Redis 기반 대기열 구조의 효과를 실증적으로 검증

---

## 8. 확장 가능성

현재는 단일 VM에서 여러 컨테이너를 실행하지만,  
본 구조는 다음과 같은 확장이 가능하다.

- 서비스별 VM 분리
- Kubernetes 기반 컨테이너 오케스트레이션
- VM Scale Set을 통한 자동 Scale-out

👉 클라우드 환경에서 자연스럽게 확장 가능한 구조
