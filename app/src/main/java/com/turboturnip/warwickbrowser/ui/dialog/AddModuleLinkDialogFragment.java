package com.turboturnip.warwickbrowser.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.turboturnip.warwickbrowser.R;

public class AddModuleLinkDialogFragment extends DialogFragment {
    public interface AddModuleLinkListener {
        String getLinkTarget();
        void onModuleLinkAdded(String title, String selected);
    }
    AddModuleLinkListener listener;

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
        builder.setTitle("Add Module Link");
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                EditText linkName = dialog.findViewById(R.id.link_name);
                EditText linkTarget = dialog.findViewById(R.id.link_target);

                listener.onModuleLinkAdded(linkName.getText().toString(), linkTarget.getText().toString());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {}
        });

        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.add_module_link_dialog, null);
        final EditText linkTarget = dialogView.findViewById(R.id.link_target);
        linkTarget.setText(listener.getLinkTarget());
        builder.setView(dialogView);

        dialog = builder.create();
        return dialog;
    }
}
