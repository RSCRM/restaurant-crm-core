-- ModifierGroup derives its branch from Product; the direct branch column is legacy.
DO $$
BEGIN
    IF to_regclass('public.modifier_groups') IS NULL THEN
        RETURN;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'modifier_groups'
          AND column_name = 'branch_id'
    ) THEN
        ALTER TABLE modifier_groups DROP COLUMN branch_id;
    END IF;
END $$;
