-- 1. PROFILES Tablosu Güncelleme
ALTER TABLE public.profiles ADD COLUMN IF NOT EXISTS is_active boolean DEFAULT false;
ALTER TABLE public.profiles ADD COLUMN IF NOT EXISTS invited_by uuid REFERENCES auth.users(id);

-- 2. INVITATIONS Tablosu Oluşturma
CREATE TABLE IF NOT EXISTS public.invitations (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    email text NOT NULL UNIQUE,
    project_id uuid REFERENCES public.projects(id) ON DELETE CASCADE,
    role text NOT NULL,
    invited_by uuid REFERENCES auth.users(id),
    token text UNIQUE DEFAULT gen_random_uuid()::text,
    expires_at timestamptz DEFAULT (now() + interval '7 days'),
    accepted_at timestamptz,
    created_at timestamptz DEFAULT now()
);

-- RLS Etkinleştirme
ALTER TABLE public.invitations ENABLE ROW LEVEL SECURITY;

-- 3. RLS POLICIES: INVITATIONS
-- Sadece Proje Müdürü veya Proje Sahibi davet oluşturabilir
CREATE POLICY "Project Managers can manage invitations" ON public.invitations
    FOR ALL USING (
        EXISTS (
            SELECT 1 FROM public.projects p
            LEFT JOIN public.team_members tm ON tm.project_id = p.id
            WHERE p.id = invitations.project_id 
            AND (p.owner_id = auth.uid() OR (tm.user_id = auth.uid() AND tm.role = 'PROJE_MUDURU'))
        )
    );

CREATE POLICY "Invitations are viewable by invited users" ON public.invitations
    FOR SELECT USING (auth.jwt() ->> 'email' = email);

-- 4. Fonksiyon Yardımıyla Rol Kontrolü (Edge Function için kolaylık)
CREATE OR REPLACE FUNCTION public.is_project_manager(p_project_id uuid, p_user_id uuid)
RETURNS boolean AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM public.projects p
        LEFT JOIN public.team_members tm ON tm.project_id = p.id
        WHERE p.id = p_project_id 
        AND (p.owner_id = p_user_id OR (tm.user_id = p_user_id AND tm.role = 'PROJE_MUDURU'))
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
