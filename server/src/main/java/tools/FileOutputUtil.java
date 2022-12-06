package tools;

import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Calendar;

@Slf4j
public class FileOutputUtil {

    public static final String Acc_Stuck = "Log_AccountStuck.rtf";
    public static final String Login_Error = "Log_Login_Error.rtf";
    public static final String Zakum_Log = "Log_Zakum.rtf";
    public static final String Horntail_Log = "Log_Horntail.rtf";
    public static final String PinkBean_Log = "Log_Pinkbean.rtf";

    public static final String ScriptEx_Log = "Log_Script_Except.rtf";
    public static final String PacketEx_Log = "Log_Packet_Except.rtf";
    private static final SimpleDateFormat date_hour_formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat normal_date_date_formatter = new SimpleDateFormat("yyyy-MM-dd");

    public static void logPacket(final String file, final String msg) {
        logETCs(file, msg, true);
    }

    public static void logUsers(final String file, final String msg) {
        logETCs(file, msg, false);
    }

    public static void logETCs(final String file, final String msg, final boolean packet) {
        log.info(file + " : " + msg);
    }

    public static void log(final String file, final String msg) {
        log.info(file + " : " + msg);
    }

    public static void outputFileError(final String file, final Throwable t) {
        log.info(file, t);
    }

    public static String CurrentReadable_Date() {
        return normal_date_date_formatter.format(Calendar.getInstance().getTime());
    }

    public static String CurrentReadable_Time() {
        return date_hour_formatter.format(Calendar.getInstance().getTime());
    }

}
