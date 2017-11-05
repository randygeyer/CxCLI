//package com.checkmarx.cxviewer.ws.results;
//
//import com.checkmarx.cxviewer.ws.generated.CxWSBasicResponse;
//import java.util.ArrayList;
//import java.util.List;
//
//import org.jdom.Element;
//
//import com.checkmarx.login.soap.dto.TeamDTO;
//import com.checkmarx.cxviewer.ws.generated.ArrayOfGroup;
//import com.checkmarx.cxviewer.ws.generated.CxWSResponseGroupList;
//import com.checkmarx.cxviewer.ws.generated.Group;
//
//public class GetTeamsListResult extends SimpleResult {
//	private List<TeamDTO> teams;
//
//	private List<Group> teamsList;
//
//	public List<TeamDTO> getTeams() {
//		return teams;
//	}
//
//	public GetTeamsListResult() {
//		teams = new ArrayList<TeamDTO>();
//	}
//
//	@Override
//	protected void parseReturnValue(Element returnValueNode) {
//		if (teams == null) {
//			teams = new ArrayList<TeamDTO>();
//		}
//		if (teams.size() > 0) {
//			teams = new ArrayList<TeamDTO>();
//		}
//		for (Object childObj : returnValueNode.getChildren()) {
//			Element child = (Element) childObj;
//			teams.add(new TeamDTO(child.getAttribute("Id").getValue(), child.getAttribute("Name").getValue()));
//		}
//	}
//
//	@Override
//	public String toString() {
//		String result;
//		if (isSuccessfulResponse()) {
//			result = "" + this.getClass().getSimpleName() + "(teams.size():" + teams.size() + ", teams:"+teams+")";
////			result = "" + this.getClass().getSimpleName() + "(presets.size():" + presets.size() + ")";
//		} else {
//			result = "" + this.getClass().getSimpleName() + "(Message:" + getErrorMessage() + ")";
//		}
//		return result;
//	}
//
//	@Override
//	protected void parseReturnValue(CxWSBasicResponse responseObject) {
//		if (responseObject instanceof CxWSResponseGroupList) {
//
//			CxWSResponseGroupList teamsSetList = (CxWSResponseGroupList) responseObject;
//			ArrayOfGroup array = teamsSetList.getGroupList();
//			if (array != null) {
//				teamsList = array.getGroup();
//				for (Group gr : teamsList) {
//					teams.add(new TeamDTO(gr.getID(), gr.getGroupName()));
//				}
//			}
//		} else {
//			throw new IllegalArgumentException(getInvalidTypeErrMsg(responseObject));
//		}
//	}
//
//	public List<Group> getTeamsList() {
//		return teamsList;
//	}
//}
