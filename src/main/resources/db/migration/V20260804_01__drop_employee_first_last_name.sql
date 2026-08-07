DO $$
BEGIN
    IF to_regclass('public.employees') IS NOT NULL THEN
        ALTER TABLE employees
            DROP COLUMN IF EXISTS first_name,
            DROP COLUMN IF EXISTS last_name;
    END IF;
END $$;
