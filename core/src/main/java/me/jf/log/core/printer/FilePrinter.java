package me.jf.log.core.printer;

import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import me.jf.log.core.Config;
import me.jf.log.core.LogLevel;
import me.jf.log.core.format.Formater;

/**
 * Created by jf.zhang on 2018/3/27.
 */

public class FilePrinter implements IPrinter {

    private static final String BAK_EXT = ".bak";

    private static final int MAX_COUNT = 1;

    /** Back file num limit, when this is exceeded, will delete older logs. */
    private static final int BAK_FILE_NUM_LIMIT = 2;

    private Formater mFormater;
    private Worker mWorker;
    private Config mConfig;

    public FilePrinter(final Config config) {
        mFormater   = new Formater();
        mWorker     = new Worker(config);
        mConfig     = config;
    }

    private boolean availableMemInSDcard() {
        if (!Environment.getExternalStorageState().equalsIgnoreCase(
                Environment.MEDIA_MOUNTED)) {
            return false;
        }

        File sdcard = Environment.getExternalStorageDirectory();
        StatFs statFs = new StatFs(sdcard.getPath());
        long blockSize = statFs.getBlockSize();
        long avaliableBlocks = statFs.getAvailableBlocks();
        long total = avaliableBlocks * blockSize / 1024;
        return total >= 10;
    }

    @Override
    public void print(LogLevel invokeLevel, String tag, String msg, Throwable throwable) {
        if (!availableMemInSDcard()) {
            return;
        }

        mWorker.print(invokeLevel, tag, msg, throwable);
    }

    class FileNameGenerator {
        ThreadLocal<SimpleDateFormat> formater = new ThreadLocal<SimpleDateFormat>() {
            @Override
            protected SimpleDateFormat initialValue() {
                return new SimpleDateFormat("yyyy-MM-dd-HH", Locale.US);
            }
        };

        public String create() {
            return mConfig.logFilePrefix + formater.get().format(new Date(System.currentTimeMillis()));
        }
    }

    class Worker {

        ExecutorService mExecutorService = new ThreadPoolExecutor(
                MAX_COUNT,
                MAX_COUNT,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new ThreadFactory() {
            @Override
            public Thread newThread(@NonNull Runnable r) {
                Thread t = new Thread(r, "YLogPrint" + hashCode());
                if (t.isDaemon()) {
                    t.setDaemon(false);
                }
                if (t.getPriority() != Thread.NORM_PRIORITY) {
                    t.setPriority(Thread.NORM_PRIORITY);
                }
                return t;
            }
        });

        private FileNameGenerator mFileNameGenerator;
        private CleanTask mCleanTask;
        private String mFolderName = "YLogDefault"; // default folder name
        private String mFileName   = "";
        private Config mConfig;

        public Worker(final Config config) {
            mConfig            = config;
            mFileNameGenerator = new FileNameGenerator();
            mFolderName        = config.logDir;
            mCleanTask         = new CleanTask(getLogFilePath());
        }

        public String getLogFilePath() {
            return new StringBuilder().append(Environment.getExternalStorageDirectory().getAbsolutePath())
                    .append(File.separator)
                    .append(mFolderName)
                    .append(File.separator)
                    .toString();
        }

