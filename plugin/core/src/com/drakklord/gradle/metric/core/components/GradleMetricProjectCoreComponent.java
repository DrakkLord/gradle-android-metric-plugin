package com.drakklord.gradle.metric.core.components;

import com.android.tools.idea.gradle.project.build.invoker.GradleBuildInvoker;
import com.android.tools.idea.gradle.project.build.invoker.GradleInvocationResult;
import com.android.tools.idea.gradle.project.facet.gradle.GradleFacet;
import com.android.tools.idea.gradle.project.facet.java.JavaFacet;
import com.android.tools.idea.gradle.project.sync.GradleSyncListener;
import com.android.tools.idea.gradle.project.sync.GradleSyncState;
import com.android.tools.idea.gradle.util.GradleUtil;
import com.android.tools.idea.model.AndroidModel;
import com.drakklord.gradle.metric.core.Constants;
import com.drakklord.gradle.metric.core.Extensions;
import com.drakklord.gradle.metric.core.contributor.*;
import com.drakklord.gradle.metric.core.model.MetricGradleModel;
import com.drakklord.gradle.metric.core.contributor.model.GradleMetricEntryWrapper;
import com.drakklord.gradle.metric.core.contributor.model.GradleMetricResultContributor;
import com.drakklord.gradle.metric.core.view.GradleMetricConfigReportDialog;
import com.drakklord.gradle.metric.core.view.GradleMetricResultsToolWindowFactory;
import com.drakklord.gradle.metric.core.view.GradleMetricResultsView;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.TransactionGuard;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId;
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationEvent;
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener;
import com.intellij.openapi.externalSystem.service.notification.ExternalSystemNotificationManager;
import com.intellij.openapi.externalSystem.service.notification.NotificationCategory;
import com.intellij.openapi.externalSystem.service.notification.NotificationData;
import com.intellij.openapi.externalSystem.service.notification.NotificationSource;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.util.containers.HashSet;
import com.intellij.util.ui.UIUtil;
import net.n3.nanoxml.*;
import org.gradle.internal.invocation.BuildAction;
import org.gradle.tooling.model.gradle.GradleBuild;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.annotations.NotNull;
import com.android.tools.idea.gradle.util.BuildMode;
import org.jetbrains.jps.android.model.impl.JpsAndroidModuleProperties;

import java.io.*;
import java.util.*;

/**
 * Project component that accesses the built in android gradle plugin and handles metric contributor plugins.
 * Created by DrakkLord on 2015.11.20..
 */
public class GradleMetricProjectCoreComponent extends AbstractProjectComponent implements GradleInvocationListener.IGradleInvocationResult, GradleMetricUtil, GradleSyncListener {

    private static final String ANDROID_PLUGIN_ID = "org.jetbrains.android";

    private static final String TAG = "Gradle code metrics: ";

    public static final NotificationGroup LOGGING_NOTIFICATION = NotificationGroup.logOnlyGroup("Android-Gradle Metrics (Logging)");

    private boolean mInitialized;
    private volatile boolean mMetricCheckInProgress;
    private ExternalSystemNotificationManager myNotificationManager;

    private List<String> pendingTaskList;
    private boolean pendingTaskResult;
    private ProgressIndicator pendingIndicator;
    private double extFraction;
    private int collectorIndex;
    private HashMap<GradleMetricContributor, GradleMetricResultContributor> fullMetricContainer;

    public GradleMetricProjectCoreComponent(@NotNull final Project project) {
        super(project);
        GradleSyncState.subscribe(myProject, this);
    }

    @Override
    @NotNull
    public String getComponentName() {
        return "GradleMetricProjectCoreComponent";
    }

    @Override
    public void projectOpened() {
        super.projectOpened();
        mInitialized = false;
        mMetricCheckInProgress = false;
        myNotificationManager = ExternalSystemNotificationManager.getInstance(myProject);

        final String r = checkAndroidGradlePlugin();
        if (r != null) {
            showBasicMessage(Constants.PLUGIN_NAME, r, NotificationCategory.ERROR);
            return;
        }
        if (!isExtensionsAvailable()) {
            showBasicMessage(Constants.PLUGIN_NAME, Constants.NO_EXTENSIONS, NotificationCategory.ERROR);
        }
        mInitialized = true;
    }

