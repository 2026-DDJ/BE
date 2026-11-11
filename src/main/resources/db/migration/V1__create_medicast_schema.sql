CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL
);

CREATE TABLE health_assessments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    household_size INTEGER NOT NULL,
    income_range VARCHAR(30) NOT NULL,
    health_insurance_type VARCHAR(40) NOT NULL,
    region VARCHAR(20) NOT NULL,
    self_rated_health VARCHAR(20) NOT NULL,
    height_cm DECIMAL(10, 4) NOT NULL,
    weight_kg DECIMAL(10, 4) NOT NULL,
    bmi DECIMAL(12, 4) NOT NULL,
    exercises BOOLEAN NOT NULL,
    outpatient_visits_last_year INTEGER NOT NULL,
    dental_visits_last_year INTEGER NOT NULL,
    has_private_health_insurance BOOLEAN NOT NULL,
    hospitalized_last_year BOOLEAN NOT NULL,
    activity_limitation VARCHAR(20) NOT NULL,
    anxiety_depression VARCHAR(20) NOT NULL,
    occupation VARCHAR(40) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_health_assessments_user
        FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_health_assessments_user_id ON health_assessments (user_id);

CREATE TABLE predictions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    assessment_id BIGINT NOT NULL,
    predicted_medical_cost DECIMAL(15, 2),
    high_cost_probability DECIMAL(8, 7),
    status VARCHAR(20) NOT NULL,
    model_version VARCHAR(100),
    feature_schema_version VARCHAR(100),
    preprocessing_version VARCHAR(100),
    error_code VARCHAR(50),
    retryable BOOLEAN NOT NULL,
    completed_at TIMESTAMP(6),
    created_at TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_predictions_assessment
        FOREIGN KEY (assessment_id) REFERENCES health_assessments (id)
);

CREATE INDEX idx_predictions_assessment_id ON predictions (assessment_id);
