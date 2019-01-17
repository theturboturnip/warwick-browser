package com.turboturnip.warwickbrowser.ui;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.Toolbar;
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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.turboturnip.warwickbrowser.Statics;
import com.turboturnip.warwickbrowser.db.actions.AsyncDBModuleCreate;
import com.turboturnip.warwickbrowser.db.actions.AsyncDBModuleLinkInsert;
import com.turboturnip.warwickbrowser.ui.dialog.AddModuleDialogFragment;
import com.turboturnip.warwickbrowser.R;
import com.turboturnip.warwickbrowser.db.ModuleAndLinks;
import com.turboturnip.warwickbrowser.db.ModuleDatabase;
import com.turboturnip.warwickbrowser.db.ModuleLink;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import static com.turboturnip.warwickbrowser.ui.ModuleAddLinkActivity.MODULE_ID;

public class MainActivity extends AppCompatActivity implements AddModuleDialogFragment.AddModuleListener {

    private RecyclerView moduleHolder;
    private ModuleViewAdapter moduleAdapter;
    private LinearLayoutManager moduleLayout;
    private View permissionsDialog;

    private RecyclerView.RecycledViewPool modulePool = new RecyclerView.RecycledViewPool(),
            moduleLinkPool = new RecyclerView.RecycledViewPool(),
            moduleFilePool = new RecyclerView.RecycledViewPool();

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

        moduleAdapter = new ModuleViewAdapter();
        moduleDatabase.daoModules().getModules().observe(this, moduleAdapter.moduleObserver);