    @Override
    public void projectClosed() {
        super.projectClosed();
        mInitialized = false;
        mMetricCheckInProgress = false;
    }

    public Project getProject() {
        return myProject;
    }

    public boolean isExtensionsAvailable() {
        return Extensions.EP_NAME.getExtensions().length > 0;
    }

    public boolean isInitialized() {
        return mInitialized;
    }

    public boolean isMetricCheckInProgress() {
        return mMetricCheckInProgress;
    }

    private boolean moduleHasMetricTasks(Module m) {
        if (!m.isLoaded() || m.isDisposed()) {
            return false;
        }
        MetricGradleModel mm = MetricGradleModel.get(m);
        return mm != null && mm.hasModels();
    }

    public boolean hasTasksToExecute(MetricGradleModel mm) {
        if (!mInitialized) {
            return false;
        }

        if (mm != null) {
            return mm.hasModels();
        }

        Module[] modules = ModuleManager.getInstance(myProject).getModules();
        for (Module m : modules) {
            if (moduleHasMetricTasks(m)) {
                return true;
            }
        }
        return false;
    }

    private Collection<GradleMetricContributor> getContributorPlugins() {
        final ArrayList<GradleMetricContributor> result = new ArrayList<GradleMetricContributor>();
        final GradleMetricContributor[] exts = Extensions.EP_NAME.getExtensions();
        for (GradleMetricContributor c : exts) {
            result.add(c);
        }
        return result;
    }

    private HashMap<GradleMetricContributor, HashMap<Module, String> > getProblemReports() {
        // collect eligible modules
        final Module[] modules = ModuleManager.getInstance(myProject).getModules();
        final HashSet<Module> modulesFiltered = new HashSet<Module>();
        for (Module m : modules) {
            if (!m.isLoaded() || m.isDisposed()) {
                continue;
            }

            MetricGradleModel mm = MetricGradleModel.get(m);
            if (mm != null && mm.hasModels()) {
                modulesFiltered.add(m);
            }
        }

        final HashMap<GradleMetricContributor, HashMap<Module, String> > problemReports = new HashMap<GradleMetricContributor, HashMap<Module, String>>();
        final GradleMetricContributor[] exts = Extensions.EP_NAME.getExtensions();
        for (GradleMetricContributor c : exts) {
            for (Module m : modulesFiltered) {
                final MetricGradleModel mm = MetricGradleModel.get(m);
                if (mm != null && mm.hasModels()) {
                    final String problem = c.getConfigurationIssueReport(mm.getModelFor(c));
                    if (problem == null) {
                        continue;
                    }

                    HashMap<Module, String> p = problemReports.get(c);
                    if (p == null) {
                        p = new HashMap<Module, String>();
                    }
                    p.put(m, problem);
                    problemReports.put(c, p);
                }
            }
        }
        return problemReports;
    }

    private void performMetricConfigCheck() {
        if (mMetricCheckInProgress || !myProject.isOpen()) {
            return;
        }
        HashMap<GradleMetricContributor, HashMap<Module, String> > problemReports = getProblemReports();
        if (!problemReports.isEmpty()) {
            LOGGING_NOTIFICATION.createNotification("There are configuration problems with gradle metric plugin", MessageType.ERROR).setImportant(true).notify(myProject);
            performMetricConfigReport();
        }
    }

    public void performMetricConfigReport() {
        if (mMetricCheckInProgress || !myProject.isOpen()) {
            return;
        }
        GradleMetricConfigReportDialog.create(getProblemReports(), getContributorPlugins());
    }

