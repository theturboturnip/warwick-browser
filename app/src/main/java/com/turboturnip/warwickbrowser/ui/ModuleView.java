package com.turboturnip.warwickbrowser.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;

import com.turboturnip.warwickbrowser.R;
import com.turboturnip.warwickbrowser.SortBy;
import com.turboturnip.warwickbrowser.Statics;
import com.turboturnip.warwickbrowser.db.ModuleAndLinks;
import com.turboturnip.warwickbrowser.db.ModuleLink;
import com.turboturnip.warwickbrowser.ui.dialog.AddModuleLinkDialogFragment;
import com.turboturnip.warwickbrowser.ui.dialog.DeleteModuleDialogFragment;
import com.turboturnip.warwickbrowser.ui.dialog.UpdateDescriptionDialogFragment;
import com.turboturnip.warwickbrowser.ui.dialog.UpdateSortDialogFragment;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.FileProvider;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.turboturnip.warwickbrowser.ui.ModuleAddLinkActivity.MODULE_ID;

public class ModuleView extends RecyclerView.ViewHolder {
    private Toolbar toolbar;

    private RecyclerView linksHolder;
    private ModuleLinksAdapter linksAdapter;
    private LinearLayoutManager linksLayout;

    private RecyclerView filesHolder;
    private ModuleFilesAdapter filesAdapter;
    private LinearLayoutManager filesLayout;

    private ConstraintLayout layout;
    private ConstraintSet filesClosed, filesOpen;

    private long moduleID;

    private boolean closed = false;

    private final WeakReference<AppCompatActivity> activity;

    final boolean addLinkToActivity;

    ModuleView(@NonNull View itemView, AppCompatActivity activity, RecyclerView.RecycledViewPool moduleLinkPool, RecyclerView.RecycledViewPool moduleFilePool, boolean addLinkToActivity) {
        super(itemView);
        layout = itemView.findViewById(R.id.module_layout);

        toolbar = itemView.findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.module_context_menu);

        linksAdapter = new ModuleLinksAdapter();
        linksLayout = new LinearLayoutManager(itemView.getContext());
        linksLayout.setOrientation(RecyclerView.VERTICAL);
        linksHolder = itemView.findViewById(R.id.links);
        linksHolder.setRecycledViewPool(moduleLinkPool);
        linksHolder.setAdapter(linksAdapter);
        linksHolder.setLayoutManager(linksLayout);

        filesAdapter = new ModuleFilesAdapter(activity);
        filesLayout = new LinearLayoutManager(itemView.getContext());
        filesLayout.setOrientation(RecyclerView.VERTICAL);
        filesHolder = itemView.findViewById(R.id.files);
        filesHolder.setRecycledViewPool(moduleFilePool);
        filesHolder.setAdapter(filesAdapter);
        filesHolder.setLayoutManager(filesLayout);

        this.activity = new WeakReference<>(activity);
        this.addLinkToActivity = addLinkToActivity;

