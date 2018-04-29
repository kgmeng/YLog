package me.jf.log.core.printer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import me.jf.log.core.LogLevel;
import me.jf.log.core.format.Formater;

/**
 * Created by jf.zhang on 2018/4/6.
 */

class Writer {
    final static int MAX_COUNT = 4 * 1024;

    final File mFile;
    BufferedWriter bw;

    public Writer(File file) {
        this.mFile = file;
    }

    public void write(LogLevel invokeLevel, String tag, String msg, Throwable throwable) {

        try {
            if (!mFile.exists()) {
                File parent = mFile.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                mFile.createNewFile();
            }

            String log = new Formater().format(invokeLevel, tag, msg, throwable).toString();

            bw = new BufferedWriter(new FileWriter(mFile, true));

            int maxLog = log.length();
            int start  = 0;
            int end    = Math.min(maxLog, MAX_COUNT);
            while (start < maxLog) {
                bw.write(log.substring(start, end));
                bw.newLine();
                bw.flush();
                start  = end;
                end    = Math.min(start + MAX_COUNT, maxLog);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    bw = null;
                }
            }
        }
    }


}
