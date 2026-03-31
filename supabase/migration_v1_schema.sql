-- 1. PROFILES Table (Extends auth.users)
CREATE TABLE public.profiles (
    id uuid PRIMARY KEY REFERENCES auth.users ON DELETE CASCADE,
    full_name text,
    avatar_url text,
    department text,
    created_at timestamptz DEFAULT now()
);

-- 2. PROJECTS Table
CREATE TABLE public.projects (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name text NOT NULL,
    description text,
    phase text,
    status text,
    owner_id uuid REFERENCES auth.users DEFAULT auth.uid(),
    created_at timestamptz DEFAULT now()
);

-- 3. MILESTONES Table
CREATE TABLE public.milestones (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id uuid REFERENCES public.projects ON DELETE CASCADE NOT NULL,
    title text NOT NULL,
    due_date date,
    status text,
    created_at timestamptz DEFAULT now()
);

-- 4. TASKS Table
CREATE TABLE public.tasks (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    milestone_id uuid REFERENCES public.milestones ON DELETE CASCADE NOT NULL,
    title text NOT NULL,
    description text,
    assigned_to uuid REFERENCES auth.users,
    priority text,
    status text,
    created_at timestamptz DEFAULT now(),
    updated_at timestamptz DEFAULT now()
);

-- 5. TEAM_MEMBERS Table
CREATE TABLE public.team_members (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid REFERENCES auth.users ON DELETE CASCADE NOT NULL,
    project_id uuid REFERENCES public.projects ON DELETE CASCADE NOT NULL,
    role text,
    joined_at timestamptz DEFAULT now(),
    UNIQUE(user_id, project_id)
);

-- ENABLE ROW LEVEL SECURITY
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.projects ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.milestones ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.tasks ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.team_members ENABLE ROW LEVEL SECURITY;

-- POLICIES: PROFILES
CREATE POLICY "Public profiles are viewable by everyone" ON public.profiles
    FOR SELECT USING (true);
CREATE POLICY "Users can update their own profile" ON public.profiles
    FOR UPDATE USING (auth.uid() = id);
CREATE POLICY "Users can insert their own profile" ON public.profiles
    FOR INSERT WITH CHECK (auth.uid() = id);

-- POLICIES: PROJECTS
CREATE POLICY "Users can view projects they are members of" ON public.projects
    FOR SELECT USING (
        auth.uid() = owner_id OR 
        EXISTS (SELECT 1 FROM public.team_members WHERE project_id = projects.id AND user_id = auth.uid())
    );
CREATE POLICY "Owners can update/insert projects" ON public.projects
    FOR ALL USING (auth.uid() = owner_id);

-- POLICIES: MILESTONES (Inherit from projects access)
CREATE POLICY "View milestones of accessible projects" ON public.milestones
    FOR SELECT USING (
        EXISTS (SELECT 1 FROM public.projects WHERE id = milestones.project_id) -- Restricted by Project SELECT policy
    );
CREATE POLICY "Project owners can manage milestones" ON public.milestones
    FOR ALL USING (
        EXISTS (SELECT 1 FROM public.projects WHERE id = milestones.project_id AND owner_id = auth.uid())
    );

-- POLICIES: TASKS (Inherit from milestones/projects)
CREATE POLICY "View tasks of accessible milestones" ON public.tasks
    FOR SELECT USING (
        EXISTS (SELECT 1 FROM public.milestones WHERE id = tasks.milestone_id)
    );
CREATE POLICY "Assigned users can update tasks" ON public.tasks
    FOR UPDATE USING (auth.uid() = assigned_to);
CREATE POLICY "Project owners can manage tasks" ON public.tasks
    FOR ALL USING (
        EXISTS (
            SELECT 1 FROM public.projects p 
            JOIN public.milestones m ON m.project_id = p.id 
            WHERE m.id = tasks.milestone_id AND p.owner_id = auth.uid()
        )
    );

-- POLICIES: TEAM_MEMBERS
CREATE POLICY "Team members are viewable by members of the same project" ON public.team_members
    FOR SELECT USING (
        EXISTS (SELECT 1 FROM public.projects WHERE id = team_members.project_id)
    );
CREATE POLICY "Project owners can manage team members" ON public.team_members
    FOR ALL USING (
        EXISTS (SELECT 1 FROM public.projects WHERE id = team_members.project_id AND owner_id = auth.uid())
    );
