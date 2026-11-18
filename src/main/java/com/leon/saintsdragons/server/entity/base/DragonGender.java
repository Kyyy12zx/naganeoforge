package com.leon.saintsdragons.server.entity.base;

/**
 * Gender value persisted by dragon entities.
 *
 * Ported from Saint's Dragons 1.20.1 by LilRicefield:
 * https://github.com/LilRicefield/saints-dragons/blob/1.20.1/common/src/main/java/com/leon/saintsdragons/server/entity/base/DragonGender.java
 * The source code is MIT-licensed; see LICENSE.md for the retained terms.
 */
public enum DragonGender {
    MALE((byte) 0),
    FEMALE((byte) 1);

    private final byte id;

    DragonGender(byte id) {
        this.id = id;
    }

    public byte getId() {
        return id;
    }

    /**
     * Unknown IDs intentionally fall back to MALE to preserve source behavior.
     */
    public static DragonGender fromId(byte id) {
        for (DragonGender gender : values()) {
            if (gender.id == id) {
                return gender;
            }
        }
        return MALE;
    }
}
