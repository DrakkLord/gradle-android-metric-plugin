package com.drakklord.gradle.metric.core.view;

import com.drakklord.gradle.metric.core.Extensions;
import com.drakklord.gradle.metric.core.contributor.GradleMetricContributor;
import com.drakklord.gradle.metric.core.resources.PluginBundle;
import com.intellij.openapi.module.Module;
import com.intellij.ui.JBColor;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class GradleMetricConfigReportDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JLabel mainInfoText;
    private JTextPane mainDescText;
    private final HashMap<GradleMetricContributor, HashMap<Module, String> > problemReports;

    public GradleMetricConfigReportDialog(HashMap<GradleMetricContributor, HashMap<Module, String> > problemReportsIn,
                                          Collection<GradleMetricContributor> mertricPlugins) {
        problemReports = problemReportsIn;

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onDone();
            }
        });

        mainDescText.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    if(Desktop.isDesktopSupported()) {
                        try {
                            Desktop.getDesktop().browse(e.getURL().toURI());
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        } catch (URISyntaxException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            }
        });

        final GradleMetricContributor[] exts = Extensions.EP_NAME.getExtensions();
        if (exts.length != 0) {
            mainInfoText.setText("There are " + exts.length + " plugins installed");
            mainInfoText.setForeground(JBColor.BLACK);

            if (problemReports.size() > 0) {
                final StringBuilder sb = new StringBuilder();

                sb.append("There are configuration problems with the current project:<br><br>");
                for (Map.Entry<GradleMetricContributor, HashMap<Module, String> > e : problemReports.entrySet()) {
                    sb.append(e.getKey().getName() + " plugin reports<br>");

                    if (e.getValue() == null) {
                        continue;
                    }
                    sb.append("<ul>");
                    for(Map.Entry<Module, String> k : e.getValue().entrySet()) {
                        sb.append("<li>In module '" + k.getKey().getName() + "' : " + k.getValue() + "</li>");
                    }
                    sb.append("</ul>");
                    sb.append("<br>");
                }
                mainDescText.setText(sb.toString());
            } else {
                final StringBuilder sb = new StringBuilder();

                sb.append("No issues<br><br>");
                sb.append("Installed metric contributor plugins:<br><ul>");

                for (GradleMetricContributor c : mertricPlugins) {
                    sb.append("<li>" + c.getName() + "</li>");
                }
                sb.append("</ul>");

                mainDescText.setText(sb.toString());
            }
        } else {
            mainInfoText.setText("No plugins installed!");
            mainInfoText.setForeground(JBColor.RED);

            final StringBuilder sb = new StringBuilder();
            sb.append("The core plugin only implements an API, you must download additional plugins which add support for specific code quality tools:<br>");
            sb.append("<li><a href=\"https://plugins.jetbrains.com/plugin/9197?pr=idea\">Android Gradle Metrics - Checkstyle</a></li>");
            sb.append("<li><a href=\"https://plugins.jetbrains.com/plugin/9198?pr=idea\">Android Gradle Metrics - PMD</a></li>");
            mainDescText.setText(sb.toString());
        }

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onDone();
            }
        });

        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onDone();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onDone() {
        dispose();
    }

    public static void create(HashMap<GradleMetricContributor, HashMap<Module, String>> problemReports,
                              Collection<GradleMetricContributor> plugins) {
        GradleMetricConfigReportDialog dialog = new GradleMetricConfigReportDialog(problemReports, plugins);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setTitle(PluginBundle.message("dialog.report.title"));
        dialog.setVisible(true);
    }
}