    public void performMetricCheck(MetricGradleModel mi) {
        if (mMetricCheckInProgress || !myProject.isOpen()) {
            return;
        }
        if (!isExtensionsAvailable()) {
            performMetricConfigReport();
            return;
        }
        if (!hasTasksToExecute(mi)) {
            showBasicMessage("There are no metric tasks to execute!", NotificationCategory.ERROR);
            return;
        }

        mMetricCheckInProgress = true;

        final HashSet<String> taskSet = new HashSet<String>();


        Module[] modules = ModuleManager.getInstance(myProject).getModules();
        for (Module m : modules) {
            if (!m.isLoaded() || m.isDisposed()) {
                continue;
            }

            if (mi != null && !mi.getModuleName().equalsIgnoreCase(m.getName())) {
                continue;
            }

            MetricGradleModel mm = MetricGradleModel.get(m);
            if (mm != null && mm.hasModels()) {
                final String gradlePath = mm.getGradePath();

                // ask the contributors to tell us what tasks we need to run
                final GradleMetricContributor[] exts = Extensions.EP_NAME.getExtensions();
                for (GradleMetricContributor c : exts) {
                    List<String> moduleTasks = c.getGradleTasksToExecute(mm.getModelFor(c));
                    if (moduleTasks == null || moduleTasks.isEmpty()) {
                        continue;
                    }
                    for (String s : moduleTasks) {
                        taskSet.add(gradlePath + ":" + s);
                    }
                }
            }
        }

        if (taskSet.isEmpty()) {
            showBasicMessage("extension plugins returned an empty list of tasks to execute", NotificationCategory.ERROR);
            mMetricCheckInProgress = false;
            return;
        }

        final GradleBuildInvoker gi = GradleBuildInvoker.getInstance(myProject);
        pendingTaskList = new ArrayList<String>(taskSet);
        final File rootGradleBuildFile = GradleUtil.getGradleBuildFilePath(new File(myProject.getBaseDir().getPath()));
        final GradleBuildInvoker.Request rq = new GradleBuildInvoker.Request(myProject, rootGradleBuildFile, pendingTaskList);
        rq.setTaskListener(new GradleInvocationListener(this));
        gi.executeTasks(rq);
    }

    // NOTE: this runs on the 'UI thread'!
    protected void onCollectorTaskCompleted(boolean success) {
        final ToolWindowManager mgr = ToolWindowManager.getInstance(myProject);
        if (mgr == null) {
            addToEventLog("failed to get gradle metrics tool window", MessageType.ERROR);
            fullMetricContainer = null;
            mMetricCheckInProgress = false;
            return;
        }
        final ToolWindow resultsWindow = mgr.getToolWindow(GradleMetricResultsToolWindowFactory.ID);
        if (resultsWindow == null) {
            addToEventLog("failed to get gradle metrics tool window", MessageType.ERROR);
            fullMetricContainer = null;
            mMetricCheckInProgress = false;
            return;
        }
        GradleMetricResultsView.getInstance(myProject).clearResults();

        if (pendingTaskResult && mMetricCheckInProgress) {
            if (!success) {
                addToEventLog(TAG + "result collection cancelled", MessageType.INFO);
            } else if (fullMetricContainer == null) {
                showBasicMessage("result collection failed", NotificationCategory.ERROR);
            } else {
                if (fullMetricContainer.isEmpty()) {
                    showBasicMessage("no issues found", NotificationCategory.SIMPLE);
                } else {
                    int issueCount = getNumberOfIssues(fullMetricContainer);
                    addToEventLog(TAG + "found " + issueCount + " issue" + (issueCount > 1 ? "s" : "") + " [without duplicate removal]", MessageType.WARNING);
                }
                // TODO: show the UI with the results! + make sure the UI gets hold onto the reference of the metric list!
                final HashMap<GradleMetricContributor, GradleMetricResultContributor> metricHolder = fullMetricContainer;
                resultsWindow.activate(new Runnable() {
                    @Override
                    public void run() {
                        GradleMetricResultsView.getInstance(myProject).showResults(metricHolder);
                    }
                });
            }
        } else {
            showBasicMessage("gradle tasks failed to execute", NotificationCategory.ERROR);
        }

        if (resultsWindow.isVisible()) {
            resultsWindow.getComponent().updateUI();
        }
        fullMetricContainer = null;
        mMetricCheckInProgress = false;
    }

