package com.medicine.reminderapp;

import static android.view.View.GONE;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fxn.stash.Stash;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class StockRemindersActivity extends AppCompatActivity implements ActionMode.Callback, ReminderInterface {
    private static final String TAG = "ABC StockReminders";
    private List<Reminder> reminderList =
            Stash.getArrayList(Constants.STOCKS_LIST, Reminder.class);

    private RecyclerView reminderRecyclerView;
    private StockReminderAdapter reminderLAdapter;
    private StockReminderDatabaseAdapter remindersDatabaseAdapter;
    private boolean rAIsMultiSelect = false;
    private List<Integer> rASelectedPositions = new ArrayList<>();
    private FloatingActionButton rAActionButton;
    protected TextView rAESTitleTextView;
    protected TextView rAESContentTextView;
    protected LinearLayout rAESLinearLayout;
    View rARootLayout;
    ImageView imgBack;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_reminders);
        Log.d(TAG, "onCreate: started");

        Float elevation = 0.0f;
//        getSupportActionBar().setElevation(elevation);

        rARootLayout = findViewById(R.id.ch_root_layout);
        imgBack = findViewById(R.id.imageView_back);

        remindersDatabaseAdapter = new StockReminderDatabaseAdapter(this);
        remindersDatabaseAdapter.open();

        initializeReminderList();

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        reminderRecyclerView = (RecyclerView) findViewById(R.id.reminders_recycler_view);
        rAESTitleTextView = findViewById(R.id.ra_empty_state_title_text_view);
        rAESContentTextView = findViewById(R.id.ra_empty_state_text_view);
        rAESLinearLayout = findViewById(R.id.ra_empty_state_linear_layout);

        reminderLAdapter = new StockReminderAdapter(this, reminderList, this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        reminderRecyclerView.setLayoutManager(layoutManager);
        reminderRecyclerView.setItemAnimator(new DefaultItemAnimator());
//        reminderRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, reminderRecyclerView, new RecyclerTouchListener.OnItemClickListener() {
//            @Override
//            public void onItemClick(View view, int position) {
//                if (rAIsMultiSelect) {
//                    multiSelect(position);
//                }
//
//                else {
//                    openEditDialog(position);
//                }
//
//            }
//
////            @Override
////            public void onItemLongClick(View view, int position) {
////                if (!rAIsMultiSelect) {
////                    rASelectedPositions = new ArrayList<>();
////                    rAIsMultiSelect = true;
////
////                    if (rAActionMode == null) {
////                        rAActionButton.hide();
////                        rAActionMode = startActionMode(RemindersActivity.this);
////                    }
////                }
////
////                multiSelect(position);
////            }
//        }));
        reminderRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        reminderRecyclerView.setAdapter(reminderLAdapter);

//        setNextReminderAlarm();
        setRAEmptyState();

        rAActionButton = findViewById(R.id.new_reminder_fab);
        rAActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openNewReminderDialog();
            }
        });
    }

    private void openNewReminderDialog() {
        Log.d(TAG, "openNewReminderDialog: ");
        FragmentManager oNRFSD = getSupportFragmentManager();
        StockReminderFSD newReminderFSD = StockReminderFSD.newInstance(0, "", "", "", 0, 0, false, "0");
        FragmentTransaction transaction = oNRFSD.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, newReminderFSD).addToBackStack(null).commit();
    }

    private int getNextReminderAPosition() {
        Log.d(TAG, "getNextReminderAPosition: ");
        int nRAPosition = 0;

        int i = 0;
        while (i >= 0 && i <= reminderList.size() - 1) {
            Reminder bReminder = reminderList.get(i);
            long nowTIM = getNowTIM();
            long bReminderTIM = bReminder.getReminderTIM();

            if (bReminderTIM > nowTIM) {
                nRAPosition = i;
                i = reminderList.size();
            }

            ++i;
        }

        return nRAPosition;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setNextReminderAlarm() {
        Log.d(TAG, "setNextReminderAlarm: ");

        if (reminderList != null) {
            if (reminderList.size() != 0) {

                int nRAPosition = getNextReminderAPosition();

                Reminder latestReminder = reminderList.get(nRAPosition);

                long nowTIM = getNowTIM();
                long lReminderTIM = latestReminder.getReminderTIM();

                /*int newCode = new Random().nextInt(9999);
                int oldCode = Stash.getInt(latestReminder.getReminderTitle(), newCode);

                if (oldCode == newCode){
                    Stash.put(latestReminder.getReminderTitle(), newCode);
                    oldCode = newCode;
                }*/

                if (lReminderTIM >= nowTIM) {

                    Intent rARIntent = new Intent(this, NotificationReceiver.class);
                    rARIntent.putExtra("lReminderTitle", latestReminder.getReminderTitle());
                    rARIntent.putExtra("isStock", true);
                    rARIntent.putExtra("code", 100);
                    PendingIntent rARPIntent = PendingIntent.getBroadcast(this, 100, rARIntent, PendingIntent.FLAG_IMMUTABLE);
                    AlarmManager rAAlarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

                    if (Build.VERSION.SDK_INT >= 23) {
                        rAAlarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, lReminderTIM, rARPIntent);
                    } else if (Build.VERSION.SDK_INT >= 19) {
                        rAAlarmManager.setExact(AlarmManager.RTC_WAKEUP, lReminderTIM,rARPIntent);
                    } else {
                        rAAlarmManager.set(AlarmManager.RTC_WAKEUP, lReminderTIM,rARPIntent);
                    }
                    /*TODO: COMMENTED
                       rAAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                            lReminderTIM, 10000, rARPIntent);*/

                    /*int SDK_INT = Build.VERSION.SDK_INT;
                    if (SDK_INT < Build.VERSION_CODES.KITKAT) {
                        assert rAAlarmManager != null;
                        rAAlarmManager.set(AlarmManager.RTC_WAKEUP, lReminderTIM, rARPIntent);

                    } else if (SDK_INT >= Build.VERSION_CODES.KITKAT && SDK_INT < Build.VERSION_CODES.M) {
                        assert rAAlarmManager != null;
                        rAAlarmManager.setExact(AlarmManager.RTC_WAKEUP, lReminderTIM, rARPIntent);

                    } else if (SDK_INT >= Build.VERSION_CODES.M) {
                        assert rAAlarmManager != null;
                        rAAlarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, lReminderTIM, rARPIntent);
                    }*/


                }


            }

        }

    }

    private void openEditDialog(int reminderPosition) {
        Log.d(TAG, "openEditDialog: ");
        Reminder selectedReminder = reminderList.get(reminderPosition);
        int reminderId = selectedReminder.getReminderId();
        String reminderTitle = selectedReminder.getReminderTitle();
        String reminderDOF = selectedReminder.getReminderDOF();
        String reminderTOF = selectedReminder.getReminderTOF();
        String pills = selectedReminder.getStock();
        long reminderTIM = selectedReminder.getReminderTIM();

        FragmentManager openERFSD = getSupportFragmentManager();
        StockReminderFSD editReminderFSD = StockReminderFSD.newInstance(reminderId, reminderTitle, reminderDOF, reminderTOF, reminderTIM, reminderPosition, true, pills);
        FragmentTransaction oEditRFSDTransaction = openERFSD.beginTransaction();
        oEditRFSDTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        oEditRFSDTransaction.add(android.R.id.content, editReminderFSD).addToBackStack(null).commit();
    }

    private int getNewReminderAddPosition(Reminder newReminder) {
        Log.d(TAG, "getNewReminderAddPosition: ");

        List<Reminder> remindersSortList = reminderList;
        remindersSortList.add(newReminder);

        ReminderComparator reminderComparator = new ReminderComparator();
        Collections.sort(remindersSortList, reminderComparator);

        int newReminderPosition = 0;

        int i = 0;
        while (i >= 0 && i <= (remindersSortList.size() - 1)) {

            int reminderId = reminderList.get(i).getReminderId();

            if (reminderId == newReminder.getReminderId()) {
                newReminderPosition = i;
            }

            ++i;
        }

        return newReminderPosition;
    }

    @Override
    public void hideActionBar() {

    }

    @Override
    public void showActionBar() {

    }

    public void addReminder(final String reminderTitle, final String reminderDTS, final long reminderTIM, final String stock) {
        final int[] newReminderId = new int[1];
        Log.d(TAG, "addReminder: ");
        /*Runnable addReminderRunnable = new Runnable() {
            @Override
            public void run() {
                newReminderId[0] = remindersDatabaseAdapter.createReminder(reminderTitle, reminderDTS,stock);

            }
        };
        Thread addReminderThread = new Thread(addReminderRunnable);
        addReminderThread.start();
        try {
            addReminderThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        int reminderId = newReminderId[0];
        String reminderDOF = reminderDTS.substring(7, 17);
        String reminderTOF = reminderDTS.substring(0, 5);
        Reminder newReminder = new Reminder(reminderId, reminderTitle, reminderDOF, reminderTOF, reminderTIM, stock);

        ArrayList<Reminder> reminderArrayList =
                Stash.getArrayList(Constants.STOCKS_LIST, Reminder.class);

        for (Reminder reminder : reminderArrayList) {
            if (reminder.getReminderTitle().equals(reminderTitle)) {
                Toast.makeText(this, "This has already been added!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        reminderArrayList.add(newReminder);

        Stash.put(Constants.STOCKS_LIST, reminderArrayList);

        addReminderToList(newReminder);

        setRAEmptyState();
        setNextReminderAlarm();

        Snackbar.make(rARootLayout, R.string.reminder_set_c, Snackbar.LENGTH_SHORT).show();
    }

    public void updateReminder(final String reminderTitle, final String reminderDTS, final long reminderTIM, final int reminderId, final int reminderPosition, final String stock) {
        Log.d(TAG, "updateReminder: ");
        /*Runnable updateReminderRunnable = new Runnable() {
            @Override
            public void run() {
                remindersDatabaseAdapter.updateReminder(reminderId,reminderTitle,reminderDTS,stock);
            }
        };
        Thread updateReminderThread = new Thread(updateReminderRunnable);
        updateReminderThread.start();
        try {
            updateReminderThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        String reminderDOF = reminderDTS.substring(7, 17);
        String reminderTOF = reminderDTS.substring(0, 5);
        Reminder newReminder = new Reminder(0, reminderTitle, reminderDOF, reminderTOF, reminderTIM, stock);

        ArrayList<Reminder> reminderArrayList =
                Stash.getArrayList(Constants.STOCKS_LIST, Reminder.class);

        for (Reminder reminder : reminderArrayList) {
            if (reminder.getReminderTitle().equals(reminderTitle)) {

                reminder.setReminderTitle(reminderTitle);
                reminder.setReminderDOF(reminderDOF);
                reminder.setReminderTOF(reminderTOF);
                reminder.setReminderTIM(reminderTIM);
                reminder.setStock(stock);
                break;
            }
        }

        Stash.put(Constants.STOCKS_LIST, reminderArrayList);

        updateReminderListItem(reminderTitle, reminderDTS, reminderTIM, reminderPosition, stock);

        setNextReminderAlarm();

        Snackbar.make(rARootLayout, "Reminder Updated", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void editClick(int reminderPosition) {
        Log.d(TAG, "editClick: ");
        openEditDialog(reminderPosition);
    }

    @Override
    public void deleteClick(int reminderPosition) {
        Log.d(TAG, "deleteClick: ");

        AlertDialog.Builder deleteRDialogBuilder = new AlertDialog.Builder(this);
        deleteRDialogBuilder.setTitle(getResources().getString(R.string.delete_reminder_dialog_title));
        deleteRDialogBuilder.setMessage(getResources().getString(R.string.delete_reminder_dialog_message));
        deleteRDialogBuilder.setNegativeButton(getResources().getString(R.string.del_reminder_dialog_negative_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
//                        rAActionMode.finish();
            }
        });

        deleteRDialogBuilder.setPositiveButton(getResources().getString(R.string.delete_reminder_dialog_positive_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(TAG, "onClick: ");

                           /* final int sReminderId = reminderList.get(reminderPosition).getReminderId();

                            Runnable deleteRRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    remindersDatabaseAdapter.deleteReminder(sReminderId);
                                }
                            };
                            Thread deleteRThread = new Thread(deleteRRunnable);
                            deleteRThread.start();
                            try {
                                deleteRThread.join();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }*/
                ArrayList<Reminder> reminderArrayList =
                        Stash.getArrayList(Constants.STOCKS_LIST, Reminder.class);

                for (int ii = 0; ii < reminderArrayList.size(); ii++) {
                    Reminder reminder = reminderArrayList.get(ii);
                    if (reminder.getReminderTitle().equals(reminderList.get(reminderPosition).getReminderTitle())) {
                        reminderArrayList.remove(ii);
                    }
                }

                Stash.put(Constants.STOCKS_LIST, reminderArrayList);
                deleteReminderListItem(reminderPosition);
                setRAEmptyState();


                dialogInterface.dismiss();

//                        rAActionMode.finish();
            }
        });
        deleteRDialogBuilder.create().show();

    }


