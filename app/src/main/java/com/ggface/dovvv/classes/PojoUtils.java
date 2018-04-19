package com.ggface.dovvv.classes;

import android.support.annotation.Nullable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public class PojoUtils {

    private PojoUtils() {
    }

    public static <T> T checkNotNull(T reference) {
        if (reference == null) {
            throw new NullPointerException();
        }
        return reference;
    }

    public static boolean equal(@Nullable Object a, @Nullable Object b) {
        return a == b || (a != null && a.equals(b));
    }

    public static int hashCode(@Nullable Object... objects) {
        return Arrays.hashCode(objects);
    }

    public static boolean isNull(Object value) {
        if (value instanceof String) {
            return ((String) value).isEmpty();
        } else if (value instanceof Collection) {
            return ((Collection) value).isEmpty();
        } else if (value instanceof Map) {
            return ((Map) value).isEmpty();
        }
        return value == null;
    }

    public static boolean isNotNull(Object value) {
        return !isNull(value);
    }

    @SuppressWarnings("NumberEquality")
    public static boolean equalsInt(BigDecimal v1, BigDecimal v2) {
        if (v1 == v2) {
            return true;
        } else if (isNull(v1) || isNull(v2)) {
            return false;
        }
        return v1.intValue() == v2.intValue();
    }

    public static String join(String delimiter, Object... values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (isNotNull(values[i])) {
                if (sb.length() > 0) {
                    sb.append(delimiter);
                }
                sb.append(values[i]);
            }
        }
        return sb.toString();
    }

    /**
     * Конвертирует значение {@code byte} в {@code boolean}. Если значение не равно 0, то вернет
     * true, иначе - false
     *
     * @param value значение для конвертации
     * @return true или false
     */
    public static boolean toBoolean(byte value) {
        return value != 0;
    }

    /**
     * Конвертирует значение {@code boolean} в {@code byte}. Если true, то вернет 1, иначе - 0
     *
     * @param value значение для конвертации
     * @return 1 или 0
     */
    public static byte toByte(boolean value) {
        return (byte) (value ? 1 : 0);
    }
}