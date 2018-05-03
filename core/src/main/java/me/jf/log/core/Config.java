package me.jf.log.core;

import android.text.TextUtils;

/**
 * Created by jf.zhang on 2018/3/19.
 */

public class Config {

    public enum PrintType {
        File, Logcat
    }

    public final String logDir;
    public final PrintType printType;
    public final int cleanDayInterval;
    public final LogLevel logLevel;
    public final String logFilePrefix;
    public final int maxMegabyte;

    public Config(Builder builder) {
        this.logDir             = builder.getLogDir();
        this.printType          = builder.getPrintType();
        this.logLevel           = builder.getLogLevel();
        this.cleanDayInterval   = builder.getCleanDayInterval();
        this.logFilePrefix      = builder.getLogFilePrefix();
        this.maxMegabyte        = builder.getMaxMegabyte();
    }

    public static class Builder {
        private String logDir;                          //日志sd卡目录名
        private PrintType printType  = PrintType.Logcat; //默认logcat输出
        private int cleanDayInterval = 10;              //默认10天
        private LogLevel logLevel    = LogLevel.d;       //默认日志级别
        private String logFilePrefix = "";              //文件名前缀
        private int maxMegabyte      = 5;               //单个文件最大存储M

        public int getMaxMegabyte() {
            return maxMegabyte;
        }

        public Builder setMaxMegabyte(int maxMegabyte) {
            this.maxMegabyte = maxMegabyte;
            return this;
        }

        public String getLogFilePrefix() {
            return TextUtils.isEmpty(logFilePrefix)? "" : logFilePrefix;
        }

        public Builder setLogFilePrefix(String logFilePrefix) {
            this.logFilePrefix = logFilePrefix;
            return this;
        }

        public LogLevel getLogLevel() {
            return logLevel;
        }

        public Builder setLogLevel(LogLevel logLevel) {
            this.logLevel = logLevel;
            return this;
        }

        public String getLogDir() {
            return logDir;
        }

        public Builder setLogDir(String logDir) {
            this.logDir = logDir;
            return this;
        }

        public PrintType getPrintType() {
            return printType;
        }

        public Builder setPrintType(PrintType printType) {
            this.printType = printType;
            return this;
        }

        public int getCleanDayInterval() {
            return cleanDayInterval;
        }

        public Builder setCleanDayInterval(int cleanDayInterval) {
            this.cleanDayInterval = cleanDayInterval;
            return this;
        }
    }
}
