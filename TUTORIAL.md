# Re Content Project: Teknik Eğitim Rehberi (Faz 1-6)

Bu belge, **Re Content Project**'in gelişim sürecini, mimari kararlarını ve teknik derinliğini 6 faz perspektifiyle anlatan kapsamlı bir eğitim kılavuzudur.

---

## 1. Giriş ve Proje Amacı
Modern yazılım dünyasında içerik kataloğu (TMDb) ile kullanıcıya özel veriyi (Personalization) ayırmak, ölçeklenebilir bir platformun temelidir.
- **Problem:** TMDb gibi devasa katalogları tamamen kopyalamak maliyetli ve gereksizdir.
- **Çözüm:** Katalog verisini API üzerinden *on-the-fly* okumak, kullanıcıya özel seçimleri (favoriler, puanlar, incelemeler) ise kendi backend'imizde sahiplenmek.
- **Vizyon:** TMDb kataloğunu zenginleşmiş bir kullanıcı deneyimi ile birleştiren, production-ready bir ekosistem kurmak.

---

## 2. Teknoloji Seçimi ve Mimari Gerekçeler
Proje, kurumsal standartlarda (Enterprise-ready) teknolojileri tercih eder:
- **Java 21 & Spring Boot 3.4+:** Güçlü tip güvenliği, sanal thread desteği ve olgun ekosistem.
- **React:** Dinamik ve hızlı UI, zengin bileşen kütüphanesi.
- **PostgreSQL:** Karmaşık ilişkisel veri yapıları için güvenilir liman (ACID).
- **Docker & Nginx:** "Her yerde çalışabilir" (Environment parity) ve güvenli yönlendirme.
- **Flyway:** Veritabanını kod gibi versiyonlama disiplini.

---

## 3. Gelişim Yolculuğu: Faz Bazlı İlerleme Stratejisi
Proje, kaostan düzene şu fazlarla ilerlemiştir:
1.  **Faz 1 (Temeller):** Spring Boot iskeleti, PostgreSQL bağlantısı ve temel CRUD.
2.  **Faz 2 (Güvenlik):** Spring Security, JWT ve HttpOnly Cookie entegrasyonu.
3.  **Faz 3 (Kullanıcı Verisi):** Favoriler, Watchlist ve Profil yönetimi.
4.  **Faz 4 (Sosyal Katman):** Puanlama (Ratings) ve İnceleme (Reviews) sistemleri.
5.  **Faz 5 (Operasyonel Hazırlık):** Üyelik (Membership) iskeleti, Actuator ve Merkezi Audit Log.
6.  **Faz 6 (Modern Infra):** Full-stack Dockerization ve Nginx Reverse Proxy.

---

## 4. Frontend ve Backend Sınırları: TMDb vs. Kendi Verimiz
- **TMDb (Dış Dünya):** Film başlıkları, poster yolları, fragmanlar, oyuncu bilgileri.
- **Backend (Bizim Dünyamız):** "Hangi kullanıcı hangi filmi sevdi?", "Kimi takip ediyor?", "Kaç puan verdi?".
- **Dosya Referansı:** `re-content/src/services/catalogService.js` katalog, `backendApi.js` bizim verimiz için kullanılır.

---

## 5. Modüler Monolit Mimari ve Paket Yapısı
Karmaşıklığı yönetmek için uygulama "Feature-based Packaging" kullanır:
- **`auth`:** Kayıt ve giriş.
- **`user`:** Profil ve ayarlar.
- **`favorite` / `watchlist`:** Liste yönetimi.
- **`rating` / `review`:** Geri bildirim motoru.
- **`admin` / `audit`:** Platform yönetimi ve izlenebilirlik.

---

## 6. Veritabanı Tasarımı ve Migration (Flyway)
- **Strateji:** UUID kullanımı ile ID tahmin edilebilirliği engellenmiş ve güvenlik artırılmıştır.
- **Migration:** `V1__initial_schema.sql` tüm tabloları, kısıtlamaları (Constraints) ve indeksleri tek seferde oluşturur.
- **Kritik Kazanç:** Index stratejisi (`idx_favorites_user_created` gibi) sayesinde milyonlarca kayıt arasında milisaniyelik sorgu performansı sağlanır.

---

## 7. Kimlik Doğrulama (Auth) Derin Dalış: JWT ve Refresh Cookie
Proje, modern ve güvenli bir hibrit auth yöntemi kullanır:
- **Access Token (JWT):** Memory'de tutulur, her istekte `Authorization: Bearer` olarak gönderilir.
- **Refresh Token:** HttpOnly Cookie içinde tutulur. JavaScript tarafından okunamaz (XSS koruması).
- **Akış:** Access token bittiğinde, backend cookie'deki refresh token ile otomatik yeni bir access token üretir (Silent Refresh).
- **Dosya:** `com.recontent.backend.security.SecurityConfig` ve `AuthController.java`.

