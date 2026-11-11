# AI 모델 서버 계약 v1

백엔드는 운영 프로필에서 `AI_MODEL_BASE_URL`의 `AI_MODEL_PREDICTION_PATH`로 동기 `POST` 요청을
보낸다. 기본 경로는 `/predict`이며 연결 제한은 2초, 응답 제한은 10초다.

15개 사용자 입력은 백엔드에서 검증·저장한 뒤 아래 모델 키와 숫자 코드로 변환한다.
`health_ins`의 미가입과 `eco9`의 비경제활동은 유효한 `null` 값이다. BMI는 평가 재현을 위해
백엔드에 저장하지만 현재 AI 서버 요청에는 포함하지 않는다.

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
  "predictedAnnualMedicalCost": 1234567.89,
  "highCostProbability": 0.4321
}
```

오류 매핑은 다음과 같다.

| 모델 호출 결과 | 저장 오류 코드 | 재시도 |
|---|---|---|
| timeout/연결 실패 | `MODEL_TIMEOUT` | 예 |
| 모델 4xx | `MODEL_REQUEST_REJECTED` | 아니오 |
| 모델 5xx | `MODEL_UNAVAILABLE` | 예 |
| `success: false` | `MODEL_PREDICTION_FAILED` | 아니오 |
| 응답 JSON/schema 오류 | `MODEL_INVALID_RESPONSE` | 아니오 |
