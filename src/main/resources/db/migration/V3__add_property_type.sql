-- 건물 유형 컬럼 추가
ALTER TABLE apt_complexes ADD COLUMN IF NOT EXISTS property_type VARCHAR(20) NOT NULL DEFAULT 'apt';

-- 기존 유니크 제약 삭제 후 재생성
ALTER TABLE apt_complexes DROP CONSTRAINT IF EXISTS uq_apt_complex_lawd_dong_name;
ALTER TABLE apt_complexes ADD CONSTRAINT uq_apt_complex_lawd_dong_name_type
    UNIQUE (lawd_cd, dong, name, property_type);
