package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
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
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Vector;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by lucian on 03/01/2017.
 */
public class HistoricValuesActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        OnChartValueSelectedListener, RadioGroup.OnCheckedChangeListener {

    private static int months[] = {1,3,6,12,24};
    private int selected = 0;

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

        public int getMonths() {
            Date today = new Date(); // the date of today

            long daysInMs = today.getTime() - timeStamp; //days in MS's

            int months = (int)(daysInMs/1000/60/60/12/30);

            return months;
        }

        public String getDate() {
            SimpleDateFormat sdf = new SimpleDateFormat(getResources().getString(R.string.activity_historic_date_formatter));
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timeStamp);

            return sdf.format(calendar.getTime());
        }

        @Override
        public int compareTo(HistoryDay o) {
            if (timeStamp < o.timeStamp)
                return -1;
            if (timeStamp > o.timeStamp)
                return 1;
            return 0;
        }
    }


    @BindView(R.id.historic_values_chart)
    CombinedChart historicValuesChart;
    @BindView(R.id.btn_six_month)
    ToggleButton toggleButtonSixMonths;
    @BindView(R.id.toggleGroup)
    RadioGroup toggleGoup;

    private static final int STOCK_HISTORY_LOADER = 0;
    private String symbol;
    private Vector<HistoryDay> historyData;

    public void onToggle(View view) {
        ((RadioGroup) view.getParent()).clearCheck();
        ((RadioGroup) view.getParent()).check(view.getId());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_historic_values);
        ButterKnife.bind(this);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null)
            symbol = bundle.getString(getResources().getString(R.string.activity_symbol_parameter));
        getSupportLoaderManager().initLoader(STOCK_HISTORY_LOADER, null, this);

        StringBuilder activityTitle = new StringBuilder();
        Formatter formatter = new Formatter(activityTitle, Locale.getDefault());
        formatter.format(getBaseContext().getResources().getString(R.string.label_activity_historicvalues), symbol);

        this.setTitle(activityTitle.toString());

        historicValuesChart.getDescription().setEnabled(false);
        historicValuesChart.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.colorPrimaryDark));
        historicValuesChart.setDrawGridBackground(false);
        historicValuesChart.setDrawBarShadow(false);
        historicValuesChart.setHighlightFullBarEnabled(false);
        historicValuesChart.setGridBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.colorPrimaryDark));
        historicValuesChart.setPinchZoom(false);
        historicValuesChart.setDoubleTapToZoomEnabled(false);

        historicValuesChart.setViewPortOffsets(4f, 4f, 4f, 4f);
        historicValuesChart.setOnChartValueSelectedListener(this);

        toggleGoup.setOnCheckedChangeListener(this);
        toggleGoup.check(toggleButtonSixMonths.getId());
    }


    protected void populateChartData() {
        CombinedData data = new CombinedData();

        data.setData(generateLineData());

        XAxis xAxis = historicValuesChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED);
        xAxis.setAxisMinimum(data.getXMin()/*0f*/);
        xAxis.setGranularity(1f);
        xAxis.setAxisMaximum(data.getXMax()/* + 0.25f*/);
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

        historicValuesChart.setPadding(0, 0, 0, 0);

        historicValuesChart.invalidate();
    }

    private LineData generateLineData() {

        LineData d = new LineData();

        ArrayList<Entry> entries = new ArrayList<Entry>();
        Collections.sort(historyData);

        int xPos = 0;
        for (int position = 0; position < historyData.size(); ++position) {
            if(historyData.get(position).getMonths() <= months[selected])
                entries.add(new Entry(xPos++ /* + 0.5f */, historyData.get(xPos).getValue()));
        }

        LineDataSet set = new LineDataSet(entries, null);
        set.setDrawCircles(false);
        set.setDrawHorizontalHighlightIndicator(false);

        set.setColor(getBaseContext().getResources().getColor(R.color.colorAccent));
        set.setLineWidth(1.5f);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setDrawValues(false);

        set.setFillAlpha(255);
        set.setDrawFilled(true);
        set.setFillColor(getBaseContext().getResources().getColor(R.color.colorPrimary));
        set.setHighLightColor(getBaseContext().getResources().getColor(R.color.colorAccent));
        set.setDrawCircleHole(false);
        d.addDataSet(set);

        return d;
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
        if (data.getCount() > 0) {
            data.moveToFirst();
            String historyString = data.getString(Contract.Quote.POSITION_HISTORY);

            StringTokenizer historyParser = new StringTokenizer(historyString, "\n");
            while (historyParser.hasMoreTokens()) {
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

    }

    @Override
    public void onNothingSelected() {

    }

    @Override
    public void onCheckedChanged(final RadioGroup radioGroup, final int i) {
        for (int j = 0; j < radioGroup.getChildCount(); j++) {
            final ToggleButton view = (ToggleButton) radioGroup.getChildAt(j);

            if (view.getId() == i) {
                view.setTextColor(radioGroup.getResources().getColor(R.color.material_grey_200));
                view.setChecked(true);
                selected = j;
            } else {
                view.setTextColor(radioGroup.getResources().getColor(R.color.material_grey_700));
                view.setChecked(false);
            }
        }
        if(historyData != null)
            populateChartData();
    }
}
