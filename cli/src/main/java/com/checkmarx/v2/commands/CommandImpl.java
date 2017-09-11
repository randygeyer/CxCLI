package com.checkmarx.v2.commands;

import com.checkmarx.v2.models.IModel;

public abstract class CommandImpl<TResult> implements Command, Runnable {

    protected final IModel model;

    protected CommandImpl(IModel model) {
        this.model = model;
    }

    @Override
    public void execute() {

    }

    @Override
    public void run() {
        try {
            TResult result = handleExecution();
            updateModelState(result);
        } catch (Exception e) {
            notifyErrorHasOccurred(e);
        }
    }

    protected abstract void updateModelState(TResult result);

    protected abstract TResult handleExecution() throws Exception;

    protected void notifyErrorHasOccurred(Exception exception) {
        model.notifyErrorHasOccurred(exception);
    }

}
