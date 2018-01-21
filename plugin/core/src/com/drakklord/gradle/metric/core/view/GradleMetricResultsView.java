package com.drakklord.gradle.metric.core.view;

import com.drakklord.gradle.metric.core.contributor.GradleMetricContributor;
import com.drakklord.gradle.metric.core.contributor.GradleMetricEntry;
import com.drakklord.gradle.metric.core.contributor.model.GradleMetricEntryWrapper;
import com.drakklord.gradle.metric.core.contributor.model.GradleMetricResultContributor;
import com.drakklord.gradle.metric.core.view.components.MetricTree;
import com.intellij.analysis.AnalysisUIOptions;
import com.intellij.codeInspection.ui.InspectionResultsView;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.execution.ui.layout.PlaceInGrid;
import com.intellij.ide.DataManager;
import com.intellij.ide.OccurenceNavigator;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.*;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.EditSourceOnDoubleClickHandler;
import com.intellij.util.OpenSourceUtil;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.HashSet;

public class GradleMetricResultsView implements Disposable {

  @NotNull private final Project myProject;
  @NotNull private final MetricTree myTree;

  private JPanel myBasePanel;
  private Splitter mySplitter;

  public GradleMetricResultsView(@NotNull Project project) {
    myProject = project;
    myTree = new MetricTree(myProject);
    Disposer.register(this, myTree);

    initTreeListeners();
  }

  public static GradleMetricResultsView getInstance(@NotNull Project project) {
    return ServiceManager.getService(project, GradleMetricResultsView.class);
  }

  public void createToolWindowContent(@NotNull ToolWindow toolWindow) {
    //Create runner UI layout
    RunnerLayoutUi.Factory factory = RunnerLayoutUi.Factory.getInstance(myProject);
    RunnerLayoutUi layoutUi = factory.create("", "", "session", myProject);

    // Adding actions
    DefaultActionGroup group = new DefaultActionGroup();
 /*
    if (layoutUi.getOptions() != null) {
       layoutUi.getOptions().setLeftToolbar(group, ActionPlaces.UNKNOWN);
    }
*/
    Content tree = layoutUi.createContent(GradleMetricResultsToolWindowFactory.ID, myTree.getComponent(), "hello", null, null);
    layoutUi.addContent(tree, 0, PlaceInGrid.right, false);

    mySplitter = new OnePixelSplitter(false, AnalysisUIOptions.getInstance(myProject).SPLITTER_PROPORTION);

    mySplitter.setFirstComponent(ScrollPaneFactory.createScrollPane(myTree, SideBorder.LEFT | SideBorder.RIGHT));
// splitter is 50/50 by default, this command adds a second panel
//    mySplitter.setSecondComponent(getNothingToShowTextLabel());

    myBasePanel.add(mySplitter, BorderLayout.CENTER);

//    JComponent layoutComponent = layoutUi.getComponent();
//    myBasePanel.add(layoutComponent, BorderLayout.CENTER);

    //noinspection ConstantConditions
    Content content = ContentFactory.SERVICE.getInstance().createContent(myBasePanel, null, true);
    toolWindow.getContentManager().addContent(content);
  }

  @NotNull
  private static JLabel getNothingToShowTextLabel() {
    final JLabel multipleSelectionLabel = new JBLabel("Nothing to show");
    multipleSelectionLabel.setVerticalAlignment(SwingConstants.TOP);
    multipleSelectionLabel.setBorder(IdeBorderFactory.createEmptyBorder(5, 14, 0, 0));
    return multipleSelectionLabel;
  }

  @Override
  public void dispose() {
  }

  public void showResults(HashMap<GradleMetricContributor, GradleMetricResultContributor> entries) {
      myTree.showResults(entries);
      TreeUtil.selectFirstNode(myTree);
  }

  public void clearResults() {
    myTree.clearResults();
    TreeUtil.selectFirstNode(myTree);
  }

  private void initTreeListeners() {
    myTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
      @Override
      public void valueChanged(TreeSelectionEvent e) {
//        System.out.println("dblclick");
/*
        if (myTree.isUnderQueueUpdate()) return;
        syncRightPanel();
        if (isAutoScrollMode()) {
          OpenSourceUtil.openSourcesFrom(DataManager.getInstance().getDataContext(InspectionResultsView.this), false);
        }*/
      }
    });

    EditSourceOnDoubleClickHandler.install(myTree, new Runnable() {
        @Override
        public void run() {
            myTree.navigateToSelection();
        }
    });

    myTree.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            myTree.navigateToSelection();
        }
      }
    });

    myTree.addMouseListener(new PopupHandler() {
      @Override
      public void invokePopup(Component comp, int x, int y) {
//        System.out.println("popup menu invoked");
          // popupInvoked(comp, x, y);
      }
    });

    SmartExpander.installOn(myTree);
  }
}
