package com.sam_chordas.android.stockhawk.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import com.sam_chordas.android.stockhawk.events.DataEvent;
import com.sam_chordas.android.stockhawk.utils.DateUtils;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import de.greenrobot.event.EventBus;

/**
 * Created by aung on 3/27/16.
 */
public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    private static final String BARG_DATE = "BARG_DATE";
    private static final String BARG_DATE_TYPE = "BARG_DATE_TYPE";

    public static final int DATE_TYPE_START = 1;
    public static final int DATE_TYPE_END = 2;

    private int mDateType;

    public static DatePickerFragment newInstance(String date, int dateType) {
        DatePickerFragment fragment = new DatePickerFragment();
        Bundle bundle = new Bundle();
        bundle.putString(BARG_DATE, date);
        bundle.putInt(BARG_DATE_TYPE, dateType);
        fragment.setArguments(bundle);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker

        Bundle bundle = getArguments();
        int year, month, day;
        Calendar calendar = Calendar.getInstance();
        if (bundle != null) {
            String date = bundle.getString(BARG_DATE);
            mDateType = bundle.getInt(BARG_DATE_TYPE);
            try {
                calendar.setTime(DateUtils.sdfQueryDate.parse(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Do something with the date chosen by the user
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);

        Date pickedDate = calendar.getTime();

        if (mDateType == DATE_TYPE_START) {
            DataEvent.PickedStartDateEvent event = new DataEvent.PickedStartDateEvent(pickedDate);
            EventBus.getDefault().post(event);
        } else if (mDateType == DATE_TYPE_END) {
            DataEvent.PickedEndDateEvent event = new DataEvent.PickedEndDateEvent(pickedDate);
            EventBus.getDefault().post(event);
        }
    }
}