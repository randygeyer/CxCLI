package com.checkmarx.cxconsole.utils;

import java.util.ArrayList;
import java.util.Collection;

public class CustomStringList extends ArrayList<String> {

    public CustomStringList(Collection<? extends String> c) {
        super(c);
    }

    @Override
    public boolean contains(Object o) {
        String paramStr = (String)o;
        for (String s : this) {
            if (paramStr.equalsIgnoreCase(s)) return true;
        }
        return false;
    }
}