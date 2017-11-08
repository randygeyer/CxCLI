package com.checkmarx.cxconsole.commands.constants;

import com.checkmarx.cxviewer.ws.generated.SourceLocationType;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public enum LocationType {

	FOLDER("folder"),
	SHARED("shared"),
	TFS("TFS"),
	SVN("SVN"),
    PERFORCE("Perforce"),
	GIT("GIT");
	
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
	
	public static SourceLocationType getCorrespondingType(LocationType locationType) {
		switch (locationType) {
			case FOLDER:
				return SourceLocationType.LOCAL;
			case SHARED:
				return SourceLocationType.SHARED;
			case TFS:
			case SVN:
			case GIT:
			case PERFORCE:
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
