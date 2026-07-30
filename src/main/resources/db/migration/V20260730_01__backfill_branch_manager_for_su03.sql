UPDATE organization_branches b
SET manager_id = seed_manager.employee_id
FROM (
    VALUES
        ('e0000000-0000-0000-0000-000000000001', 'f0000000-0000-0000-0000-000000000001'),
        ('e0000000-0000-0000-0000-000000000003', 'f0000000-0000-0000-0000-000000000002'),
        ('e0000000-0000-0000-0000-000000000005', 'f0000000-0000-0000-0000-000000000003')
) AS seed_manager(branch_id, employee_id)
WHERE b.id = seed_manager.branch_id
  AND b.manager_id IS NULL
  AND EXISTS (
      SELECT 1
      FROM employees e
      WHERE e.id = seed_manager.employee_id
        AND e.branch_id = seed_manager.branch_id
  );