---

## 8. REST API Tasarımı ve Hata Yönetimi
- **Standart:** API'lar tutarlı bir hata şablonu döner (`GlobalExceptionHandler.java`).
- **Validasyon:** Bean Validation (@NotBlank, @Size) ile bozuk veri daha Controller seviyesindeyken reddedilir.
- **DTO:** Veritabanı modelleri (Entities) asla doğrudan dışarı açılmaz; katmanlar arası veri transferi DTO'lar ile yapılır.

---

## 9. Güvenlik Yapılandırması ve CORS
- **CORS:** Frontend (`localhost:3000`) ve Backend (`localhost:8080`) arasındaki "yabancı köken" engeli, `SecurityConfig` üzerinden hassas bir şekilde yönetilir.
- **Credentials:** Cookie bazlı çalıştığımız için `allowCredentials(true)` kritik öneme sahiptir.

---

## 10. Dockerize Etme: Dockerfile ve Compose
- **Multi-stage Build:** Dockerfile'lar, önce kodu derler (Maven/NPM), sonra sadece çalıştırılabilir çıktıyı (JAR/Static files) son imaja alır. Bu sayede imaj boyutu minimuma indirilir.
- **Env Files:** `.env` dosyaları ile credential'lar kodun dışına çıkarılır.

---

## 11. Nginx: Reverse Proxy ve Yönlendirme Stratejisi
`infra/nginx/default.conf` projenin beynidir:
- `/api/` isteklerini backend'e,
- Geri kalan (`/`) tüm istekleri frontend'e yönlendirir.
- **Neden önemli?** Frontend'in backend portunu bilmesine gerek kalmaz; tek bir port (80) üzerinden tüm stack konuşur.

---

## 12. Altyapı (Infra) Yönetimi: Tek Komutla Çalıştırma
`re-content-backend/infra/docker-compose.yml` şu servisleri ayağa kaldırır:
1. `db`: PostgreSQL
2. `backend`: Spring Boot App
3. `frontend`: Production-grade Nginx serving React
4. `proxy`: Main Nginx Entrypoint
- **Komut:** `docker compose up --build`

---

## 13. Hibrit Kullanıcı Deneyimi: Guest ve Auth Uyum
Kullanıcı deneyimi kesintiye uğramaz:
- **Guest Modu:** Favoriler `localStorage`'da tutulur.
- **Login:** Kullanıcı giriş yaptığı an, frontend (`useFavorites.js`) `localStorage`'daki veriyi otomatik olarak backend'e senkronize eder.
- **Sonuç:** Kullanıcı "önce liste yap, sonra kaydol" diyebilir.

---

## 14. Kritik Teknik Kararlar ve Karşılaşılan Zorluklar
- **Decision:** Neden UUID? Dağıtık sistemlere hazırlık ve güvenlik.
- **Challenge:** Refresh token rotation sırasında yarış durumları (race conditions). Çözüm: Tek kullanımlık (Single-use) token mantığı.
- **Decision:** Neden Redux + Context? Global state (Auth) ve Feature state (Favorites) ayrımı için.

---

## 15. Kavram Sözlüğü
- **JWT (JSON Web Token):** Taraflar arasında güvenli veri iletimi sağlayan kompakt format.
- **Flyway:** Veritabanı şema versiyonlama aracı.
- **Rev Proxy (Nginx):** İstekleri karşılayıp ilgili servise dağıtan kapı görevlisi.
- **Hibernate/JPA:** Java nesnelerini SQL tablolarına eşleyen köprü.
- **Actuator:** Uygulamanın sağlık durumunu ve metriklerini izleyen modül.

---

## 16. Projenin Mevcut Durumu ve Faz 6 Kazanımları
- **Faz 6 Sonu:** Tamamen konteynerize edilmiş, CI/CD'ye hazır, merkezi audit ve güvenlik mekanizmaları kurulu, TMDb ile entegre bir sistem.
- **Test:** Integration testleri ile uçtan uca senaryolar (Register -> Favorite -> Logout) garanti altına alınmıştır.

---

## 17. Gelecek Vizyonu: Faz 7+ ve Yol Haritası
- **Faz 7:** Elasticsearch ile gelişmiş arama ve inceleme analitiği.
- **Faz 8:** Microservices dönüşümü (Auth service, Recommendation service ayrımı).
- **Faz 9:** Kubernetes (K8s) manifests ve Helm Chart ile gerçek cloud orkestrasyonu.

---

## 18. Son Söz ve Kariyer Kazanımları
Bu proje, sadece bir "film sitesi" değil; modern backend mimarisi, güvenli kimlik doğrulama, hibrit frontend state yönetimi ve DevOps pratiklerinin birleştiği tam kapsamlı bir mühendislik portfolyosudur.

---
*Hazırlayan: Re Content Engineering Team*
