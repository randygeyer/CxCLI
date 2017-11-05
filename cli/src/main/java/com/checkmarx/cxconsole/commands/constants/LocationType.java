package com.checkmarx.cxconsole.commands.constants;

import com.checkmarx.cxviewer.ws.generated.SourceLocationType;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public enum LocationType {

	folder("folder"),
	shared("shared"),
	tfs("TFS"),
	svn("SVN"),
    perforce("Perforce"),
	git("GIT");
	
	private String locationType;
	
	LocationType(String location) {
		this.locationType = location;
	}
	
	public static LocationType byName(String name) {
		LocationType[] vals = values();
		
		for (LocationType value : vals) {
			if (value.locationType.equalsIgnoreCase(name)) {
				return value;
			}
		}
		return null;
	}
	
	public String getLocationType() {
		return locationType;
	}
	
	public SourceLocationType getCorrespondingType() {
		switch (this) {
			case folder:
				return SourceLocationType.LOCAL;
			case shared:
				return SourceLocationType.SHARED;
			case tfs:
			case svn:
			case git:
            case perforce:
				return SourceLocationType.SOURCE_CONTROL;
		}
		
		return null;
	}

    public static String stringOfValues()
    {
        ArrayList<String> locationTypeNames = new ArrayList<>(values().length);
        for (LocationType lt : values())
        {
            locationTypeNames.add(lt.getLocationType());
        }
        return StringUtils.join(locationTypeNames,"|");
    }
}
