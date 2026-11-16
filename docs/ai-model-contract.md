# AI 모델 서버 계약 v1

백엔드는 운영 프로필에서 `AI_MODEL_BASE_URL`의 `AI_MODEL_PREDICTION_PATH`로 동기 `POST` 요청을
보낸다. 기본 서버는 `https://medicast-ai-0yh8.onrender.com`, 기본 경로는 `/predict`이며
연결 제한은 2초, 응답 제한은 30초다. API 문서는
`https://medicast-ai-0yh8.onrender.com/docs`에서 확인한다.

15개 사용자 입력은 백엔드에서 검증·저장한 뒤 아래 모델 키와 숫자 코드로 변환한다.
`health_ins`의 미가입과 `eco9`의 비경제활동은 유효한 `null` 값이다. BMI는 평가 재현을 위해
백엔드에 저장하지만 현재 AI 서버 요청에는 직접 포함하지 않는다. BMI 시뮬레이션에서는 기존 키를
유지한 채 `목표 BMI × 키(cm)^2 ÷ 10000`으로 몸무게를 역산하여 `weight`를 바꾸고,
운동 여부는 규칙적으로 함 `1`, 안 함 `2`로 `exercise`에 전달한다.

```json
{
  "ingun": 3,
  "income": 3380,
  "health_ins": 1,
  "region1": 11,
  "self_rated_health": 3,
  "height": 170,
  "weight": 65,
  "exercise": 1,
  "ougun": 4,
  "ougun_dent": 1,
  "indemnity_insurance": 1,
  "hospitalized_year": 0,
  "activity_limitation": 2,
  "anxiety_depression": 3,
  "eco9": 31
}
```

정상 응답은 아래 세 필드를 반환해야 한다. 금액이 누락·음수이거나 고비용 확률이 0~1 밖이면
예측은 `MODEL_INVALID_RESPONSE`로 실패 저장된다. 모델·전처리 버전은 배포 환경변수로 주입한다.

```json
{
  "success": true,
  "predictedAnnualMedicalCost": 1114802,
  "highCostProbability": 0.0934
}
```

위 정상 응답 예시는 2026-11-16에 배포 서버의 문서와 실제 `/predict` 호출로 확인했다.
OpenAPI상 `ingun`, `income`, `height`, `weight`, `ougun`, `ougun_dent`는 number이고,
`health_ins`, `activity_limitation`, `anxiety_depression`, `eco9`는 null을 허용한다.

오류 매핑은 다음과 같다.

| 모델 호출 결과 | 저장 오류 코드 | 재시도 |
|---|---|---|
| timeout/연결 실패 | `MODEL_TIMEOUT` | 예 |
| 모델 4xx | `MODEL_REQUEST_REJECTED` | 아니오 |
| 모델 5xx | `MODEL_UNAVAILABLE` | 예 |
| `success: false` | `MODEL_PREDICTION_FAILED` | 아니오 |
| 응답 JSON/schema 오류 | `MODEL_INVALID_RESPONSE` | 아니오 |
