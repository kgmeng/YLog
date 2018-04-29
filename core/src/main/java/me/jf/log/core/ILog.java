package me.jf.log.core;

/**
 * Created by jf.zhang on 2018/3/27.
 */

interface ILog<T> {

    void v(T tag, String msg);
    void d(T tag, String msg);
    void i(T tag, String msg);
    void w(T tag, String msg);
    void e(T tag, String msg);
    void e(T tag, String msg, Throwable t);
}
