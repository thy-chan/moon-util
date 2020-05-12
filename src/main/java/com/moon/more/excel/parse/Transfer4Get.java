package com.moon.more.excel.parse;

import com.moon.core.lang.DoubleUtil;
import com.moon.core.time.TimeUtil;
import com.moon.core.util.CalendarUtil;
import com.moon.core.util.DateUtil;
import org.apache.poi.ss.usermodel.Cell;
import sun.util.BuddhistCalendar;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

import static java.util.Collections.unmodifiableSet;

/**
 * @author benshaoye
 */
public enum Transfer4Get {
    STRING(String.class,
        StringBuilder.class,
        StringBuffer.class,
        StringJoiner.class,
        com.moon.core.lang.StringJoiner.class,
        Character.class,
        char.class) {
        @Override
        public boolean test(Object data) { return data instanceof CharSequence; }

        @Override
        public void setCellValue(Object data, Cell cell) {
            if (data == null) {
                cell.setBlank();
            } else {
                cell.setCellValue(data.toString());
            }
        }
    },
    NUMBER(Integer.class,
        int.class,
        Double.class,
        double.class,
        Float.class,
        float.class,
        Short.class,
        short.class,
        byte.class,
        Byte.class,
        BigDecimal.class,
        BigInteger.class,
        AtomicInteger.class,
        AtomicLong.class,
        DoubleAdder.class,
        LongAdder.class) {
        @Override
        public boolean test(Object data) { return data instanceof Number; }

        @Override
        public void setCellValue(Object data, Cell cell) {
            if (data instanceof Double) {
                cell.setCellValue((Double) data);
            } else if (data instanceof Number) {
                cell.setCellValue(((Number) data).doubleValue());
            } else {
                cell.setCellValue(DoubleUtil.toDoubleValue(data));
            }
        }
    },
    DATE(Date.class, Time.class, Timestamp.class, java.sql.Date.class) {
        @Override
        public boolean test(Object data) { return data instanceof Date; }

        @Override
        public void setCellValue(Object data, Cell cell) {
            if (data instanceof Date) {
                cell.setCellValue((Date) data);
            } else {
                cell.setCellValue(DateUtil.toDate(data));
            }
        }
    },
    CALENDAR(Calendar.class, GregorianCalendar.class, BuddhistCalendar.class) {
        @Override
        public boolean test(Object data) { return data instanceof Calendar; }

        @Override
        public void setCellValue(Object data, Cell cell) {
            if (data instanceof Calendar) {
                cell.setCellValue((Calendar) data);
            } else {
                cell.setCellValue(CalendarUtil.toCalendar(data));
            }
        }
    },
    TemporalAccessor(LocalDate.class, LocalDateTime.class, LocalTime.class) {

        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        @Override
        public boolean test(Class type) { return TemporalAccessor.class.isAssignableFrom(type); }

        @Override
        public boolean test(Object data) { return data instanceof TemporalAccessor; }

        @Override
        public void setCellValue(Object data, Cell cell) {
            if (data instanceof LocalDateTime) {
                cell.setCellValue((LocalDateTime) data);
            } else if (data instanceof LocalDate) {
                cell.setCellValue((LocalDate) data);
            } else if (data instanceof LocalTime) {
                cell.setCellValue(((LocalTime) data).format(formatter));
            } else {
                cell.setCellValue(TimeUtil.toDateTime(data));
            }
        }
    },
    BOOLEAN(Boolean.class, boolean.class) {
        @Override
        public void setCellValue(Object data, Cell cell) { cell.setCellValue((Boolean) data); }
    },
    NULL(new Class[]{null}) {
        @Override
        public boolean test(Object data) { return data == null; }

        @Override
        public void setCellValue(Object data, Cell cell) { cell.setBlank(); }
    },
    ;

    private static class Transfers {

        final static Set<Transfer4Get> VALUES = toSet(Transfer4Get.values());
    }

    private static class Cached {

        final static Map<Class, Transfer4Get> SUPPORTS = new HashMap<>();
    }

    private final Set<Class<?>> defaults;

    Transfer4Get(Class<?>... supports) {
        for (Class<?> support : supports) {
            Cached.SUPPORTS.put(support, this);
        }
        defaults = toSet(supports);
    }

    private static <T> Set toSet(T... ts) {
        return unmodifiableSet(new HashSet<>(Arrays.asList(ts)));
    }

    public static Transfer4Get find(Class type) {
        Transfer4Get transfer = Cached.SUPPORTS.get(type);
        if (transfer != null) {
            return transfer;
        }
        for (Transfer4Get gets : Transfers.VALUES) {
            if (gets.test(type)) {
                return gets;
            }
        }
        return STRING;
    }

    public boolean test(Class type) { return defaults.contains(type); }

    public boolean test(Object data) { return data instanceof Boolean; }

    public abstract void setCellValue(Object data, Cell cell);
}