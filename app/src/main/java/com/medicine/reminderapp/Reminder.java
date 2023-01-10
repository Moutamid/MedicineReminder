package com.medicine.reminderapp;

public class Reminder {
    private int reminderId;
    private String reminderTitle;
    private String reminderDOF;
    private String reminderTOF;
    private long reminderTIM;
    private String stock;


    public Reminder(int reminderId, String reminderTitle, String reminderDOF, String reminderTOF, long reminderTIM, String stock) {
        this.reminderId = reminderId;
        this.reminderTitle = reminderTitle;
        this.reminderDOF = reminderDOF;
        this.reminderTOF = reminderTOF;
        this.reminderTIM = reminderTIM;
        this.stock = stock;
    }

    public int getReminderId() {
        return reminderId;
    }

    public void setReminderId(int reminderId) {
        this.reminderId = reminderId;
    }

    public String getReminderTitle() {
        return reminderTitle;
    }

    public void setReminderTitle(String reminderTitle) {
        this.reminderTitle = reminderTitle;
    }

    public String getReminderDOF() {
        return reminderDOF;
    }

    public void setReminderDOF(String reminderDOF) {
        this.reminderDOF = reminderDOF;
    }

    public String getReminderTOF() {
        return reminderTOF;
    }

    public void setReminderTOF(String reminderTOF) {
        this.reminderTOF = reminderTOF;
    }

    public long getReminderTIM() {
        return reminderTIM;
    }

    public void setReminderTIM(long reminderTIM) {
        this.reminderTIM = reminderTIM;
    }

    public String getStock() {
        return stock;
    }

    public void setStock(String stock) {
        this.stock = stock;
    }
}