        private int[] getTime(long timeMillis) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timeMillis);
            int time[] = new int[5];
            time[0] = calendar.get(Calendar.YEAR);
            time[1] = calendar.get(Calendar.MONTH) + 1;
            time[2] = calendar.get(Calendar.DAY_OF_MONTH);
            time[3] = calendar.get(Calendar.HOUR_OF_DAY);
            time[4] = calendar.get(Calendar.MINUTE);
            return time;
        }

        public void updateFileName(File file) {
            int[] currentTime = getTime(System.currentTimeMillis());
            int[] fileModTime = getTime(file.lastModified());

            boolean needChange = false;
            for (int i = 0; i < 5; i++) {
                needChange |= (currentTime[i] == fileModTime[i]);
            }
            needChange |= currentTime[4] > 0? ((currentTime[4] / 5) % 10 == 0) : true;

        }


        public void print(final LogLevel invokeLevel, final String tag, final String msg, final Throwable throwable) {
            mExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    if (TextUtils.isEmpty(mFileName)) {
                        mFileName = mFileNameGenerator.create();
                    }

                    File file = new File(getLogFilePath() + mFileName);
                    if (!file.exists()) {
                        File parent = file.getParentFile();
                        if (!parent.exists()) {
                            parent.mkdirs();
                        }
                    } else {


                        long fileSize = (file.length() >>> 20);// convert to M bytes
                        if (fileSize >= mConfig.maxMegabyte) {

                            File fileNameTo = new File(getLogFilePath() + file.getName() + BAK_EXT);
                            file.renameTo(fileNameTo);

                            mCleanTask.clean();
                        }
                    }

                    new Writer(file).write(invokeLevel, tag, msg, throwable);
                }
            });
        }


    }

    /**
     * 删除旧文件的异步操作
     */
    class CleanTask implements Runnable {

        private static final long DAY_DELAY = 10L * 24 * 60 * 60 * 1000;

        long mLastCleanTime = 0;

        final String logFilePath;

        public CleanTask(String logFilePath) {
            this.logFilePath = logFilePath;
        }

        public void clean() {
            synchronized (this) {
                final long currentTime = System.currentTimeMillis();
                if (currentTime - mLastCleanTime > DAY_DELAY) {
                    mLastCleanTime = currentTime;
                    new Thread(this, "YLogCleaner" + hashCode()).start();
                }
            }
        }

        @Override
        public void run() {
            deleteOldFiles();
            limitVolume();
        }

        private void deleteOldFiles() {
            File dirFile = new File(logFilePath);
            if (!dirFile.exists()) {
                return;
            }

            long now = System.currentTimeMillis();
            File files[] = dirFile.listFiles();
            if (files == null) {
                return;
            }
            for (File file : files) {
                if (file.getName().endsWith(BAK_EXT)) {
                    long lastModifiedTime = file.lastModified();
                    if (now - lastModifiedTime > DAY_DELAY) {
                        file.delete();
                    }
                }
            }
        }

        private void limitVolume() {
            File dirFile = new File(logFilePath);
            if (!dirFile.exists()) {
                return;
            }

            final File files[] = dirFile.listFiles();
            if (files == null || files.length <= Math.max(0, BAK_FILE_NUM_LIMIT)) {
                return;
            }

            AtomicInteger numOfDeletable = new AtomicInteger(0);
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (file.getName().endsWith(BAK_EXT)) {
                    numOfDeletable.incrementAndGet();
                }
            }

            if (numOfDeletable.get() <= 0) {
                // really weird, the naming rule have been changed!
                // this function won't work anymore.
                return;
            }

            // the logs.txt and uncaught_exception.txt may be missing,
            // so just allocate same size as the old.
            File[] deletables = new File[numOfDeletable.get()];
            int i = 0;
            for (File e : files) {
                if (i >= numOfDeletable.get()) {
                    // unexpected case.
                    break;
                }
                if (e.getName().endsWith(BAK_EXT)) {
                    deletables[i++] = e;
                }
            }

            deleteIfOutOfBound(deletables);
        }

        private void deleteIfOutOfBound(File[] files) {
            if (files.length <= BAK_FILE_NUM_LIMIT) {
                return;
            }

            // sort files by create time(time is on the file name) DESC.
            Comparator<? super File> comparator = new Comparator<File>() {

                @Override
                public int compare(File lhs, File rhs) {
                    return rhs.getName().compareTo(lhs.getName());
                }

            };

            Arrays.sort(files, comparator);

            final int filesNum = files.length;

            for (int i = 0; i < BAK_FILE_NUM_LIMIT; ++i) {
                Log.d("FilePrinter", "keep file " + files[i]);
            }

            // delete files from index to size.
            for (int i = BAK_FILE_NUM_LIMIT; i < filesNum; ++i) {
                File file = files[i];
                if (!file.delete()) {
                    // NOTE here we cannot call YLog, we are to be depended by YLog.
                    Log.e("FilePrinter", "failed to delete file " + file);
                }
                else {
                    Log.d("FilePrinter", "delete file " + file);
                }
            }
        }
    }
}
