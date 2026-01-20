-- Enable the pg_trgm extension
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Set similarity threshold for the session
SET pg_trgm.similarity_threshold = 0.1;

-- Make the change persistent for the database
ALTER DATABASE team03 SET pg_trgm.similarity_threshold = 0.1;
