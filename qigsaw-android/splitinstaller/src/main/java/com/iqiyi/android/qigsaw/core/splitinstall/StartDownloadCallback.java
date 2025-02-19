/*
 * MIT License
 *
 * Copyright (c) 2019-present, iQIYI, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.iqiyi.android.qigsaw.core.splitinstall;

import com.iqiyi.android.qigsaw.core.splitdownload.DownloadCallback;
import com.iqiyi.android.qigsaw.core.splitrequest.splitinfo.SplitInfo;

import java.util.List;

final class StartDownloadCallback implements DownloadCallback {

    private final SplitInstallInternalSessionState sessionState;

    private final int sessionId;

    private final SplitInstallSessionManager sessionManager;

    private final List<SplitInfo> splitInfoList;

    private final SplitSessionInstaller installer;

    StartDownloadCallback(SplitInstaller splitInstaller,
                          int sessionId,
                          SplitInstallSessionManager sessionManager,
                          List<SplitInfo> splitInfoList) {
        this.sessionId = sessionId;
        this.sessionManager = sessionManager;
        this.installer = new SplitSessionInstallerImpl(splitInstaller, sessionManager, SplitBackgroundExecutor.getExecutor());
        this.splitInfoList = splitInfoList;
        this.sessionState = sessionManager.getSessionState(sessionId);
    }

    @Override
    public void onStart() {
        sessionManager.changeSessionState(sessionId, SplitInstallInternalSessionStatus.DOWNLOADING);
        broadcastSessionStatusChange();
    }

    @Override
    public void onCanceled() {
        sessionManager.changeSessionState(sessionId, SplitInstallInternalSessionStatus.CANCELED);
        broadcastSessionStatusChange();
    }

    @Override
    public void onCanceling() {
        sessionManager.changeSessionState(sessionId, SplitInstallInternalSessionStatus.CANCELING);
        broadcastSessionStatusChange();
    }

    @Override
    public void onProgress(long currentBytes) {
        sessionState.setBytesDownloaded(currentBytes);
        sessionManager.changeSessionState(sessionId, SplitInstallInternalSessionStatus.DOWNLOADING);
        broadcastSessionStatusChange();
    }

    @Override
    public void onCompleted() {
        sessionManager.changeSessionState(sessionId, SplitInstallInternalSessionStatus.DOWNLOADED);
        broadcastSessionStatusChange();
        onInstall();
    }

    @Override
    public void onError(int errorCode) {
        sessionState.setErrorCode(SplitInstallInternalErrorCode.DOWNLOAD_FAILED);
        sessionManager.changeSessionState(sessionId, SplitInstallInternalSessionStatus.FAILED);
        broadcastSessionStatusChange();
    }

    private void onInstall() {
        installer.install(sessionId, splitInfoList);
    }

    private void broadcastSessionStatusChange() {
        sessionManager.emitSessionState(sessionState);
    }
}
