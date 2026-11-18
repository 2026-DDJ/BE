ALTER TABLE predictions ADD COLUMN same_income_group_average_annual_medical_cost DECIMAL(15, 2);
ALTER TABLE predictions ADD COLUMN same_outpatient_group_average_annual_medical_cost DECIMAL(15, 2);
ALTER TABLE predictions ADD COLUMN overall_average_annual_medical_cost DECIMAL(15, 2);
