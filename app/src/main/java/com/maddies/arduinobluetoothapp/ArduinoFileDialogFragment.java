package com.maddies.arduinobluetoothapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;


public class ArduinoFileDialogFragment extends DialogFragment{


    public static ArduinoFileDialogFragment newInstance(String[] array) {
        ArduinoFileDialogFragment frag = new ArduinoFileDialogFragment();
        Bundle args = new Bundle();
        args.putStringArray(MainActivity.PUT_ARRAY, array);
        frag.setArguments(args);
        return frag;
    }


    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface ArduinoFileDialogListener {
        public void onArduinoFileClick(DialogFragment dialog, int which);
        public void onCancel(DialogFragment dialog);
    }

    ArduinoFileDialogListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String[] array = getArguments().getStringArray(MainActivity.PUT_ARRAY);



        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose A File")
                .setItems(array, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onArduinoFileClick(ArduinoFileDialogFragment.this, which);
                    }
                });


        // Create the AlertDialog object and return it
        AlertDialog dialog = builder.create();
        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        mListener.onCancel(ArduinoFileDialogFragment.this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (ArduinoFileDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }

    }

}
