package me.jf.log.core.printer;

import me.jf.log.core.LogLevel;

/**
 * Created by jf.zhang on 2018/3/27.
 */

public interface IPrinter {

    void print(LogLevel invokeLevel, String tag, String msg, Throwable throwable);
}
