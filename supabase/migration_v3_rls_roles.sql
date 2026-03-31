-- 1. YARDIMCI FONKSİYON: get_my_role
CREATE OR REPLACE FUNCTION public.get_my_role(p_project_id uuid)
RETURNS text AS $$
    SELECT role FROM public.team_members
    WHERE user_id = auth.uid() AND project_id = p_project_id;
$$ LANGUAGE sql SECURITY DEFINER;

-- 2. ESKİ POLİTİKALARI TEMİZLE
DROP POLICY IF EXISTS "Users can view projects they are members of" ON public.projects;
DROP POLICY IF EXISTS "Owners can update/insert projects" ON public.projects;

DROP POLICY IF EXISTS "View milestones of accessible projects" ON public.milestones;
DROP POLICY IF EXISTS "Project owners can manage milestones" ON public.milestones;

DROP POLICY IF EXISTS "View tasks of accessible milestones" ON public.tasks;
DROP POLICY IF EXISTS "Assigned users can update tasks" ON public.tasks;
DROP POLICY IF EXISTS "Project owners can manage tasks" ON public.tasks;

DROP POLICY IF EXISTS "Team members are viewable by members of the same project" ON public.team_members;
DROP POLICY IF EXISTS "Project owners can manage team members" ON public.team_members;

-- 3. YENİ ROL TABANLI POLİTİKALAR

-- 3a. PROJECTS
CREATE POLICY "Select Projects: Members or Owner" ON public.projects
    FOR SELECT USING (
        auth.uid() = owner_id OR 
        EXISTS (SELECT 1 FROM public.team_members WHERE project_id = projects.id AND user_id = auth.uid())
    );

CREATE POLICY "Manage Projects: Owner Only" ON public.projects
    FOR ALL USING (auth.uid() = owner_id);

-- 3b. MILESTONES
CREATE POLICY "Select Milestones: Project Members" ON public.milestones
    FOR SELECT USING (
        EXISTS (SELECT 1 FROM public.projects WHERE id = milestones.project_id)
    );

CREATE POLICY "Manage Milestones: PM or Technical Lead" ON public.milestones
    FOR ALL USING (
        get_my_role(project_id) IN ('PROJE_MUDURU', 'TEKNIK_LIDER') OR
        EXISTS (SELECT 1 FROM public.projects WHERE id = project_id AND owner_id = auth.uid())
    );

-- 3c. TASKS
CREATE POLICY "Select Tasks: Project Members" ON public.tasks
    FOR SELECT USING (
        EXISTS (SELECT 1 FROM public.milestones m WHERE m.id = tasks.milestone_id)
    );

CREATE POLICY "Insert Tasks: PM Only" ON public.tasks
    FOR INSERT WITH CHECK (
        EXISTS (
            SELECT 1 FROM public.milestones m 
            WHERE m.id = tasks.milestone_id 
            AND (get_my_role(m.project_id) = 'PROJE_MUDURU' OR EXISTS (SELECT 1 FROM public.projects p WHERE p.id = m.project_id AND p.owner_id = auth.uid()))
        )
    );

CREATE POLICY "Update Tasks: PM or Assigned User" ON public.tasks
    FOR UPDATE USING (
        auth.uid() = assigned_to OR
        EXISTS (
            SELECT 1 FROM public.milestones m 
            WHERE m.id = tasks.milestone_id 
            AND (get_my_role(m.project_id) = 'PROJE_MUDURU' OR EXISTS (SELECT 1 FROM public.projects p WHERE p.id = m.project_id AND p.owner_id = auth.uid()))
        )
    );

CREATE POLICY "Delete Tasks: PM Only" ON public.tasks
    FOR DELETE USING (
        EXISTS (
            SELECT 1 FROM public.milestones m 
            WHERE m.id = tasks.milestone_id 
            AND (get_my_role(m.project_id) = 'PROJE_MUDURU' OR EXISTS (SELECT 1 FROM public.projects p WHERE p.id = m.project_id AND p.owner_id = auth.uid()))
        )
    );

-- 3d. TEAM_MEMBERS
CREATE POLICY "Select Team: Project Members" ON public.team_members
    FOR SELECT USING (
        EXISTS (SELECT 1 FROM public.projects WHERE id = team_members.project_id)
    );

CREATE POLICY "Manage Team: PM Only" ON public.team_members
    FOR ALL USING (
        get_my_role(project_id) = 'PROJE_MUDURU' OR
        EXISTS (SELECT 1 FROM public.projects WHERE id = project_id AND owner_id = auth.uid())
    );
