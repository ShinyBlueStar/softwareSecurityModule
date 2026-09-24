-- ShedLock table for distributed job locking (Oracle).
-- Run this script if the table does not exist.
CREATE TABLE shedlock (
  name VARCHAR(64) NOT NULL,
  lock_until TIMESTAMP NOT NULL,
  locked_at TIMESTAMP NOT NULL,
  locked_by VARCHAR(255) NOT NULL,
  PRIMARY KEY (name)
);
