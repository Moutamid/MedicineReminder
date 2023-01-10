package com.medicine.reminderapp;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class StockReminderFSD extends DialogFragment implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private static final String TAG = "ABC StockReminderFSD";
    protected TextView nRDateTvw;
    protected TextView nRTimeTvw;
    protected EditText nRTitleTIEditT;
//    protected TextInputLayout nRTitleTILayout;
    protected ImageButton actionBarCloseButton;
    protected TextView titleTvw;

    private int setDay = 0;
    private int setMonth = 0;
    private int setYear = 0;
    private int setHour = 0;
    private int setMinute = 0;
    protected Calendar setCalendar;

    protected DatePickerDialog nRDatePDialog;
    protected TimePickerDialog nRTimePDialog;

    protected ReminderInterface reminderInterface;
    protected EditText stockpills;

    protected int reminderId;
    protected String reminderTitle;
    protected String reminderDOF;
    protected String reminderTOF;
    protected String pillsst;

    protected long reminderTIM;
    protected int reminderPosition;
    protected boolean isEditMode;

    protected static StockReminderFSD newInstance(int reminderId, String reminderTitle, String reminderDOF, String reminderTOF, long reminderTIM, int reminderPosition, boolean isEditMode, String pills) {
        Log.d(TAG, "newInstance: ");
        StockReminderFSD newReminderFSD = new StockReminderFSD();
        Bundle params = new Bundle();

        params.putInt("reminderId", reminderId);
        params.putString("reminderTitle", reminderTitle);
        params.putString("reminderDOF", reminderDOF);
        params.putString("reminderTOF", reminderTOF);
        params.putLong("reminderTIM", reminderTIM);
        params.putInt("reminderPosition", reminderPosition);
        params.putBoolean("isEditMode", isEditMode);
        params.putString("pills", pills);

        newReminderFSD.setArguments(params);
        return newReminderFSD;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: started");

        reminderId = getArguments().getInt("reminderId");
        reminderTitle = getArguments().getString("reminderTitle");
        reminderDOF = getArguments().getString("reminderDOF");
        reminderTOF = getArguments().getString("reminderTOF");
        reminderTIM = getArguments().getLong("reminderTIM");
        reminderPosition = getArguments().getInt("reminderPosition");
        isEditMode = getArguments().getBoolean("isEditMode");
        pillsst = getArguments().getString("pills");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.stock_fragment_rfsd, container, false);
        Log.d(TAG, "onCreateView: ");
        actionBarCloseButton = rootView.findViewById(R.id.action_bar_close_button);
        actionBarCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: actionBarCloseButton");
                hideKeyboard(rootView);
                dismiss();
            }
        });

        nRDateTvw =rootView.findViewById(R.id.nrdl_date_value_tvw);
        nRTimeTvw= rootView.findViewById(R.id.nrtl_time_value_tvw);
        stockpills= rootView.findViewById(R.id.ntotal_pills);

        titleTvw = rootView.findViewById(R.id.action_bar_title_tvw);

        if(isEditMode){
            titleTvw.setText("Edit Reminder");
        }

        else {
            titleTvw.setText("New Reminder");
        }

        reminderInterface = (ReminderInterface)getActivity();

        String nowDTS = getDTS(getNowTIM());
        String nowDS = nowDTS.substring(7,17);
        String nowTS = nowDTS.substring(0, 5);

        nRDateTvw.setText(nowDS);
        nRTimeTvw.setText(nowTS);

        setCalendar = Calendar.getInstance();

//        nRTitleTILayout = rootView.findViewById(R.id.nr_title_til);
        nRTitleTIEditT = rootView.findViewById(R.id.nr_title_tiet);

        Button actionBarSButton = rootView.findViewById(R.id.action_bar_save_button);
        actionBarSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: actionBarSButton");

                String nReminderTitle = nRTitleTIEditT.getText().toString();
                long nReminderTIM = setCalendar.getTimeInMillis();
                  pillsst= stockpills.getText().toString();
                String nReminderDTS = getDTS(nReminderTIM);
//                long nReminderTIM2 = setCalendar.getTimeInMillis();
                if(!dialogTextIsSpaces(nReminderTitle)){

                    if(!isEditMode){
                        reminderInterface.addReminder(nReminderTitle,nReminderDTS,nReminderTIM,pillsst);
                    }
                    else {
                        reminderInterface.updateReminder(nReminderTitle,nReminderDTS,nReminderTIM,reminderId,reminderPosition,pillsst);
                    }

                    hideKeyboard(rootView);
                    dismiss();
                }

                else {
                    nRTitleTIEditT.setError(getResources().getString(R.string.teit_error_text));

                    nRTitleTIEditT.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            Log.d(TAG, "onTextChanged: ");
                            String editTextText = nRTitleTIEditT.getText().toString();
                            boolean textIsSpaces = dialogTextIsSpaces(editTextText);

                            if(textIsSpaces){
                                nRTitleTIEditT.setError(getResources().getString(R.string.teit_error_text));
                            }


                        }

                        @Override
                        public void afterTextChanged(Editable editable) {
                        }
                    });
                }
            }
        });

        initializeDateAndTimePickerDialogs();

        nRDateTvw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nRDatePDialog.show();
            }
        });

        nRTimeTvw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nRTimePDialog.show();
            }
        });

        if(isEditMode){
            setEditMode();
        }

