# 本应用为纯本地 Compose + Room 项目，未启用混淆。
# 若后续开启 isMinifyEnabled = true，请保留以下规则。

-keep class com.family.order.data.local.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-dontwarn androidx.room.paging.**
