package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.ToggleButton;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.StringTokenizer;
import java.util.Vector;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Created by lucian on 03/01/2017.
 */
public class HistoricValuesActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        OnChartValueSelectedListener {

    class HistoryDay implements Comparable<HistoryDay> {

        private long timeStamp;
        private float value;

        public HistoryDay(long timeStamp, float value) {
            this.timeStamp = timeStamp;
            this.value = value;
        }

        public float getValue() {
            return value;
        }

        public String getDate() {
            SimpleDateFormat sdf = new SimpleDateFormat(getResources().getString(R.string.activity_historic_date_formatter));
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timeStamp);

            return sdf.format(calendar.getTime());
        }

        @Override
        public int compareTo(HistoryDay o) {
            if(timeStamp < o.timeStamp)
                return -1;
            if(timeStamp > o.timeStamp)
                return 1;
            return 0;
        }
    }

    static final RadioGroup.OnCheckedChangeListener ToggleListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(final RadioGroup radioGroup, final int i) {
            for (int j = 0; j < radioGroup.getChildCount(); j++) {
                final ToggleButton view = (ToggleButton) radioGroup.getChildAt(j);
                view.setChecked(view.getId() == i);
            }
        }
    };

    private static final int STOCK_HISTORY_LOADER = 0;
    private String symbol;
    private Vector<HistoryDay> historyData;

    private final int itemcount = 12;

    public void onToggle(View view) {
        ((RadioGroup)view.getParent()).check(view.getId());
        // app specific stuff ..
    }

    @BindView(R.id.historic_values_chart)
    CombinedChart historicValuesChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_historic_values);
        ButterKnife.bind(this);

        ((RadioGroup) findViewById(R.id.toggleGroup)).setOnCheckedChangeListener(ToggleListener);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null)
            symbol = bundle.getString(getResources().getString(R.string.activity_symbol_parameter));
        getSupportLoaderManager().initLoader(STOCK_HISTORY_LOADER, null, this);

        historicValuesChart.getDescription().setEnabled(false);
        historicValuesChart.setBackgroundColor(Color.WHITE);
        historicValuesChart.setDrawGridBackground(false);
        historicValuesChart.setDrawBarShadow(false);
        historicValuesChart.setHighlightFullBarEnabled(false);
        historicValuesChart.setGridBackgroundColor(Color.WHITE);

        historicValuesChart.setOnChartValueSelectedListener(this);
    }


    protected void populateChartData() {
        CombinedData data = new CombinedData();

        data.setData(generateLineData());

        //data.setData(generateBarData());

        XAxis xAxis = historicValuesChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED);
        xAxis.setAxisMinimum(0f);
        xAxis.setGranularity(1f);
        xAxis.setAxisMaximum(data.getXMax() + 0.25f);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawLabels(false);
        xAxis.setDrawGridLines(false);

        YAxis leftYAxis = historicValuesChart.getAxisLeft();
        leftYAxis.setDrawAxisLine(false);
        leftYAxis.setDrawZeroLine(false);
        leftYAxis.setDrawGridLines(false);
        leftYAxis.setDrawLabels(false);

        YAxis rightYAxis = historicValuesChart.getAxisRight();
        rightYAxis.setDrawAxisLine(false);
        rightYAxis.setDrawZeroLine(false);
        rightYAxis.setDrawGridLines(false);
        rightYAxis.setDrawLabels(false);

        historicValuesChart.setData(data);

        // get the legend (only possible after setting data)
        Legend l = historicValuesChart.getLegend();
        l.setEnabled(false);

        historicValuesChart.invalidate();
    }

    private LineData generateLineData() {

        LineData d = new LineData();

        ArrayList<Entry> entries = new ArrayList<Entry>();
        Collections.sort(historyData);

        for(int position = 0; position < historyData.size(); ++position) {
            entries.add(new Entry(position + 0.5f, historyData.get(position).getValue()));
            Timber.d(historyData.get(position).getDate() + " = " + historyData.get(position).getValue());
        }

        LineDataSet set = new LineDataSet(entries, null);
        set.setDrawCircles(false);
        set.setDrawHorizontalHighlightIndicator(false);

        set.setColor(Color.rgb(240, 238, 70));
        set.setLineWidth(1.5f);
       // set.setCircleColor(Color.rgb(240, 238, 70));
        //set.setCircleRadius(5f);
      //  set.setFillColor(Color.rgb(240, 238, 70));
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setDrawValues(false);
      //  set.setValueTextSize(10f);
       // set.setValueTextColor(Color.rgb(240, 238, 70));

        set.setFillAlpha(255);
        set.setDrawFilled(true);
        set.setFillAlpha(110);
        set.setFillColor(Color.RED);
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setDrawCircleHole(false);
      /*  set.setFillFormatter(new IFillFormatter() {
            @Override
            public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                return historicValuesChart.getAxisLeft().getAxisMinimum();
            }
        });

        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        */d.addDataSet(set);

        return d;
    }

    private BarData generateBarData() {

        ArrayList<BarEntry> entries1 = new ArrayList<BarEntry>();
        ArrayList<BarEntry> entries2 = new ArrayList<BarEntry>();

        for (int index = 0; index < itemcount; index++) {
            entries1.add(new BarEntry(0, getRandom(25, 25)));

            // stacked
            entries2.add(new BarEntry(0, new float[]{getRandom(13, 12), getRandom(13, 12)}));
        }

        BarDataSet set1 = new BarDataSet(entries1, "Bar 1");
        set1.setColor(Color.rgb(60, 220, 78));
        set1.setValueTextColor(Color.rgb(60, 220, 78));
        set1.setValueTextSize(10f);
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);

        BarDataSet set2 = new BarDataSet(entries2, "");
        set2.setStackLabels(new String[]{"Stack 1", "Stack 2"});
        set2.setColors(new int[]{Color.rgb(61, 165, 255), Color.rgb(23, 197, 255)});
        set2.setValueTextColor(Color.rgb(61, 165, 255));
        set2.setValueTextSize(10f);
        set2.setAxisDependency(YAxis.AxisDependency.LEFT);

        float groupSpace = 0.06f;
        float barSpace = 0.02f; // x2 dataset
        float barWidth = 0.45f; // x2 dataset
        // (0.45 + 0.02) * 2 + 0.06 = 1.00 -> interval per "group"

        BarData d = new BarData(set1, set2);
        d.setBarWidth(barWidth);

        // make this BarData object grouped
        d.groupBars(0, groupSpace, barSpace); // start at x = 0

        return d;
    }


    protected float getRandom(float range, float startsfrom) {
        return (float) (Math.random() * range) + startsfrom;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                Contract.Quote.makeUriForStock(symbol),
                Contract.Quote.QUOTE_COLUMNS,
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        historyData = new Vector<>();
        if(data.getCount() > 0) {
            data.moveToFirst();
            String historyString = data.getString(Contract.Quote.POSITION_HISTORY);

            StringTokenizer historyParser = new StringTokenizer(historyString, "\n");
            while(historyParser.hasMoreTokens()) {
                StringTokenizer oneDayParser = new StringTokenizer(historyParser.nextToken(), ",");
                long timeStamp = Long.valueOf(oneDayParser.nextToken()).longValue();
                float val = Float.valueOf(oneDayParser.nextToken()).floatValue();

                historyData.add(new HistoryDay(timeStamp, val));
            }
        }

        populateChartData();
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

        int position = (int)(e.getX());

        Timber.d(position + " " + historyData.get(position).getDate() + " = " + historyData.get(position).getValue());
    }

    @Override
    public void onNothingSelected() {

    }
}
