-- Drop legacy single-column UNIQUE constraint(s) on org_roles.role_name.
-- Name is Hibernate auto-generated so it differs per dev machine (e.g. uk8sqjrel1snbhma2kiwinv1v6o).
-- Keep the composite unique (organization_id, role_name).
-- Defensive: no-op on a fresh DB where Hibernate has not created the table yet (Flyway runs before Hibernate).
DO $$
DECLARE
    c record;
BEGIN
    IF to_regclass('public.org_roles') IS NULL THEN
        RETURN;
    END IF;

    FOR c IN
        SELECT con.conname
        FROM pg_constraint con
        WHERE con.conrelid = 'public.org_roles'::regclass
          AND con.contype = 'u'
          AND array_length(con.conkey, 1) = 1
          AND (SELECT attname FROM pg_attribute
               WHERE attrelid = con.conrelid AND attnum = con.conkey[1]) = 'role_name'
    LOOP
        EXECUTE format('ALTER TABLE public.org_roles DROP CONSTRAINT %I', c.conname);
        RAISE NOTICE 'Dropped legacy unique constraint on role_name: %', c.conname;
    END LOOP;
END $$;
