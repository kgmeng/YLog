package me.jf.log;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import me.jf.log.core.Config;
import me.jf.log.core.LogLevel;
import me.jf.log.core.YLog;

/**
 * Created by jf.zhang on 2018/4/6.
 */

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Config.Builder builder = new Config.Builder()
                .setLogDir("yylog")
                .setCleanDayInterval(1)
                .setLogLevel(LogLevel.i)
                .setPrintType(Config.PrintType.File)
                .setLogFilePrefix("jflog")
                .setMaxMegabyte(2);
        YLog.init(new Config(builder));

        findViewById(R.id.btn_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = getResources().getString(R.string.test_log);
                for (int i = 0; i < 100; i++) {
                    YLog.i(this, "hello:" + i + ",str=" + str);
                }
            }
        });
    }

}
