# Room entities and backup models are serialized/deserialized by Gson via
# reflection (see BackupManager), so keep their fields from being renamed.
-keep class com.mohaseb.soft.data.entity.** { *; }
-keep class com.mohaseb.soft.utils.BackupData { *; }
-dontwarn org.jetbrains.annotations.**
