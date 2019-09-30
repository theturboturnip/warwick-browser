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
import com.turboturnip.warwickbrowser.db.Module;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class UpdateDescriptionDialogFragment extends DialogFragment {
    public interface ShouldUpdateDescriptionListener {
        void onDescriptionUpdateRequested(long moduleId, String newDescription);
    }
    ShouldUpdateDescriptionListener listener;

    private static final String MODULE_ID_KEY = "moduleid";
    private static final String MODULE_NAME_KEY = "modulename";
    private static final String MODULE_DESC_KEY = "moduledesc";

    public static UpdateDescriptionDialogFragment newInstance(Module module) {
        Bundle args = new Bundle();
        args.putString(MODULE_NAME_KEY, module.title);
        args.putLong(MODULE_ID_KEY, module.id);
        args.putString(MODULE_DESC_KEY, module.description);

        UpdateDescriptionDialogFragment fragment = new UpdateDescriptionDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private Dialog dialog;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (ShouldUpdateDescriptionListener)context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle("Update Description");

        Bundle args = getArguments();
        if (args == null) {
            dismiss();
        } else {
            long moduleId = args.getLong(MODULE_ID_KEY);
            String moduleName = args.getString(MODULE_NAME_KEY);
            String oldDescription = args.getString(MODULE_DESC_KEY);

            builder.setTitle("Update Description for " + moduleName)
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            EditText newDesc = dialog.findViewById(R.id.new_desc);

                            listener.onDescriptionUpdateRequested(moduleId, newDesc.getText().toString());
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                        }
                    });

            View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.update_description_dialog, null);
            EditText newDesc = dialogView.findViewById(R.id.new_desc);
            newDesc.setText(oldDescription);
            builder.setView(dialogView);
        }

        dialog = builder.create();
        return dialog;
    }
}
