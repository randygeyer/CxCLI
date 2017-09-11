package com.checkmarx.v2.observers;

public interface PreScanDataObserver {
    void notifyPreScanDataHasArrived();
    void notifyErrorOccurred();
}
