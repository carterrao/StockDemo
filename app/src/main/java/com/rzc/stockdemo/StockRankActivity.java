package com.rzc.stockdemo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.rzc.stockdemo.data.StockRankData;
import com.rzc.widget.RankHorScrollView;

import java.text.DecimalFormat;
import java.util.List;

public class StockRankActivity extends Activity implements View.OnClickListener {
    private ListView mListView;
    private RankAdapter mAdapter;
    private ProgressDialog mProgressDialog;
    private List<StockRankData> mStockList;
    private RankHorScrollView hsvTitle;
    private int mTitleScrollLeft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_rank);
        findViewById(R.id.ivBack).setOnClickListener(this);
        hsvTitle = findViewById(R.id.hsvTitle);
        hsvTitle.setOnScrollChangedListener(new RankHorScrollView.OnScrollChangedListener() {
            @Override
            public void onScrollChanged(int l, int t) {
                mTitleScrollLeft = l;
                int count = mListView.getChildCount();
                for (int i = 0; i < count; i++) {
                    RankHorScrollView hsvI = (RankHorScrollView) mListView.getChildAt(i).findViewById(R.id.hsv);
                    if (hsvI != null) {
                        hsvI.scrollTo(l, t);
                    }
                }
            }
        });
        mListView = findViewById(R.id.mListView);
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int count = mListView.getChildCount();
                for (int i = 0; i < count; i++) {
                    RankHorScrollView hsvI = (RankHorScrollView) mListView.getChildAt(i).findViewById(R.id.hsv);
                    if (hsvI != null) {
                        hsvI.scrollTo(mTitleScrollLeft, 0);
                    }
                }
            }
        });
        mAdapter = new RankAdapter();
        mListView.setAdapter(mAdapter);

        mProgressDialog = ProgressDialog.show(this, "", "正在加载数据...");

        StockRankData.downloadData(new StockRankData.OnDataDownloadListener() {
            @Override
            public void onDataDownload(List<StockRankData> list) {
                mProgressDialog.dismiss();
                mStockList = list;
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailed(Exception e) {
                mProgressDialog.dismiss();
                toast("数据下载失败");
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

    class ItemClickListener implements View.OnClickListener {
        int index;

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(StockRankActivity.this, StockTimeSharingActivity.class);
            intent.putExtra("basePrice", mStockList.get(index).昨收盘价);
            intent.putExtra("maxPrice", mStockList.get(index).最高价);
            intent.putExtra("minPrice", mStockList.get(index).最低价);
            intent.putExtra("name", mStockList.get(index).S名字);
            intent.putExtra("code", mStockList.get(index).代码);
            startActivity(intent);
        }
    }

    class RankAdapter extends BaseAdapter {
        private DecimalFormat df = new DecimalFormat("0.00%");
        int redColor = 0xffd6433b;
        int greenColor = 0xff538b34;

        @Override
        public int getCount() {
            return mStockList != null ? mStockList.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return mStockList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(StockRankActivity.this, R.layout.item_stock_rank, null);
                final RankHorScrollView hsv = (RankHorScrollView) convertView.findViewById(R.id.hsv);
                hsv.setOnScrollChangedListener(new RankHorScrollView.OnScrollChangedListener() {
                    @Override
                    public void onScrollChanged(int l, int t) {
                        int count = mListView.getChildCount();
                        for (int i = 0; i < count; i++) {
                            RankHorScrollView hsvI = (RankHorScrollView) mListView.getChildAt(i).findViewById(R.id.hsv);
                            hsvI.scrollTo(l, t);
                        }
                        hsvTitle.scrollTo(l, t);
                    }
                });

                ItemClickListener itemClickListener = new ItemClickListener();
                itemClickListener.index = position;
                convertView.setOnClickListener(itemClickListener);
                hsv.getChildAt(0).setOnClickListener(itemClickListener);
                convertView.setTag(itemClickListener);
            }
            ItemClickListener itemClickListener = (ItemClickListener) convertView.getTag();
            itemClickListener.index = position;
            TextView tvName = (TextView) convertView.findViewById(R.id.tvName);
            TextView tvCode = (TextView) convertView.findViewById(R.id.tvCode);
            TextView tvPrice = (TextView) convertView.findViewById(R.id.tvPrice);
            TextView tvZhangFu = (TextView) convertView.findViewById(R.id.tvZhangFu);
            TextView tvZhangE = (TextView) convertView.findViewById(R.id.tvZhangE);
            TextView tvZhenFu = (TextView) convertView.findViewById(R.id.tvZhenFu);
            TextView tvShiYin = (TextView) convertView.findViewById(R.id.tvShiYin);
            TextView tvHuanShou = (TextView) convertView.findViewById(R.id.tvHuanShou);
            TextView tvLiangBi = (TextView) convertView.findViewById(R.id.tvLiangBi);
            TextView tvWeiBi = (TextView) convertView.findViewById(R.id.tvWeiBi);
            TextView tvKai = (TextView) convertView.findViewById(R.id.tvKai);
            TextView tvZuoShou = (TextView) convertView.findViewById(R.id.tvZuoShou);
            TextView tvMax = (TextView) convertView.findViewById(R.id.tvMax);
            TextView tvMin = (TextView) convertView.findViewById(R.id.tvMin);
            TextView tvShiZhi = (TextView) convertView.findViewById(R.id.tvShiZhi);

            int color = 0xff000000;

            StockRankData data = mStockList.get(position);

            if (data.涨跌幅 > 0) {
                color = redColor;
            } else if (data.涨跌幅 < 0) {
                color = greenColor;
            }

            tvPrice.setTextColor(color);
            tvZhangFu.setTextColor(color);
            tvZhangE.setTextColor(color);

            tvZhenFu.setTextColor(0xff000000);
            tvShiYin.setTextColor(0xff000000);
            tvHuanShou.setTextColor(0xff000000);
            tvLiangBi.setTextColor(0xff000000);
            tvWeiBi.setTextColor(0xff000000);
            tvKai.setTextColor(0xff000000);
            tvZuoShou.setTextColor(0xff000000);
            tvMax.setTextColor(0xff000000);
            tvMin.setTextColor(0xff000000);
            tvShiZhi.setTextColor(0xff000000);

            tvName.setText(data.S名字);
            tvCode.setText(data.代码);
            tvPrice.setText(String.format("%.2f", data.价格));
            tvZhangFu.setText(df.format(data.涨跌幅));
            tvZhangE.setText(String.format("%.2f", data.涨跌额));
            tvZhenFu.setText(df.format(data.振幅));
            tvShiYin.setText(String.format("%.2f", data.市盈率));
            tvHuanShou.setText(df.format(data.换手率));
            tvLiangBi.setText(String.format("%.2f", data.量比));
            tvWeiBi.setText(df.format(data.委比));
            tvKai.setText(String.format("%.2f", data.开盘价));
            tvZuoShou.setText(String.format("%.2f", data.昨收盘价));
            tvMax.setText(String.format("%.2f", data.最高价));
            tvMin.setText(String.format("%.2f", data.最低价));
            tvShiZhi.setText(getShiZhiStr(data.总市值));

            return convertView;
        }
    }

    private String getShiZhiStr(double shiZhi) {
        double wan = shiZhi / 10000;
        double yi = wan / 10000;
        if (yi > 1) {
            return String.format("%.2f亿", yi);
        } else {
            return String.format("%.0f万", wan);
        }
    }
}
