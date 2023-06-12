-- Keyspaces
CREATE KEYSPACE IF NOT EXISTS test_keyspace WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1 };
CREATE KEYSPACE IF NOT EXISTS other_keyspace WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1 };
