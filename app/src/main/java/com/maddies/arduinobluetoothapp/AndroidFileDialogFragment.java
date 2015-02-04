package com.maddies.arduinobluetoothapp;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

public class AndroidFileDialogFragment extends DialogFragment{

    // Stores names of traversed directories
    ArrayList<String> str = new ArrayList<>();
    // Check if the first level of the directory structure is the one showing
    private Boolean firstLvl = true;
    private Item[] fileList;
    private File path = Environment.getExternalStorageDirectory();
    private String chosenFile;
    ListAdapter adapter;

    /* The activity that creates an instance of this dialog fragment must
    * implement this interface in order to receive event callbacks.
    * Each method passes the DialogFragment in case the host needs to query it. */
    public interface AndroidFileDialogListener {
        public void onAndroidFileClick(DialogFragment dialog, File file, Boolean again);
        public void onCancel(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    AndroidFileDialogListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        loadFileList();

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(getString(R.string.file_explorer_dialog_title));


        if (fileList == null) {
            // no files loaded
        }

        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                chosenFile = fileList[which].file;
                File sel = new File(path + "/" + chosenFile);

                if (sel.isDirectory()) {
                    firstLvl = false;

                    // Adds chosen directory to list
                    str.add(chosenFile);
                    fileList = null;
                    path = new File(sel + "");

                    loadFileList();

                    mListener.onAndroidFileClick(AndroidFileDialogFragment.this, sel, true);



                }

                // Checks if 'up' was clicked
                else if (chosenFile.equalsIgnoreCase("up") && !sel.exists()) {

                    // present directory removed from list
                    String s = str.remove(str.size() - 1);

                    // path modified to exclude present directory
                    path = new File(path.toString().substring(0,
                            path.toString().lastIndexOf(s)));
                    fileList = null;

                    // if there are no more directories in the list, then
                    // its the first level
                    if (str.isEmpty()) {
                        firstLvl = true;
                    }
                    loadFileList();

                    mListener.onAndroidFileClick(AndroidFileDialogFragment.this, sel, true);

                }
                // File picked
                else {
                    mListener.onAndroidFileClick(AndroidFileDialogFragment.this, sel, false);
                }
            }
        });

        // Create the AlertDialog object and return it
        AlertDialog dialog = builder.create();
        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        mListener.onCancel(AndroidFileDialogFragment.this);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (AndroidFileDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }

    }



    private void loadFileList() {
        if (path.mkdirs() || path.isDirectory()) {
            // able to write to sd card
        } else {
            // unable to write on the sd card
        }

        // Checks whether path exists
        if (path.exists()) {
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    File sel = new File(dir, filename);
                    // Filters based on whether the file is hidden or not
                    return (sel.isFile() || sel.isDirectory())
                            && !sel.isHidden();

                }
            };

            String[] fList = path.list(filter);
            fileList = new Item[fList.length];
            for (int i = 0; i < fList.length; i++) {
                fileList[i] = new Item(fList[i], R.drawable.file_icon);

                // Convert into file path
                File sel = new File(path, fList[i]);

                // Set drawables
                if (sel.isDirectory()) {
                    fileList[i].icon = R.drawable.directory_icon;
                } else {

                }
            }

            if (!firstLvl) {
                Item temp[] = new Item[fileList.length + 1];
                for (int i = 0; i < fileList.length; i++) {
                    temp[i + 1] = fileList[i];
                }
                temp[0] = new Item("Up", R.drawable.directory_up);
                fileList = temp;
            }
        } else {
            // path does not exist
        }

        adapter = new ArrayAdapter<Item>(getActivity(), android.R.layout.select_dialog_item,
                android.R.id.text1, fileList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // creates view
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view
                        .findViewById(android.R.id.text1);

                // put the image on the text view
                textView.setCompoundDrawablesWithIntrinsicBounds(
                        fileList[position].icon, 0, 0, 0);

                // add margin between image and text (support various screen
                // densities)
                int dp5 = (int) (5 * getResources().getDisplayMetrics().density + 0.5f);
                textView.setCompoundDrawablePadding(dp5);

                return view;
            }
        };
    }



}

