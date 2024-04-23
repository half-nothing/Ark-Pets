package cn.harryh.arkpets.i18n;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;


public final class Locales {
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

    public static SupportedLocale getLocaleByName(String name) {
        if (name == null) return DEFAULT;
        return switch (name.toLowerCase(Locale.ROOT)) {
            case "en" -> EN;
            case "zh_tw" -> ZH_TW;
            case "zh_cn" -> ZH_CN;
            default -> DEFAULT;
        };
    }

    public static List<String> getSupportedLanguages() {
        return List.of("en", "zh_tw", "zh_cn");
    }

    public static String getNameByLocale(SupportedLocale locale) {
        if (locale == EN) return "en";
        if (locale == ZH_TW) return "zh_tw";
        if (locale == ZH_CN) return "zh_cn";
        if (locale == DEFAULT) return "def";
        throw new IllegalArgumentException("Unknown locale: " + locale);
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
