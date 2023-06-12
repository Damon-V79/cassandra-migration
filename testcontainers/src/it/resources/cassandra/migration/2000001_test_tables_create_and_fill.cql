-- Create test migrations tables
CREATE TABLE IF NOT EXISTS test_string_by_id (id uuid, value text, PRIMARY KEY (id));
CREATE TABLE IF NOT EXISTS other_keyspace.test_integer_by_id (id int, value int, PRIMARY KEY (id));

-- Filling test migrations tables
INSERT INTO test_string_by_id (id, value) VALUES (uuid(), 'text');
INSERT INTO test_string_by_id (id, value) VALUES (0b54a376-9aba-43bd-be21-4c20aa0445d4, 'text1');
INSERT INTO test_string_by_id (id, value) VALUES (9a9a6dc0-b49d-476d-af9d-e072c0c16825, 'text2');
INSERT INTO test_string_by_id (id, value) VALUES (56182f9f-eea5-4504-b0ad-518bb2ca8c82, 'text3');
INSERT INTO test_string_by_id (id, value) VALUES (7eef59a9-7b61-442c-821d-0fa861ae5a86, 'text4');
INSERT INTO test_string_by_id (id, value) VALUES (c4e457de-3153-4c12-8a8e-9c3305fa5484, 'text5');

INSERT INTO other_keyspace.test_integer_by_id (id, value) VALUES (1, 41);
INSERT INTO other_keyspace.test_integer_by_id (id, value) VALUES (2, 42);
INSERT INTO other_keyspace.test_integer_by_id (id, value) VALUES (10, 55);
INSERT INTO other_keyspace.test_integer_by_id (id, value) VALUES (11, 56);
INSERT INTO other_keyspace.test_integer_by_id (id, value) VALUES (12, 57);
INSERT INTO other_keyspace.test_integer_by_id (id, value) VALUES (15, 58);
