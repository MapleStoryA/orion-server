package tools;

import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Calendar;

@Slf4j
public class DateHelper {

    private static final SimpleDateFormat date_hour_formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat normal_date_date_formatter = new SimpleDateFormat("yyyy-MM-dd");


    public static String getCurrentReadableDate() {
        return normal_date_date_formatter.format(Calendar.getInstance().getTime());
    }

    public static String getCurrentReadableTime() {
        return date_hour_formatter.format(Calendar.getInstance().getTime());
    }

}
