package com.example.android.capstone.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import butterknife.BindColor;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.example.android.capstone.R;
import com.example.android.capstone.barcode.BarcodeCaptureActivity;
import com.example.android.capstone.data.Task;
import com.example.android.capstone.data.TaskContract.TaskEntry;
import com.example.android.capstone.helper.Constants;
import com.example.android.capstone.helper.Utils;
import com.example.android.capstone.widget.WidgetIntentService;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.vision.barcode.Barcode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import timber.log.Timber;


public class TaskEditActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,
        DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener, OnClickListener {

    private static final String STATE_DUE_DATE = "state_due_date";
    private static final String STATE_DUE_TIME = "state_due_time";
    private static final String STATE_EXTRA_INFO_DISP = "state_extra_info_disp";
    private static final String STATE_EXTRA_INFO = "state_extra_info";
    private static final String STATE_EXTRA_INFO_TYPE = "state_extra_info_type";
    private static final String STATE_NEW_TASK = "state_new_task";

    final Context mContext = this;
    private Uri mCurrentUri;
    private String mToolbarTitle;
    private boolean mIsNewTask;
    private Task mTask;
    private Toast mToast;

    @BindView(R.id.appbar_taskedit)
    AppBarLayout mAppbarTaskEdit;
    @BindView(R.id.toolbar_taskedit)
    Toolbar mToolbarTaskEdit;
    @BindView(R.id.edittext_task_title)
    EditText mEditTextTaskTitle;

    @BindView(R.id.spinner_task_category)
    Spinner mSpinnerCategory;
    @BindView(R.id.spinner_task_priority)
    Spinner mSpinnerPriority;
    @BindView(R.id.spinner_repeat)
    Spinner mSpinnerRepeat;
    @BindView(R.id.spinner_extra_info)
    Spinner mSpinnerExtraInfoType;

    @BindView(R.id.button_due_date)
    ImageButton mButtonPickDate;
    @BindView(R.id.button_due_time)
    ImageButton mButtonPickTime;
    @BindView(R.id.textview_due_date)
    TextView mTextViewDate;
    @BindView(R.id.textview_due_time)
    TextView mTextViewTime;
    @BindView(R.id.textview_error_msg)
    TextView mTextViewError;
    @BindView(R.id.textview_extra_info)
    TextView mTextViewExtraInfo;
    @BindView(R.id.textview_alert_msg)
    TextView mTextViewAlert;

    @BindColor(R.color.colorPrimary)
    int mColorPrimary;
    @BindColor(R.color.colorWhite)
    int mColorWhite;
    @BindString(R.string.error_save_msg)
    String mSaveErrorMessage;
    @BindString(R.string.info_save_success)
    String mSaveSuccessMessage;
    @BindString(R.string.errormsg_missing_title)
    String mErrorTitle;
    @BindString(R.string.errormsg_missing_category)
    String mErrorCategory;
    @BindString(R.string.errormsg_missing_frequency)
    String mErrorRepeatFrequency;
    @BindString(R.string.errormsg_missing_due_date)
    String mErrorRepeatDueDate;
    @BindString(R.string.errormsg_missing_extra)
    String mErrorExtraInfo;

    /**
     * Variables to hold UI data
     */
    private long mTaskId;
    private String mTaskTitle;
    private String mTaskCateogory;
    private int mIndexCategorySpinner;
    private int mPriority;
    private String mPriorityText;
    private String mDateDue;
    private String mTimeDue;
    private int mTagRepeat;
    private String mRepeatFrequency;
    private int mTagCompleted;
    private int mExtraInfoTypeIndex;
    private String mExtraInfoType;
    private String mExtraInfo;
    private String mExtraInfoDisplay;
    private String mDateAdded;

    private boolean mIsRestoredInstance;
    private boolean mIsUserInteracting;
    private MediaPlayer mMediaPlayer;


    // Boolean flag that keeps track of whether the product has been edited (true) or not (false)
    private boolean mItemHasChanged = false;

    // OnTouchListener that listens for any user touches on a View, implying that they are modifying view
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_edit);
        ButterKnife.bind(this);

        // Prepare Toolbar widget
        setupToolbar();

        // Prepare views
        initViews();

        // Receive Intent Extras
        if (savedInstanceState == null) {
            getIntentData();
            mIsRestoredInstance = false;
        } else {
            mIsRestoredInstance = true;
        }

        // Setup Spinners
        setupSpinners();

        if (!mIsNewTask) {
            populateUI();
        }

        // A tiny beep is played if barcode is scanned. This released the media when played.
        mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                if (mMediaPlayer != null) {
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                }
            }
        });

        mToolbarTaskEdit.setNavigationOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemHasChanged || hasEntry()) {
                    // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                    // Create a click listener to handle the user confirming that changes should be discarded.
                    DialogInterface.OnClickListener discardButtonClickListener =
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // User clicked "Discard" button, navigate to list
                                    returnToList();
                                }
                            };

                    // Show a dialog that notifies the user they have unsaved changes
                    showUnsavedChangesDialog(discardButtonClickListener);
                } else {
                    finish();
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_DUE_DATE, mDateDue);
        outState.putString(STATE_DUE_TIME, mTimeDue);
        outState.putString(STATE_EXTRA_INFO, mExtraInfo);
        outState.putString(STATE_EXTRA_INFO_TYPE, mExtraInfoType);
        outState.putString(STATE_EXTRA_INFO_DISP, mExtraInfoDisplay);
        outState.putBoolean(STATE_NEW_TASK, mIsNewTask);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            mDateDue = savedInstanceState.getString(STATE_DUE_DATE);
            mTimeDue = savedInstanceState.getString(STATE_DUE_TIME);
            mExtraInfo = savedInstanceState.getString(STATE_EXTRA_INFO);
            mExtraInfoType = savedInstanceState.getString(STATE_EXTRA_INFO_TYPE);
            mExtraInfoDisplay = savedInstanceState.getString(STATE_EXTRA_INFO_DISP);
            mIsNewTask = savedInstanceState.getBoolean(STATE_NEW_TASK);

            displayDate();
            displayTime();
            displayExtraInfo();
        }
    }

    /**
     * Method to customize the Toolbar widget
     */
    private void setupToolbar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mAppbarTaskEdit.setElevation(4);
        }

        setSupportActionBar(mToolbarTaskEdit);

        mToolbarTitle = getString(R.string.title_taskedit);

        try {
            getSupportActionBar().setTitle(mToolbarTitle);
        } catch (NullPointerException ne) {
            Timber.e(ne.getMessage());
        }

        if (mToolbarTaskEdit != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Method to receive Intent Extra Data, which is the URI, and amend title for Toolbar for new or existing task
     */
    private void getIntentData() {

        if (getIntent().getExtras() == null || (mIsNewTask)) {
            mIsNewTask = true;
        } else {
            // edit existing task
            mIsNewTask = false;

            ArrayList<Task> tasks = getIntent().getParcelableArrayListExtra(Constants.INTENT_KEY_TASK);
            mTask = tasks.get(0);
            getTaskDetails();
        }
    }

    /**
     * Method to modify some views for display
     */
    private void initViews() {
        // Change Hint color of EditText
        mEditTextTaskTitle.setHintTextColor(mColorPrimary);

        // Set color of spinner arrow
        mSpinnerCategory.getBackground().setColorFilter(mColorPrimary, PorterDuff.Mode.SRC_ATOP);
        mSpinnerPriority.getBackground().setColorFilter(mColorPrimary, PorterDuff.Mode.SRC_ATOP);
        mSpinnerRepeat.getBackground().setColorFilter(mColorPrimary, PorterDuff.Mode.SRC_ATOP);
        mSpinnerExtraInfoType.getBackground().setColorFilter(mColorPrimary, PorterDuff.Mode.SRC_ATOP);

        mMediaPlayer = MediaPlayer.create(mContext, R.raw.beep);
    }

    /**
     * Method to set up Spinners
     */
    private void setupSpinners() {

        // Category spinner
        ArrayAdapter<String> adapterCategory = new ArrayAdapter<>(this, R.layout.item_spinner_edit,
                Utils.getTaskCategories(mContext));
        mSpinnerCategory.setAdapter(adapterCategory);

        // Priority spinner
        ArrayAdapter<String> adapterPriority = new ArrayAdapter<>(this, R.layout.item_spinner_edit,
                Utils.getTaskPriorities(mContext));
        mSpinnerPriority.setAdapter(adapterPriority);

        // Repeat spinner
        ArrayAdapter<String> adapterRepeat = new ArrayAdapter<>(this, R.layout.item_spinner_edit,
                Utils.getRepeatOptions(mContext));
        mSpinnerRepeat.setAdapter(adapterRepeat);

        // Repeat spinner
        ArrayAdapter<String> adapterExtraInfo = new ArrayAdapter<>(this, R.layout.item_spinner_edit,
                Utils.getExtraInfoOptions(mContext));
        mSpinnerExtraInfoType.setAdapter(adapterExtraInfo);

        // Set Listeners on Clickable Views
        mSpinnerCategory.setOnItemSelectedListener(this);
        mSpinnerPriority.setOnItemSelectedListener(this);
        mSpinnerRepeat.setOnItemSelectedListener(this);
        mSpinnerExtraInfoType.setOnItemSelectedListener(this);
        mButtonPickDate.setOnClickListener(this);
        mButtonPickTime.setOnClickListener(this);
    }

    /**
     * Inflate the menu options from the res/menu/menu_editor.xml file.
     *
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    /**
     * Method to add clear top flag so it doesn't create new instance of parent
     *
     * @return intent
     */
    @Override
    public Intent getSupportParentActivityIntent() {
        Intent intent = super.getSupportParentActivityIntent();
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        return intent;
    }

    /**
     * Method to handle actions when individual menu item is clicked
     *
     * @return true/false
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                if (mIsNewTask) {
                    addTask();
                } else {
                    updateTask();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.spinner_task_category:
                mIndexCategorySpinner = position;
                if (position > 0) {
                    mTaskCateogory = parent.getItemAtPosition(position).toString();
                }
                break;

            case R.id.spinner_task_priority:
                mPriority = position;
                mPriorityText = parent.getItemAtPosition(position).toString();
                break;

            case R.id.spinner_repeat:
                mRepeatFrequency = parent.getItemAtPosition(position).toString();
                mTagRepeat = (position == 0) ? TaskEntry.TAG_NOT_REPEAT : TaskEntry.TAG_REPEAT;
                break;

            case R.id.spinner_extra_info:
                mExtraInfoTypeIndex = position;
                mExtraInfoType = parent.getItemAtPosition(position).toString();
                if (position == 0) {
                    mTextViewExtraInfo.setText("");
                    mTextViewAlert.setVisibility(View.GONE);
                } else {

                    switch (mExtraInfoTypeIndex) {
                        case 1:
                            // phone
                            mTextViewAlert.setVisibility(View.GONE);
                            if (!mIsRestoredInstance || Utils.isEmptyString(mExtraInfo)) {
                                if (mIsUserInteracting) {
                                    getContactPhone();
                                }
                            }
                            break;

                        case 2:
                            // email
                            mTextViewAlert.setVisibility(View.GONE);
                            if (!mIsRestoredInstance || Utils.isEmptyString(mExtraInfo)) {
                                if (mIsUserInteracting) {
                                    getContactEmail();
                                }
                            }
                            break;

                        case 3:
                            // location
                            if (!mIsRestoredInstance || Utils.isEmptyString(mExtraInfo)) {
                                if (mIsUserInteracting) {
                                    getLocation();
                                }
                            }
                            break;

                        case 4:
                            // barcode
                            mTextViewAlert.setVisibility(View.GONE);
                            if (!mIsRestoredInstance || Utils.isEmptyString(mExtraInfo)) {
                                if (mIsUserInteracting) {
                                    getProductBarcode();
                                }
                            }
                            break;
                    }
                }
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        mIsUserInteracting = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mIsUserInteracting = true;
        return super.onTouchEvent(event);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_due_date:
                DialogFragment dialogFragment = new DatePickerFragment();
                dialogFragment.show(getSupportFragmentManager(), getString(R.string.label_due_on));
                break;

            case R.id.button_due_time:
                DialogFragment timePicker = new TimePickerFragment();
                timePicker.show(getSupportFragmentManager(), getString(R.string.label_at_time));
                break;
        }
    }

    /**
     * Method to check if any entry has been made. This is to handle situation when motion event
     * is not detected, yet entries have been made (e.g. using emulator and typing using keyboard)
     */
    public boolean hasEntry() {
        boolean hasInput = false;

        if (!Utils.isEmptyString(mEditTextTaskTitle.getText().toString()) ||
                (mIndexCategorySpinner > 0) ||
                (mPriority > 0) ||
                (mTagRepeat > 0) ||
                (mExtraInfoTypeIndex > 0) ||
                (!Utils.isEmptyString(mDateDue)) ||
                (!Utils.isEmptyString(mTimeDue)) ||
                (!Utils.isEmptyString(mExtraInfo))) {
            hasInput = true;
        }
        return hasInput;
    }

    /**
     * Method invoked when device's back button is pressed
     */
    @Override
    public void onBackPressed() {
        // If the item hasn't changed, continue with navigating up to parent activity
        if (!mItemHasChanged && !hasEntry()) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, navigate to parent activity.
                        finish();
                    }
                };

        // Show a dialog that notifies the user they have unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener - click listener action to take when user confirms discarding changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_unsaved_changes);
        builder.setPositiveButton(R.string.action_yes, discardButtonClickListener);
        builder.setNegativeButton(R.string.action_no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        mDateDue = year + "-" + String.format(Locale.ENGLISH, "%02d", ++month) + "-" + String
                .format(Locale.ENGLISH, "%02d", dayOfMonth);
        displayDate();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);

        mTimeDue =
                String.format(Locale.ENGLISH, "%02d", hourOfDay) + ":" + String.format(Locale.ENGLISH, "%02d", minute);
        displayTime();
    }

    /**
     * Method to get all details if an existing task is being edited
     */
    private void getTaskDetails() {
        mTaskId = mTask.getTaskId();
        mTaskTitle = mTask.getTaskTitle();

        mTaskCateogory = mTask.getCategory();
        mIndexCategorySpinner = Utils.getCategoryIndex(mTaskCateogory);

        mPriority = mTask.getPriority();
        mPriorityText = Utils.getPriorityText(mPriority);
        mDateDue = mTask.getDueDate();
        mTimeDue = mTask.getDueTime();
        mTagRepeat = mTask.getTagRepeat();
        mRepeatFrequency = mTask.getRepeatFrequency();

        mExtraInfoType = mTask.getExtraInfoType();
        mExtraInfo = mTask.getExtraInfo();
        mExtraInfoTypeIndex = Utils.getExtraInfoTypeIndex(mExtraInfoType);

        mExtraInfoDisplay = mExtraInfo;
        if (mExtraInfoTypeIndex == 3) {
            // remove geo locations if extra info is address
            if (mExtraInfoDisplay.contains("[") && mExtraInfoDisplay.contains("]")) {
                mExtraInfoDisplay = mExtraInfoDisplay
                        .replace(mExtraInfoDisplay.substring(mExtraInfoDisplay.indexOf("["),
                                mExtraInfoDisplay.indexOf("]")), "");
                mExtraInfoDisplay = mExtraInfoDisplay.replace("]", "");
            }
        }

        mDateAdded = mTask.getDateAdded();
    }

    /**
     * Method to populate UI with all details if an existing task is being edited
     */
    private void populateUI() {
        mEditTextTaskTitle.setText(mTaskTitle);

        mSpinnerCategory.setSelection(mIndexCategorySpinner, true);

        mSpinnerPriority.setSelection(mPriority, true);

        mSpinnerRepeat.setSelection(Utils.getRepeatIndex(mRepeatFrequency), true);

        if (!Utils.isEmptyString(mDateDue)) {
            String date = Utils.isEmptyString(mDateDue) ? "" : mDateDue;
            mTextViewDate.setText(Utils.getDisplayDate(date));
        } else {
            mTextViewDate.setText("");
        }

        if (!Utils.isEmptyString(mTimeDue)) {
            mTextViewTime.setText(mTimeDue);
        } else {
            mTextViewTime.setText("");
        }

        mSpinnerExtraInfoType.setSelection(mExtraInfoTypeIndex, true);
        if (mExtraInfoTypeIndex > 0) {
            mTextViewExtraInfo.setText(mExtraInfoDisplay);
        }
    }

    /**
     * Method to display due date
     */
    private void displayDate() {
        if (!Utils.isEmptyString(mDateDue)) {
            mTextViewDate.setText(Utils.getDisplayDate(mDateDue));
        } else {
            mTextViewDate.setText("");
        }
    }

    /**
     * Method to display due time
     */
    private void displayTime() {
        if (!Utils.isEmptyString(mTimeDue)) {
            mTextViewTime.setText(mTimeDue);
        } else {
            mTextViewTime.setText("");
        }
    }

    /**
     * Method to display extra information
     */
    private void displayExtraInfo() {
        if (!Utils.isEmptyString(mExtraInfo)) {
            mTextViewExtraInfo.setText(mExtraInfoDisplay);

            if (mExtraInfoType.equals(Constants.EXTRA_INFO_LOCATION)) {
                mTextViewAlert.setVisibility(View.VISIBLE);
            } else {
                mTextViewAlert.setVisibility(View.GONE);
            }

        } else {
            mTextViewTime.setText("");
            mTextViewAlert.setVisibility(View.GONE);
        }
    }

    /**
     * Method to scan barcode
     */
    private void getProductBarcode() {
        Intent intent = new Intent(getApplicationContext(), BarcodeCaptureActivity.class);
        startActivityForResult(intent, Constants.BARCODE_READER_REQUEST_CODE);
    }

    /**
     * Method to pick a desired contact phone from Contacts List
     */
    private void getContactPhone() {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK);
        contactPickerIntent.setType(CommonDataKinds.Phone.CONTENT_TYPE);
        if (contactPickerIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(contactPickerIntent, Constants.PHONE_PICKER_REQUEST);
        }

    }

    /**
     * Method to pick a desired contact email from Contacts List
     */
    private void getContactEmail() {
        Intent emailPickerIntent = new Intent(Intent.ACTION_PICK);
        emailPickerIntent.setType(CommonDataKinds.Email.CONTENT_TYPE);
        if (emailPickerIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(emailPickerIntent, Constants.EMAIL_PICKER_REQUEST);
        }

    }

    /**
     * Method to pick a desired location from Google Map
     */
    private void getLocation() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(this), Constants.PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // check whether the result is ok

        switch (requestCode) {
            case Constants.PHONE_PICKER_REQUEST:
                if (resultCode == RESULT_OK) {
                    try {
                        String phoneNo;
                        Uri contactUri = data.getData();
                        String[] projection = new String[]{CommonDataKinds.Phone.NUMBER};
                        Cursor cursorContact = getContentResolver().query(contactUri, projection, null, null, null);

                        if (cursorContact != null && cursorContact.moveToFirst()) {
                            phoneNo = cursorContact
                                    .getString(cursorContact.getColumnIndex(CommonDataKinds.Phone.NUMBER));
                            mExtraInfo = phoneNo;
                            mExtraInfoDisplay = phoneNo;
                            displayExtraInfo();
                            cursorContact.close();
                        }

                    } catch (Exception e) {
                        Timber.e(e.getMessage());
                    }
                }

                break;

            case Constants.EMAIL_PICKER_REQUEST:
                if (resultCode == RESULT_OK) {
                    try {
                        String email;
                        Uri contactUri = data.getData();
                        String[] projection = new String[]{Email.ADDRESS};
                        Cursor cursorContact = getContentResolver().query(contactUri, projection, null, null, null);

                        if (cursorContact != null && cursorContact.moveToFirst()) {
                            email = cursorContact.getString(cursorContact.getColumnIndex(Email.ADDRESS));
                            mExtraInfo = email;
                            mExtraInfoDisplay = email;
                            displayExtraInfo();
                            cursorContact.close();
                        }

                    } catch (Exception e) {
                        Timber.e(e.getMessage());
                    }
                }

                break;

            case Constants.PLACE_PICKER_REQUEST:
                if (resultCode == RESULT_OK) {
                    Place place = PlacePicker.getPlace(this, data);

                    // get street name and address
                    String address = place.getName().toString();
                    address += "\n" + place.getAddress().toString();
                    mExtraInfoDisplay = address;

                    // get latitude and longitude, and format before saving
                    String latLng = place.getLatLng().toString();
                    latLng = "[" + latLng.substring(latLng.indexOf("(") + 1, latLng.indexOf(")")) + "]";
                    mExtraInfo = mExtraInfoDisplay + latLng;

                    // display chosen location
                    displayExtraInfo();
                } else {
                    Utils.showToastMessage(mContext, mToast, getString(R.string.errormsg_location_not_found)).show();
                }

                break;

            case Constants.BARCODE_READER_REQUEST_CODE:
                if (data != null) {
                    mMediaPlayer.start();
                    Barcode barcode = data.getParcelableExtra(Constants.INTENT_KEY_BARCODE);
                    mExtraInfo = barcode.displayValue;
                    mExtraInfoDisplay = mExtraInfo;
                    displayExtraInfo();
                } else {
                    Utils.showToastMessage(mContext, mToast, getString(R.string.barcode_failure));
                }

                break;
        }

    }

    /**
     * Method to return to List
     */
    private void returnToList() {
        Intent intent = new Intent(mContext, TaskListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(Constants.INTENT_KEY_TASK_FILTER, 0);
        startActivity(intent);
    }

    /**
     * Method to show/hide error messages based on UI input validation
     */
    private void showHideErrors(boolean isInvalidData, String errorMessage) {
        if (isInvalidData) {
            mTextViewError.setText("");
            mTextViewError.setVisibility(View.GONE);
        } else {
            mTextViewError.setVisibility(View.VISIBLE);
            mTextViewError.setText(errorMessage);
        }
    }

    /**
     * Method to read and validate UI inputs
     */
    private boolean validateInputs() {

        // Priority : no validation needed
        mPriority = Utils.getPriority(mPriorityText);

        // Completion Tag : no validation needed
        mTagCompleted = 0;

        // Date Added : no validation needed
        mDateAdded = Utils.getDateToday();

        // Task Title : check if it is not null or empty
        mTaskTitle = mEditTextTaskTitle.getText().toString().trim();
        if (Utils.isEmptyString(mTaskTitle)) {
            showHideErrors(false, mErrorTitle);
            return false;
        }

        // Task Category : check if a valid category has been selected
        if (mIndexCategorySpinner == 0) {
            showHideErrors(false, mErrorCategory);
            return false;
        }

        // Due date : this is not a required field
        // However a repeat task must have the first due date set
        if ((mTagRepeat == TaskEntry.TAG_REPEAT) && (Utils.isEmptyString(mDateDue))) {
            showHideErrors(false, mErrorRepeatDueDate);
            return false;
        }

        // Extra Info : this is not a required field
        // However, if extra info type is selected then check if details are there too
        if ((mExtraInfoTypeIndex > 0) && (Utils.isEmptyString(mExtraInfo))) {
            showHideErrors(false, mErrorExtraInfo);
            return false;
        }

        return true;
    }

    /**
     * Method to add a new task to database
     */
    private void addTask() {

        if (validateInputs()) {
            ContentValues values = new ContentValues();

            values.put(TaskEntry.COLUMN_TASK_TITLE, mTaskTitle);
            values.put(TaskEntry.COLUMN_CATEGORY, mTaskCateogory);
            values.put(TaskEntry.COLUMN_PRIORITY, mPriority);
            values.put(TaskEntry.COLUMN_EXTRA_INFO_TYPE, mExtraInfoType);
            values.put(TaskEntry.COLUMN_EXTRA_INFO, mExtraInfo);
            values.put(TaskEntry.COLUMN_DUE_DATE, mDateDue);
            values.put(TaskEntry.COLUMN_DUE_TIME, mTimeDue);
            values.put(TaskEntry.COLUMN_TAG_REPEAT, mTagRepeat);
            values.put(TaskEntry.COLUMN_REPEAT_FREQUENCY, mRepeatFrequency);
            values.put(TaskEntry.COLUMN_TAG_COMPLETED, mTagCompleted);
            values.put(TaskEntry.COLUMN_DATE_ADDED, mDateAdded);
            values.put(TaskEntry.COLUMN_DATE_COMPLETED, "");
            values.put(TaskEntry.COLUMN_DATE_UPDATED, Utils.getDateToday());

            mCurrentUri = getContentResolver().insert(TaskEntry.CONTENT_URI, values);
            if (mCurrentUri == null) {
                Toast.makeText(this, mSaveErrorMessage, Toast.LENGTH_SHORT).show();
            } else {
                WidgetIntentService.startActionUpdateWidgets(mContext);
                Toast.makeText(this, mSaveSuccessMessage, Toast.LENGTH_SHORT).show();
                //finish();

                returnToList();
            }
        }
    }

    /**
     * Method to update an existing task to database
     */
    private void updateTask() {
        if (validateInputs()) {
            ContentValues valuesUpdate = new ContentValues();

            valuesUpdate.put(TaskEntry.COLUMN_TASK_TITLE, mTaskTitle);
            valuesUpdate.put(TaskEntry.COLUMN_CATEGORY, mTaskCateogory);
            valuesUpdate.put(TaskEntry.COLUMN_PRIORITY, mPriority);
            valuesUpdate.put(TaskEntry.COLUMN_EXTRA_INFO_TYPE, mExtraInfoType);
            valuesUpdate.put(TaskEntry.COLUMN_EXTRA_INFO, mExtraInfo);
            valuesUpdate.put(TaskEntry.COLUMN_DUE_DATE, mDateDue);
            valuesUpdate.put(TaskEntry.COLUMN_DUE_TIME, mTimeDue);
            valuesUpdate.put(TaskEntry.COLUMN_TAG_REPEAT, mTagRepeat);
            valuesUpdate.put(TaskEntry.COLUMN_REPEAT_FREQUENCY, mRepeatFrequency);
            valuesUpdate.put(TaskEntry.COLUMN_TAG_COMPLETED, mTagCompleted);
            valuesUpdate.put(TaskEntry.COLUMN_DATE_ADDED, mDateAdded);
            valuesUpdate.put(TaskEntry.COLUMN_DATE_COMPLETED, "");
            valuesUpdate.put(TaskEntry.COLUMN_DATE_UPDATED, Utils.getDateToday());

            mCurrentUri = ContentUris.withAppendedId(TaskEntry.CONTENT_URI, mTaskId);
            int numRowsUpdated = mContext.getContentResolver().update(mCurrentUri, valuesUpdate, null, null);

            if (!(numRowsUpdated > 0)) {
                Toast.makeText(this, mSaveErrorMessage, Toast.LENGTH_SHORT).show();
            } else {
                WidgetIntentService.startActionUpdateWidgets(mContext);
                Toast.makeText(this, mSaveSuccessMessage, Toast.LENGTH_SHORT).show();
                //finish();

                returnToList();
            }
        }
    }
}