            /*filesOpen = new ConstraintSet();
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
            });*/
    }

    void updateContents(ModuleAndLinks data) {
        moduleID = data.module.id;
        toolbar.setTitle(data.module.title);
        toolbar.setSubtitle(data.module.description);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.action_add_link_browser: {
                        Intent intent = new Intent(activity.get(), ModuleAddLinkActivity.class);
                        intent.putExtra(MODULE_ID, data.module.id);
                        intent.putExtra(ModuleWebViewActivity.MODULE_NAME, data.module.title);
                        activity.get().startActivityForResult(intent, 0);
                        break;
                    }
                    case R.id.action_add_link_manual: {
                        AddModuleLinkDialogFragment newFragment = AddModuleLinkDialogFragment.newInstance(data.module, "");
                        newFragment.show(activity.get().getSupportFragmentManager(), "addLinkManual");
                        break;
                    }
                    case R.id.action_delete_module: {
                        DeleteModuleDialogFragment newFragment = DeleteModuleDialogFragment.newInstance(data.module);
                        newFragment.show(activity.get().getSupportFragmentManager(), "shouldDelete");
                        break;
                    }
                    case R.id.action_set_desc: {
                        UpdateDescriptionDialogFragment newFragment = UpdateDescriptionDialogFragment.newInstance(data.module);
                        newFragment.show(activity.get().getSupportFragmentManager(), "updateDesc");
                        break;
                    }
                    case R.id.action_set_sort: {
                        UpdateSortDialogFragment newFragment = UpdateSortDialogFragment.newInstance(data.module);
                        newFragment.show(activity.get().getSupportFragmentManager(), "updateSort");
                        break;
                    }
                }
                return false;
            }
        });
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(activity.get(), SingleModuleActivity.class);
                i.putExtra(SingleModuleActivity.ID_EXTRA, data.module.id);
                activity.get().startActivityForResult(i, 0);
            }
        });

        linksAdapter.moduleData = data;
        linksAdapter.data = data.links;
        linksAdapter.notifyDataSetChanged();
        filesAdapter.setSortBy(data.module.sortBy);
        filesAdapter.setDirectory(Statics.getStorageDirForModule(data.module.title).getAbsolutePath());

        //closeFiles();
    }

        /*private void closeFiles(){
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
        }*/
}
class ModuleLinkView extends RecyclerView.ViewHolder {
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
                Intent intent = new Intent(v.getContext(), ModuleWebViewActivity.class);
                intent.putExtra(ModuleWebViewActivity.MODULE_NAME, moduleData.module.title);
                intent.putExtra(ModuleWebViewActivity.REQUESTED_PATH, link.target);
                v.getContext().startActivity(intent);
            }
        });
    }
}
class ModuleFileView extends RecyclerView.ViewHolder {
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
        itemView.setOnClickListener(null);
        dividerView.setVisibility(isFirst ? View.GONE : View.VISIBLE);
        indentView.setVisibility(View.GONE);
    }
    void setFile(String fileName, final Intent openFileIntent, boolean isFirst, boolean indented) {
        textView.setText(fileName);

        Log.v("turnipwarwick", "setFile " + fileName);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("turnipwarwick", "onClick " + fileName);
                v.getContext().startActivity(openFileIntent);
            }
        });

        dividerView.setVisibility(isFirst ? View.GONE : View.VISIBLE);
        indentView.setVisibility(indented ? View.VISIBLE : View.GONE);
    }
    void setNoFile() {
        textView.setText("No Files");
        dividerView.setVisibility(View.GONE);
        indentView.setVisibility(View.GONE);
        itemView.setOnClickListener(null);
    }
}

class ModuleFilesAdapterSwapFilesHandler extends Handler {
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
class ModuleFilesAdapter extends RecyclerView.Adapter<ModuleFileView> {
    private class ModuleFile {
        File file;
        final Intent openFileIntent;

