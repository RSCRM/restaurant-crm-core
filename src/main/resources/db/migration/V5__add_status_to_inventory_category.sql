-- Add status column to inventory_categories
-- Inventory categories can be ACTIVE or INACTIVE
DO $$
BEGIN
    IF to_regclass('public.inventory_categories') IS NULL THEN
        RETURN;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'inventory_categories'
          AND column_name = 'status'
    ) THEN
        ALTER TABLE inventory_categories
            ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'inventory_categories_status_check'
          AND conrelid = 'public.inventory_categories'::regclass
    ) THEN
        ALTER TABLE inventory_categories
            ADD CONSTRAINT inventory_categories_status_check
            CHECK (
                status IN (
                    'ACTIVE',
                    'INACTIVE'
                )
            );
    END IF;
END $$;


-- Add INACTIVE status to inventories
-- Existing inventory statuses:
-- GOOD, LOW, OUT_OF_STOCK
DO $$
BEGIN
    IF to_regclass('public.inventories') IS NULL THEN
        RETURN;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'inventories_status_check'
          AND conrelid = 'public.inventories'::regclass
    ) THEN
        ALTER TABLE inventories
            DROP CONSTRAINT inventories_status_check;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'inventories_status_check'
          AND conrelid = 'public.inventories'::regclass
    ) THEN
        ALTER TABLE inventories
            ADD CONSTRAINT inventories_status_check
            CHECK (
                status IN (
                    'GOOD',
                    'LOW',
                    'OUT_OF_STOCK',
                    'INACTIVE'
                )
            );
    END IF;
END $$;
