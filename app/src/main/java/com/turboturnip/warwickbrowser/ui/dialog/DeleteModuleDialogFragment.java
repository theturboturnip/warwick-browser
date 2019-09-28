package com.turboturnip.warwickbrowser.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import com.turboturnip.warwickbrowser.db.Module;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DeleteModuleDialogFragment extends DialogFragment {
    public interface ShouldDeleteListener {
        void onDeleteRequestAccepted(long moduleID);
    }
    ShouldDeleteListener listener;

    private static final String MODULE_ID_KEY = "moduleid";
    private static final String MODULE_NAME_KEY = "modulename";

    public static DeleteModuleDialogFragment newInstance(Module module) {
        Bundle args = new Bundle();
        args.putString(MODULE_NAME_KEY, module.title);
        args.putLong(MODULE_ID_KEY, module.id);

        DeleteModuleDialogFragment fragment = new DeleteModuleDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (ShouldDeleteListener)context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle("Confirm Delete");

        Bundle args = getArguments();
        if (args == null) {
            dismiss();
        } else {
            String moduleName = args.getString(MODULE_NAME_KEY);
            long moduleID = args.getLong(MODULE_ID_KEY);

            builder.setMessage("Are you sure you want to delete module " + moduleName + "?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            listener.onDeleteRequestAccepted(moduleID);
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
