package me.jf.log.core.format;

import me.jf.log.core.LogLevel;

/**
 * Created by jf.zhang on 2018/3/28.
 */

public interface IFormater {
    CharSequence format(LogLevel level, String tag, String msg, Throwable throwable);
}