    /** Function used to perform metric results collection. */
    protected void onCollectorTaskRun(ProgressIndicator indicator) {
        if (!isExtensionsAvailable() || !mMetricCheckInProgress || !myProject.isOpen()) {
            return;
        }

        // figure out the modules we are interested in and pass them to each plugin extension
        final ArrayList<Module> targetModules = new ArrayList<Module>();
        Module[] modules = ModuleManager.getInstance(myProject).getModules();
        for (Module m : modules) {
            if (!m.isLoaded() || m.isDisposed()) {
                continue;
            }

            MetricGradleModel mm = MetricGradleModel.get(m);
            if (mm != null && mm.hasModels()) {
                targetModules.add(m);
            }
        }
        if (targetModules.isEmpty()) {
            addToEventLog("unable to collect metric results failed to find modules to collect results from", MessageType.ERROR);
            indicator.cancel();
            return;
        }

        final GradleMetricContributor[] exts = Extensions.EP_NAME.getExtensions();
        extFraction = 1.0 / (double) exts.length;
        pendingIndicator = indicator;
        collectorIndex = 0;

        fullMetricContainer = new HashMap<GradleMetricContributor, GradleMetricResultContributor>();
        for (GradleMetricContributor c : exts) {
            indicator.setText("Collecting: " + c.getName());
            indicator.setText2("");

//            TODO this functionality should depend on the task container or some other method so the collection is in sync with what the user requested!
            final Module[] mls = new Module[targetModules.size()];
            targetModules.toArray(mls);
            List<GradleMetricEntry> r  = new ArrayList<GradleMetricEntry>();
            try {
                for (Module m : targetModules) {
                    MetricGradleModel mm = MetricGradleModel.get(m);
                    if (mm != null) {
                        final GradleMetricModelHolder holder = mm.getModelFor(c);
                        if (holder != null) {
                            List<GradleMetricEntry> list = c.onGradleTasksCompleted(this, m, pendingTaskResult);
                            if (list != null) {
                                r.addAll(list);
                            }
                        }
                    }
                }
            } catch(GradleMetricContributorException e) {
                fullMetricContainer = null;
                addToEventLog("Gradle code metrics: collection error >> " + e.getMessage(), MessageType.ERROR);
                showBasicMessage("report collection failed : " + c.getName(), NotificationCategory.ERROR);
                e.printStackTrace();
                break;
            }

            if (indicator.isCanceled()) {
                fullMetricContainer = null;
                break;
            }
            if (!r.isEmpty()) {
                for (GradleMetricEntry en : r) {
                    final GradleMetricEntryWrapper e = new GradleMetricEntryWrapper(en, myProject);

                    GradleMetricResultContributor ct;
                    if (fullMetricContainer.containsKey(e.source)) {
                        ct = fullMetricContainer.get(e.source);
                    } else {
                        ct = new GradleMetricResultContributor(e.source, myProject);
                    }
                    ct.addMetric(e);
                    fullMetricContainer.put(e.source, ct);
                }
            }
            collectorIndex++;
        }

        pendingIndicator = null;
    }

    // called from the extension via the util class
    @Override
    public void setCollectorProgressFraction(double pct) {
        if (pendingIndicator == null || !mMetricCheckInProgress) {
            return;
        }
        pendingIndicator.setFraction(collectorIndex * extFraction + pct * extFraction);
    }

    // called from the extension via the util class
    @Override
    public void setCollectorProgressMessage(String text) {
        if (pendingIndicator != null && mMetricCheckInProgress) {
            pendingIndicator.setText2(text);
        }
    }

    // called from the extension via the util class
    @Override
    public boolean isCollectorCancelled() {
        return !mMetricCheckInProgress || (pendingIndicator != null && pendingIndicator.isCanceled());
    }

