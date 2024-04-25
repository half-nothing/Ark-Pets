package cn.harryh.arkpets.i18n;


import cn.harryh.arkpets.utils.Logger;

import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * I18n implementation class
 * @since ArkPets 3.1
 */
public final class I18n {
    static private Locales.SupportedLocale supportedLocale = Locales.DEFAULT;

    private I18n() {
    }

    /**
     * set i18n use language, default see {@link Locales#DEFAULT}
     * @param language country code of i18n, detail see {@link Locales#getLocaleByName}
     */
    public static void setLanguage(String language) {
        try {
            supportedLocale = Locales.getLocaleByName(language);
        } catch (IllegalStateException e) {
            supportedLocale = Locales.DEFAULT;
        }
    }

    public static ResourceBundle getResourceBundle() {
        return supportedLocale.getResourceBundle();
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
}
