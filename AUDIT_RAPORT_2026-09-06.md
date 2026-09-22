# Raport de audit — OmnicientScanner v2.2

Data: 2026-09-06
Metodă: scanare automată a celor 356 fișiere `.kt` din `app/`, `scios_marrow/`, `planet_scanner_core/` după markeri de date simulate/hardcodate (`mock`, `dummy`, `fake`, `placeholder`, `TODO`, `FIXME`), urmată de verificare manuală a fiecărui rezultat real (grep-ul inițial fără word-boundary a dat ~180 fals-pozitive, ex. „TODO” se potrivea cu „toDouble” — au fost filtrate).

## Context important
Proiectul conține zeci de module cu denumiri fanteziste (Warp Drive, Multiverse Oracle, Temporal Physics, Dark Matter, AI Consciousness, Black Hole, XenoIntelligence) care, prin natura lor, nu pot avea date "reale" — nu există fizică reală de warp drive. **Decizie confirmată cu clientul: aceste module rămân simulări/joc, dar trebuie etichetate clar ca atare în UI.** Acest raport nu le tratează ca bug-uri, doar pe cele de mai jos, care erau prezentate ca date reale deși nu erau.

## Probleme confirmate și reparate

### 1. `CopernicusSatelliteService.kt` — număr sateliți hardcodat
Funcția `getActiveEarthObservationSatelliteCount()` returna constant `8`, cu comentariu explicit „TODO: Integrate TLE tracker service for real-time count”.
**Fix:** acum numără efectiv obiectele cu numele conținând „SENTINEL” din catalogul orbital live populat de `TleTrackerService` (date reale Celestrak). Dacă acel catalog nu e încă sincronizat, cade pe numărul general de sateliți urmăriți (tot real), nu pe un număr inventat.

### 2. `CernDataRepository.kt` — fallback confundabil cu date live
La eșec HTTP/parsing, `getFallbackStatus()` întorcea valori plauzibile (6.8 TeV etc.) fără niciun marcaj distinctiv față de răspunsul real — UI-ul nu putea ști dacă afișează date CERN live sau inventate. În plus, `json.optDouble("energy", 6.8)` ar fi mascat silențios o schimbare de schemă JSON cu o valoare falsă care pare reală.
**Fix:** am adăugat câmpul explicit `isSimulated: Boolean`, am verificat prezența câmpurilor cheie înainte de a le trata ca reale, și am propagat `isSimulated` până în `QuantumColliderViewModel` — descoperirile de particule sunt marcate „confirmate de CERN” doar când datele NU sunt simulate.

### 3. `BioAgeChronosViewModel.kt` — vârstă biologică hardcodată
Pe calea de analiză locală, rezultatul afișat era mereu `biologicalAge = 33`, indiferent de imagine sau de răspunsul motorului AI local — comentat explicit „Valoare placeholder”.
**Fix:** extrage vârsta din răspunsul motorului local la fel cum se face deja pe calea cloud, în loc de valoare fixă. Am adăugat și flag `isEstimateOnly = true`: de menționat că nici calea locală, nici cea cloud nu folosesc un model de estimare biometrică validat clinic — ambele cer unui LLM să „ghicească” o vârstă dintr-un prompt text, deci rezultatul e o estimare experimentală, nu un diagnostic medical. Recomand afișarea explicită a acestei mențiuni în UI.

### 4. `FaceCloudService.kt` — endpoint inexistent, cod neconectat
Serviciul apelează `https://api.sovereign-football.io/v1/faces/...`, un domeniu placeholder care nu corespunde niciunui backend real. Mai important: **nu e apelat de nicio parte din UI** — cod complet neconectat.
**Fix:** comportamentul (return null la eșec, fără imagine falsă de rezervă) era deja corect; am făcut eșecurile vizibile în log și am documentat explicit că serviciul are nevoie de un backend real configurat înainte de a fi folosit.

### 5. `BillingManager.kt` — complet neimplementat
`launchProBillingFlow()` și `isProUser()` erau goale (TODO), `isProUser()` întorcea mereu `false`.
**Fix:** implementare completă cu Google Play Billing Library v7 (`com.android.billingclient:billing-ktx:7.1.1`, adăugat în `app/build.gradle.kts`): conectare la `BillingClient`, interogare reală a achizițiilor deținute, lansare flux de cumpărare pentru produsul `pro_upgrade_yearly`, confirmare (acknowledge) automată, cache sincron pentru citire rapidă din UI.
**Important — pas manual obligatoriu:** produsul `pro_upgrade_yearly` trebuie creat de tine în Google Play Console; până atunci cumpărarea va eșua cu `ITEM_UNAVAILABLE`, ceea ce e normal, nu un bug de cod.

## Ce NU am putut face în acest pas
- **Nu am compilat proiectul** — nu am acces la Android SDK/Gradle cu rețea în acest mediu, deci fix-urile de mai sus nu au fost verificate printr-un build real. Recomand `./gradlew assembleDebug` local înainte de release.
- **Nu am revizuit toate cele 356 de fișiere linie-cu-linie** — am prioritizat fișierele semnalate de scanarea automată. Un audit complet, verificat prin compilare, ar necesita mai multe sesiuni țintite pe module.
- Modulele fanteziste (Warp Drive, Multiverse Oracle etc.) **nu au primit etichetă vizuală de „simulare” în acest pas** — am confirmat doar direcția (le păstrăm ca joc). Adăugarea efectivă a etichetelor în ecranele Compose e un pas separat, recomand să-l facem într-o sesiune dedicată UI.
- `ProDialogFragment` are deja o problemă structurală preexistentă (nefolosit nicăieri, constructor cu parametru care nu respectă recreerea standard de Fragment la rotație) — nu e cauzată de fix-ul de billing, dar merită reparată separat dacă vrei să folosești efectiv acest dialog.

## Fals-pozitive verificate și excluse
`ReactionSimulator`, `CompetitionService.simulateOtherMatches` (simulare meciuri AI), textele „placeholder” din câmpurile de login/parolă — sunt funcționalități reale, nu date false.
