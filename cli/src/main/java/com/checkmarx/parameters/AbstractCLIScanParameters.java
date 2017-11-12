package com.checkmarx.parameters;

/**
 * Created by nirli on 30/10/2017.
 */
abstract class AbstractCLIScanParameters {

    static final String KEY_DESCR_INTEND_SINGLE = "\t";
    static final String KEY_DESCR_INTEND_SMALL = "\t\t";

    public abstract String getMandatoryParams();

    public abstract String getKeyDescriptions();

    abstract void initCommandLineOptions();

}
