package com.rzc.stockdemo.data;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rzc on 17/11/2.
 */

public class StockRankData {
    public String 序号;
    public String 代码;
    public String 长代码;
    public String 名字;
    public String S名字;
    public double 价格;
    public double 涨跌幅;
    public double 涨跌额;
    public String 五分钟涨幅;
    public double 开盘价;
    public double 昨收盘价;
    public double 最高价;
    public double 最低价;
    public String 成交量;
    public String 成交额;
    public double 换手率;
    public double 量比;
    public double 委比;
    public double 振幅;
    public double 市盈率;
    public String 流通市值;
    public double 总市值;
    public String 每股收益;
    public String 净利润;
    public String 主营收入;

    public static StockRankData fromJson(JSONObject obj) throws JSONException {
        StockRankData item = new StockRankData();
        item.序号 = obj.getString("NO");
        item.代码 = obj.getString("SYMBOL");
        item.长代码 = obj.getString("CODE");
        item.名字 = obj.getString("NAME");
        item.S名字 = obj.getString("SNAME");
        item.价格 = obj.optDouble("PRICE");
        item.涨跌幅 = obj.optDouble("PERCENT");
        item.涨跌额 = obj.optDouble("UPDOWN");
        item.五分钟涨幅 = obj.getString("FIVE_MINUTE");
        item.开盘价 = obj.optDouble("OPEN");
        item.昨收盘价 = obj.optDouble("YESTCLOSE");
        item.最高价 = obj.optDouble("HIGH");
        item.最低价 = obj.optDouble("LOW");
        item.成交量 = obj.getString("VOLUME");
        item.成交额 = obj.getString("TURNOVER");
        if (obj.has("HS")) {
            try {
                item.换手率 = obj.optDouble("HS");
            } catch (Exception e) {
            }
        }
        if (obj.has("LB")) {
            try {
                item.量比 = obj.optDouble("LB");
            } catch (Exception e) {
            }
        }
        item.委比 = obj.optDouble("WB");
        item.振幅 = obj.optDouble("ZF");
        if (obj.has("PE")) {
            item.市盈率 = obj.optDouble("PE");
        }
        if (obj.has("MCAP")) {
            try {
                item.流通市值 = obj.getString("MCAP");
            } catch (Exception e) {
            }
        }
        item.总市值 = obj.optDouble("TCAP");
        if (obj.has("MFSUM")) {
            item.每股收益 = obj.getString("MFSUM");
        }
        if (obj.has("MFSUM")) {
            JSONObject sr = obj.getJSONObject("MFRATIO");
            item.净利润 = sr.getString("MFRATIO2");
            item.主营收入 = sr.getString("MFRATIO10").toString();
        }
        return item;
    }


    public interface OnDataDownloadListener {
        void onDataDownload(List<StockRankData> list);
        void onFailed(Exception e);
    }

    private static Handler mHandler = new Handler(Looper.getMainLooper());

    public static void downloadData(final OnDataDownloadListener listener) {
        new Thread() {
            @Override
            public void run() {
                try {
                    final List<StockRankData> list = new ArrayList<StockRankData>();
                    doDownload(list);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onDataDownload(list);
                        }
                    });
                } catch (final Exception e) {
                    e.printStackTrace();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onFailed(e);
                        }
                    });
                }
            }
        }.start();
    }

    private static void doDownload(List<StockRankData> list) throws JSONException, IOException {
        int pageIndex = 0;
        while (true) {
            int pageSize = 480;
            String urlStr = "http://quotes.money.163.com/hs/service/diyrank.php?page="
                    + pageIndex++
                    + "&query=STYPE%3AEQA&fields=NO%2CSYMBOL%2CNAME%2CPRICE%2CPERCENT%2CUPDOWN%2CFIVE_MINUTE%2COPEN%2CYESTCLOSE%2CHIGH%2CLOW%2CVOLUME%2CTURNOVER%2CHS%2CLB%2CWB%2CZF%2CPE%2CMCAP%2CTCAP%2CMFSUM%2CMFRATIO.MFRATIO2%2CMFRATIO.MFRATIO10%2CSNAME%2CCODE%2CANNOUNMT%2CUVSNEWS&sort=PERCENT&order=desc&count="
                    + pageSize + "&type=query";
            Document doc = Jsoup.connect(urlStr).timeout(15000).get();
            String jsonStr = doc.text();
            JSONObject jsonObject = new JSONObject(jsonStr);
            int pageCount = jsonObject.getInt("pagecount");
            JSONArray jsonArray = jsonObject.getJSONArray("list");

            for (int i = 0; i < jsonArray.length(); i++) {
                list.add(StockRankData.fromJson(jsonArray.getJSONObject(i)));
            }

            if (pageIndex >= pageCount) {
                break;
            }
        }
    }
}
