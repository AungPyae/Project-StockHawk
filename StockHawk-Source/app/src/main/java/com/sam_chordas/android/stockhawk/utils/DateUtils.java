package com.sam_chordas.android.stockhawk.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by aung on 3/27/16.
 */
public class DateUtils {

    public static final SimpleDateFormat sdfQueryDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    public static final SimpleDateFormat sdfChartByDate = new SimpleDateFormat("MM/dd", Locale.getDefault());

    public static Date getFirstDateOfCurrentMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
        return cal.getTime();
    }
}
