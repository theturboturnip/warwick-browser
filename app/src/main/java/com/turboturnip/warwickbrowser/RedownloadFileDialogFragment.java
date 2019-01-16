package com.turboturnip.warwickbrowser;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class RedownloadFileDialogFragment extends DialogFragment {
    public interface ShouldDownloadListener {
        void onRedownloadRequested(int internalRequestId);
    }
    ShouldDownloadListener listener;

    public static RedownloadFileDialogFragment newInstance(int internalRequestId, String fileName, String moduleName) {

        Bundle args = new Bundle();
        args.putInt("REQUEST", internalRequestId);
        args.putString("FILENAME", fileName);
        args.putString("MODULENAME", moduleName);

        RedownloadFileDialogFragment fragment = new RedownloadFileDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (ShouldDownloadListener)context;
    }

    private Dialog dialog;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args == null) {
            dismiss();
            return null;
        }

        int internalRequestId = args.getInt("REQUEST", -1);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Confirm Redownload");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                listener.onRedownloadRequested(internalRequestId);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {}
        });
        //builder.setView(R.layout.add_module_dialog);
        /*View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.warning_dialog, null);
        final TextView dialogText = dialogView.findViewById(R.id.message);*/
        String fileName = args.getString("FILENAME");
        String moduleName = args.getString("MODULENAME");
        builder.setMessage("You've already downloaded a file named " + fileName + " for " + moduleName + ". Do you want to download it again?");

        dialog = builder.create();
        return dialog;
    }
}