//    private void multiSelect(int position) {
//       Reminder selectedReminder = reminderLAdapter.getItem(position);
//        if (selectedReminder != null) {
//            if (rAActionMode != null) {
//                int previousPosition = -1;
//                if (rASelectedPositions.size() > 0) {
//                    previousPosition = rASelectedPositions.get(0);
//                }
//                rASelectedPositions.clear();
//                rASelectedPositions.add(position);
//
//                reminderLAdapter.setSelectedPositions(previousPosition, rASelectedPositions);
//            }
//        }
//    }

    private void initializeReminderList() {
        Log.d(TAG, "initializeReminderList: ");

       /* Runnable initializeRListRunnable = new Runnable() {
            @Override
            public void run() {
                reminderList = remindersDatabaseAdapter.fetchReminders();
            }
        };
        Thread initializeRListThread = new Thread(initializeRListRunnable);
        initializeRListThread.start();
        try {
            initializeRListThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
    }

    private void addReminderToList(Reminder newReminder) {
        Log.d(TAG, "addReminderToList: ");
        int newReminderPosition = getNewReminderAddPosition(newReminder);
        reminderLAdapter.notifyDataSetChanged();

        if (newReminderPosition >= 0 && newReminderPosition <= (reminderList.size() - 1)) {
            reminderRecyclerView.scrollToPosition(newReminderPosition);
        }

    }

    private long getNowTIM() {
        Log.d(TAG, "getNowTIM: ");

        Date nowDate = new Date();
        long nowTIM = nowDate.getTime();
        return nowTIM;
    }

    private void deleteReminderListItem(int reminderPosition) {
        Log.d(TAG, "deleteReminderListItem: ");

        reminderList.remove(reminderPosition);
        reminderLAdapter.notifyItemRemoved(reminderPosition);
        setNextReminderAlarm();
    }

    private void updateReminderListItem(String reminderTitle, String reminderDTS, long reminderTIM, int reminderPosition, String stock) {
        Log.d(TAG, "updateReminderListItem: ");

        String reminderDOF = reminderDTS.substring(7, 17);
        String reminderTOF = reminderDTS.substring(0, 5);

        reminderList.get(reminderPosition).setReminderTitle(reminderTitle);
        reminderList.get(reminderPosition).setReminderDOF(reminderDOF);
        reminderList.get(reminderPosition).setReminderTOF(reminderTOF);
        reminderList.get(reminderPosition).setReminderTIM(reminderTIM);
        reminderList.get(reminderPosition).setStock(stock);

        ReminderComparator reminderComparator = new ReminderComparator();
        Collections.sort(reminderList, reminderComparator);

        reminderLAdapter.notifyDataSetChanged();
    }

    private void setRAEmptyState() {
        Log.d(TAG, "setRAEmptyState: ");

        if (reminderList.size() == 0) {

            if (reminderRecyclerView.getVisibility() == View.VISIBLE) {
                reminderRecyclerView.setVisibility(GONE);
            }

            if (rAESLinearLayout.getVisibility() == GONE) {
                rAESLinearLayout.setVisibility(View.VISIBLE);

                String rAESTitle = getResources().getString(R.string.ra_empty_state_title);
                String rAESText = getResources().getString(R.string.ra_empty_state_text);

                rAESTitleTextView.setText(rAESTitle);
                rAESContentTextView.setText(rAESText);
            }


        } else {

            if (rAESLinearLayout.getVisibility() == View.VISIBLE) {
                rAESLinearLayout.setVisibility(GONE);
            }

            if (reminderRecyclerView.getVisibility() == GONE) {
                reminderRecyclerView.setVisibility(View.VISIBLE);
            }

        }


    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        return false;
    }

//    @Override
//    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
//        MenuInflater inflater = mode.getMenuInflater();
//        inflater.inflate(R.menu.ra_action_view_menu, menu);
//        return true;
//    }

//    @Override
//    public boolean onActionItemClicked(ActionMode mode, MenuItem menuItem) {
//        switch (menuItem.getItemId()) {
//            case R.id.ra_action_copy:
//                if (rASelectedPositions.size() > 0) {
//
//                    String selectedReminderTitle = reminderList.get(rASelectedPositions.get(0)).getReminderTitle();
//
//                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
//                    ClipData clip = ClipData.newPlainText("l", selectedReminderTitle);
//                    clipboard.setPrimaryClip(clip);
//                }
//
//                rAActionMode.finish();
//
//                String rACopyConfirmationText = getResources().getString(R.string.ra_copy_confirmation_text);
//
//                Snackbar.make(rARootLayout,rACopyConfirmationText,Snackbar.LENGTH_SHORT).show();
//
//                return true;
//
//            case R.id.ra_action_edit:
//
//                if (rASelectedPositions.size() > 0) {
//                    int selectedPosition = rASelectedPositions.get(0);
//                    openEditDialog(selectedPosition);
//                }
//
//                rAActionMode.finish();
//                return true;
//
//
//            case R.id.ra_action_delete:
//                AlertDialog.Builder deleteRDialogBuilder = new AlertDialog.Builder(this);
//                deleteRDialogBuilder.setTitle(getResources().getString(R.string.delete_reminder_dialog_title));
//                deleteRDialogBuilder.setMessage(getResources().getString(R.string.delete_reminder_dialog_message));
//                deleteRDialogBuilder.setNegativeButton(getResources().getString(R.string.del_reminder_dialog_negative_button), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        dialogInterface.dismiss();
//                        rAActionMode.finish();
//                    }
//                });
//
//                deleteRDialogBuilder.setPositiveButton(getResources().getString(R.string.delete_reminder_dialog_positive_button), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//
//                        if (rASelectedPositions.size() > 0) {
//
//                            int selectedPosition = rASelectedPositions.get(0);
//                            final int sReminderId = reminderList.get(selectedPosition).getReminderId();
//
//                            Runnable deleteRRunnable = new Runnable() {
//                                @Override
//                                public void run() {
//                                    remindersDatabaseAdapter.deleteReminder(sReminderId);
//                                }
//                            };
//                            Thread deleteRThread = new Thread(deleteRRunnable);
//                            deleteRThread.start();
//                            try {
//                                deleteRThread.join();
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//
//                            deleteReminderListItem(selectedPosition);
//                            setRAEmptyState();
//                        }
//
//                        dialogInterface.dismiss();
//
//                        rAActionMode.finish();
//                    }
//                });
//                deleteRDialogBuilder.create().show();
//
//                return true;
//
//
//            default:
//
//        }
//        return false;
//    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        Log.d(TAG, "onDestroyActionMode: ");

        rAActionButton.show();
//        rAActionMode = null;
        rAIsMultiSelect = false;

        int previousPosition = -1;
        if (rASelectedPositions.size() > 0) {
            previousPosition = rASelectedPositions.get(0);
        }
        rASelectedPositions = new ArrayList<>();

        reminderLAdapter.setSelectedPositions(previousPosition, new ArrayList<Integer>());
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.rem_activity_options_menu,menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.rem_activity_help:
//                openHelp();
//                return true;
//
//            case R.id.rem_activity_sa:
//                shareApp();
//                return true;
//
//            default:
//                return false;
//        }
//    }

    protected void shareApp() {
        Intent sAIntent = new Intent();
        sAIntent.setAction(Intent.ACTION_SEND);
        sAIntent.putExtra(Intent.EXTRA_TEXT, "Reminder App is a fast and simple app that I use to schedule tasks. Get it from: https://play.google.com/store/apps/details?id=" + getPackageName());
        sAIntent.setType("text/plain");
        Intent.createChooser(sAIntent, "Share via");
        startActivity(sAIntent);
    }

    protected void openHelp() {
        Intent remToHelpActivityIntent = new Intent(StockRemindersActivity.this, HelpActivity.class);
        startActivity(remToHelpActivityIntent);
    }

    protected static class ReminderComparator implements Comparator<Reminder> {

        public int compare(Reminder reminderOne, Reminder reminderTwo) {
            if (reminderOne.getReminderTIM() == reminderTwo.getReminderTIM()) {
                return 0;
            } else if (reminderOne.getReminderTIM() > reminderTwo.getReminderTIM()) {
                return 1;
            } else {
                return -1;
            }
        }


    }

}


