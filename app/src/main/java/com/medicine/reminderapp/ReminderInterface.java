package com.medicine.reminderapp;

public interface ReminderInterface {

    public void hideActionBar();
    public void showActionBar();
    public void addReminder(final String reminderTitle, final String reminderDTS, final long reminderTIM ,final String stock);
    public void updateReminder(final String reminderTitle, final String reminderDTS, final long reminderTIM, final int reminderId, final int reminderPosition,final String stock);
    public void editClick(int reminderPosition);
    public void deleteClick(int reminderPosition);

}
