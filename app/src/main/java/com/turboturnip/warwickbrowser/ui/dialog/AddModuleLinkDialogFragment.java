package com.turboturnip.warwickbrowser.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.turboturnip.warwickbrowser.R;
import com.turboturnip.warwickbrowser.db.Module;

public class AddModuleLinkDialogFragment extends DialogFragment {
    public interface AddModuleLinkListener {
        void onModuleLinkAdded(long moduleId, String title, String selected);
    }
    AddModuleLinkListener listener;

    private static final String MODULE_ID_KEY = "moduleid";
    private static final String MODULE_NAME_KEY = "modulename";
    private static final String LINK_TARGET_KEY = "linktarget";

    public static AddModuleLinkDialogFragment newInstance(long moduleId, String moduleName, String linkTarget) {
        Bundle args = new Bundle();
        args.putString(MODULE_NAME_KEY, moduleName);
        args.putLong(MODULE_ID_KEY, moduleId);
        args.putString(LINK_TARGET_KEY, linkTarget);

        AddModuleLinkDialogFragment fragment = new AddModuleLinkDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }
    public static AddModuleLinkDialogFragment newInstance(Module module, String linkTarget) {
        return newInstance(module.id, module.title, linkTarget);
    }
    private AddModuleLinkDialogFragment(){
        super();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (AddModuleLinkListener)context;
    }

    private Dialog dialog;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        Bundle args = getArguments();
        if (args == null) {
            dismiss();
        } else {
            final long moduleId = args.getLong(MODULE_ID_KEY);
            String moduleName = args.getString(MODULE_NAME_KEY);
            String linkTargetStr = args.getString(LINK_TARGET_KEY);

            builder.setTitle("Add Link for " + MODULE_NAME_KEY);
            builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    EditText linkName = dialog.findViewById(R.id.link_name);
                    EditText linkTarget = dialog.findViewById(R.id.link_target);

                    listener.onModuleLinkAdded(moduleId, linkName.getText().toString(), linkTarget.getText().toString());
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                }
            });

            View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.add_module_link_dialog, null);
            final EditText linkTarget = dialogView.findViewById(R.id.link_target);
            linkTarget.setText(linkTargetStr);
            builder.setView(dialogView);
        }

        dialog = builder.create();
        return dialog;
    }
}
