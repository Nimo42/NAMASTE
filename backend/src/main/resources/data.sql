-- Code systems
INSERT INTO code_systems (name, display_name, version, is_active, type, created_at, updated_at)
VALUES ('NAMASTE', 'NAMASTE', '1.0', true, 'LOCAL', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO code_systems (name, display_name, version, is_active, type, created_at, updated_at)
VALUES ('ICD11_TM2', 'ICD-11 TM2', '2025-06', true, 'AUTHORITY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO code_systems (name, display_name, version, is_active, type, created_at, updated_at)
VALUES ('ICD11_BIOMED', 'ICD-11 Biomedical', '2025-06', true, 'AUTHORITY', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Concepts for NAMASTE
INSERT INTO concepts (code, display_name, description, system_id, is_active, lifecycle_status, created_at, updated_at)
VALUES (
  'N123',
  'Ama Jwara',
  'Fever due to impaired digestion',
  (SELECT id FROM code_systems WHERE name = 'NAMASTE'),
  true, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

INSERT INTO concepts (code, display_name, description, system_id, is_active, lifecycle_status, created_at, updated_at)
VALUES (
  'N456',
  'Madhumeha',
  'Sweet urination disorder',
  (SELECT id FROM code_systems WHERE name = 'NAMASTE'),
  true, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- Concepts for ICD11_TM2
INSERT INTO concepts (code, display_name, description, system_id, is_active, lifecycle_status, created_at, updated_at)
VALUES (
  'TM2-789',
  'Fever Pattern',
  'Traditional fever concept',
  (SELECT id FROM code_systems WHERE name = 'ICD11_TM2'),
  true, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

INSERT INTO concepts (code, display_name, description, system_id, is_active, lifecycle_status, created_at, updated_at)
VALUES (
  'TM2-999',
  'Metabolic Disorder Pattern',
  'Sugar disorder pattern',
  (SELECT id FROM code_systems WHERE name = 'ICD11_TM2'),
  true, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- Concepts for ICD11_BIOMED
INSERT INTO concepts (code, display_name, description, system_id, is_active, lifecycle_status, created_at, updated_at)
VALUES (
  '5A11',
  'Type 2 Diabetes Mellitus',
  'Modern diabetes classification',
  (SELECT id FROM code_systems WHERE name = 'ICD11_BIOMED'),
  true, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

INSERT INTO concepts (code, display_name, description, system_id, is_active, lifecycle_status, created_at, updated_at)
VALUES (
  'AA12',
  'Fever',
  'Fever in modern medicine',
  (SELECT id FROM code_systems WHERE name = 'ICD11_BIOMED'),
  true, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- Mappings: Madhumeha -> Type 2 Diabetes
INSERT INTO mappings (from_concept_id, to_concept_id, mapping_type, confidence, status, created_at)
VALUES (
  (SELECT c1.id FROM concepts c1
   JOIN code_systems cs1 ON c1.system_id = cs1.id
   WHERE c1.code = 'N456' AND cs1.name = 'NAMASTE'),
  (SELECT c2.id FROM concepts c2
   JOIN code_systems cs2 ON c2.system_id = cs2.id
   WHERE c2.code = '5A11' AND cs2.name = 'ICD11_BIOMED'),
  'EQUIVALENT',
  0.95,
  'VERIFIED',
  CURRENT_TIMESTAMP
);

-- Mappings: Ama Jwara -> Fever
INSERT INTO mappings (from_concept_id, to_concept_id, mapping_type, confidence, status, created_at)
VALUES (
  (SELECT c1.id FROM concepts c1
   JOIN code_systems cs1 ON c1.system_id = cs1.id
   WHERE c1.code = 'N123' AND cs1.name = 'NAMASTE'),
  (SELECT c2.id FROM concepts c2
   JOIN code_systems cs2 ON c2.system_id = cs2.id
   WHERE c2.code = 'AA12' AND cs2.name = 'ICD11_BIOMED'),
  'EQUIVALENT',
  0.90,
  'VERIFIED',
  CURRENT_TIMESTAMP
);
