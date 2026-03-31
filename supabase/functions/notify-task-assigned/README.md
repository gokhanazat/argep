# Task Assigned Notification Function

Bu Edge Function, bir kullanıcıya yeni bir görev atandığında otomatik olarak tetiklenir ve bilgilendirme yapar.

## Kurulum ve Deployment

1. **Supabase CLI Giriş:**
   ```bash
   supabase login
   ```

2. **Fonksiyonu Deploy Et:**
   ```bash
   supabase functions deploy notify-task-assigned
   ```

3. **Webhook Ayarları (Supabase Dashboard):**
   - **Database > Webhooks** sekmesine gidin.
   - Yeni bir Webhook oluşturun:
     - **Name:** `notify_on_task_assignment`
     - **Table:** `tasks`
     - **Events:** `INSERT`, `UPDATE`
     - **Type:** `HTTP Request`
     - **Method:** `POST`
     - **URL:** `${YOUR_PROJECT_URL}/functions/v1/notify-task-assigned`
     - **Headers:** `Authorization: Bearer ${SERVICE_ROLE_KEY}`

## Geliştirme

Lokalde test etmek için:
```bash
supabase functions serve notify-task-assigned --no-verify-jwt
```
