package com.turboturnip.warwickbrowser.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.turboturnip.warwickbrowser.R;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DownloadAsDialogFragment extends DialogFragment {
    public interface DownloadAsListener {
        void onModifiedDownloadRequested(int internalRequestId, String newFileName);
    }
    DownloadAsListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (DownloadAsListener)context;
    }

    private static final String REQUEST_ID_KEY = "request";
    private static final String FILE_NAME_KEY = "filename";
    private static final String MODULE_NAME_KEY = "modulename";

    public static DownloadAsDialogFragment newInstance(int internalRequestId, String fileName, String moduleName) {
        Bundle args = new Bundle();
        args.putInt(REQUEST_ID_KEY, internalRequestId);
        args.putString(FILE_NAME_KEY, fileName);
        args.putString(MODULE_NAME_KEY, moduleName);

        DownloadAsDialogFragment fragment = new DownloadAsDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private Dialog dialog;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Download As");

        Bundle args = getArguments();
        if (args == null) {
            dismiss();
        } else {
            int internalRequestId = args.getInt(REQUEST_ID_KEY, -1);
            String fileName = args.getString(FILE_NAME_KEY);
            String moduleName = args.getString(MODULE_NAME_KEY);



            builder.setPositiveButton("Download", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    EditText newName = dialog.findViewById(R.id.new_name);

                    listener.onModifiedDownloadRequested(internalRequestId, newName.getText().toString());
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {}
            });

            View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.download_as_dialog, null);
            final TextView oldName = dialogView.findViewById(R.id.old_name);
            oldName.setText(fileName);
            final EditText newName = dialogView.findViewById(R.id.new_name);
            newName.setText(fileName);
            builder.setView(dialogView);
        }



        dialog = builder.create();
        return dialog;
    }
}
