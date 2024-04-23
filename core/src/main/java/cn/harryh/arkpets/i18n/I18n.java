package cn.harryh.arkpets.i18n;


import cn.harryh.arkpets.ArkConfig;
import cn.harryh.arkpets.utils.Logger;

import java.util.*;


public final class I18n {

    private I18n() {
    }

    public static Locales.SupportedLocale getCurrentLocale() {
        try {
            return Locales.getLocaleByName(Objects.requireNonNull(ArkConfig.getConfig()).prefer_language);
        } catch (IllegalStateException e) {
            return Locales.DEFAULT;
        }
    }

    public static ResourceBundle getResourceBundle() {
        return getCurrentLocale().getResourceBundle();
    }

    public static String i18n(String key, Object... formatArgs) {
        try {
            return String.format(getResourceBundle().getString(key), formatArgs);
        } catch (MissingResourceException e) {
            Logger.error("I18n", "Cannot find key " + key + " in resource bundle" + e);
        } catch (IllegalFormatException e) {
            Logger.error("I18n", "Illegal format string, key=" + key + ", args=" + Arrays.toString(formatArgs) + e);
        }

        return key + Arrays.toString(formatArgs);
    }

    public static String i18n(String key) {
        try {
            return getResourceBundle().getString(key);
        } catch (MissingResourceException e) {
            Logger.error("I18n", "Cannot find key " + key + " in resource bundle", e);
            return key;
        }
    }

    public static boolean hasKey(String key) {
        return getResourceBundle().containsKey(key);
    }
}
