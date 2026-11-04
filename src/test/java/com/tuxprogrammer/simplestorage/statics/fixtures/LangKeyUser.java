package com.tuxprogrammer.simplestorage.statics.fixtures;

/**
 * Fixture for {@code LangKeysTest}: one key-shaped constant no lang file defines, and identifiers that are not keys.
 */
public class LangKeyUser {

    /** A JVM property name: camelCase, so not a key. */
    public static final String PROPERTY = "simplestoragenh.testHooks";

    /** Returns a key that is missing from every lang file. */
    public String key() {
        return "simplestoragenh.fixture.missing_key";
    }
}
