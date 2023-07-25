-- Keyspaces
CREATE KEYSPACE IF NOT EXISTS other_keyspace WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1 };
