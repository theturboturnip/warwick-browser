package com.turboturnip.warwickbrowser.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class RedownloadFileDialogFragment extends DialogFragment {
    public interface ShouldRedownloadListener {
        void onRedownloadRequested(int internalRequestId);
    }
    ShouldRedownloadListener listener;

    private static final String REQUEST_ID_KEY = "request";
    private static final String FILE_NAME_KEY = "filename";
    private static final String MODULE_NAME_KEY = "modulename";

    public static RedownloadFileDialogFragment newInstance(int internalRequestId, String fileName, String moduleName) {
        Bundle args = new Bundle();
        args.putInt(REQUEST_ID_KEY, internalRequestId);
        args.putString(FILE_NAME_KEY, fileName);
        args.putString(MODULE_NAME_KEY, moduleName);

        RedownloadFileDialogFragment fragment = new RedownloadFileDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (ShouldRedownloadListener)context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle("Confirm Redownload");

        Bundle args = getArguments();
        if (args == null) {
            dismiss();
        } else {
            int internalRequestId = args.getInt(REQUEST_ID_KEY, -1);
            String fileName = args.getString(FILE_NAME_KEY);
            String moduleName = args.getString(MODULE_NAME_KEY);

            builder.setMessage("You've already downloaded a file named " + fileName + " for " + moduleName + ". Do you want to download it again?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            listener.onRedownloadRequested(internalRequestId);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                        }
                    });
        }

        return builder.create();
    }
}
