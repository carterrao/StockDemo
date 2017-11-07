package com.rzc.stockdemo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.rzc.stockdemo.data.TimeSharingData;
import com.rzc.util.CookieUtil;
import com.rzc.widget.TimeSharingChartView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class StockTimeSharingActivity extends Activity implements View.OnClickListener {
    private ProgressDialog mProgressDialog;
    private TimeSharingChartView mTimeSharingChartView;
    private double basePrice;
    private double maxPrice;
    private double minPrice;
    private String code;
    private TextView tvName;
    private TextView tvCode;
    private TextView tvMaxValue;
    private TextView tvMaxPercent;
    private TextView tvMinValue;
    private TextView tvMinPercent;
    private TextView tvBaseValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_time_sharing);
        findViewById(R.id.ivBack).setOnClickListener(this);
        mTimeSharingChartView = findViewById(R.id.mTimeSharingChartView);
        basePrice = getIntent().getDoubleExtra("basePrice", 0);
        maxPrice = getIntent().getDoubleExtra("maxPrice", 0);
        minPrice = getIntent().getDoubleExtra("minPrice", 0);
        String name = getIntent().getStringExtra("name");
        code = getIntent().getStringExtra("code");
        tvName = findViewById(R.id.tvName);
        tvName.setText(name);
        tvCode = findViewById(R.id.tvCode);
        tvCode.setText(code);

        tvMaxValue = findViewById(R.id.tvMaxValue);
        tvMaxPercent = findViewById(R.id.tvMaxPercent);
        tvMinValue = findViewById(R.id.tvMinValue);
        tvMinPercent = findViewById(R.id.tvMinPercent);
        tvBaseValue = findViewById(R.id.tvBaseValue);

        mProgressDialog = ProgressDialog.show(this, "", "正在加载数据...");
        downloadData();
    }

    private void downloadData() {
        String cookie = getPrefCookie();
        if (cookie == null) {
            queryAndStoreCookie(code);
        } else {
            TimeSharingData.download(code, cookie, new TimeSharingData.OnDownloadedListener() {
                @Override
                public void onDownloaded(List<TimeSharingData> list) {
                    mProgressDialog.dismiss();
                    mTimeSharingChartView.setData((float) basePrice, (float) maxPrice, (float) minPrice, list);
                    tvMaxValue.setText(String.format("%.2f", mTimeSharingChartView.getMaxValue()));
                    tvMaxPercent.setText(String.format("%.2f%%", (mTimeSharingChartView.getMaxValue() - basePrice) / basePrice * 100));
                    tvMinValue.setText(String.format("%.2f", mTimeSharingChartView.getMinValue()));
                    tvMinPercent.setText(String.format("%.2f%%", (mTimeSharingChartView.getMinValue() - basePrice) / basePrice * 100));
                    tvBaseValue.setText(String.format("%.2f", basePrice));
                }

                @Override
                public void onFailed(boolean cookieError) {
                    if (cookieError) {
                        queryAndStoreCookie(code);
                    } else {
                        mProgressDialog.dismiss();
                        toast("下载数据失败");
                    }
                }
            });
        }
    }

    private String getPrefCookie() {
        SharedPreferences pref = getSharedPreferences("common_pref", MODE_PRIVATE);
        return pref.getString("cookie", null);
    }

    private int retryTimes;
    private static final int RETRY_MAX_TIMES = 3;

    private void queryAndStoreCookie(String code) {
        if (retryTimes++ > RETRY_MAX_TIMES) {
            mProgressDialog.dismiss();
            toast("下载数据失败");
            return;
        }
        CookieUtil.getCookie(this, "http://stockpage.10jqka.com.cn/" + code, new CookieUtil.OnCookieLoadedListener() {
            @Override
            public void onCookieLoaded(String cookie) {
                SharedPreferences pref = getSharedPreferences("common_pref", MODE_PRIVATE);
                pref.edit().putString("cookie", cookie).commit();
                downloadData();
            }
        });
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        onBackPressed();
    }


    //这里code是股票代码，比如工商银行的601398；cookie通过上面的工具方法得到，工具方法的url参数为"http://stockpage.10jqka.com.cn/" + code
    private static void doDownload(String code, String cookie) {
        String url = "http://d.10jqka.com.cn/v2/time/hs_" + code + "/last.js";
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10 * 1000);
            connection.setReadTimeout(10 * 1000);
            //同花顺网站做了cookie跟referer的校验，referer是固定的，cookie会隔断时间就更新
            connection.setRequestProperty("cookie", cookie);
            connection.setRequestProperty("referer", "http://stockpage.10jqka.com.cn/HQ_v3.html");

            int responseCode = connection.getResponseCode();

            BufferedReader br = null;

            StringBuilder sb = new StringBuilder();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }

                br.close();
                String str = sb.substring("quotebridge_v2_time_hs_666666_last({\"hs_666666\":".length(), sb.length() - 3);
            } else {
                if (responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
                    //同花顺服务器做了cookie校验，调用这个方法如果捕捉到Error，则用获取cookie的工具方法刷新得到最新的cookie再调用此方法重试
                    throw new Error("403");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
