package me.jf.log.core;

import junit.framework.*;
/**
 * Created by jf.zhang on 2018/3/28.
 */

public class YLog {
    static volatile LogImpl mLog;

    public static void init(Config config) {
        if (mLog == null) {
            mLog = new LogImpl(config);
        }
    }

    public static void d(Object tag, String msg) {
        Assert.assertNotNull("YLog Not initialized!", mLog);

        mLog.d(tag, msg);
    }

    public static void v(Object tag, String msg) {
        Assert.assertNotNull("YLog Not initialized!", mLog);

        mLog.v(tag, msg);
    }

    public static void i(Object tag, String msg) {
        Assert.assertNotNull("YLog Not initialized!", mLog);

        mLog.i(tag, msg);
    }

    public static void w(Object tag, String msg) {
        Assert.assertNotNull("YLog Not initialized!", mLog);

        mLog.w(tag, msg);
    }

    public static void e(Object tag, String msg) {
        Assert.assertNotNull("YLog Not initialized!", mLog);

        mLog.e(tag, msg);
    }

    public static void e(Object tag, String msg, Throwable t) {
        Assert.assertNotNull("YLog Not initialized!", mLog);

        mLog.e(tag, msg, t);
    }
}
