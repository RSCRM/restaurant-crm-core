-- V2: Add organization_id column to employees table.
-- Owner employees have branch_id = NULL and organization_id = their org.
-- Regular employees have both branch_id and organization_id (organization derived from branch).
-- This migration is defensive: no-op if column already exists (Hibernate may have created it).

DO $$
BEGIN
    IF to_regclass('public.employees') IS NULL THEN
        RETURN;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'employees'
          AND column_name = 'organization_id'
    ) THEN
        ALTER TABLE employees ADD COLUMN organization_id VARCHAR(36);
        CREATE INDEX idx_employees_organization_id ON employees(organization_id);
        RAISE NOTICE 'Added organization_id column to employees table';
    ELSE
        RAISE NOTICE 'organization_id column already exists, skipping';
    END IF;
END $$;
