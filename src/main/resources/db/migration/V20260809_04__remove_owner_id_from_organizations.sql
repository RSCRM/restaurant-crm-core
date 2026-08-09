-- Remove owner_id column from organizations
-- Owner is now tracked via Employee entity with OWNER org_role and organization_id
DO $$
BEGIN
    IF to_regclass('public.organizations') IS NULL THEN RETURN; END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'organizations' AND column_name = 'owner_id') THEN
        ALTER TABLE organizations DROP COLUMN owner_id;
    END IF;
END $$;
