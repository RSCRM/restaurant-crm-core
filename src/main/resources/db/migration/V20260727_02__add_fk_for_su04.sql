DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_organizations_owner'
    ) THEN
        ALTER TABLE organizations
            ADD CONSTRAINT fk_organizations_owner
            FOREIGN KEY (owner_id)
            REFERENCES users(id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uk_organization_branches_manager'
    ) THEN
        ALTER TABLE organization_branches
            ADD CONSTRAINT uk_organization_branches_manager
            UNIQUE (manager_id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_organization_branches_manager'
    ) THEN
        ALTER TABLE organization_branches
            ADD CONSTRAINT fk_organization_branches_manager
            FOREIGN KEY (manager_id)
            REFERENCES employees(id);
    END IF;
END $$;
