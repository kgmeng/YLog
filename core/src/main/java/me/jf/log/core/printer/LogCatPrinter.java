package me.jf.log.core.printer;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import me.jf.log.core.LogLevel;
import me.jf.log.core.format.Formater;

/**
 * Created by jf.zhang on 2018/3/27.
 */

public class LogCatPrinter implements IPrinter {
    static final int MAX_LENGTH_OF_SINGLE_MESSAGE = 1024 * 4;

    Map<LogLevel, Integer> levelMap = new HashMap<LogLevel, Integer>() {{
        put(LogLevel.v, Log.VERBOSE);
        put(LogLevel.d, Log.DEBUG);
        put(LogLevel.w, Log.WARN);
        put(LogLevel.e, Log.ERROR);
        put(LogLevel.i, Log.INFO);
    }};

    private Formater mFormater;

    public LogCatPrinter() {
        this.mFormater = new Formater();
    }

    @Override
    public void print(LogLevel invokeLevel, String tag, String msg, Throwable throwable) {
        CharSequence outputMsg = mFormater.format(invokeLevel, tag, msg, throwable);

        int msgLength = outputMsg.length();
        int start = 0;
        int end   = Math.min(start + MAX_LENGTH_OF_SINGLE_MESSAGE, msgLength);
        while (start < msgLength) {
            print(invokeLevel, tag, outputMsg.toString().substring(start, end));

            start = end;
            end = Math.min(start + MAX_LENGTH_OF_SINGLE_MESSAGE, msgLength);
        }
    }

    void print(LogLevel invokeLevel, String tag, String msg) {
        android.util.Log.println(levelMap.get(invokeLevel), tag, msg);
    }
}
