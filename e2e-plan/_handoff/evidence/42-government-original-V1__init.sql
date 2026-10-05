CREATE TYPE verification_status AS ENUM ('PENDING', 'VERIFIED', 'REJECTED', 'APPROVED', 'FAILED', 'MANUAL_REVIEW');

CREATE TYPE document_type AS ENUM ('AADHAAR', 'PAN', 'DRIVING_LICENSE', 'FSSAI_LICENSE', 'GST_CERTIFICATE', 'RC', 'SELFIE', 'GSTIN', 'FSSAI');

CREATE TABLE executive_bank_details (
    bank_id UUID PRIMARY KEY,
    executive_id UUID NOT NULL UNIQUE,
    account_number VARCHAR(50) NOT NULL,
    ifsc_code VARCHAR(20) NOT NULL,
    bank_registered_name VARCHAR(255),
    penny_drop_status verification_status DEFAULT 'PENDING',
    name_match_score NUMERIC(4, 3),
    verified_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE biometric_verifications (
    verification_id UUID PRIMARY KEY,
    executive_id UUID NOT NULL,
    selfie_url VARCHAR(512) NOT NULL,
    confidence_score NUMERIC(4, 3) NOT NULL,
    is_live BOOLEAN NOT NULL,
    verification_time TIMESTAMP WITH TIME ZONE
);

CREATE TABLE brand_documents (
    id UUID PRIMARY KEY,
    brand_id UUID,
    doc_type document_type,
    document_number VARCHAR(255),
    api_raw_response JSONB,
    api_verification_status verification_status,
    verified_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE brand_bank_details (
    id UUID PRIMARY KEY,
    brand_id UUID,
    account_number VARCHAR(255),
    ifsc_code VARCHAR(255),
    bank_registered_name VARCHAR(255),
    name_match_score NUMERIC,
    penny_drop_status verification_status,
    verified_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE brand_verification_audit_logs (
    id UUID PRIMARY KEY,
    entity_type VARCHAR(255),
    entity_id UUID,
    verification_provider VARCHAR(255),
    raw_request_payload JSONB,
    raw_response_payload JSONB,
    similarity_score DOUBLE PRECISION,
    status VARCHAR(255),
    created_at TIMESTAMP
);

CREATE TABLE executive_documents (
    document_id UUID PRIMARY KEY,
    executive_id UUID NOT NULL,
    doc_type document_type NOT NULL DEFAULT 'DRIVING_LICENSE',
    document_number VARCHAR(100) NOT NULL,
    document_url VARCHAR(512),
    api_verification_status verification_status DEFAULT 'PENDING',
    api_raw_response JSONB,
    expiry_date DATE,
    created_at TIMESTAMP WITH TIME ZONE
);




CREATE INDEX IF NOT EXISTS idx_biometric_exec_time ON biometric_verifications(executive_id, verification_time DESC);