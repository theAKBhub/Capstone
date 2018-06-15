package com.acubed.android.taskmate.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A {@link Task} object that contains details related to a single Task item
 */

public class Task implements Parcelable {

    private int mTaskId;                        // ID of task item
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
        mTaskId = parcel.readInt();
        mTaskTitle = parcel.readString();
        mCategory = parcel.readString();
        mPriority = parcel.readInt();
        mExtraInfoType = parcel.readString();
        mExtraInfo = parcel.readString();
        mDueDate = parcel.readString();
        mDueTime = parcel.readString();
        mTagRepeat = parcel.readInt();
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
        dest.writeInt(mTaskId);
        dest.writeString(mTaskTitle);
        dest.writeString(mCategory);
        dest.writeInt(mPriority);
        dest.writeString(mExtraInfoType);
        dest.writeString(mExtraInfo);
        dest.writeString(mDueDate);
        dest.writeString(mDueTime);
        dest.writeInt(mTagRepeat);
        dest.writeInt(mTagCompleted);
        dest.writeString(mDateAdded);
        dest.writeString(mDateCompleted);
    }

    /**
     * Getter and Setter methods for class Recipe
     */
    public int getTaskId() {
        return mTaskId;
    }

    public void setTaskId(int taskId) {
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
}