    @Override
    public String getModuleGradleBuildPath(Module m) {
        if (m == null) {
            return null;
        }
        if (!m.isLoaded() || m.isDisposed()) {
            return null;
        }

        GradleFacet gradleFacet = GradleFacet.getInstance(m);
        if (gradleFacet == null) {
            return null;
        }

        String gradlePath = gradleFacet.getConfiguration().GRADLE_PROJECT_PATH;
        if (StringUtil.isEmpty(gradlePath)) {
            // Gradle project path is never, ever null. If the path is empty, it shows as ":". We had reports of this happening. It is likely that
            // users manually added the Android-Gradle facet to a project. After all it is likely not to be a Gradle module. Better quit and not
            // build the module.
            return null;
        }

        AndroidFacet androidFacet = AndroidFacet.getInstance(m);
        if (androidFacet != null) {
            JpsAndroidModuleProperties properties = androidFacet.getProperties();
            if (StringUtil.isNotEmpty(properties.ASSEMBLE_TASK_NAME)) {
                final AndroidModel am = androidFacet.getAndroidModel();
                if (am == null) {
                    return null;
                }
                final File rootPath = am.getRootDirPath();
                if (rootPath == null) {
                    return null;
                }
                return rootPath.getAbsolutePath() + File.separatorChar + "build";
            }
        } else {

            JavaFacet javaFacet = JavaFacet.getInstance(m);
            if (javaFacet != null && javaFacet.getGradleTaskName(BuildMode.COMPILE_JAVA) != null) {
                return new File(javaFacet.getConfiguration().BUILD_FOLDER_PATH).getAbsolutePath();
            }
        }
        return null;
    }

    @Override
    public boolean isFilePartOfTheProject(@NotNull File file) {
        final VirtualFile f = VirtualFileManager.getInstance().findFileByUrl("file://" + file.getAbsolutePath());
        if (f == null) {
            return false;
        }
        if (!f.exists()) {
            return false;
        }

        Module[] mlist = ModuleManager.getInstance(myProject).getModules();
        for (Module m : mlist) {
            if (m.getModuleScope(true).contains(f)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public IXMLElement parseXMLFrom(@NotNull File xmlFile) throws GradleMetricContributorException {
        IXMLParser p;
        try {
            p = XMLParserFactory.createDefaultXMLParser();
        } catch (ClassNotFoundException e) {
            throw (GradleMetricContributorException) new GradleMetricContributorException("xml parser error").initCause(e);
        } catch (InstantiationException e) {
            throw (GradleMetricContributorException) new GradleMetricContributorException("xml parser error").initCause(e);
        } catch (IllegalAccessException e) {
            throw (GradleMetricContributorException) new GradleMetricContributorException("xml parser error").initCause(e);
        }
        if (p == null) {
            throw new GradleMetricContributorException("failed to create xml parser");
        }

        IXMLReader rd;
        try {
            rd = new StdXMLReader(new FileInputStream(xmlFile));
        } catch (IOException e) {
            throw (GradleMetricContributorException) new GradleMetricContributorException("xml reader error").initCause(e);
        }
        p.setReader(rd);

        IXMLElement rr;
        try {
            rr = (IXMLElement) p.parse();
        } catch (XMLException e) {
            throw (GradleMetricContributorException) new GradleMetricContributorException("xml parsing error").initCause(e);
        }
        if (rr == null) {
            throw new GradleMetricContributorException("xml parsing error");
        }
        return rr;
    }

    private void recursiveFileLister(ArrayList<String> list, File baseDir, File rootDir, FilenameFilter filter) {
        if (!rootDir.isDirectory()) {
            return;
        }

        // list files from directory
        String[] files = rootDir.list(filter);
        if (files == null) {
            return;
        }
        for (String s : files) {
            final File f = new File(rootDir, s);
            if (f.exists() && f.isFile()) {
                list.add(rootDir.toString().replace(baseDir.toString(), "") + File.separator + s);
            }
        }

        // list and scan directories within the root directory
        String[] dirList = rootDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return file.isDirectory() && !file.isFile();
            }
        });
        if (dirList == null) {
            return;
        }
        for (String s : dirList) {
            final File f = new File(rootDir, s);
            if (f.exists() && f.isDirectory()) {
                recursiveFileLister(list, baseDir, f, filter);
            }
        }
    }

    @Override
    public Collection<String> listFilesRecursively(File rootDir, FilenameFilter filter) {
        final ArrayList<String> files = new ArrayList<String>();
        recursiveFileLister(files, rootDir, rootDir, filter);
        return files;
    }

    public boolean isLastSyncFailed() {
        if (!mInitialized) {
            return false;
        }
        return GradleSyncState.getInstance(myProject).lastSyncFailed();
    }

    public boolean isSyncInProgress() {
        return mInitialized && GradleSyncState.getInstance(myProject).isSyncInProgress();
    }

    public void showBasicMessage(String text, NotificationCategory type) {
        showBasicMessage(Constants.PLUGIN_NAME, text, type);
    }