        moduleLayout = new LinearLayoutManager(this) {
            @Override
            public boolean canScrollHorizontally() {
                return false;
            }

            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        moduleLayout.setOrientation(LinearLayoutManager.VERTICAL);

        moduleHolder = findViewById(R.id.modules);
        moduleHolder.setRecycledViewPool(modulePool);
        moduleHolder.setAdapter(moduleAdapter);
        moduleHolder.setLayoutManager(moduleLayout);
        moduleAdapter.notifyDataSetChanged();

        findViewById(R.id.my_warwick_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getPackageManager().getLaunchIntentForPackage("uk.ac.warwick.my.app");
                if (intent != null)
                    startActivity(intent);
            }
        });
    }

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
                    permissionsDialog.setVisibility(View.GONE);
                } else {
                    permissionsDialog.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public void onModuleAdded(final String title) {
        new AsyncDBModuleCreate(moduleDatabase, title).execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        if (requestCode == 0 && data != null) {
            final long moduleId = data.getLongExtra(MODULE_ID, -1);
            final String linkName = data.getStringExtra("LINK_NAME");
            final String linkTarget = data.getStringExtra("LINK_TARGET");
            Log.e("turnipwarwick", "Got data back from link selection: " + moduleId + " : " + linkName + " : " + linkTarget);
            new AsyncDBModuleLinkInsert(moduleDatabase, new ModuleLink(moduleId, linkName, linkTarget)).execute();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private class ModuleView extends RecyclerView.ViewHolder {
        private Toolbar toolbar;

        private RecyclerView linksHolder;
        private ModuleLinksAdapter linksAdapter;
        private LinearLayoutManager linksLayout;

        private RecyclerView filesHolder;
        private ModuleFilesAdapter filesAdapter;
        private LinearLayoutManager filesLayout;

        private ConstraintLayout layout;
        private ConstraintSet filesClosed, filesOpen;

        private boolean closed = false;

        ModuleView(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.module_layout);

            toolbar = itemView.findViewById(R.id.toolbar);
            toolbar.inflateMenu(R.menu.module_context_menu);

            linksAdapter = new ModuleLinksAdapter();
            linksLayout = new LinearLayoutManager(itemView.getContext());
            linksLayout.setOrientation(LinearLayoutManager.HORIZONTAL);
            linksHolder = itemView.findViewById(R.id.links);
            linksHolder.setRecycledViewPool(moduleLinkPool);
            linksHolder.setAdapter(linksAdapter);
            linksHolder.setLayoutManager(linksLayout);

            filesAdapter = new ModuleFilesAdapter();
            filesLayout = new LinearLayoutManager(itemView.getContext());
            filesLayout.setOrientation(LinearLayoutManager.VERTICAL);
            filesHolder = itemView.findViewById(R.id.files);
            filesHolder.setRecycledViewPool(moduleFilePool);
            filesHolder.setAdapter(filesAdapter);
            filesHolder.setLayoutManager(filesLayout);

            filesOpen = new ConstraintSet();
            filesOpen.clone(layout);
            filesClosed = new ConstraintSet();
            filesClosed.clone(filesOpen);
            filesClosed.constrainMaxHeight(R.id.files, 24);
            filesOpen.setRotation(R.id.open_files_button, 0);
            filesClosed.setRotation(R.id.open_files_button, 180);

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

        void updateContents(ModuleAndLinks data) {
            toolbar.setTitle(data.module.title);
            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    if (menuItem.getItemId() == R.id.action_add_link) {
                        Intent intent = new Intent(MainActivity.this, ModuleAddLinkActivity.class);
                        intent.putExtra(MODULE_ID, data.module.id);
                        intent.putExtra(ModuleViewActivity.MODULE_NAME, data.module.title);
                        MainActivity.this.startActivityForResult(intent, 0);

                    }
                    return false;
                }
            });

            linksAdapter.moduleData = data;
            linksAdapter.data = data.links;
            linksAdapter.notifyDataSetChanged();
            filesAdapter.setDirectory(Statics.getStorageDirForModule(data.module.title).getAbsolutePath());

            closeFiles();
        }
        private void closeFiles(){
            if (closed) return;
            Transition transition = new AutoTransition();
            //transition.setDuration(1000);
            TransitionManager.beginDelayedTransition(layout, transition);
            filesClosed.applyTo(layout);
            filesHolder.setVerticalScrollBarEnabled(false);
            closed = true;
        }
        private void openFiles(){
            if (!closed) return;
            Transition transition = new AutoTransition();
            //transition.setDuration(1000);
            TransitionManager.beginDelayedTransition(layout, transition);
            filesOpen.applyTo(layout);
            filesHolder.setVerticalScrollBarEnabled(true);
            closed = false;
        }
    }
    private class ModuleLinkView extends RecyclerView.ViewHolder {
        private Button linkButton;
        ModuleLinkView(@NonNull View itemView) {
            super(itemView);
            linkButton = (Button)itemView;
        }
        void setLink(final ModuleAndLinks moduleData, final ModuleLink link) {
            linkButton.setText(link.title);
            linkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), ModuleViewActivity.class);
                    intent.putExtra(ModuleViewActivity.MODULE_NAME, moduleData.module.title);
                    intent.putExtra(ModuleViewActivity.REQUESTED_PATH, link.target);
                    v.getContext().startActivity(intent);
                }
            });
        }
    }
    private class ModuleFileView extends RecyclerView.ViewHolder {
        private TextView textView;
        private View dividerView;
        private View indentView;

        ModuleFileView(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.file_name);
            dividerView = itemView.findViewById(R.id.divider);
            indentView = itemView.findViewById(R.id.indent);
        }
        void setDirectory(File directory, boolean isFirst) {
            textView.setText(directory.getName());
            textView.setOnClickListener(null);
            dividerView.setVisibility(isFirst ? View.GONE : View.VISIBLE);
            indentView.setVisibility(View.GONE);
        }
        void setFile(File file, boolean isFirst, boolean indented) {
            textView.setText(file.getName());

            Uri fileURI = FileProvider.getUriForFile(MainActivity.this, "com.turboturnip.warwickbrowser.fileprovider", file);

            String mimetype = null;
            //String possibleExtensions = file.getName().substring(file.getName().indexOf('.'));
            String extension = MimeTypeMap.getFileExtensionFromUrl(fileURI.toString());
            if (extension != null) {
                Log.e("turnipwarwick", "Found ext " + extension + " for " + file.getName());
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
            Log.e("turnipwarwick", "Selected mimetype " + mimetype + " for " + file.getName());
            final Intent openFileIntent = mimetype == null ? (Intent.createChooser(fileIntent, "Open " + file.getName() + " with")) : fileIntent;

            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(openFileIntent);
                }
            });

            dividerView.setVisibility(isFirst ? View.GONE : View.VISIBLE);
            indentView.setVisibility(indented ? View.VISIBLE : View.GONE);
        }
        void setNoFile() {
            textView.setText("No Files");
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {}
            });
        }
    }

    private static class ModuleFilesAdapterSwapFilesHandler extends Handler {
        private final WeakReference<ModuleFilesAdapter> adapterRef;

        ModuleFilesAdapterSwapFilesHandler(ModuleFilesAdapter adapter) {
            adapterRef = new WeakReference<>(adapter);
        }

        @Override
        public void handleMessage(Message msg) {
            ModuleFilesAdapter adapter = adapterRef.get();
            if (adapter == null) return;

            adapter.files = adapter.pendingFiles;
            adapter.notifyDataSetChanged();
        }
    }
    private class ModuleFilesAdapter extends RecyclerView.Adapter<ModuleFileView> {
        private class ModuleDirectoryObserver extends FileObserver {
            final String directoryPath;
            final Map<String, ModuleDirectoryObserver> childObservers;
            final boolean watchSubdirectories;
            ModuleDirectoryObserver(String directoryPath, boolean watchSubdirectories) {
                super(directoryPath);
                this.directoryPath = directoryPath;
                this.childObservers = new HashMap<>();
                this.watchSubdirectories = watchSubdirectories;
                updateSubdirectories();
                startWatching();
            }

            private synchronized void updateSubdirectories() {
                if (!watchSubdirectories) return;

                File currentFile = new File(directoryPath);
                Set<String> toRemove = new HashSet<>(childObservers.keySet());
                for (File subdirectory : currentFile.listFiles(File::isDirectory)) {
                    String subdirName = subdirectory.getName();

                    toRemove.remove(subdirName);
                    ModuleDirectoryObserver currentObserver = childObservers.get(subdirName);
                    if (currentObserver == null)
                        childObservers.put(subdirName, new ModuleDirectoryObserver(subdirectory.getPath(), false));
                }
                for (String nonexistantDir : toRemove)
                    childObservers.remove(nonexistantDir);
            }

            @Override
            public void onEvent(int event, @Nullable String path) {
                switch(event) {
                    case MOVED_FROM:
                    case MOVED_TO:
                    case CREATE:
                    case DELETE:
                        updateSubdirectories();
                        updateFiles();
                        break;
                    case MOVE_SELF:
                    case DELETE_SELF:
                        stopWatching();
                        break;
                }
            }
        }
        private ModuleDirectoryObserver directoryObserver;
        private List<File> pendingFiles;
        private List<File> files;
        private final Comparator<File> fileComparator = (f1, f2) -> (int)(f2.lastModified() - f1.lastModified());

        private void updateFiles(){
            File directory = new File(directoryObserver.directoryPath);

            List<File> directChildren = Arrays.asList(directory.listFiles());
            Collections.sort(directChildren, fileComparator);
            pendingFiles = new ArrayList<>(directChildren.size());

            for (File file : directChildren) {
                pendingFiles.add(file);
                if (file.isDirectory()) {
                    File[] childFiles = file.listFiles(File::isFile);
                    List<File> childFileList = Arrays.asList(childFiles);
                    Collections.sort(childFileList, fileComparator);
                    pendingFiles.addAll(childFileList);
                }
            }

            handler.sendEmptyMessage(0);
        }

        private Handler handler;

        ModuleFilesAdapter() {
            handler = new ModuleFilesAdapterSwapFilesHandler(this);
        }

        @Override
        protected void finalize() {
            if (directoryObserver != null)
                directoryObserver.stopWatching();
        }

        void setDirectory(String path) {
            if (directoryObserver != null)
                directoryObserver.stopWatching();
            directoryObserver = new ModuleDirectoryObserver(path, true);
            updateFiles();
        }

        @NonNull
        @Override
        public ModuleFileView onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new ModuleFileView(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.module_file_item, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ModuleFileView moduleFileView, int i) {
            if (files != null && !files.isEmpty()) {
                File fileForView = files.get(i);
                if (fileForView.isDirectory())
                    moduleFileView.setDirectory(fileForView, i == 0);
                else
                    moduleFileView.setFile(fileForView, i == 0, !fileForView.getParent().equals(directoryObserver.directoryPath));
            } else
                moduleFileView.setNoFile();
        }

        @Override
        public int getItemCount() {
            if (files != null && !files.isEmpty())
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
            moduleLinkView.setLink(moduleData, data.get(idx));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    private class ModuleViewAdapter extends RecyclerView.Adapter<ModuleView> {
        private Observer<List<ModuleAndLinks>> moduleObserver = new Observer<List<ModuleAndLinks>>() {
            @Override
            public void onChanged(@Nullable List<ModuleAndLinks> modules) {
                if (modules == null)
                    ModuleViewAdapter.this.modules = null;
                else {
                    ModuleViewAdapter.this.modules = new ArrayList<>(modules);
                    Collections.sort(ModuleViewAdapter.this.modules, (m1, m2) -> m1.module.title.compareTo(m2.module.title));
                }

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
