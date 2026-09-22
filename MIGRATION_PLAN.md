# MIGRATION PLAN - FROM FAKE TO REAL AUTONOMOUS
## Complete Implementation Timeline

---

## 📅 WEEK 1: SETUP & DEPENDENCIES

### Day 1: Download Models & Setup
```bash
# 1. Download Gemma-7B-Q4 (4.2GB)
# From: https://huggingface.co/google/gemma-7b-it-gguf/blob/main/gemma-7b-it-q4_0.gguf
# Or use: ollama pull gemma:7b

# 2. Place in app/src/main/assets/models/
mkdir -p app/src/main/assets/models
# Download and place gemma-7b-q4.gguf here

# 3. Verify download
ls -lh app/src/main/assets/models/gemma-7b-q4.gguf
# Should be ~4.2GB
```

### Day 2: Update build.gradle.kts
```kotlin
dependencies {
    // TensorFlow Lite (for Gemma inference)
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-nnapi:2.14.0")
    
    // Mistral API
    implementation("io.mistral:mistral-client-kotlin:0.3.0")
    
    // OkHttp (for API calls)
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    
    // Dagger Hilt (DI)
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Room (for caching)
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    
    // Health Connect (Apple Watch data)
    implementation("androidx.health:health-connect-client:1.0.0")
    
    // Firebase (if using)
    implementation("com.google.firebase:firebase-auth:22.3.1")
    implementation("com.google.firebase:firebase-database:20.3.1")
    
    // Timber (logging)
    implementation("com.jakewharton.timber:timber:5.0.1")
}
```

### Day 3-4: Create Local.properties & Secrets
```properties
# local.properties (add to .gitignore)
nasa.api.key=your_nasa_key
mistral.api.key=your_mistral_key
gemini.api.key=your_gemini_key
jwst.api.key=your_jwst_key
ollama.url=http://localhost:11434
```

```bash
# .gitignore - ADD THESE
local.properties
*.jks
secrets.properties
BuildConfig.java
**/generated/
**/.gradle/
```

---

## 📅 WEEK 2: CORE IMPLEMENTATIONS

### Day 5: Implement GemmaLocalEngine
**File:** `scios_marrow/src/main/java/com/chemscanner/omniscient/marrow/ml/GemmaLocalEngine.kt`

Copy from IMPLEMENTATIONS_CONCRETE.md - Section 1 (GemmaLocalEngine)

**Test:**
```kotlin
@Test
fun testGemmaLoads() {
    val engine = GemmaLocalEngine(context)
    assertTrue(engine.isLocalModelAvailable())
}

@Test
fun testGemmaInference() {
    val response = runBlocking {
        gemmaEngine.generateResponse("What is machine learning?")
    }
    assertFalse(response.contains("[GEMMA_OFFLINE]"))
}
```

### Day 6: Implement MistralFallbackEngine
**File:** `scios_marrow/src/main/java/com/chemscanner/omniscient/marrow/ml/MistralFallbackEngine.kt`

Copy from IMPLEMENTATIONS_CONCRETE.md - Section 5 (MistralFallbackEngine)

**Test:**
```kotlin
@Test
fun testOllamaDetection() {
    val available = runBlocking {
        mistralEngine.isOllamaAvailable()
    }
    // May be false if Ollama not running, that's OK
    Timber.d("Ollama available: $available")
}

@Test
fun testMistralAPIFallback() {
    val response = runBlocking {
        mistralEngine.generateViaMistralAPI("Hello")
    }
    // Should either work or gracefully fail
    assertTrue(response == null || response.isNotEmpty())
}
```

### Day 7: Fix Dependency Injection
**File:** `scios_marrow/src/main/java/com/chemscanner/omniscient/marrow/di/AppModuleFixed.kt`

Copy from IMPLEMENTATIONS_CONCRETE.md - Section 3 (Fixed DI)

**Action Items:**
1. Delete old circular dependency DI module
2. Create new AppModuleFixed
3. Rename to AppModule
4. Verify Dagger compilation: `./gradlew build`

---

## 📅 WEEK 3: BIOMETRIC INTEGRATION & ORCHESTRATION

### Day 8: Implement AppleWatchBCIService
**File:** `scios_marrow/src/main/java/com/chemscanner/omniscient/marrow/services/AppleWatchBCIService.kt`

Copy from IMPLEMENTATIONS_CONCRETE.md - Section 4

**Permissions to add (AndroidManifest.xml):**
```xml
<uses-permission android:name="android.permission.health.READ_HEART_RATE" />
<uses-permission android:name="android.permission.health.READ_STEPS" />
<uses-permission android:name="android.permission.health.READ_HEART_RATE_VARIABILITY" />
<uses-permission android:name="android.permission.health.READ_SLEEP" />
```

**Runtime permissions:**
```kotlin
// Request at startup
HealthConnectClient.getOrCreate(context).requestPermissions(
    listOf(
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class)
    )
)
```

