package com.rzc.stockdemo.data;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rzc on 17/11/3.
 */

public class TimeSharingData {
    public int second;
    public float value;

    private static Handler mHandler = new Handler(Looper.getMainLooper());

    public TimeSharingData(int second, float value) {
        this.second = second;
        this.value = value;
    }

    @Override
    public String toString() {
        return "TimeSharingData{" +
                "second=" + second +
                ", value=" + value +
                '}';
    }

    public interface OnDownloadedListener {
        void onDownloaded(List<TimeSharingData> list);
        void onFailed(boolean cookieError);
    }

    public static void download(final String code, final String cookie, final OnDownloadedListener listener) {
        new Thread() {
            @Override
            public void run() {
                try {
                    final List<TimeSharingData> list = doDownload(code, cookie);
                    if (list != null) {
                        if (listener != null) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onDownloaded(list);
                                }
                            });
                        }
                    } else {
                        if (listener != null) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onFailed(false);
                                }
                            });
                        }
                    }
                } catch (Error error) {
                    if (listener != null) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onFailed(true);
                            }
                        });
                    }
                }
            }
        }.start();
    }

    private static List<TimeSharingData> doDownload(String code, String cookie) {
        String url = "http://d.10jqka.com.cn/v2/time/hs_" + code + "/last.js";
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10 * 1000);
            connection.setReadTimeout(10 * 1000);
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
                JSONObject jsonObject = new JSONObject(str);
                String data = jsonObject.getString("data");
                String timeDataArr[] = data.split(";");
                List<TimeSharingData> list = new ArrayList<TimeSharingData>();
                for (String one : timeDataArr) {
                    String timeValue[] = one.split(",");
                    int hour = Integer.parseInt(timeValue[0].substring(0, 2));
                    int minute = Integer.parseInt(timeValue[0].substring(2));
                    list.add(new TimeSharingData(hour * 3600 + minute * 60, Float.parseFloat(timeValue[1])));
                }

                return list;
            } else {
                if (responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
                    throw new Error("403");
                }
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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
