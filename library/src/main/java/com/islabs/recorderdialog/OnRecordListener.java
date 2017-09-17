package com.islabs.recorderdialog;

import java.io.File;

public interface OnRecordListener {
    void onRecorded(File file);

    void onError(Exception e);

    void onDialogDismiss();

    void onPositiveButtonClick();
}