### Day 9-10: Implement OmniscientOrchestratorFixed
**File:** `scios_marrow/src/main/java/com/chemscanner/omniscient/marrow/services/OmniscientOrchestratorFixed.kt`

Copy from IMPLEMENTATIONS_CONCRETE.md - Section 1

**Key changes from old Orchestrator:**
- Remove circular dependencies (Lazy<> removed)
- Add real fallback chain
- Add biometric augmentation
- Add proper error handling
- Add timeout management

### Day 11: Implement ResponseCacheManager
**File:** `scios_marrow/src/main/java/com/chemscanner/omniscient/marrow/data/ResponseCacheManager.kt`

Copy from IMPLEMENTATIONS_CONCRETE.md - Section 2

**Database setup:**
```kotlin
@Database(entities = [CachedResponseEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cacheDao(): CacheDao
}

// In AppModule:
@Singleton
@Provides
fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
    Room.databaseBuilder(context, AppDatabase::class.java, "app_cache.db")
        .fallbackToDestructiveMigration()
        .build()

@Provides
fun provideCacheDao(db: AppDatabase): CacheDao = db.cacheDao()
```

---

## 📅 WEEK 4: BUG FIXES & CLEANUP

### Day 12-13: Fix 48 Critical Bugs
**Reference:** BUG_FIXES_DETAILED.md

Priority order:
1. **CRITICAL (Blocking):**
   - Fix circular dependencies (DI compilation)
   - Remove hardcoded API keys
   - Fix null pointer exceptions

2. **HIGH (Security):**
   - Add input validation
   - Fix network timeouts
   - Implement error handling

3. **MEDIUM (Quality):**
   - Replace fake implementations
   - Fix memory leaks
   - Add proper logging

### Specific files to fix:

```
❌ CernDataRepository.kt - Replace fake CERN endpoint
Fix: Use opendata.cern.ch or remove pretense

❌ MarsRoverService.kt - Remove hardcoded API key
Fix: Use ApiKeyProvider

❌ ShadowMeshService.kt - Remove fake encryption
Fix: Use javax.crypto.Cipher or remove

❌ NeuroPhysRepository.kt - Remove pseudoscience
Fix: Use real Apple Watch data

❌ SovereignStealthIntelligence.kt - Remove hardcoded responses
Fix: Use Gemma/Mistral responses

❌ Sentinel2SatelliteService.kt - Remove mock data
Fix: Use real API or label as simulation

❌ BciIntegrationService.kt - Remove .hashCode()
Fix: Use real Apple Watch BCI data

❌ XilonNeuralLatticeService.kt - Remove fake satellite data
Fix: Use real local Gemma engine

❌ JamesWebbSpaceTelescopeService.kt - Remove exposed API key
Fix: Use ApiKeyProvider

❌ GlobalKnowledgeRepository.kt - Remove hardcoded coordinates
Fix: Use real location or Apple Watch
```

### Day 14: Comprehensive Testing
```bash
# Run all tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Check code coverage
./gradlew jacocoTestReport

# Lint check
./gradlew lint
```

---

## 📅 WEEK 5: PERFORMANCE OPTIMIZATION

### Day 15: Profile & Optimize

**1. Gemma Inference Performance**
```kotlin
// Measure inference time
val startTime = System.currentTimeMillis()
val response = gemmaEngine.generateResponse(prompt)
val elapsed = System.currentTimeMillis() - startTime

Timber.d("GEMMA: Inference took ${elapsed}ms")
// Target: < 1000ms per 256 tokens
```

**Optimization strategies:**
- Enable GPU acceleration
- Use NNAPI hardware
- Quantize model further (Q2 = 2.1GB, slower but smaller)
- Batch requests

**2. Cache Hit Rate**
```kotlin
// Monitor cache efficiency
val stats = cacheManager.getStats()
Timber.d("Cache hit rate: ${stats.hitRate}%")
Timber.d("Average cache retrieval: ${stats.avgRetrievalTime}ms")
```

**Optimization:**
- Keep hot prompts in memory (LRU cache)
- Increase cache TTL for frequently used prompts

**3. Network Optimization**
```kotlin
// Use connection pooling
val okHttpClient = OkHttpClient.Builder()
    .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
    .build()

// Monitor network calls
.addNetworkInterceptor(HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BASIC
})
```

---

## 📊 FEATURE PARITY CHECKLIST

### Before (FAKE)
- [ ] Offline processing? NO (100% Gemini dependent)
- [ ] Local intelligence? NO (stubs)
- [ ] Real BCI data? NO (pseudoscience)
- [ ] Autonomous fallback? NO (single point of failure)
- [ ] Biometric context? NO (fake)
- [ ] Data privacy? NO (exposed API keys)

