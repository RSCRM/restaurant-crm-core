-- Migration: Add status column to customer_point table for per-organization customer status (ACTIVE / LOCKED)
DO $$
BEGIN
    IF to_regclass('public.customer_point') IS NOT NULL THEN
        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = 'customer_point'
              AND column_name = 'restaurant_id'
        ) AND NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = 'customer_point'
              AND column_name = 'organization_id'
        ) THEN
            ALTER TABLE customer_point RENAME COLUMN restaurant_id TO organization_id;
        END IF;

        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = 'customer_point'
              AND column_name = 'status'
        ) THEN
            ALTER TABLE customer_point ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';
        END IF;
    END IF;
END $$;
