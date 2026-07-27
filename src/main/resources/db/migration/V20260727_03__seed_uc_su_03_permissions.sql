DO $$
DECLARE
    has_permission_code boolean;
    permission_match text;
BEGIN
    SELECT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'org_permissions'
          AND column_name = 'permission_code'
    ) INTO has_permission_code;

    IF has_permission_code THEN
        EXECUTE $sql$
            INSERT INTO org_permissions (id, version, permission_code, permission_name, description, created_at, updated_at)
            VALUES
                ('d0000000-0000-0000-0000-000000000301', 0, 'BRANCH_MANAGER_VIEW', 'View branch managers', 'View branch manager list and detail', NOW(), NOW()),
                ('d0000000-0000-0000-0000-000000000302', 0, 'BRANCH_MANAGER_CREATE', 'Create branch manager', 'Create branch manager accounts', NOW(), NOW()),
                ('d0000000-0000-0000-0000-000000000303', 0, 'BRANCH_MANAGER_UPDATE', 'Update branch manager', 'Update branch manager accounts', NOW(), NOW()),
                ('d0000000-0000-0000-0000-000000000304', 0, 'BRANCH_MANAGER_DELETE', 'Delete branch manager', 'Disable branch manager accounts', NOW(), NOW())
            ON CONFLICT (permission_code) DO NOTHING
        $sql$;
        permission_match := 'p.permission_code';
    ELSE
        EXECUTE $sql$
            INSERT INTO org_permissions (id, version, permission_name, created_at, updated_at)
            VALUES
                ('d0000000-0000-0000-0000-000000000301', 0, 'BRANCH_MANAGER_VIEW', NOW(), NOW()),
                ('d0000000-0000-0000-0000-000000000302', 0, 'BRANCH_MANAGER_CREATE', NOW(), NOW()),
                ('d0000000-0000-0000-0000-000000000303', 0, 'BRANCH_MANAGER_UPDATE', NOW(), NOW()),
                ('d0000000-0000-0000-0000-000000000304', 0, 'BRANCH_MANAGER_DELETE', NOW(), NOW())
            ON CONFLICT (permission_name) DO NOTHING
        $sql$;
        permission_match := 'p.permission_name';
    END IF;

    IF to_regclass('public.org_role_permissions') IS NOT NULL THEN
        EXECUTE format($sql$
            INSERT INTO org_role_permissions (org_role_id, org_permission_id)
            SELECT r.id, p.id
            FROM org_roles r
            JOIN org_permissions p
                ON %s IN (
                    'BRANCH_MANAGER_VIEW',
                    'BRANCH_MANAGER_CREATE',
                    'BRANCH_MANAGER_UPDATE',
                    'BRANCH_MANAGER_DELETE'
                )
            WHERE r.role_name = 'OWNER'
            ON CONFLICT DO NOTHING
        $sql$, permission_match);
    END IF;

    IF to_regclass('public.org_roles_org_permissions') IS NOT NULL THEN
        EXECUTE format($sql$
            INSERT INTO org_roles_org_permissions (org_role_id, org_permissions_id)
            SELECT r.id, p.id
            FROM org_roles r
            JOIN org_permissions p
                ON %s IN (
                    'BRANCH_MANAGER_VIEW',
                    'BRANCH_MANAGER_CREATE',
                    'BRANCH_MANAGER_UPDATE',
                    'BRANCH_MANAGER_DELETE'
                )
            WHERE r.role_name = 'OWNER'
            ON CONFLICT DO NOTHING
        $sql$, permission_match);
    END IF;
END $$;
