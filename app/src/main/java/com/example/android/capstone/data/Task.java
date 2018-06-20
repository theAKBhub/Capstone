package com.example.android.capstone.data;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.example.android.capstone.data.TaskContract.TaskEntry;

/**
 * A {@link Task} object that contains details related to a single Task item
 */

public class Task implements Parcelable {

    private long mTaskId;                        // ID of task item
    private String mTaskTitle;                  // e.g. Participate in marathon
    private String mCategory;                   // e.g. Health, Business, Personal, etc.
    private int mPriority;                      // 1 (high), 2 (Medium), 3 (Low), 0 (No priority)
    private String mExtraInfoType;              // Location OR Barcode OR Phone OR Email
    private String mExtraInfo;                  // e.g. location address
    private String mDueDate;                    // e.g. 2018-07-10
    private String mDueTime;                    // 09:30
    private int mTagRepeat;                     // 1 if Task is to be repeated, 0 if not
    private String mRepeatFrequency;            // D - daily, W - Weekly, M = Monthly
    private int mTagCompleted;                  // 1 if Task is completed, 0 if not
    private String mDateAdded;                  // e.g. 2018-06-05
    private String mDateCompleted;              // e.g. 2018-07-10 if task completed, else null

    /**
     * Empty Constructor
     */
    public Task() {
    }

    /**
     * Default Constructor - Constructs a new {@link Task} object
     * Scope for this constructor is private so CREATOR can access it
     * @param parcel - Parcel object
     */
    private Task(Parcel parcel) {
        mTaskId = parcel.readLong();
        mTaskTitle = parcel.readString();
        mCategory = parcel.readString();
        mPriority = parcel.readInt();
        mExtraInfoType = parcel.readString();
        mExtraInfo = parcel.readString();
        mDueDate = parcel.readString();
        mDueTime = parcel.readString();
        mTagRepeat = parcel.readInt();
        mRepeatFrequency = parcel.readString();
        mTagCompleted = parcel.readInt();
        mDateAdded = parcel.readString();
        mDateCompleted = parcel.readString();
    }

    public static final Creator<Task> CREATOR = new Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mTaskId);
        dest.writeString(mTaskTitle);
        dest.writeString(mCategory);
        dest.writeInt(mPriority);
        dest.writeString(mExtraInfoType);
        dest.writeString(mExtraInfo);
        dest.writeString(mDueDate);
        dest.writeString(mDueTime);
        dest.writeInt(mTagRepeat);
        dest.writeString(mRepeatFrequency);
        dest.writeInt(mTagCompleted);
        dest.writeString(mDateAdded);
        dest.writeString(mDateCompleted);
    }

    /**
     * Getter and Setter methods for class Recipe
     */
    public long getTaskId() {
        return mTaskId;
    }

    public void setTaskId(long taskId) {
        mTaskId = taskId;
    }

    public String getTaskTitle() {
        return mTaskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        mTaskTitle = taskTitle;
    }

    public String getCategory() {
        return mCategory;
    }

    public void setCategory(String category) {
        mCategory = category;
    }

    public int getPriority() {
        return mPriority;
    }

    public void setPriority(int priority) {
        mPriority = priority;
    }

    public String getExtraInfoType() {
        return mExtraInfoType;
    }

    public void setExtraInfoType(String extraInfoType) {
        mExtraInfoType = extraInfoType;
    }

    public String getExtraInfo() {
        return mExtraInfo;
    }

    public void setExtraInfo(String extraInfo) {
        mExtraInfo = extraInfo;
    }

    public String getDueDate() {
        return mDueDate;
    }

    public void setDueDate(String dueDate) {
        mDueDate = dueDate;
    }

    public String getDueTime() {
        return mDueTime;
    }

    public void setDueTime(String dueTime) {
        mDueTime = dueTime;
    }

    public int getTagRepeat() {
        return mTagRepeat;
    }

    public void setTagRepeat(int tagRepeat) {
        mTagRepeat = tagRepeat;
    }

    public String getRepeatFrequency() {
        return mRepeatFrequency;
    }

    public void setRepeatFrequency(String repeatFrequency) {
        mRepeatFrequency = repeatFrequency;
    }

    public int getTagCompleted() {
        return mTagCompleted;
    }

    public void setTagCompleted(int tagCompleted) {
        mTagCompleted = tagCompleted;
    }

    public String getDateAdded() {
        return mDateAdded;
    }

    public void setDateAdded(String dateAdded) {
        mDateAdded = dateAdded;
    }

    public String getDateCompleted() {
        return mDateCompleted;
    }

    public void setDateCompleted(String dateCompleted) {
        mDateCompleted = dateCompleted;
    }

    /**
     * Method to create and populate a Task object from the cursor data
     * This task parcelable object will be used to pass between activities
     * @param cursor
     * @return {@link Task} object
     */
    public Task getTaskObject(Cursor cursor) {
        Task task = new Task();

        task.setTaskId(cursor.getLong(cursor.getColumnIndex(TaskEntry._ID)));
        task.setTaskTitle(cursor.getString(cursor.getColumnIndex(TaskEntry.COLUMN_TASK_TITLE)));
        task.setCategory(cursor.getString(cursor.getColumnIndex(TaskEntry.COLUMN_CATEGORY)));
        task.setPriority(cursor.getInt(cursor.getColumnIndex(TaskEntry.COLUMN_PRIORITY)));
        task.setExtraInfoType(cursor.getString(cursor.getColumnIndex(TaskEntry.COLUMN_EXTRA_INFO_TYPE)));
        task.setExtraInfo(cursor.getString(cursor.getColumnIndex(TaskEntry.COLUMN_EXTRA_INFO)));
        task.setDueDate(cursor.getString(cursor.getColumnIndex(TaskEntry.COLUMN_DUE_DATE)));
        task.setDueTime(cursor.getString(cursor.getColumnIndex(TaskEntry.COLUMN_DUE_TIME)));
        task.setTagRepeat(cursor.getInt(cursor.getColumnIndex(TaskEntry.COLUMN_TAG_REPEAT)));
        task.setRepeatFrequency(cursor.getString(cursor.getColumnIndex(TaskEntry.COLUMN_REPEAT_FREQUENCY)));
        task.setTagCompleted(cursor.getInt(cursor.getColumnIndex(TaskEntry.COLUMN_TAG_COMPLETED)));
        task.setDateAdded(cursor.getString(cursor.getColumnIndex(TaskEntry.COLUMN_DATE_ADDED)));
        task.setDateCompleted(cursor.getString(cursor.getColumnIndex(TaskEntry.COLUMN_DATE_COMPLETED)));

        return task;
    }
}
