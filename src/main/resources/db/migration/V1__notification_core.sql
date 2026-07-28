-- =====================================================================================
-- V1 — Notification core (P1)
--
-- Reshapes `notifications` around the four delivery scopes (SYSTEM / BRANCH / GROUP /
-- DIRECT), introduces the sparse `notification_receipts` table for per-recipient read
-- state, and adds the indexes the read path depends on.
--
-- This is the project's first migration and it runs against databases whose schema was
-- created by `ddl-auto=update`. Every statement is therefore idempotent: it must work on
-- a brand-new database and on an existing one alike.
-- =====================================================================================


-- -------------------------------------------------------------------------------------
-- 1. notifications — create for fresh databases
--    Columns are declared nullable here; NOT NULL is applied at the end, after the
--    backfill, so the same script works on databases that already hold rows.
-- -------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notifications (
    id                  VARCHAR(255) PRIMARY KEY,
    version             BIGINT,
    created_by          VARCHAR(255),
    updated_by          VARCHAR(255),
    created_at          TIMESTAMP(6) WITH TIME ZONE,
    updated_at          TIMESTAMP(6) WITH TIME ZONE,
    scope               VARCHAR(30),
    organization_id     VARCHAR(255),
    branch_id           VARCHAR(255),
    target_key          VARCHAR(64),
    group_type          VARCHAR(30),
    recipient_id        VARCHAR(255),
    sender_id           VARCHAR(255),
    sender_type         VARCHAR(30),
    title               VARCHAR(255),
    content             VARCHAR(255),
    type                VARCHAR(40),
    required_permission VARCHAR(64),
    priority            VARCHAR(30),
    payload             TEXT,
    expires_at          TIMESTAMP(6) WITH TIME ZONE,
    dedupe_key          VARCHAR(128)
);


-- -------------------------------------------------------------------------------------
-- 2. notifications — add the new columns to databases that already had the table
-- -------------------------------------------------------------------------------------
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS scope               VARCHAR(30);
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS organization_id     VARCHAR(255);
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS target_key          VARCHAR(64);
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS group_type          VARCHAR(30);
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS sender_type         VARCHAR(30);
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS required_permission VARCHAR(64);
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS priority            VARCHAR(30);
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS payload             TEXT;
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS expires_at          TIMESTAMP(6) WITH TIME ZONE;
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS dedupe_key          VARCHAR(128);

-- SYSTEM notifications have no branch, and module-emitted ones have no human sender.
ALTER TABLE notifications ALTER COLUMN branch_id DROP NOT NULL;
ALTER TABLE notifications ALTER COLUMN sender_id DROP NOT NULL;


-- -------------------------------------------------------------------------------------
-- 3. notifications — backfill legacy rows
--    Before this migration the model had two shapes only: recipient_id NULL meant "the
--    whole branch", non-NULL meant one employee. The only type ever written was
--    READY_TO_SERVE, whose audience is "whoever may read orders".
-- -------------------------------------------------------------------------------------
UPDATE notifications
SET scope = CASE WHEN recipient_id IS NULL THEN 'GROUP' ELSE 'DIRECT' END
WHERE scope IS NULL;

UPDATE notifications
SET group_type          = 'BY_PERMISSION',
    target_key          = 'ORDER_READ',
    required_permission = 'ORDER_READ'
WHERE scope = 'GROUP'
  AND group_type IS NULL;

UPDATE notifications n
SET organization_id = b.organization_id
FROM organization_branches b
WHERE n.branch_id = b.id
  AND n.organization_id IS NULL;

UPDATE notifications SET sender_type = 'USER'   WHERE sender_type IS NULL;
UPDATE notifications SET priority    = 'NORMAL' WHERE priority IS NULL;


