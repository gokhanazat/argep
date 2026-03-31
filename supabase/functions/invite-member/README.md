# Üye Davet Sistemi (Invite Member)

Bu fonksiyon, bir projeye sadece yetkili (PROJE_MUDURU veya SAHIBI) kişiler tarafından yeni üye davet edilmesini sağlar.

## Kurulum ve Deployment

1. **Migration Uygula:**
   `migration_v2_invitations.sql` dosyasındaki SQL komutlarını Supabase SQL Editor'da çalıştırın.

2. **Supabase Auth Ayarı (KRİTİK):**
   - **Dashboard > Auth > Settings** kısmında "Enable Sign Ups" seçeneğini **KAPATIN**.
   - Bu sayede kimse davetiye almadan sisteme kayıt olamaz.

3. **Fonksiyonu Deploy Et:**
   ```bash
   supabase functions deploy invite-member
   ```

4. **Kullanım:**
   Fonksiyona aşağıdaki JSON gövdesi ile POST isteği atılır:
   ```json
   {
     "email": "yeni.uye@example.com",
     "projectId": "PROJE_UUID",
     "role": "Test Mühendisi"
   }
   ```
   **Header:** `Authorization: Bearer USER_TOKEN`

## Güvenlik
- Fonksiyon, `is_project_manager` RPC fonksiyonunu kullanarak talebi yapan kişinin yetkisini veritabanı seviyesinde kontrol eder.
- Kayıtlar `invitations` tablosunda saklanır ve audit trail oluşturulur.
