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

    const payload = await req.json()
    const { record, old_record, type } = payload

    // Sadece assigned_to değiştiğinde veya yeni atama yapıldığında çalış
    const newAssignedTo = record?.assigned_to
    const oldAssignedTo = old_record?.assigned_to

    if (!newAssignedTo || (type === 'UPDATE' && newAssignedTo === oldAssignedTo)) {
      return new Response(JSON.stringify({ success: true, message: 'No assignment change' }), {
        headers: { ...corsHeaders, 'Content-Type': 'application/json' },
        status: 200,
      })
    }

    // Atanan kullanıcının profilini çek
    const { data: profile, error: profileError } = await supabaseClient
      .from('profiles')
      .select('full_name, id')
      .eq('id', newAssignedTo)
      .single()

    if (profileError || !profile) {
      throw new Error(`Profile not found: ${profileError?.message}`)
    }

    // Auth.users tablosuna service_role ile erişip email almamız gerekebilir 
    // Veya profil tablosunda email varsa oradan çekilebilir. 
    // Mevcut şemada email profilde yoksa auth.admin kullanmalıyız:
    const { data: { user }, error: userError } = await supabaseClient.auth.admin.getUserById(newAssignedTo)
    
    if (userError || !user) {
        throw new Error(`User auth not found: ${userError?.message}`)
    }

    const email = user.email

    // Bildirim Gönderimi (Placeholder)
    console.log(`BİLDİRİM: ${profile.full_name} (${email}) kullanıcısına yeni bir görev atandı!`)
    console.log(`Görev Başlığı: ${record.title}`)

    return new Response(JSON.stringify({ success: true, notified: email }), {
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
