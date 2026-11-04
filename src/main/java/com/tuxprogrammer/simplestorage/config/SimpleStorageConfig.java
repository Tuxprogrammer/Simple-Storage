package com.tuxprogrammer.simplestorage.config;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import com.gtnewhorizon.gtnhlib.config.Config;
import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhlib.config.ConfigurationManager;
import com.tuxprogrammer.simplestorage.SimpleStorage;

/**
 * Registers the mod's GTNHLib config classes, which together make up {@code config/simplestoragenh.cfg} (decision
 * D16). Registering is all the automatic config screen needs, so the mod has no GUI factory.
 *
 * <p>
 * Source: GTNHLib 0.11.54,
 * https://github.com/GTNewHorizons/GTNHLib/blob/0.11.54/src/main/java/com/gtnewhorizon/gtnhlib/config/ConfigurationManager.java#L61-L99
 * (register before or during preInit; the file is read at registration).
 *
 * <p>
 * Side: both.
 */
public final class SimpleStorageConfig {

    /** Every config class, in the order they are registered. Tests and the dev self-check walk this list. */
    public static final List<Class<?>> CLASSES = Collections.unmodifiableList(
        Arrays.<Class<?>>asList(
            ModulesConfig.class,
            NetworkConfig.class,
            WirelessConfig.class,
            BehaviorConfig.class,
            EuSinkConfig.class));

    /** What FML's config screen appends to an entry's lang key to find its tooltip. */
    public static final String TOOLTIP_SUFFIX = ".tooltip";

    private SimpleStorageConfig() {}

    /**
     * Loads every config class from {@code config/simplestoragenh.cfg}, writing missing keys with their defaults. Call
     * once, in {@code preInit}.
     *
     * @throws ConfigException when a config class is malformed, which is a programming error that stops the game
     */
    public static void register() {
        for (Class<?> configClass : CLASSES) {
            ConfigurationManager.registerConfig(configClass);
        }
    }

    /**
     * Returns the options of a config class: its public static non-final fields that GTNHLib reads.
     *
     * @param configClass one of {@link #CLASSES}
     * @return the option fields, sorted by name
     */
    public static List<Field> options(Class<?> configClass) {
        List<Field> options = new ArrayList<>();
        for (Field field : configClass.getDeclaredFields()) {
            int modifiers = field.getModifiers();
            if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers)
                && !Modifier.isFinal(modifiers)
                && !field.isSynthetic()
                && !field.isAnnotationPresent(Config.Ignore.class)) {
                options.add(field);
            }
        }
        options.sort(Comparator.comparing(Field::getName));
        return options;
    }

    /**
     * Returns the lang keys GTNHLib's config screen looks up: per category its label and tooltip, per option its label
     * and tooltip, built as {@code simplestoragenh.<category>.<field>}, lowercased. The {@code modules.integration}
     * subcategory is listed explicitly.
     *
     * @return the keys, sorted
     */
    public static Set<String> langKeys() {
        Set<String> keys = new TreeSet<>();
        for (Class<?> configClass : CLASSES) {
            Config config = configClass.getAnnotation(Config.class);
            addKeys(
                keys,
                SimpleStorage.MODID + "."
                    + config.category()
                        .toLowerCase(Locale.ROOT),
                options(configClass));
        }
        addKeys(
            keys,
            SimpleStorage.MODID + ".modules.integration",
            Arrays.asList(ModulesConfig.Integration.class.getDeclaredFields()));
        return keys;
    }

    private static void addKeys(Set<String> keys, String category, List<Field> fields) {
        keys.add(category);
        keys.add(category + TOOLTIP_SUFFIX);
        for (Field option : fields) {
            if (option.isSynthetic()) {
                continue;
            }
            String key = category + "."
                + option.getName()
                    .toLowerCase(Locale.ROOT);
            keys.add(key);
            keys.add(key + TOOLTIP_SUFFIX);
        }
    }
}