    public void showBasicMessage(String title, String text, NotificationCategory type) {
        NotificationData notification =
                new NotificationData(title, text, type, NotificationSource.TASK_EXECUTION, null, 0, 0, true);
        myNotificationManager.showNotification(GradleUtil.GRADLE_SYSTEM_ID, notification);
    }

    public void addToEventLog(@NotNull String message, @NotNull MessageType type) {
        LOGGING_NOTIFICATION.createNotification(message, type).notify(myProject);
    }

    @Override
    public void syncStarted(@NotNull Project project, boolean b, boolean b1) {

    }

    @Override
    public void setupStarted(@NotNull Project project) {
    }

    @Override
    public void syncSucceeded(@NotNull Project project) {
        if (myProject == project) {
            performMetricConfigCheck();
        }
    }

    @Override
    public void syncFailed(@NotNull Project project, @NotNull String s) {

    }

    @Override
    public void syncSkipped(@NotNull Project project) {

    }

    @Override
    public void initComponent() {

    }

    @Override
    public void disposeComponent() {

    }

    @Override
    public void onGradleInvocationCompleted(GradleInvocationListener.EGradleInvocationResult result) {
        if (pendingTaskList == null || pendingTaskList.isEmpty()) {
            mMetricCheckInProgress = false;
            return;
        }
        pendingTaskList = null;
        pendingTaskResult = result == GradleInvocationListener.EGradleInvocationResult.GI_SUCCESS;

        final GradleMetricProjectCoreComponent.GradleResultCollectorTask collector = new GradleMetricProjectCoreComponent.GradleResultCollectorTask(myProject);
        if (ApplicationManager.getApplication().isDispatchThread()) {
            collector.queue();
        } else {
            UIUtil.invokeAndWaitIfNeeded(new Runnable() {
                @Override
                public void run() {
                    collector.queue();
                }
            });
        }
    }

    private class GradleResultCollectorTask extends Task.Backgroundable {

        GradleResultCollectorTask(@NotNull Project project) {
            super(project, "Gradle Code Metric Results", true, new PerformInBackgroundOption() {

                @Override
                public boolean shouldStartInBackground() {
                    return true;
                }

                @Override
                public void processSentToBackground() {
                }
            });
        }

        @Override
        public String getProcessId() {
            return "GradleResultCollectorTask";
        }

        /** This callback will be invoked on AWT dispatch thread. */
        @Override
        public void onCancel() {
            onCollectorTaskCompleted(false);
        }

        /** This callback will be invoked on AWT dispatch thread. */
        @Override
        public void onSuccess() {
            onCollectorTaskCompleted(true);
        }

        @Override
        public void run(@NotNull ProgressIndicator indicator) {
            onCollectorTaskRun(indicator);
        }
    }

    public String checkAndroidGradlePlugin() {

        if (!PluginManager.isPluginInstalled(PluginId.getId(ANDROID_PLUGIN_ID))) {
            return "android gradle plugin is not installed";
        }

        final IdeaPluginDescriptor agradle = PluginManager.getPlugin(PluginId.getId(ANDROID_PLUGIN_ID));
        if (agradle == null) {
            return "unable to get android gradle plugin";
        }

        if (!agradle.isEnabled()) {
            return "android gradle plugin is not enabled";
        }

        final String ver = agradle.getVersion();
        if (ver == null || ver.isEmpty()) {
            return "failed to get android gradle plugin version";
        }

        final String[] verSplit = ver.split("\\.");
        if (verSplit == null || verSplit.length < 1) {
            return  "failed to parse android gradle plugin version";
        }

        final int baseVersion = Integer.valueOf(verSplit[0]);
        if (baseVersion < 10) {
            return "android gradle plugin version is older than 10";
        }

        final ClassLoader cs = agradle.getPluginClassLoader();
        if (cs == null) {
            return "failed to get android gradle plugin's class loader";
        }

        return null;
    }

    private int getNumberOfIssues(HashMap<GradleMetricContributor, GradleMetricResultContributor> issues) {
        int out = 0;
        for (GradleMetricResultContributor e : issues.values()) {
            out += e.getProblemCount();
        }
        return out;
    }
}