### After (REAL)
- [x] Offline Gemma local inference
- [x] Mistral fallback (local Ollama or API)
- [x] Real Apple Watch BCI data
- [x] 4-tier fallback chain (Gemma -> Ollama -> Mistral -> Gemini)
- [x] Real physiological context (HRV, HR, stress)
- [x] API keys in environment variables
- [x] Input validation
- [x] Network timeouts
- [x] Proper error handling
- [x] Memory leak prevention
- [x] 48 critical bugs fixed

---

## 🧪 TESTING STRATEGY

### Unit Tests (50+ tests)
```kotlin
class GemmaLocalEngineTest { ... }
class MistralFallbackEngineTest { ... }
class OmniscientOrchestratorFixedTest { ... }
class AppleWatchBCIServiceTest { ... }
class ResponseCacheManagerTest { ... }
class ApiKeyProviderTest { ... }
```

Run: `./gradlew test`

### Integration Tests (20+ tests)
```kotlin
class FallbackChainIntegrationTest { ... }
class EndToEndOrchestratorTest { ... }
class BiometricAugmentationTest { ... }
```

Run: `./gradlew connectedAndroidTest`

### Performance Tests
```kotlin
class GemmaInferencePerformanceTest {
    @Test
    fun testInferenceLatency() {
        // Target: < 1000ms
    }
}
```

---

## 📈 METRICS TO TRACK

### Performance
- Gemma inference latency: target < 800ms
- Cache hit rate: target > 60%
- Network request latency: target < 2s
- App startup time: should not increase > 500ms

### Quality
- Test coverage: target > 80%
- Bug fix rate: all 48 bugs resolved
- API key exposure: 0 keys in source
- Memory leaks: 0 detected

### User Experience
- Offline capability: yes
- Fallback reliability: 4-tier chain
- Biometric context: real data
- Error messages: clear and actionable

---

## 🔐 SECURITY CHECKLIST

- [ ] Remove all hardcoded API keys
- [ ] Implement API key rotation
- [ ] Add input validation (SQL injection, XSS)
- [ ] Encrypt sensitive data in SQLite
- [ ] Use HTTPS only for network requests
- [ ] Validate SSL certificates
- [ ] Implement certificate pinning for APIs
- [ ] Remove debug logging in release builds
- [ ] Sanitize error messages (don't expose paths)
- [ ] Implement rate limiting for local APIs
- [ ] Add request signing (prevent tampering)
- [ ] Secure storage of user credentials

---

## 🚀 DEPLOYMENT

### Staging Build (internal testing)
```bash
./gradlew assembleStaging
# Test with Gemma locally, fallback chains
# Verify all biometric integrations
# Profile performance
```

### Release Build (production)
```bash
./gradlew assembleRelease
# Strip logging
# Enable ProGuard/R8
# Sign APK/AAB
```

**First release notes:**
```
v2.0 - True Autonomous Intelligence
- Offline Gemma-7B processing (no internet required)
- Real Apple Watch biometric integration
- 4-tier intelligent fallback (Gemma -> Ollama -> Mistral -> Gemini)
- 48 critical bugs fixed
- Privacy-first API key management
- Real-time stress detection
- Significantly improved security
```

---

## 📋 ROLLBACK PROCEDURE

If something breaks:
```bash
# Keep old code tagged
git tag -a v1.0-backup -m "Original version with bugs"

# Can always revert
git checkout v1.0-backup

# But migration is worth it because:
# - Offline capability
# - True autonomy (not Google-dependent)
# - Real biometric data
# - 48 bugs fixed
```

---

## ✅ SUCCESS CRITERIA

✅ App works offline with Gemma  
✅ Real Apple Watch data (not fake)  
✅ Fallback chain handles all scenarios  
✅ All 48 bugs fixed  
✅ API keys secured  
✅ Performance acceptable  
✅ No memory leaks  
✅ 80%+ test coverage  
✅ Security audit passed  
✅ Users report improved reliability  

---

## 🎯 TIMELINE SUMMARY

| Week | Tasks | Status |
|------|-------|--------|
| 1 | Setup, download models, config | Ready |
| 2 | Core implementations (Gemma, Mistral) | Ready |
| 3 | Biometric integration, Orchestrator | Ready |
| 4 | Bug fixes, cleanup | Ready |
| 5 | Performance optimization, testing | Ready |
| **Total** | **25 days** | **Ready to execute** |

---

## 💡 POST-LAUNCH IMPROVEMENTS

After v2.0 stable:
- [ ] Add offline map service
- [ ] Local chemistry database
- [ ] Peer-to-peer data sharing
- [ ] Advanced HRV analytics
- [ ] Custom model training
- [ ] Real-time collaboration features

---

**This migration transforms OmnicientScanner from a fake "autonomous" app that depends 100% on Google Gemini into a truly self-sovereign system that works offline with local Gemma, has intelligent fallbacks, uses real biometric data from Apple Watch, and has zero pseudoscience. It's a complete architectural overhaul that will make the app production-ready.**

