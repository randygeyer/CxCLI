package com.checkmarx.cxviewer.data;

import java.io.Serializable;
import java.util.List;

import com.checkmarx.cxviewer.annotation.PersisentCompoundField;
import com.checkmarx.cxviewer.annotation.PersistentField;

public class ScanResultPath implements Serializable {

	private static final long serialVersionUID = -6855750224623616090L;

	@PersisentCompoundField
	private List<ScanResultPathNode> pathNodes;

	@PersistentField
	private long pathId;
	
	@Deprecated
	private long resultId;
	
	@PersistentField
	private long similarityId;

	public List<ScanResultPathNode> getPathNodes() {
		return pathNodes;
	}
	
	public void setPathNodes(List<ScanResultPathNode> pathNodes) {
		this.pathNodes = pathNodes;
	}

	public long getPathId() {
		return pathId;
	}
	
	public void setPathId(long pathId) {
		this.pathId = pathId;
	}
	
	@Deprecated
	public long getResultId() {
		return resultId;
	}
	
	public long getSimilarityId() {
		return similarityId;
	}
	
	public void setSimilarityId(long similarityId) {
		this.similarityId = similarityId;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ScanResultPath)) {
			return false;
		}
		return ((ScanResultPath)obj).pathId == this.pathId;
	}

	@Override
	public int hashCode() {
		return 0;
	}
	
	public boolean intersect(ScanResultPath otherPath) {
		
		for (ScanResultPathNode node: otherPath.pathNodes) {
			for (ScanResultPathNode localnode : this.pathNodes) {
				if (localnode.equals(node)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		return "" + pathId + "{" + "," + similarityId + "}";
	}
}