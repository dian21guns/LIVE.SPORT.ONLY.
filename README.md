# DPCH21TV

Scaffold awal aplikasi Android untuk branding **DPCH21TV**.

## Detail yang sudah diset
- Nama APK/aplikasi: **DPCH21TV**
- Package name: `com.dpch21tv.app`
- Minimum Android: API 24 (Android 7.0)

## Cara build APK (paling mudah - Android Studio)
1. Install **Android Studio** (versi terbaru).
2. Buka folder project ini: `LIVE.SPORT.ONLY.`
3. Tunggu proses **Gradle Sync** sampai selesai.
4. Pilih menu **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
5. Tunggu selesai, lalu klik notifikasi **locate**.
6. File APK debug ada di:
   - `app/build/outputs/apk/debug/app-debug.apk`

## Cara build APK via terminal (opsional)
> Jalankan ini dari root project.

```bash
./gradlew assembleDebug
```

Output APK debug:
- `app/build/outputs/apk/debug/app-debug.apk`

Untuk APK release (unsigned/signed tergantung config):
```bash
./gradlew assembleRelease
```

Output APK release:
- `app/build/outputs/apk/release/app-release.apk`

## Kalau `./gradlew` belum ada
Karena ini scaffold awal, jika file Gradle Wrapper belum ada, buat dari Android Studio:
- Menu: **Tools > Gradle > Add Gradle Wrapper** (atau sync project yang otomatis membuat wrapper pada beberapa setup)

Atau via terminal jika `gradle` global terinstall:
```bash
gradle wrapper
```

## Install APK ke HP Android
1. Kirim `app-debug.apk` ke HP.
2. Aktifkan izin **Install unknown apps** untuk aplikasi pengelola file/browser.
3. Tap APK, lalu install.

## Next step pengembangan live TV m3u8
1. Tambahkan ExoPlayer/Media3 untuk memutar stream m3u8.
2. Buat daftar channel live TV.
3. Tambah splash screen dan ikon custom DPCH21TV.
