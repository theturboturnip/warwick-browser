package com.turboturnip.warwickbrowser;

import android.Manifest;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.content.FileProvider;
import android.transition.AutoTransition;
import android.transition.Transition;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.turboturnip.warwickbrowser.db.Module;
import com.turboturnip.warwickbrowser.db.ModuleAndLinks;
import com.turboturnip.warwickbrowser.db.ModuleDatabase;
import com.turboturnip.warwickbrowser.db.ModuleLink;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static android.os.FileObserver.MOVED_FROM;
import static com.turboturnip.warwickbrowser.ModuleAddLinkActivity.MODULE_ID;

public class MainActivity extends AppCompatActivity implements AddModuleDialogFragment.AddModuleListener {

    private RecyclerView moduleHolder;
    private ModuleViewAdapter moduleAdapter;
    private LinearLayoutManager moduleLayout;
    private View permissionsDialog;

    private ModuleDatabase moduleDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.add_module_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddModuleDialogFragment newFragment = new AddModuleDialogFragment();
                newFragment.show(getSupportFragmentManager(), "addModule");
            }
        });

        moduleDatabase = ModuleDatabase.getDatabase(this);

        permissionsDialog = findViewById(R.id.permission_dialog);
        permissionsDialog.setVisibility(View.GONE);
        checkPermission();

        moduleHolder = findViewById(R.id.modules);

        moduleAdapter = new ModuleViewAdapter();
        moduleDatabase.daoModules().getModules().observe(this, moduleAdapter.moduleObserver);
        moduleHolder.setAdapter(moduleAdapter);
        moduleAdapter.notifyDataSetChanged();

        moduleLayout = new LinearLayoutManager(this);
        moduleLayout.setOrientation(LinearLayoutManager.VERTICAL);
        moduleHolder.setLayoutManager(moduleLayout);

        findViewById(R.id.my_warwick_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getPackageManager().getLaunchIntentForPackage("uk.ac.warwick.my.app");
                if (intent != null)
                    startActivity(intent);
            }
        });
    }

    /*private void loadFromDatabase(){
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                moduleAdapter.modules = moduleDatabase.daoModules().getModules();
                moduleAdapter.notifyDataSetChanged();
                return null;
            }
        }.execute();

    }*/

    private static final int REQUEST_EXT_STORAGE = 0;
    private void checkPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                permissionsDialog.setVisibility(View.VISIBLE);
                permissionsDialog.findViewById(R.id.permission_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.e("turnipwarwick", "Requesting again");
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                REQUEST_EXT_STORAGE);
                    }
                });
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_EXT_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }

        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXT_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    permissionsDialog.setVisibility(View.GONE);
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    permissionsDialog.setVisibility(View.VISIBLE);
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    @Override
    public void onModuleAdded(final String title) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                long moduleId = moduleDatabase.daoModules().insertModule(new Module(title));
                if (title.startsWith("CS")) {
                    moduleDatabase.daoModules().insertModuleLink(new ModuleLink(moduleId, "Home", "fac/sci/dcs/teaching/material/" + title + "/"));
                } else if (title.startsWith("ES")) {
                    int year = Integer.parseInt("" + title.charAt(2));
                    if (year > 4)
                        year = 4;
                    moduleDatabase.daoModules().insertModuleLink(new ModuleLink(moduleId, "Home", "fac/sci/eng/eso/modules/year" + year + "/" + title + "/"));
                }
                return null;
            }
        }.execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        if (requestCode == 0 && data != null) {
            final long moduleId = data.getLongExtra(MODULE_ID, -1);
            final String linkName = data.getStringExtra("LINK_NAME");
            final String linkTarget = data.getStringExtra("LINK_TARGET");
            Log.e("turnipwarwick", "Got data back from link selection: " + moduleId + " : " + linkName + " : " + linkTarget);
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {

                    moduleDatabase.daoModules().insertModuleLink(new ModuleLink(moduleId, linkName, linkTarget));
                    return null;
                }
            }.execute();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    // TODO: Use RecyclerView.ViewHolder.innerRecyclerView.setRecycledViewPool to make all recyclerviews of a type use the same pool

    private class ModuleView extends RecyclerView.ViewHolder {
        private TextView titleView;

        private RecyclerView dataHolder;
        private ModuleLinksAdapter dataAdapter;
        private LinearLayoutManager dataLayout;

        private RecyclerView fileHolder;
        private ModuleFilesAdapter filesAdapter;
        private LinearLayoutManager filesLayout;

        private ConstraintLayout layout;
        private ConstraintSet filesClosed, filesOpen;

        private boolean closed = false;

        public ModuleView(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.module_layout);

            titleView = itemView.findViewById(R.id.title);

            dataHolder = itemView.findViewById(R.id.links);
            dataAdapter = new ModuleLinksAdapter();
            dataHolder.setAdapter(dataAdapter);
            dataLayout = new LinearLayoutManager(itemView.getContext());
            dataLayout.setOrientation(LinearLayoutManager.HORIZONTAL);
            dataHolder.setLayoutManager(dataLayout);

            fileHolder = itemView.findViewById(R.id.files);
            filesAdapter = new ModuleFilesAdapter();
            fileHolder.setAdapter(filesAdapter);
            filesLayout = new LinearLayoutManager(itemView.getContext());
            filesLayout.setOrientation(LinearLayoutManager.VERTICAL);
            fileHolder.setLayoutManager(filesLayout);

            filesOpen = new ConstraintSet();
            filesOpen.clone(layout);
            filesClosed = new ConstraintSet();
            filesClosed.clone(filesOpen);
            filesClosed.constrainMaxHeight(R.id.files, 20);
            filesClosed.setRotation(R.id.open_files_button, 90);

            ImageView openFilesButton = itemView.findViewById(R.id.open_files_button);
            openFilesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (closed)
                        openFiles();
                    else
                        closeFiles();
                }
            });
        }

        public void updateContents(ModuleAndLinks data) {
            titleView.setText(data.module.title);
            dataAdapter.moduleData = data;
            dataAdapter.data = data.links;
            dataAdapter.notifyDataSetChanged();
            filesAdapter.setDirectory(DownloadHelper.getStorageDirForModule(data.module.title).getAbsolutePath());

            closeFiles();
        }
        private void closeFiles(){
            if (closed) return;
            Transition transition = new AutoTransition();
            //transition.setDuration(1000);
            TransitionManager.beginDelayedTransition(layout, transition);
            filesClosed.applyTo(layout);
            fileHolder.setVerticalScrollBarEnabled(false);
            closed = true;
        }
        private void openFiles(){
            if (!closed) return;
            Transition transition = new AutoTransition();
            //transition.setDuration(1000);
            TransitionManager.beginDelayedTransition(layout, transition);
            filesOpen.applyTo(layout);
            fileHolder.setVerticalScrollBarEnabled(true);
            closed = false;
        }
    }
    private class ModuleLinkView extends RecyclerView.ViewHolder {
        private Button linkButton;
        public ModuleLinkView(@NonNull View itemView) {
            super(itemView);
            linkButton = (Button)itemView;
        }
        public void setLink(final ModuleAndLinks moduleData, final ModuleLink link) {
            linkButton.setText(link.title);
            linkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), ModuleWebView.class);
                    intent.putExtra(ModuleWebView.MODULE_NAME, moduleData.module.title);
                    intent.putExtra(ModuleWebView.REQUESTED_PATH, link.target);//"fac/sci/dcs/teaching/material/" + owner + "/");
                    v.getContext().startActivity(intent);
                }
            });
        }
        public void setToAddLink(final ModuleAndLinks moduleData) {
            linkButton.setText("+");
            linkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), ModuleAddLinkActivity.class);
                    intent.putExtra(MODULE_ID, moduleData.module.id);
                    intent.putExtra(ModuleWebView.MODULE_NAME, moduleData.module.title);
                    MainActivity.this.startActivityForResult(intent, 0);
                }
            });
        }
    }
    private class ModuleFileView extends RecyclerView.ViewHolder {
        private TextView textView;
        public ModuleFileView(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.file_name);
        }
        public void setFile(File file) {
            textView.setText(file.getName());

            Uri fileURI = FileProvider.getUriForFile(MainActivity.this, "com.turboturnip.warwickbrowser.fileprovider", file);

            String mimetype = null;
            String extension = MimeTypeMap.getFileExtensionFromUrl(file.getAbsolutePath());
            if (extension != null) {
                mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            }

            Intent fileIntent = new Intent(Intent.ACTION_VIEW);
            fileIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (mimetype != null) {
                fileIntent.setDataAndType(fileURI, mimetype);
                fileIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
            } else {
                fileIntent.setData(fileURI);
            }
            final Intent openFileIntent = mimetype == null ? (Intent.createChooser(fileIntent, "Open " + file.getName() + " with")) : fileIntent;

            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(openFileIntent);
                }
            });
        }
        public void setNoFile() {
            textView.setText("No Files");
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {}
            });
        }
    }

    private class ModuleFilesAdapter extends RecyclerView.Adapter<ModuleFileView> {

        private class ModuleDirectoryObserver extends FileObserver {
            public final String directoryPath;
            public ModuleDirectoryObserver(String directoryPath) {
                super(directoryPath);
                this.directoryPath = directoryPath;
                startWatching();
            }

            @Override
            public void onEvent(int event, @Nullable String path) {
                switch(event) {
                    case MOVED_FROM:
                    case MOVED_TO:
                    case CREATE:
                    case DELETE:
                        updateFiles();
                    case MOVE_SELF:
                    case DELETE_SELF:
                        stopWatching();
                }
            }
        }
        private ModuleDirectoryObserver directoryObserver;
        private List<File> pendingFiles;
        private List<File> files;
        private void updateFiles(){
            File directory = new File(directoryObserver.directoryPath);
            pendingFiles = Arrays.asList(directory.listFiles());
            Collections.sort(pendingFiles, (f1, f2) -> (int)(f2.lastModified() - f1.lastModified()));

            handler.sendEmptyMessage(0);
        }
        private Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                files = pendingFiles;
                notifyDataSetChanged();
            }
        };

        public void setDirectory(String path) {
            if (directoryObserver != null)
                directoryObserver.stopWatching();
            directoryObserver = new ModuleDirectoryObserver(path);
            updateFiles();
        }

        @NonNull
        @Override
        public ModuleFileView onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new ModuleFileView(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.module_file_item, null, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ModuleFileView moduleFileView, int i) {
            if (files != null)
                moduleFileView.setFile(files.get(i));
            else
                moduleFileView.setNoFile();
        }

        @Override
        public int getItemCount() {
            if (files != null)
                return files.size();
            return 1;
        }
    }

    private class ModuleLinksAdapter extends RecyclerView.Adapter<ModuleLinkView> {
        ModuleAndLinks moduleData;
        List<ModuleLink> data;

        @NonNull
        @Override
        public ModuleLinkView onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new ModuleLinkView(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.module_link_item, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ModuleLinkView moduleLinkView, int idx) {
            if (idx == data.size())
                moduleLinkView.setToAddLink(moduleData);
            else
                moduleLinkView.setLink(moduleData, data.get(idx));
        }

        @Override
        public int getItemCount() {
            return data.size() + 1;
        }
    }

    private class ModuleViewAdapter extends RecyclerView.Adapter<ModuleView> {
        private Observer<List<ModuleAndLinks>> moduleObserver = new Observer<List<ModuleAndLinks>>() {
            @Override
            public void onChanged(@Nullable List<ModuleAndLinks> modules) {
                ModuleViewAdapter.this.modules = modules;
                notifyDataSetChanged();
            }
        };
        private List<ModuleAndLinks> modules;

        @NonNull
        @Override
        public ModuleView onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new ModuleView(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.module, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ModuleView moduleView, int i) {
            if (modules != null)
                moduleView.updateContents(modules.get(i));
        }

        @Override
        public int getItemCount() {
            if (modules != null)
                return modules.size();
            else return 0;
        }
    }
}
