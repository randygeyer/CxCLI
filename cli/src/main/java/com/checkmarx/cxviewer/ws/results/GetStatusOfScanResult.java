package com.checkmarx.cxviewer.ws.results;

import org.jdom.Element;

import com.checkmarx.cxviewer.ws.generated.CurrentStatusEnum;
import com.checkmarx.cxviewer.ws.generated.CxDateTime;
import com.checkmarx.cxviewer.ws.generated.CxWSBasicRepsonse;
import com.checkmarx.cxviewer.ws.generated.CxWSResponseScanStatus;

public class GetStatusOfScanResult extends SimpleResult {

	private String runId;

	private CurrentStatusEnum runStatus;

	private int totalPercent = 0;

	private String currentStage;

	private String stageName;

	private int currentStagePercent = 0;

	private String stageMessage;

	private String stepMessage;

	private String timeStarted;

	private String timeFinished;

	private String queuePosition;

	private String details;
	
	private long projectId;
	
	private long scanId;
	
	private long resultId;
	
	private long taskId;
	
	private CxWSResponseScanStatus statusResponse;

	public String getRunId() {
		return runId;
	}

	public CurrentStatusEnum getRunStatus() {
		return runStatus;
	}

	public int getTotalPercent() {
		return totalPercent;
	}

	public String getCurrentStage() {
		return currentStage == null ? "" : currentStage;
	}

	public String getStageName() {
		return stageName == null ? "" : stageName;
	}

	public int getCurrentStagePercent() {
		return currentStagePercent;
	}

	public String getStageMessage() {
		return stageMessage == null ? "" : stageMessage;
	}

	public String getStepMessage() {
		return stepMessage == null ? "" : stepMessage;
	}

	public String getTimeStarted() {
		return timeStarted == null ? "" : timeStarted;
	}

	public String getTimeFinished() {
		return timeFinished == null ? "" : timeFinished;
	}

	public String getQueuePosition() {
		return queuePosition == null ? "" : queuePosition;
	}

	public String getDetails() {
		return details == null ? "" : details;
	}
	
	public long getProjectId() {
		return projectId;
	}
	
	public long getScanId() {
		return scanId;
	}
	
	public long getTaskId() {
		return taskId;
	}
	
	public long getResultId() {
		return resultId;
	}

	// <ReturnValue>
	// <Status
	// RunId="f0a74498-2bf0-4ffb-94de-d3a7378424f3" RunStatus="Finished"
	// TotalPercent="100" CurrentStage="2147483647" StageName=""
	// CurrentStagePercent="0" StageMessage="Finish saving results"
	// StepMessage="" Details="" TimeStarted="07/10/2010 15:31:01"
	// TimeFinished="07/10/2010 15:35:31" QueuePosition="-1" />
	// </ReturnValue>
	// <GetScanStatusReplySchema
	// xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	// xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns="CxWebAPI.xsd">
	// <IsSuccesfull>true</IsSuccesfull>
	// <ReturnValue>
	// <Status RunId="a0ec1598-b931-4992-805c-2820d0f07f68" RunStatus="Failed"
	// TotalPercent="0" CurrentStage="0" StageName="" CurrentStagePercent="0"
	// StageMessage="Cannot access the Source code" StepMessage="" Details="N/A"
	// TimeStarted="07/10/2010 21:13:07" TimeFinished="07/10/2010 21:13:18"
	// QueuePosition="-1" />
	// </ReturnValue>
	// </GetScanStatusReplySchema>
	// (<?xml version="1.0" encoding="utf-16"?>
	// <GetScanStatusReplySchema
	// xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	// xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns="CxWebAPI.xsd">
	// <IsSuccesfull>true</IsSuccesfull>
	// <ReturnValue>
	// <Status RunId="38a2e660-a56e-4051-bafc-cab324099f81" RunStatus="Running"
	// TotalPercent="97" CurrentStage="20" StageName="Querying"
	// CurrentStagePercent="95"
	// StageMessage="Running query: Non_Private_Field_In_ActionForm_Class"
	// StepMessage="Processing # 0 of 122" Details="N/A"
	// TimeStarted="07/10/2010 21:16:03" TimeFinished="07/10/2012 21:16:03"
	// QueuePosition="-1" />
	// </ReturnValue>
	// </GetScanStatusReplySchema>)
	@Override
	protected void parseReturnValue(Element returnValueNode) {

		for (Object childObj : returnValueNode.getChildren()) {
			Element child = (Element) childObj;
			if (child.getName().equals("Status")) {
				runId = getAttributeValue(child, "RunId");
				runStatus = CurrentStatusEnum.fromValue(getAttributeValue(child, "RunStatus"));
				totalPercent = getAttributeValueInt(child, "TotalPercent");
				currentStage = getAttributeValue(child, "CurrentStage");
				stageName = getAttributeValue(child, "StageName");
				currentStagePercent = getAttributeValueInt(child, "CurrentStagePercent");
				stageMessage = getAttributeValue(child, "StageMessage");
				stepMessage = getAttributeValue(child, "StepMessage");
				timeStarted = getAttributeValue(child, "TimeStarted");
				timeFinished = getAttributeValue(child, "TimeFinished");
				queuePosition = getAttributeValue(child, "QueuePosition");
				details = getAttributeValue(child, "Details");
			}
		}
	}

