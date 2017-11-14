package com.checkmarx.clients.soap.providers.dto;

public class ConfigurationDTO {
    private long id;
    private String name;

    public ConfigurationDTO(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        String result;
        result = "" + this.getClass().getSimpleName() + "(id:" + id + ", name:" + name + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        return result;
    }

}
