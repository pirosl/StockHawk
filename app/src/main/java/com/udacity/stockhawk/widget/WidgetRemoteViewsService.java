package com.udacity.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Formatter;
import java.util.Locale;


/**
 * Created by Lucian Piros on 18/02/2017.
 *
 * RemoteViewsService controlling the data being shown in the scrollable stock detail widget
 */

public class WidgetRemoteViewsService extends RemoteViewsService {
    public final String LOG_TAG = WidgetRemoteViewsService.class.getSimpleName();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private DecimalFormat dollarFormatWithPlus;
            private DecimalFormat dollarFormat;
            private DecimalFormat percentageFormat;
            private Cursor data = null;

            @Override
            public void onCreate() {
                dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                dollarFormatWithPlus.setPositivePrefix("$");
                percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
                percentageFormat.setMaximumFractionDigits(2);
                percentageFormat.setMinimumFractionDigits(2);
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                data = getContentResolver().query(Contract.Quote.uri,
                        Contract.Quote.QUOTE_COLUMNS,
                        null,
                        null,
                        Contract.Quote.COLUMN_SYMBOL);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_item);

                views.setTextViewText(R.id.symbol, data.getString(Contract.Quote.POSITION_SYMBOL));
                views.setTextViewText(R.id.name, data.getString(Contract.Quote.POSITION_NAME));
                views.setTextViewText(R.id.price, dollarFormat.format(data.getFloat(Contract.Quote.POSITION_PRICE)));

                float rawAbsoluteChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
                float percentageChange = data.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

                if (rawAbsoluteChange > 0) {
                    views.setTextColor(R.id.change, getApplicationContext().getResources().getColor(R.color.colorStockGreen)); //setBackgroundResource(R.drawable.percent_change_pill_green);
                } else {
                    views.setTextColor(R.id.change, getApplicationContext().getResources().getColor(R.color.colorStockRed)); //setBackgroundResource(R.drawable.percent_change_pill_red);
                }

                String change = dollarFormatWithPlus.format(rawAbsoluteChange);
                String percentage = percentageFormat.format(percentageChange / 100);

                StringBuilder changeSB = new StringBuilder();
                Formatter formatter = new Formatter(changeSB, Locale.getDefault());
                formatter.format(getApplicationContext().getResources().getString(R.string.stock_change_format), change, percentage);
                views.setTextViewText(R.id.change, changeSB.toString());


                final Intent detailsIntent = new Intent();//getApplicationContext(), HistoricValuesActivity.class);
             //   Bundle bundle = new Bundle();
              //  bundle.putString(getResources().getString(R.string.activity_symbol_parameter), data.getString(Contract.Quote.POSITION_SYMBOL));
               // detailsIntent.putExtras(bundle);
            //    detailsIntent.setData(Contract.Quote.makeUriForStock(data.getString(Contract.Quote.POSITION_SYMBOL)));
                Bundle bundle = new Bundle();
                bundle.putString(getResources().getString(R.string.activity_symbol_parameter), data.getString(Contract.Quote.POSITION_SYMBOL));
                detailsIntent.putExtras(bundle);
                views.setOnClickFillInIntent(R.id.quote_item, detailsIntent);

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
