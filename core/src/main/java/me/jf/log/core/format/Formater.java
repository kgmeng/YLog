package me.jf.log.core.format;

import android.os.Looper;
import android.os.Process;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import me.jf.log.core.LogLevel;

/**
 * Created by jf.zhang on 2018/3/28.
 */

public class Formater implements IFormater {

    private final static String TIME_FORMAT = "HH:mm:ss.SSS";

    private SimpleDateFormat mDateFormat;

    private int getCallerLineNumber() {
        return Thread.currentThread().getStackTrace()[4].getLineNumber();
    }

    private String getCallerFilename() {
        return Thread.currentThread().getStackTrace()[4].getFileName();
    }

    private String getCallerMethodName() {
        return Thread.currentThread().getStackTrace()[4].getMethodName();
    }

    private String getCallerTime() {
        if (mDateFormat == null) {
            mDateFormat = new SimpleDateFormat(TIME_FORMAT, Locale.SIMPLIFIED_CHINESE);
        }
        return mDateFormat.format(new Date(System.currentTimeMillis()));
    }

    private String getThrowableMsg(Throwable throwable) {
        if (throwable == null) {
            return "\n";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append(" Exception occurs at ");
        sb.append(Log.getStackTraceString(throwable));
        sb.append('\n');
        return sb.toString();
    }

    @Override
    public CharSequence format(LogLevel level, String tag, String msg, Throwable throwable) {
        Map<LogLevel, String> levelTag = new HashMap<LogLevel, String>(){{
            put(LogLevel.v, " V/: ");
            put(LogLevel.d, " D/: ");
            put(LogLevel.i, " I/: ");
            put(LogLevel.w, " W/: ");
            put(LogLevel.e, " E/: ");
        }};
        StringBuilder sb = new StringBuilder();
        sb.append(getCallerTime());

        //pid
        sb.append("(P:");
        sb.append(Process.myPid());
        sb.append(")");
        //thread
        sb.append("(T:");
        sb.append(getCurrentThreadName());
        sb.append(")");

        sb.append(levelTag.get(level));

        sb.append(tag);
        //add tags
        sb.append(msg);

        sb.append(" at (");
        sb.append(getCallerFilename());
        sb.append(":");
        sb.append(getCallerLineNumber());
        sb.append(")");

        //throwable
        sb.append(getThrowableMsg(throwable));

        return sb.toString();
    }

    private String getCurrentThreadName(){
        Thread mainThread = null;
        Looper looper = Looper.getMainLooper();
        if (looper != null) {
            mainThread = looper.getThread();
        }

        Thread current = Thread.currentThread();
        if (mainThread == current) {
            return "main";
        } else {
            if (current.getName() == null || current.getName().length() == 0) {
                return String.valueOf(current.getId());
            } else {
                return current.getName();
            }
        }
    }
}
