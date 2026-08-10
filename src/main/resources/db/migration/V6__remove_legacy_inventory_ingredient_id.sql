-- Inventory now stores branch, category, name and unit directly.
-- Remove the required legacy link to the deleted ingredients module.
DO $$
BEGIN
    IF to_regclass('public.inventories') IS NULL THEN
        RETURN;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'inventories'
          AND column_name = 'ingredient_id'
    ) THEN
        ALTER TABLE inventories DROP COLUMN ingredient_id;
    END IF;
END $$;
