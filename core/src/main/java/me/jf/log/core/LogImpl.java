package me.jf.log.core;

import me.jf.log.core.printer.FilePrinter;
import me.jf.log.core.printer.IPrinter;
import me.jf.log.core.printer.LogCatPrinter;

/**
 * Created by jf.zhang on 2018/3/27.
 */

class LogImpl implements ILog {

    LogLevel mLevel = LogLevel.v; // default level

    IPrinter mPrinter = new LogCatPrinter();

    Config mConfig;

    public LogImpl(Config config) {
        mConfig  = config;
        mLevel   = mConfig.logLevel;
        mPrinter = mConfig.printType.equals(Config.PrintType.File)
                ? new FilePrinter(mConfig.logDir)
                : new LogCatPrinter();
    }

    @Override
    public void v(Object tag, String msg) {
        log(LogLevel.v, tag, msg, null);
    }

    @Override
    public void d(Object tag, String msg) {
        log(LogLevel.d, tag, msg, null);
    }

    @Override
    public void i(Object tag, String msg) {
        log(LogLevel.i, tag, msg, null);
    }

    @Override
    public void w(Object tag, String msg) {
        log(LogLevel.w, tag, msg, null);
    }

    @Override
    public void e(Object tag, String msg) {
        log(LogLevel.e, tag, msg, null);
    }

    @Override
    public void e(Object tag, String msg, Throwable t) {
        log(LogLevel.e, tag, msg, t);
    }

    final void log(LogLevel invokeLevel, Object tag, String msg, Throwable throwable) {
        if (invokeLevel.ordinal() >= mLevel.ordinal()) {
            String realTag;
            if (tag instanceof String) {
                realTag = (String) tag;
            } else {
                realTag = tag.getClass().getSimpleName();
            }

            mPrinter.print(invokeLevel, realTag, msg, throwable);
        }
    }
}