-- -------------------------------------------------------------------------------------
-- 4. notification_receipts — sparse per-recipient read state
--    A row exists only once somebody acknowledges a notification; its absence means
--    unread. That is what keeps a branch-wide broadcast a single insert.
-- -------------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notification_receipts (
    notification_id VARCHAR(255) NOT NULL,
    recipient_id    VARCHAR(255) NOT NULL,
    status          VARCHAR(30)  NOT NULL,
    read_at         TIMESTAMP(6) WITH TIME ZONE,
    CONSTRAINT pk_notification_receipts PRIMARY KEY (notification_id, recipient_id),
    CONSTRAINT fk_notification_receipts_notification
        FOREIGN KEY (notification_id) REFERENCES notifications (id) ON DELETE CASCADE
);


-- -------------------------------------------------------------------------------------
-- 5. Migrate the old per-row read flag into receipts, then drop it
--    `notifications.status` marked a whole row as read, so on a broadcast one employee
--    reading it hid it from everyone. Only the DIRECT rows carried a meaningful value.
-- -------------------------------------------------------------------------------------
DO $$
BEGIN
    IF EXISTS (SELECT 1
               FROM information_schema.columns
               WHERE table_name = 'notifications' AND column_name = 'status') THEN

        INSERT INTO notification_receipts (notification_id, recipient_id, status, read_at)
        SELECT n.id, n.recipient_id, 'READ', COALESCE(n.updated_at, n.created_at)
        FROM notifications n
        WHERE n.recipient_id IS NOT NULL
          AND n.status = 'READ'
        ON CONFLICT (notification_id, recipient_id) DO NOTHING;

    END IF;
END $$;

ALTER TABLE notifications DROP COLUMN IF EXISTS status;


-- -------------------------------------------------------------------------------------
-- 6. notifications — tighten the columns that are now always populated
-- -------------------------------------------------------------------------------------
ALTER TABLE notifications ALTER COLUMN scope       SET NOT NULL;
ALTER TABLE notifications ALTER COLUMN sender_type SET NOT NULL;
ALTER TABLE notifications ALTER COLUMN priority    SET NOT NULL;


-- -------------------------------------------------------------------------------------
-- 7. Indexes
--    Partial indexes keep each tree limited to the rows its query can actually return,
--    and organization_id leads every one of them: no read path filters on branch alone.
-- -------------------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_notifications_branch_feed
    ON notifications (organization_id, branch_id, created_at DESC)
    WHERE scope IN ('BRANCH', 'GROUP');

CREATE INDEX IF NOT EXISTS idx_notifications_direct_feed
    ON notifications (organization_id, recipient_id, created_at DESC)
    WHERE recipient_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_notifications_system_feed
    ON notifications (created_at DESC)
    WHERE scope = 'SYSTEM';

-- Idempotency guard for at-least-once emitters.
CREATE UNIQUE INDEX IF NOT EXISTS uq_notifications_dedupe
    ON notifications (organization_id, dedupe_key)
    WHERE dedupe_key IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_notification_receipts_recipient
    ON notification_receipts (recipient_id, notification_id);


-- -------------------------------------------------------------------------------------
-- 8. Scope/target integrity
--    Added NOT VALID: legacy rows whose branch no longer exists could not have their
--    organization backfilled and would block the constraint. New and updated rows are
--    checked from now on. Once such rows are cleaned up, run:
--        ALTER TABLE notifications VALIDATE CONSTRAINT ck_notifications_scope_target;
-- -------------------------------------------------------------------------------------
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'ck_notifications_scope_target') THEN
        ALTER TABLE notifications
            ADD CONSTRAINT ck_notifications_scope_target CHECK (
                   (scope = 'SYSTEM' AND organization_id IS NULL
                                     AND branch_id IS NULL
                                     AND recipient_id IS NULL
                                     AND target_key IS NULL)
                OR (scope = 'BRANCH' AND organization_id IS NOT NULL
                                     AND branch_id IS NOT NULL)
                OR (scope = 'GROUP'  AND organization_id IS NOT NULL
                                     AND branch_id IS NOT NULL
                                     AND target_key IS NOT NULL
                                     AND group_type IS NOT NULL)
                OR (scope = 'DIRECT' AND organization_id IS NOT NULL
                                     AND branch_id IS NOT NULL
                                     AND recipient_id IS NOT NULL)
            ) NOT VALID;
    END IF;
END $$;