//        reminderInterface.hideActionBar();

        return rootView;
    }

    protected boolean dialogTextIsSpaces(String dialogText){
        Log.d(TAG, "dialogTextIsSpaces: ");
        int i = 0;
        int noOfSpaces = 0;
        int dialogTextLength = dialogText.length();

        while (i >= 0 && i <= (dialogTextLength-1)){
            String dialogTextCharacter= String.valueOf(dialogText.charAt(i));

            if(dialogTextCharacter.equals(" ")){
                noOfSpaces = noOfSpaces+1;
            }

            ++i;
        }

        if(dialogTextLength == noOfSpaces){
            return true;
        }

        else {
            return false;
        }

    }

    private void setEditMode(){
        Log.d(TAG, "setEditMode: ");
        nRTitleTIEditT.setText(String.valueOf(reminderTitle));
        nRDateTvw.setText(reminderDOF);
        nRTimeTvw.setText(reminderTOF);
        stockpills.setText(pillsst);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: ");
//        reminderInterface.showActionBar();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog: ");
        Dialog reminderFSD = super.onCreateDialog(savedInstanceState);
        reminderFSD.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return reminderFSD;
    }

    private long getNowTIM(){
        Log.d(TAG, "getNowTIM: ");
        Date nowDate =new Date();
        long nowTIM = nowDate.getTime();
        return nowTIM;
    }

    private String getDTS(long timeInMills){
        Log.d(TAG, "getDTS: ");
        Date date= new Date(timeInMills);
        DateFormat dateFormat=new SimpleDateFormat("HH:mm  dd/MM/yyyy");
        String dateTimeString=  dateFormat.format(date);
        return dateTimeString;
    }

    private void initializeDateAndTimePickerDialogs(){
        Log.d(TAG, "initializeDateAndTimePickerDialogs: ");
        if(isEditMode){
            setCalendar.setTimeInMillis(reminderTIM);
        }
        else {
            setCalendar.setTimeInMillis(getNowTIM());
        }

        int nCYear=setCalendar.get(Calendar.YEAR);
        int nCMonth= setCalendar.get(Calendar.MONTH);
        int nCDay=setCalendar.get(Calendar.DAY_OF_MONTH);
        int nCHour= setCalendar.get(Calendar.HOUR_OF_DAY);
        int nCMinute = setCalendar.get(Calendar.MINUTE);

        setYear = nCYear;
        setMonth = nCMonth;
        setDay = nCDay;
        setHour = nCHour;
        setMinute = nCMinute;

        nRDatePDialog=new DatePickerDialog(getContext(), this, nCYear, nCMonth, nCDay);
        nRTimePDialog=new TimePickerDialog(getContext(), this, nCHour,  nCMinute, true);

        nRDatePDialog.setCanceledOnTouchOutside(true);
        nRTimePDialog.setCanceledOnTouchOutside(true);

        nRDatePDialog.setTitle("Set Reminder Date");
        nRTimePDialog.setTitle("Set Reminder Time");
    }

    private void hideKeyboard(View view){
        Log.d(TAG, "hideKeyboard: ");
        InputMethodManager inputMethodManager =(InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void updateNRDTvw() {
        Log.d(TAG, "updateNRDTvw: ");
        setCalendar.set(setYear,setMonth,setDay,setHour,setMinute);
        setCalendar.set(Calendar.SECOND, 0);//TODO: ADDED
        String dateTimeString = getDTS(setCalendar.getTimeInMillis());
        nRDateTvw.setText(dateTimeString.substring(7,17));
    }

    private void updateNRTTvw() {
        Log.d(TAG, "updateNRTTvw: ");
        setCalendar.set(setYear,setMonth,setDay,setHour,setMinute);
        setCalendar.set(Calendar.SECOND, 0);//TODO: ADDED
        String dateTimeString = getDTS(setCalendar.getTimeInMillis());
        nRTimeTvw.setText(dateTimeString.substring(0,5));
    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        Log.d(TAG, "onDateSet: ");
        setYear =i;
        setMonth = i1;
        setDay =i2;
        updateNRDTvw();
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i1) {
        Log.d(TAG, "onTimeSet: ");
        setHour =i;
        setMinute =i1;
        updateNRTTvw();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        Log.d(TAG, "onCreateOptionsMenu: ");
    }
}
