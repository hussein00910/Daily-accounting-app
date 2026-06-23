# Whatshub

A standalone, native Android messaging app (own brand, WhatsApp-style UX). Backend: Supabase (Postgres, Auth, Realtime, Storage).

## Temporary location notice

This project lives inside `daily-accounting-app/whatshub/` only because this Claude Code session's GitHub access was scoped to that repository and not to the real destination repo, `hussein00910/Whatshub`. It is a fully independent Gradle project (own `settings.gradle.kts`, wrapper, root `build.gradle.kts`) — nothing here references the accounting app. To finish the move:

```bash
# from a session/machine with access to hussein00910/Whatshub
cp -r daily-accounting-app/whatshub/* Whatshub/
cd Whatshub && git add -A && git commit -m "Import Whatshub Android app"
```

Then delete the `whatshub/` folder from `daily-accounting-app`.

## Setup

1. Copy `local.properties.example` to `local.properties` and fill in your Supabase project URL + anon key (from the Supabase dashboard, Project Settings → API).
2. Open in Android Studio or run `./gradlew assembleDebug` from this directory.

Verified working toolchain: AGP 8.13.2, Kotlin 2.4.0, Gradle 8.14.5, compileSdk/targetSdk 36, JDK 17. `./gradlew assembleDebug` and `./gradlew lintDebug` both pass clean.

## Required external setup (not provisioned by this project)

- An SMS/OTP provider (Twilio, MessageBird, Vonage, etc.) configured in the Supabase Auth dashboard — phone auth needs it to actually send OTP codes.
- (Phase 5 / later) A separate Firebase project for FCM push notifications.

## Status

See the phased plan this app was built from for current progress: scaffold + phone/OTP auth (Phase 1) → 1:1 realtime chat (Phase 2) → media + voice (Phase 3) → groups (Phase 4) → push + polish (Phase 5).
