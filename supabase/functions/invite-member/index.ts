import { serve } from 'https://deno.land/std@0.168.0/http/server.ts'
import { createClient } from 'https://esm.sh/@supabase/supabase-js@2'

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
  'Access-Control-Allow-Methods': 'POST, OPTIONS',
}

serve(async (req) => {
  if (req.method === 'OPTIONS') {
    return new Response('ok', { headers: corsHeaders })
  }

  try {
    const supabaseAdmin = createClient(
      Deno.env.get('SUPABASE_URL') ?? '',
      Deno.env.get('SUPABASE_SERVICE_ROLE_KEY') ?? '',
      { auth: { persistSession: false } }
    )

    let body
    try {
      body = await req.json()
    } catch (e) {
      throw new Error(`JSON ayrıştırma hatası (Gelen veri bozuk): ${e.message}`)
    }

    const { email, projectId, role } = body

    if (!email || !projectId) {
      throw new Error(`E-posta (${email}) veya Proje ID (${projectId}) gelmedi!`)
    }

    console.log(`[invite-member] Gelen veriler -> E-posta: ${email}, Proje: ${projectId}, Rol: ${role}`)

    // ATOMİK SQL FONKSİYONUNU ÇAĞIR
    const { data: result, error: rpcError } = await supabaseAdmin.rpc('add_team_member_by_email', { 
        p_email: email.trim().toLowerCase(),
        p_project_id: projectId,
        p_role: role || 'GOZLEMCI'
    })

    if (rpcError) {
      console.error(`[invite-member] RPC Hatası: ${rpcError.message}`)
      // RPC Hatasını UI'a açıkça dönüyoruz
      return new Response(JSON.stringify({ 
        success: false, 
        error: `SQL Hatası: ${rpcError.message}. Lütfen v11 SQL'in yüklü olduğundan emin olun.` 
      }), { 
        headers: { ...corsHeaders, 'Content-Type': 'application/json' }, 
        status: 200 
      })
    }

    if (!result || !result.success) {
      const errorMsg = result?.error || 'Bilinmeyen SQL Hatası'
      console.warn(`[invite-member] RPC başarısız: ${errorMsg}`)
      return new Response(JSON.stringify({ success: false, error: errorMsg }), { 
        headers: { ...corsHeaders, 'Content-Type': 'application/json' }, 
        status: 200 
      })
    }

    console.log(`[invite-member] Başarılı: ${email} eklendi.`)
    return new Response(JSON.stringify(result), { 
      headers: { ...corsHeaders, 'Content-Type': 'application/json' }, 
      status: 200 
    })

  } catch (error: any) {
    console.error(`[invite-member] Global Hata: ${error.message}`)
    return new Response(JSON.stringify({ success: false, error: error.message }), {
      headers: { ...corsHeaders, 'Content-Type': 'application/json' },
      status: 200 // Hata mesajlarını 200 ile dönerek snackbar'da gösterilmesini sağlıyoruz
    })
  }
})
