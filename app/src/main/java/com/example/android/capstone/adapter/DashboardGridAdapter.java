package com.example.android.capstone.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.example.android.capstone.R;
import com.example.android.capstone.helper.Constants;

/**
 * Adapter to display the dashboard panels
 */
public class DashboardGridAdapter extends BaseAdapter {

    private Context mContext;
    private int[] mTaskCounts;
    private String[] mTaskHeadings;
    private int mGridHeight;

    /**
     * Default constructor
     */
    public DashboardGridAdapter(Context context, int[] taskCounts, String[] taskHeadings, int gridHeight) {
        mContext = context;
        mTaskCounts = taskCounts;
        mTaskHeadings = taskHeadings;
        mGridHeight = gridHeight;
    }

    @Override
    public int getCount() {
        return mTaskCounts.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DashboardViewHolder holder;
        View gridView;
        int count;

        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView != null) {
            holder = (DashboardViewHolder) convertView.getTag();
            gridView = convertView;
        } else {
            gridView = new GridView(mContext);
            gridView = layoutInflater.inflate(R.layout.item_grid, null);
            holder = new DashboardViewHolder(gridView);
            gridView.setTag(holder);

            holder.textviewTaskHeading.setText(getGridPanelHeading(position));
            holder.textviewTaskCount.setText(String.valueOf(mTaskCounts[position]));
        }

        if (holder.gridCols == 2) {
            // portrait mode - divide screen space in 3 rows
            gridView.setMinimumHeight(mGridHeight / 3);
        } else if (holder.gridCols == 3) {
            // landscape mode - divide screen space in 2 rows
            gridView.setMinimumHeight(mGridHeight / 2);
        }

        return gridView;
    }

    /**
     * This class describes the view items to create a list item
     */
    public static class DashboardViewHolder {

        @BindView(R.id.textview_task_heading)
        TextView textviewTaskHeading;
        @BindView(R.id.textview_task_count)
        TextView textviewTaskCount;
        @BindInt(R.integer.grid_cols)
        int gridCols;

        public DashboardViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }

    /**
     * Formats Dashboard panel headings before displaying
     * @param position
     * @return heading
     */
    private String getGridPanelHeading(int position) {
        switch (position) {
            case 0:
                return mTaskHeadings[position] + " " + Constants.HDG_TASKS;

            case 1:
            case 2:
            case 3:
            case 5:
                return Constants.HDG_TASKS + " " + mTaskHeadings[position];

            case 4:
                return Constants.HDG_TASKS + " in " + mTaskHeadings[position];

            default:
                return null;

        }
    }
}
