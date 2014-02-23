package com.checkmarx.cxviewer.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.checkmarx.cxviewer.annotation.PersisentCompoundField;
import com.checkmarx.cxviewer.annotation.PersistentField;

public class ScanResultQuery implements Serializable {

	private static final long serialVersionUID = -5351905282333350343L;

	@PersisentCompoundField
	private List<ScanResultPoint> queryResults;

	@PersistentField
	private long id;

	@PersistentField
	private long cweId;

	@PersistentField
	private String name;

	@PersistentField
	private String group;
	
	@PersistentField
	private int severity;
	
	@PersistentField
	private int resultsAmount;
	
	public ScanResultQuery() {
	}

	public ScanResultPoint findAnyPathByNode(ScanResultPathNode pathNode) {
		List<ScanResultPoint> queryRes = getQueryResults();
		for (ScanResultPoint point : queryRes) {
			ScanResultPath path = point.getPath();
			if (path != null) {
				for (ScanResultPathNode node : path.getPathNodes()) {
					if (node.equals(pathNode)) {
						return point;
					}
				}
			}
		}
		return null;
	}

	public long findPathIdByNode(ScanResultPathNode pathNode) {
		List<ScanResultPoint> queryRes = getQueryResults();
		for (ScanResultPoint point : queryRes) {
			ScanResultPath path = point.getPath();
			if (path != null) {
				for (ScanResultPathNode node : path.getPathNodes()) {
					if (node.equals(pathNode)) {
						return path.getPathId();
					}
				}
			}
		}
		return 0;
	}

	/**
	 * Returns all IDs of paths, which  passes through this node
	 * @param pathNode
	 * @return <code>Set</code> of integer path Ids 
	 */
	public Set<Long> getAllNodePathIds(ScanResultPathNode pathNode) {
		
		Set<Long> nodePassingPathIds = new TreeSet<Long>();
		List<ScanResultPoint> queryRes = getQueryResults();
		for (ScanResultPoint point : queryRes) {
			ScanResultPath path = point.getPath();
			if (path != null) {
				for (ScanResultPathNode node : path.getPathNodes()) {
					if (node.equals(pathNode)) {
						nodePassingPathIds.add(path.getPathId());
					}
				}
			}
		}
		return nodePassingPathIds;
	}

	public long getCweId() {
		return cweId;
	}

	public String getGroup() {
		return group;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getNodesCount() {
		int nodesCount = 0;
		List<ScanResultPoint> queryRes = getQueryResults();
		for (ScanResultPoint point : queryRes) {
			ScanResultPath path = point.getPath();
			if (path != null) {
				nodesCount += path.getPathNodes().size();
			}
		}
		
		return nodesCount;
	}

	public List<ScanResultPoint> getQueryResults() {
		return queryResults;
	}

	public int getResultsAmount() {
		return resultsAmount;
	}

	public int getSeverity() {
		return severity;
	}
	
	public void setCweId(long cweId) {
		this.cweId = cweId;
	}
	
	public void setGroup(String group) {
		this.group = group;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setQueryResults(List<ScanResultPoint> queryResults) {
		this.queryResults = queryResults;
	}
	
	public void setResultsAmount(int resultsAmount) {
		this.resultsAmount = resultsAmount;
	}
	
	public void setSeverity(int severity) {
		this.severity = severity;
	}
	
	public void updateResultsNumLocally() {
		if (queryResults == null) {
			return;
		}
		int resultsNum = 0;
		for (ScanResultPoint resultPoint : queryResults) {
			if (!resultPoint.getState().equals("1")) {
				resultsNum++;
			}
		}
		resultsAmount = resultsNum;
	}
	
	@Override
	public String toString() {
		return "<scanResultQuery>\nid=" + id + "\ncweid=" + cweId + "\nname=" + 
				name + "\ngroup=" + group + "\nsevererty=" + severity + 
				"\namount=" + resultsAmount + "\n</scanResultQuery>";
	}
	

}
