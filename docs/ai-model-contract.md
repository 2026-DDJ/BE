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

정상 응답은 개인 예상 의료비와 비교 집단별 평균을 반환해야 한다. 네 금액 중 하나라도
누락되거나 음수이면 예측은 `MODEL_INVALID_RESPONSE`로 실패 저장된다. 모델·전처리 버전은
배포 환경변수로 주입한다.

```json
{
  "success": true,
  "predictedAnnualMedicalCost": 1114802,
  "comparison": {
    "sameIncomeGroup": {
      "mean": 1100000
    },
    "sameOutpatientGroup": {
      "mean": 1200000
    },
    "overall": {
      "mean": 1000000
    }
  }
}
```

`predictedAnnualMedicalCost`는 개인 예상 연간 의료비이며, `comparison.sameIncomeGroup.mean`,
`comparison.sameOutpatientGroup.mean`, `comparison.overall.mean`은 각각 동일 소득 구간,
동일 외래 이용 횟수 구간, 전체 표본의 평균 연간 의료비다.

오류 매핑은 다음과 같다.

| 모델 호출 결과 | 저장 오류 코드 | 재시도 |
|---|---|---|
| timeout/연결 실패 | `MODEL_TIMEOUT` | 예 |
| 모델 4xx | `MODEL_REQUEST_REJECTED` | 아니오 |
| 모델 5xx | `MODEL_UNAVAILABLE` | 예 |
| `success: false` | `MODEL_PREDICTION_FAILED` | 아니오 |
| 응답 JSON/schema 오류 | `MODEL_INVALID_RESPONSE` | 아니오 |