        public ModuleFile(Context c, File file){
            this.file = file;

            if (file.isDirectory()) {
                openFileIntent = null;
                return;
            }

            Uri fileURI = FileProvider.getUriForFile(c, "com.turboturnip.warwickbrowser.fileprovider", file);

            String mimetype = null;
            //String possibleExtensions = file.getName().substring(file.getName().indexOf('.'));
            String extension = MimeTypeMap.getFileExtensionFromUrl(fileURI.toString());
            if (extension != null) {
                Log.d("turnipwarwick", "Found ext " + extension + " for " + file.getName());
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
            Log.d("turnipwarwick", "Selected mimetype " + mimetype + " for " + file.getName());
            openFileIntent = mimetype == null ? (Intent.createChooser(fileIntent, "Open " + file.getName() + " with")) : fileIntent;
        }
    }
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
        public void stopWatching() {
            super.stopWatching();
            for (ModuleDirectoryObserver childObserver : childObservers.values()) {
                childObserver.stopWatching();
            }
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
    ModuleDirectoryObserver directoryObserver;
    List<ModuleFile> pendingFiles;
    List<ModuleFile> files;
    // if f2.lastModified is < f1.lastModified, f2 comes first
    // => earlier first => date ascending
    private AtomicReference<Comparator<File>> fileComparatorRef = new AtomicReference<>();

    private class UpdateFilesTask extends AsyncTask<Handler, Void, List<ModuleFile>> {
        private WeakReference<Context> context;
        UpdateFilesTask(Context c){
            this.context = new WeakReference<>(c);
        }

        @Override
        protected List<ModuleFile> doInBackground(Handler... handlers) {
            File directory = new File(directoryObserver.directoryPath);
            Comparator<File> fileComparator = fileComparatorRef.get();

            List<File> directChildren = Arrays.asList(directory.listFiles());
            Collections.sort(directChildren, fileComparator);
            ArrayList<ModuleFile> pendingFiles = new ArrayList<>(directChildren.size());

            for (File file : directChildren) {
                pendingFiles.add(new ModuleFile(context.get(), file));
                if (file.isDirectory()) {
                    File[] childFiles = file.listFiles(File::isFile);
                    List<File> childFileList = Arrays.asList(childFiles);
                    pendingFiles.addAll(childFileList.stream().sorted(fileComparator).map(f -> new ModuleFile(context.get(), f)).collect(Collectors.toList()));
                }
            }

            return pendingFiles;
        }

        @Override
        protected void onPostExecute(List<ModuleFile> files) {
            ModuleFilesAdapter.this.pendingFiles = files;
            handler.sendEmptyMessage(0);
        }
    }

    private void updateFiles(){
        new UpdateFilesTask(context.get()).execute(handler);
    }

    private Handler handler;
    private WeakReference<Context> context;

    ModuleFilesAdapter(Context c) {
        handler = new ModuleFilesAdapterSwapFilesHandler(this);
        context = new WeakReference<>(c);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (directoryObserver != null)
            directoryObserver.stopWatching();
    }

    void setDirectory(String path) {
        if (directoryObserver != null)
            directoryObserver.stopWatching();
        directoryObserver = new ModuleDirectoryObserver(path, true);
        updateFiles();
    }

    void setSortBy(SortBy sortBy) {
        fileComparatorRef.set(sortBy.getComparator());
    }

    @NonNull
    @Override
    public ModuleFileView onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ModuleFileView(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.module_file_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ModuleFileView moduleFileView, int i) {
        if (files != null && !files.isEmpty()) {
            ModuleFile fileForView = files.get(i);
            if (fileForView.file.isDirectory())
                moduleFileView.setDirectory(fileForView.file, i == 0);
            else
                moduleFileView.setFile(fileForView.file.getName(), fileForView.openFileIntent, i == 0, !fileForView.file.getParent().equals(directoryObserver.directoryPath));
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

class ModuleLinksAdapter extends RecyclerView.Adapter<ModuleLinkView> {
    ModuleAndLinks moduleData;
    List<ModuleLink> data;

    @NonNull
    @Override
    public ModuleLinkView onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ModuleLinkView(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.module_link_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ModuleLinkView moduleLinkView, int idx) {
        if (data == null) return;
        moduleLinkView.setLink(moduleData, data.get(idx));
    }

    @Override
    public int getItemCount() {
        if (data == null) return 0;
        return data.size();
    }
}

class ModuleViewAdapter extends RecyclerView.Adapter<ModuleView> {
    Observer<List<ModuleAndLinks>> moduleObserver = new Observer<List<ModuleAndLinks>>() {
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

    private WeakReference<AppCompatActivity> activity;
    private RecyclerView.RecycledViewPool linkPool;
    private RecyclerView.RecycledViewPool filePool;
    public ModuleViewAdapter(AppCompatActivity activity, RecyclerView.RecycledViewPool linkPool, RecyclerView.RecycledViewPool filePool) {
        this.activity = new WeakReference<>(activity);
        this.linkPool = linkPool;
        this.filePool = filePool;
    }

    @NonNull
    @Override
    public ModuleView onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ModuleView(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.module, viewGroup, false), activity.get(), linkPool, filePool, true);
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