# DPCH21TV

Scaffold awal aplikasi Android untuk branding **DPCH21TV**.

## Detail yang sudah diset
- Nama APK/aplikasi: **DPCH21TV**
- Package name: `com.dpch21tv.app`
- Minimum Android: API 24 (Android 7.0)

## Sumber daftar channel (mudah update)
Daftar channel dibaca dari file playlist:
- `app/src/main/assets/channels.m3u`

Cara update channel:
1. Edit file `channels.m3u`
2. Ubah/tambah blok `#EXTINF` + URL `.m3u8`
3. Build ulang APK

Contoh format:
```m3u
#EXTM3U
#EXTINF:-1 tvg-name="RCTI",RCTI
https://example.com/live/rcti/index.m3u8
```

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
