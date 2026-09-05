CREATE TABLE crash_reports (
    report_id TEXT PRIMARY KEY NOT NULL,
    received_at INTEGER NOT NULL,
    expires_at INTEGER NOT NULL,
    payload_hash TEXT NOT NULL,
    payload TEXT NOT NULL CHECK(json_valid(payload)),
    payload_bytes INTEGER NOT NULL CHECK(payload_bytes BETWEEN 1 AND 262144),
    schema_version INTEGER NOT NULL CHECK(schema_version = 1),
    server_redaction_version INTEGER NOT NULL DEFAULT 1 CHECK(server_redaction_version = 1),
    version_code INTEGER NOT NULL,
    exception_type TEXT NOT NULL,
    CHECK(expires_at > received_at)
) STRICT;
CREATE INDEX crash_reports_expiry ON crash_reports(expires_at);
CREATE INDEX crash_reports_version ON crash_reports(version_code, received_at);

-- Aggregate ingestion accounting only: no IP, installation or user identifiers.
CREATE TABLE daily_usage (
    day INTEGER PRIMARY KEY NOT NULL,
    reports INTEGER NOT NULL,
    bytes INTEGER NOT NULL
) STRICT;
CREATE TRIGGER count_report AFTER INSERT ON crash_reports BEGIN
    INSERT INTO daily_usage(day, reports, bytes)
    VALUES (NEW.received_at / 86400, 1, NEW.payload_bytes)
    ON CONFLICT(day) DO UPDATE SET reports = reports + 1, bytes = bytes + NEW.payload_bytes;
END;