	private String getAttributeValue(Element child, String attributeName) {
		if (child.getAttribute(attributeName) != null) {
			return child.getAttribute(attributeName).getValue();
		}
		return "";
	}

	private int getAttributeValueInt(Element child, String attributeName) {
		if (child.getAttribute(attributeName) != null) {
			try {
				return Integer.parseInt(child.getAttribute(attributeName).getValue());
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return 0;
	}

	public boolean isStatusFinished() {
		return (runStatus != null && runStatus == CurrentStatusEnum.FINISHED);
	}
	
	public boolean isRunStatusCanceled() {		
		return (runStatus != null && ((runStatus == CurrentStatusEnum.CANCELED) 
				|| (runStatus == CurrentStatusEnum.DELETED)));
	}

	public boolean isStatusFailed() {
		return (runStatus != null && (runStatus == CurrentStatusEnum.FAILED));
	}
	
	public boolean isStatusUnknown() {
		return (runStatus != null && (runStatus == CurrentStatusEnum.UNKNOWN));
	}

	@Override
	public String toString() {
		String result;
		if (isSuccesfullResponce()) {
			result = "" + this.getClass().getSimpleName() + "(" + getResponseXmlString() + ")";
		} else {
			result = "" + this.getClass().getSimpleName() + "(Message=" + getErrorMessage() + ")";
		}
		return result;
	}

	@Override
	protected void parseReturnValue(CxWSBasicRepsonse responseObject) {

		if (responseObject instanceof CxWSResponseScanStatus) {
			statusResponse = (CxWSResponseScanStatus) responseObject;
			this.runId = statusResponse.getRunId();
			this.runStatus = statusResponse.getCurrentStatus();
			this.totalPercent = statusResponse.getTotalPercent();
			this.currentStage = statusResponse.getCurrentStage() + "";
			this.stageName = statusResponse.getStageName();
			this.currentStagePercent = statusResponse.getCurrentStagePercent();
			this.stageMessage = statusResponse.getStageMessage();
			this.stepMessage = statusResponse.getStepMessage();
			this.timeStarted = convertTime(statusResponse.getTimeScheduled());			
			this.timeFinished = convertTime(statusResponse.getTimeFinished());
			this.queuePosition = statusResponse.getQueuePosition() + "";
			this.details = statusResponse.getStepDetails();
			this.projectId = statusResponse.getProjectId();
			this.scanId = statusResponse.getScanId();
			this.taskId = statusResponse.getTaskId();
			this.resultId = statusResponse.getResultId();
		} else {
			throw new IllegalArgumentException(getInvalidTypeErrMsg(responseObject));
		}
	}
	
	private String convertTime(CxDateTime time) {
		return time.getYear() + "-" + 	time.getMonth() + "-" + time.getDay() + " " + 
				time.getHour() + ":" + time.getMinute() + ":" + time.getSecond();
	}
	
	public CxWSResponseScanStatus getStatusResponse() {
		return statusResponse;
	}
	
	@Override
	public String getResponseXmlString() {
		return "{ " + runId + " | " 
				+ runStatus + " | "
				+ totalPercent + " | "
				+ currentStage + " | "
				+ stageName + " | "
				+ currentStagePercent + " | "
				+ stageMessage + " | "
				+ stepMessage + " | "
				+ timeStarted + " | "
				+ timeFinished + " | "
				+ queuePosition + " | "
				+ details + " | "
				+ projectId + "| "
				+ scanId + " | "
				+ taskId + " | "
				+ resultId + " }";
	}
}
