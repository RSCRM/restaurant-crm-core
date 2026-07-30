DO $$
BEGIN
    IF to_regclass('public.roles') IS NOT NULL THEN
        ALTER TABLE public.roles
            ADD COLUMN IF NOT EXISTS data_scope VARCHAR(255);

        UPDATE public.roles
        SET data_scope = 'TENANT'
        WHERE data_scope IS NULL;

        UPDATE public.roles
        SET data_scope = 'SYSTEM'
        WHERE role_name = 'ADMIN';

        ALTER TABLE public.roles
            ALTER COLUMN data_scope SET DEFAULT 'TENANT',
            ALTER COLUMN data_scope SET NOT NULL;
    END IF;

    IF to_regclass('public.org_roles') IS NOT NULL THEN
        ALTER TABLE public.org_roles
            ADD COLUMN IF NOT EXISTS data_scope VARCHAR(255);

        UPDATE public.org_roles
        SET data_scope = CASE role_name
            WHEN 'OWNER' THEN 'ORGANIZATION'
            WHEN 'MANAGER' THEN 'BRANCH'
            WHEN 'CASHIER' THEN 'BRANCH'
            WHEN 'WAITER' THEN 'SELF'
            WHEN 'CHEF' THEN 'SELF'
            ELSE 'SELF'
        END
        WHERE data_scope IS NULL;

        ALTER TABLE public.org_roles
            ALTER COLUMN data_scope SET DEFAULT 'SELF',
            ALTER COLUMN data_scope SET NOT NULL;
    END IF;
END $$;
