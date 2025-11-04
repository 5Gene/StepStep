package com.google.android.libraries.wear.companion.setup

/**
 * Step Type - Setup步骤类型枚举
 */
enum class StepType(val isPointOfNoReturn: Boolean) {
    CHINA_PERSISTENT_SERVICE(false),
    TERMS_OF_SERVICE(false),
    SECONDARY_TERMS_OF_SERVICE(false),
    LOCATION_PERMISSION(false),
    BLUETOOTH_PERMISSION(false),
    DISCOVERY(false),
    PAIRING(true),
    COMPANION_DEVICE_MANAGER_ASSOCIATION(true),
    ESIM_SETUP(false),
    CDM_DISCOVERY(true),
    EMULATOR_CONNECTION(true),
    WATCH_CONNECTION(true),
    PHONE_SWITCHING(false),
    RESTORE(false),
    RESUME_SETUP_INITIALIZATION(true),
    GMS_CHECK_IN(true),
    FACTORY_RESET_PROTECTION(true),
    WIFI_CONNECTION(false),
    GOOGLE_ACCOUNTS(false),
    PARENTAL_CONTROLS(false),
    WATCH_ORIENTATION(false),
    APP_INSTALL(false),
    GOOGLE_PAY(false),
    GOOGLE_ASSISTANT(false),
    CDM_PERMISSION_SYNC_CONSENT(true),
    LOCK_SCREEN(false),
    DISABLE_BATTERY_OPTIMIZATION(false),
    NOTIFICATION_ACCESS(false),
    CALENDAR_PERMISSION(false),
    CONTACTS_PERMISSION(false),
    PHONE_PERMISSION(false),
    SMS_PERMISSION(false),
    DAY_ZERO_OTA_UPDATE(true),
    DONE(true);
}

