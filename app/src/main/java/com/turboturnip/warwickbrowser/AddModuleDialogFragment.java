package com.turboturnip.warwickbrowser;

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

import java.util.regex.Matcher;

public class AddModuleDialogFragment extends DialogFragment {
    public interface AddModuleListener {
        void onModuleAdded(String title);
    }
    AddModuleListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (AddModuleListener)context;
    }

    private Dialog dialog;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Add Module");
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                EditText moduleName = dialog.findViewById(R.id.module_name);
                if (moduleName.getError() == null)
                    listener.onModuleAdded(moduleName.getText().toString());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {}
        });
        //builder.setView(R.layout.add_module_dialog);
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.add_module_dialog, null);
        final EditText dialogText = dialogView.findViewById(R.id.module_name);
        /*dialogText.setFilters(new InputFilter[]{
                new ModuleNameInputFilter()
        });*/
        dialogText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                Matcher m = ModuleNameInputFilter.moduleNamePattern.matcher(s.toString());
                if (!m.matches())
                    dialogText.setError("Must be of the form AA000");
            }
        });
        builder.setView(dialogView);
        dialog = builder.create();
        return dialog;
    }
}
