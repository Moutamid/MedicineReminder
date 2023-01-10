package com.medicine.reminderapp;

import static android.view.View.GONE;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fxn.stash.Stash;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.BuildConfig;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class RemindersActivity extends AppCompatActivity implements ActionMode.Callback, ReminderInterface {
    private static final String TAG = "ABC RemindersActivity";
    private List<Reminder> reminderList = new ArrayList<>();
    private RecyclerView reminderRecyclerView;
    private ReminderAdapter reminderLAdapter;
    private ReminderDatabaseAdapter remindersDatabaseAdapter;
    private boolean rAIsMultiSelect = false;
    private List<Integer> rASelectedPositions = new ArrayList<>();
    private FloatingActionButton rAActionButton;
    protected TextView rAESTitleTextView;
    protected TextView rAESContentTextView;
    protected LinearLayout rAESLinearLayout;
    View rARootLayout;

    private NavigationView navigationView;
    ImageView menuImage;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);
        Log.d(TAG, "onCreate: started");

        rARootLayout = findViewById(R.id.ch_root_layout);

        remindersDatabaseAdapter = new ReminderDatabaseAdapter(this);
        remindersDatabaseAdapter.open();


        navigationView = findViewById(R.id.nav_view);
        menuImage = findViewById(R.id.imageView_menu);


        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);


        // this is the menu image to open Navigation Drawer.

        menuImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(GravityCompat.START);
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Log.d(TAG, "onNavigationItemSelected: ");
                switch (item.getItemId()) {
                    case R.id.home:
                        drawer.closeDrawer(GravityCompat.START);
                        return true;
                    case R.id.stock_remind:
                        Intent intent = new Intent(RemindersActivity.this, StockRemindersActivity.class);
                        startActivity(intent);
                        drawer.closeDrawer(GravityCompat.START);
                        return true;
//
                    case R.id.share:
//
                        try {
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("text/plain");
                            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "ZamView");
                            String shareMessage = "\nHey check out my app at:\n\n";
                            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + getPackageName() + "\n\n";
                            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                            startActivity(Intent.createChooser(shareIntent, "choose one"));
                        } catch (Exception e) {
                            //e.toString();
                        }
                        return true;

                    case R.id.logout:
                        // on click logout a alert dialog show to ask that you want to logout or not.

                        androidx.appcompat.app.AlertDialog alertDialog = new androidx.appcompat.app.AlertDialog.Builder(RemindersActivity.this)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Are you sure you want to Logout")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //set what would happen when positive button is clicked
                                        FirebaseAuth auth = FirebaseAuth.getInstance();
                                        auth.signOut();

                                        SharedPreferences pref = getSharedPreferences("MyPref", 0);
                                        SharedPreferences.Editor editor = pref.edit();
                                        editor.putString("login", "no");
                                        editor.commit();
                                        Intent intent = new Intent(RemindersActivity.this, SelectionActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);

                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //set what should happen when negative button is clicked


                                    }
                                })
                                .show();
                        return true;

                    default:
                        return false;
                }
            }
        });


        initializeReminderList();

        reminderRecyclerView = (RecyclerView) findViewById(R.id.reminders_recycler_view);
        rAESTitleTextView = findViewById(R.id.ra_empty_state_title_text_view);
        rAESContentTextView = findViewById(R.id.ra_empty_state_text_view);
        rAESLinearLayout = findViewById(R.id.ra_empty_state_linear_layout);

        reminderLAdapter = new ReminderAdapter(this, reminderList, this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        reminderRecyclerView.setLayoutManager(layoutManager);
        reminderRecyclerView.setItemAnimator(new DefaultItemAnimator());

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
        ReminderFSD newReminderFSD = ReminderFSD.newInstance(0, "", "", "", 0, 0, false);
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


                if (lReminderTIM >= nowTIM) {

                    int newCode = new Random().nextInt(9999);
                    int oldCode = Stash.getInt(latestReminder.getReminderTitle(), newCode);

                    if (oldCode == newCode){
                        Stash.put(latestReminder.getReminderTitle(), newCode);
                        oldCode = newCode;
                    }

                    Intent rARIntent = new Intent(this, NotificationReceiver.class);
                    rARIntent.putExtra("lReminderTitle", latestReminder.getReminderTitle());
                    rARIntent.putExtra("isStock", false);
                    rARIntent.putExtra("code", oldCode);
                    PendingIntent rARPIntent = PendingIntent.getBroadcast(this, oldCode, rARIntent, PendingIntent.FLAG_IMMUTABLE);
                    AlarmManager rAAlarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

                    if (Build.VERSION.SDK_INT >= 23) {
                        rAAlarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, lReminderTIM, rARPIntent);
                    } else if (Build.VERSION.SDK_INT >= 19) {
                        rAAlarmManager.setExact(AlarmManager.RTC_WAKEUP, lReminderTIM, rARPIntent);
                    } else {
                        rAAlarmManager.set(AlarmManager.RTC_WAKEUP, lReminderTIM, rARPIntent);
                    }
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
        long reminderTIM = selectedReminder.getReminderTIM();

        FragmentManager openERFSD = getSupportFragmentManager();
        ReminderFSD editReminderFSD = ReminderFSD.newInstance(reminderId, reminderTitle, reminderDOF, reminderTOF, reminderTIM, reminderPosition, true);
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
        Log.d(TAG, "hideActionBar: ");
    }

    @Override
    public void showActionBar() {

    }

    public void addReminder(final String reminderTitle, final String reminderDTS, final long reminderTIM, final String stock) {
        Log.d(TAG, "addReminder: ");
        final int[] newReminderId = new int[1];

        Runnable addReminderRunnable = new Runnable() {
            @Override
            public void run() {
                newReminderId[0] = remindersDatabaseAdapter.createReminder(reminderTitle, reminderDTS);

            }
        };
        Thread addReminderThread = new Thread(addReminderRunnable);
        addReminderThread.start();
        try {
            addReminderThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int reminderId = newReminderId[0];
        String reminderTOF = reminderDTS.substring(0, 5);
        Reminder newReminder = new Reminder(reminderId, reminderTitle, "", reminderTOF, reminderTIM, "");

        addReminderToList(newReminder);

        setRAEmptyState();
        setNextReminderAlarm();

        Snackbar.make(rARootLayout, R.string.reminder_set_c, Snackbar.LENGTH_SHORT).show();
    }

    public void updateReminder(final String reminderTitle, final String reminderDTS, final long reminderTIM, final int reminderId, final int reminderPosition, final String stock) {
        Log.d(TAG, "updateReminder: ");
        Runnable updateReminderRunnable = new Runnable() {
            @Override
            public void run() {
                remindersDatabaseAdapter.updateReminder(reminderId, reminderTitle, reminderDTS);
            }
        };
        Thread updateReminderThread = new Thread(updateReminderRunnable);
        updateReminderThread.start();
        try {
            updateReminderThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        updateReminderListItem(reminderTitle, reminderDTS, reminderTIM, reminderPosition);

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
            }
        });

        deleteRDialogBuilder.setPositiveButton(getResources().getString(R.string.delete_reminder_dialog_positive_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.d(TAG, "onClick: ");
//                        if (rASelectedPositions.size() > 0) {

//                            int selectedPosition = rASelectedPositions.get(0);
                final int sReminderId = reminderList.get(reminderPosition).getReminderId();

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
                }

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
        Runnable initializeRListRunnable = new Runnable() {
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
        }
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

    private void updateReminderListItem(String reminderTitle, String reminderDTS, long reminderTIM, int reminderPosition) {
        Log.d(TAG, "updateReminderListItem: ");
        String reminderTOF = reminderDTS.substring(0, 5);

        reminderList.get(reminderPosition).setReminderTitle(reminderTitle);
        reminderList.get(reminderPosition).setReminderDOF("");
        reminderList.get(reminderPosition).setReminderTOF(reminderTOF);
        reminderList.get(reminderPosition).setReminderTIM(reminderTIM);

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
        Intent remToHelpActivityIntent = new Intent(RemindersActivity.this, HelpActivity.class);
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


