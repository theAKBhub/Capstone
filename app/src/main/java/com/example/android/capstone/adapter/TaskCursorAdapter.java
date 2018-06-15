package com.example.android.capstone.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import butterknife.BindColor;
import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.example.android.capstone.R;
import com.example.android.capstone.data.TaskContract.TaskEntry;

/**
 * CursorAdapter class that is used to display relevant Task details in the RecyclerView
 */
public class TaskCursorAdapter extends RecyclerView.Adapter<TaskCursorAdapter.TaskViewHolder> {

    private Cursor mCursor;
    private Context mContext;

    /**
     * Default Constructor to initialize the Context
     *
     * @param mContext the current Context
     */
    public TaskCursorAdapter(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * Method called when ViewHolders are created to fill a RecyclerView
     *
     * @return A new TaskViewHolder that holds the view for each task
     */
    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // Inflate the task_layout to a view
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_task_list, parent, false);

        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskCursorAdapter.TaskViewHolder holder, int position) {

        // get column indices
        int idIndex = mCursor.getColumnIndex(TaskEntry._ID);
        int titleIndex = mCursor.getColumnIndex(TaskEntry.COLUMN_TASK_TITLE);
        int categoryIndex = mCursor.getColumnIndex(TaskEntry.COLUMN_CATEGORY);
        int completedIndex = mCursor.getColumnIndex(TaskEntry.COLUMN_TAG_COMPLETED);
        int dateIndex = mCursor.getColumnIndex(TaskEntry.COLUMN_DUE_DATE);
        int timeIndex = mCursor.getColumnIndex(TaskEntry.COLUMN_DUE_TIME);
        int priorityIndex = mCursor.getColumnIndex(TaskEntry.COLUMN_PRIORITY);

        // move cursor to desired position
        mCursor.moveToPosition(position);

        // get values from cursor at that position
        long taskId = mCursor.getInt(idIndex);
        String taskTitle = mCursor.getString(titleIndex);
        String taskCategory = mCursor.getString(categoryIndex);
        int completed = mCursor.getInt(completedIndex);
        String dueDate = mCursor.getString(dateIndex);
        String dueTime = mCursor.getString(timeIndex);
        int priority = mCursor.getInt(priorityIndex);

        // set values to respective views
        holder.itemView.setTag(taskId);
        holder.textviewTaskTitle.setText(taskTitle);
        holder.textviewTaskCategory.setText(taskCategory);
        holder.textviewDueDate.setText(dueDate);
        holder.textviewDueTime.setText(dueTime);

        Drawable resourceId;
        resourceId = (completed == 1) ? holder.squareChecked : holder.squareEmpty;
        holder.buttonCompletedCheck.setImageDrawable(resourceId);

        switch (priority) {
            case 1:
                holder.viewPriority.setBackgroundColor(holder.colorRed);
                break;
            case 2:
                holder.viewPriority.setBackgroundColor(holder.colorAmber);
                break;
            case 3:
                holder.viewPriority.setBackgroundColor(holder.colorGreen);
                break;
            case 0:
            default:
                holder.viewPriority.setBackgroundColor(holder.colorWhite);
                break;
        }
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

    /**
     * When data changes and a re-query occurs, this function swaps the old Cursor
     * with a newly updated Cursor (Cursor newCursor) that is passed in.
     */
    public Cursor swapCursor(Cursor newCursor) {
        // check if this cursor is the same as the previous cursor (mCursor)
        if (mCursor == newCursor) {
            return null; // bc nothing has changed
        }
        Cursor tempCursor = mCursor;
        this.mCursor = newCursor; // new cursor value assigned

        //check if this is a valid cursor, then update the cursor
        if (newCursor != null) {
            this.notifyDataSetChanged();
        }
        return tempCursor;
    }

    /**
     * Inner class for creating ViewHolders
     */
    public class TaskViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.textview_task_title)
        TextView textviewTaskTitle;
        @BindView(R.id.textview_task_category)
        TextView textviewTaskCategory;
        @BindView(R.id.textview_task_date)
        TextView textviewDueDate;
        @BindView(R.id.textview_task_time)
        TextView textviewDueTime;
        @BindView(R.id.button_check)
        ImageButton buttonCompletedCheck;
        @BindView(R.id.view_priority)
        View viewPriority;

        @BindColor(R.color.colorRed)
        int colorRed;
        @BindColor(R.color.colorAmber)
        int colorAmber;
        @BindColor(R.color.colorAccent)
        int colorGreen;
        @BindColor(R.color.colorWhite)
        int colorWhite;

        @BindDrawable(R.drawable.ic_square)
        Drawable squareEmpty;
        @BindDrawable(R.drawable.ic_square_checked)
        Drawable squareChecked;

        /**
         * Constructor for the TaskViewHolders.
         *
         * @param itemView The view inflated in onCreateViewHolder
         */
        public TaskViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
        }
    }
}
