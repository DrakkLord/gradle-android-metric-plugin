package com.drakklord.gradle.metric.core.components;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId;
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationEvent;
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

public class GradleInvocationListener implements ExternalSystemTaskNotificationListener {

    public enum EGradleInvocationResult {
        GI_SUCCESS,
        GI_FAIL,
        GI_CANCELLED
    };

    public interface IGradleInvocationResult {
        void onGradleInvocationCompleted(EGradleInvocationResult result);
    }

    private final IGradleInvocationResult invocationResultCallback;

    GradleInvocationListener(IGradleInvocationResult invocationResult) {
        invocationResultCallback = invocationResult;
    }

    @Deprecated
    @Override
    public void onQueued(@NotNull ExternalSystemTaskId externalSystemTaskId, String s) {
    }

    @Deprecated
    @Override
    public void onStart(@NotNull ExternalSystemTaskId externalSystemTaskId) {
    }

    @Override
    public void onStatusChange(@NotNull ExternalSystemTaskNotificationEvent externalSystemTaskNotificationEvent) {
    }

    @Override
    public void onTaskOutput(@NotNull ExternalSystemTaskId externalSystemTaskId, @NotNull String s, boolean b) {
    }

    @Override
    public void onEnd(@NotNull ExternalSystemTaskId externalSystemTaskId) {
    }

    @Override
    public void onSuccess(@NotNull ExternalSystemTaskId externalSystemTaskId) {
        invocationResultCallback.onGradleInvocationCompleted(EGradleInvocationResult.GI_SUCCESS);
    }

    @Override
    public void onFailure(@NotNull ExternalSystemTaskId externalSystemTaskId, @NotNull Exception e) {
        invocationResultCallback.onGradleInvocationCompleted(EGradleInvocationResult.GI_FAIL);
    }

    @Override
    public void beforeCancel(@NotNull ExternalSystemTaskId externalSystemTaskId) {
    }

    @Override
    public void onCancel(@NotNull ExternalSystemTaskId externalSystemTaskId) {
        invocationResultCallback.onGradleInvocationCompleted(EGradleInvocationResult.GI_CANCELLED);
    }
}
