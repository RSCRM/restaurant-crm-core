-- Remove manager_id column from organization_branches
-- Manager assignment is now tracked via Employee entity with MANAGER org_role
DO $$
BEGIN
    IF to_regclass('public.organization_branches') IS NULL THEN RETURN; END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'organization_branches' AND column_name = 'manager_id') THEN
        ALTER TABLE organization_branches DROP COLUMN manager_id;
    END IF;
END $$;
