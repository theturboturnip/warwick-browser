package com.turboturnip.warwickbrowser.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;

import com.turboturnip.warwickbrowser.R;
import com.turboturnip.warwickbrowser.SortBy;
import com.turboturnip.warwickbrowser.db.Module;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class UpdateSortDialogFragment extends DialogFragment {
    public interface ShouldUpdateSortListener {
        void onSortUpdateRequested(long moduleId, SortBy newSort);
    }
    ShouldUpdateSortListener listener;

    private static final String MODULE_ID_KEY = "moduleid";
    private static final String MODULE_NAME_KEY = "modulename";

    public static UpdateSortDialogFragment newInstance(Module module) {
        Bundle args = new Bundle();
        args.putString(MODULE_NAME_KEY, module.title);
        args.putLong(MODULE_ID_KEY, module.id);

        UpdateSortDialogFragment fragment = new UpdateSortDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private Dialog dialog;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (ShouldUpdateSortListener)context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle("Update Sort");

        Bundle args = getArguments();
        if (args == null) {
            dismiss();
        } else {
            long moduleId = args.getLong(MODULE_ID_KEY);
            String moduleName = args.getString(MODULE_NAME_KEY);

            builder.setTitle("Update File Sorting for " + moduleName);
            String[] values = new String[SortBy.values().length];
            for (int i = 0; i < values.length; i++) {
                values[i] = SortBy.values()[i].name();
            }
            builder.setItems(values, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    listener.onSortUpdateRequested(moduleId, SortBy.toSortBy(which));
                }
            });

        }

        dialog = builder.create();
        return dialog;
    }
}
