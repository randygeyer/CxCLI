package com.checkmarx.cxviewer.ws.results;

import com.checkmarx.cxviewer.ws.generated.CxWSBasicRepsonse;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;

import com.checkmarx.cxviewer.ws.data.TeamsChild;
import com.checkmarx.cxviewer.ws.generated.ArrayOfGroup;
import com.checkmarx.cxviewer.ws.generated.CxWSResponseGroupList;
import com.checkmarx.cxviewer.ws.generated.Group;

public class GetTeamsListResult extends SimpleResult {
	private List<TeamsChild> teams;
	
	private List<Group> teamsList;
	
	public List<TeamsChild> getTeams() {
		return teams;
	}

	public GetTeamsListResult() {
		teams = new ArrayList<TeamsChild>();
	}

	@Override
	protected void parseReturnValue(Element returnValueNode) {
		if (teams == null) {
			teams = new ArrayList<TeamsChild>();
		}
		if (teams.size() > 0) {
			teams = new ArrayList<TeamsChild>();
		}
		for (Object childObj : returnValueNode.getChildren()) {
			Element child = (Element) childObj;
			teams.add(new TeamsChild(child.getAttribute("Id").getValue(), child.getAttribute("Name").getValue()));
		}
	}

	@Override
	public String toString() {
		String result;
		if (isSuccesfullResponce()) {
			result = "" + this.getClass().getSimpleName() + "(teams.size():" + teams.size() + ", teams:"+teams+")";
//			result = "" + this.getClass().getSimpleName() + "(presets.size():" + presets.size() + ")";
		} else {
			result = "" + this.getClass().getSimpleName() + "(Message:" + getErrorMessage() + ")";
		}
		return result;
	}

	@Override
	protected void parseReturnValue(CxWSBasicRepsonse responseObject) {
		if (responseObject instanceof CxWSResponseGroupList) {

			CxWSResponseGroupList teamsSetList = (CxWSResponseGroupList) responseObject;
			ArrayOfGroup array = teamsSetList.getGroupList();
			if (array != null) {
				teamsList = array.getGroup();
				for (Group gr : teamsList) {
					teams.add(new TeamsChild(gr.getID(), gr.getGroupName()));
				}
			}
		} else {
			throw new IllegalArgumentException(getInvalidTypeErrMsg(responseObject));
		}
	}
	
	public List<Group> getTeamsList() {
		return teamsList;
	}
}
