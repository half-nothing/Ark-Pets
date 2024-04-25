package cn.harryh.arkpets.i18n;


import cn.harryh.arkpets.Const;

import java.util.Locale;
import java.util.ResourceBundle;


public final class Locales {
    /**
     * Jvm default language source
     */
    public static final SupportedLocale DEFAULT = new SupportedLocale(Locale.getDefault(), "lang.default");
    /**
     * English
     */
    public static final SupportedLocale EN = new SupportedLocale(Locale.ROOT);
    /**
     * Traditional Chinese
     */
    public static final SupportedLocale ZH_TW = new SupportedLocale(Locale.TRADITIONAL_CHINESE);
    /**
     * Simplified Chinese
     */
    public static final SupportedLocale ZH_CN = new SupportedLocale(Locale.SIMPLIFIED_CHINESE);

    private Locales() {
    }

    /**
     * Support code: en, zh_tw, zh_cn
     * @param name code name
     * @return see {@link SupportedLocale}
     */
    public static SupportedLocale getLocaleByName(Const.Languages.Language name) {
        if (name == null) return DEFAULT;
        return switch (name) {
            case EN -> EN;
            case ZH_TW -> ZH_TW;
            case ZH_CN -> ZH_CN;
            case DEFAULT -> DEFAULT;
        };
    }

    public static class SupportedLocale {
        private final Locale locale;
        private final String name;
        private final ResourceBundle resourceBundle;

        SupportedLocale(Locale locale) {
            this(locale, null);
        }

        SupportedLocale(Locale locale, String name) {
            this.locale = locale;
            this.name = name;
            resourceBundle = ResourceBundle.getBundle("lang.I18N", locale);
        }

        public Locale getLocale() {
            return locale;
        }

        public ResourceBundle getResourceBundle() {
            return resourceBundle;
        }

        public String getName(ResourceBundle newResourceBundle) {
            if (name == null) return resourceBundle.getString("lang");
            else return newResourceBundle.getString(name);
        }
    }
}
