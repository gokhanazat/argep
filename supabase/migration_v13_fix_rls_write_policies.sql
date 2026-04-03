-- ### 13. FIX MISSING RLS WRITE POLICIES (CLEAN) ###
-- Bu migration, proje üyelerine (Sahip + Ekip) yazma yetkilerini geri verir.

-- A. MILESTONES
DROP POLICY IF EXISTS "Select milestones" ON public.milestones;
DROP POLICY IF EXISTS "Manage milestones" ON public.milestones;

CREATE POLICY "Manage milestones" ON public.milestones 
FOR ALL USING (public.can_access_project(project_id));

-- B. TASKS
DROP POLICY IF EXISTS "Select tasks" ON public.tasks;
DROP POLICY IF EXISTS "Manage tasks" ON public.tasks;

CREATE POLICY "Manage tasks" ON public.tasks 
FOR ALL USING (
    EXISTS (
        SELECT 1 FROM public.milestones m 
        WHERE m.id = tasks.milestone_id AND public.can_access_project(m.project_id)
    )
);

-- C. EXPENSES
DROP POLICY IF EXISTS "Select expenses" ON public.expenses;
DROP POLICY IF EXISTS "Manage expenses" ON public.expenses;

CREATE POLICY "Manage expenses" ON public.expenses 
FOR ALL USING (public.can_access_project(project_id));
