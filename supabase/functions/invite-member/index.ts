import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from "https://esm.sh/@supabase/supabase-js@2"

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
}

serve(async (req) => {
  if (req.method === 'OPTIONS') {
    return new Response('ok', { headers: corsHeaders })
  }

  try {
    const supabaseClient = createClient(
      Deno.env.get('SUPABASE_URL') ?? '',
      Deno.env.get('SUPABASE_SERVICE_ROLE_KEY') ?? ''
    )

    // İstek yapan kullanıcının tokenını al
    const authHeader = req.headers.get('Authorization')!
    const token = authHeader.replace('Bearer ', '')
    const { data: { user: requester }, error: authError } = await supabaseClient.auth.getUser(token)

    if (authError || !requester) throw new Error('Unauthorized')

    const { email, projectId, role } = await req.json()

    // 1. Yetki Kontrolü: İstek yapan PROJE_MUDURU mü?
    const { data: isManager, error: managerError } = await supabaseClient
      .rpc('is_project_manager', { p_project_id: projectId, p_user_id: requester.id })

    if (managerError || !isManager) {
      return new Response(JSON.stringify({ error: 'Yetkiniz yok. Sadece Proje Müdürü davet gönderebilir.' }), {
        headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        status: 403,
      })
    }

    // 2. Invitations tablosuna kayıt at
    const { error: inviteDbError } = await supabaseClient
      .from('invitations')
      .insert({
        email,
        project_id: projectId,
        role,
        invited_by: requester.id
      })

    if (inviteDbError) throw inviteDbError

    // 3. Supabase Auth ile davet gönder
    const { data: inviteData, error: authInviteError } = await supabaseClient.auth.admin.inviteUserByEmail(email, {
        data: { 
            invited_by: requester.id,
            project_id: projectId,
            target_role: role
        }
    })

    if (authInviteError) throw authInviteError

    return new Response(JSON.stringify({ success: true, invitedEmail: email }), {
      headers: { ...corsHeaders, 'Content-Type': 'application/json' },
      status: 200,
    })

  } catch (error) {
    return new Response(JSON.stringify({ error: error.message }), {
      headers: { ...corsHeaders, 'Content-Type': 'application/json' },
      status: 400,
    })
  }
})
