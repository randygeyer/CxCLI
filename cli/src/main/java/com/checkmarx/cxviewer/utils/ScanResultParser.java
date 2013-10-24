package com.checkmarx.cxviewer.utils;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import com.checkmarx.cxviewer.CxLogger;
import com.checkmarx.cxviewer.data.ScanResult;
import com.checkmarx.cxviewer.data.ScanResultPath;
import com.checkmarx.cxviewer.data.ScanResultPathNode;
import com.checkmarx.cxviewer.data.ScanResultPoint;
import com.checkmarx.cxviewer.data.ScanResultQuery;
import com.checkmarx.cxviewer.ws.results.SeverityEnum;

public class ScanResultParser {

	private ScanResult scanResult;
	private Document scanDocument;

	private String errorMessage;

	public ScanResult getScanResult() {
		return scanResult;
	}
	
	public Document getScanDocument() {
		return scanDocument;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}

	public boolean parseScanResultAndSaveToProject(String xmlUrl, String projectName) {
		this.scanResult = null;
		this.scanDocument = null;

		SAXBuilder sb = new SAXBuilder();
		sb.setIgnoringBoundaryWhitespace(true);
		try {
			this.scanDocument = sb.build(new URL(xmlUrl));
			//storeResults(doc, project);
			this.scanResult = parseRoot(this.scanDocument.getRootElement());
			if (this.scanResult != null) {
				this.scanResult.setProjectName(projectName);
			}
//		} catch (JDOMException e) {
//			e.printStackTrace();
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
		} catch (Exception e) {
			CxLogger.getLogger().error(e);
			errorMessage = e.getMessage();
		}
		return (this.scanResult != null);
	}
	
	public boolean parseScanResult(String projectName, InputStream xmlStream) {
		this.scanResult = null;
		this.scanDocument = null;

		SAXBuilder sb = new SAXBuilder();
		sb.setIgnoringBoundaryWhitespace(true);
		try {
			this.scanDocument = sb.build(xmlStream);
			this.scanResult = parseRoot(this.scanDocument.getRootElement());
			if (this.scanResult != null) {
				this.scanResult.setProjectName(projectName);
			}
		} catch (Exception e) {
			CxLogger.getLogger().error(e);
			errorMessage = e.getMessage();
		}
		return (this.scanResult != null);
	}
	
	public boolean parseScanResult(String projectName, Reader xmlReader) {
		this.scanResult = null;
		this.scanDocument = null;

		SAXBuilder sb = new SAXBuilder();
		sb.setIgnoringBoundaryWhitespace(true);
		try {
			this.scanDocument = sb.build(xmlReader);
			this.scanResult = parseRoot(this.scanDocument.getRootElement());
			if (this.scanResult != null) {
				this.scanResult.setProjectName(projectName);
			}
		} catch (Exception e) {
			CxLogger.getLogger().error(e);
			errorMessage = e.getMessage();
		}
		return (this.scanResult != null);
	}
	
	public boolean parseScanResult(String xml, String projectName) {
		this.scanResult = null;

		SAXBuilder sb = new SAXBuilder();
		sb.setIgnoringBoundaryWhitespace(true);
		Document doc;
		try {
			doc = sb.build(new StringReader(xml));
			this.scanResult = parseRoot(doc.getRootElement());
			if (this.scanResult != null) {
				this.scanResult.setProjectName(projectName);
			}
			this.scanDocument = doc;
//		} catch (JDOMException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
		} catch (Exception e) {
			//e.printStackTrace();
			CxLogger.getLogger().error(e);
			errorMessage = e.getMessage();
		}
		return (this.scanResult != null);
	}
	
	public boolean parseScanResult(Document xml, String projectName) {
		this.scanResult = null;

		try {
			this.scanResult = parseRoot(xml.getRootElement());
			if (this.scanResult != null) {
				this.scanResult.setProjectName(projectName);
			}
			this.scanDocument = xml;
		} catch (Exception e) {
			CxLogger.getLogger().error(e);
			errorMessage = e.getMessage();
		}
		return (this.scanResult != null);
	}
	
	private ScanResult parseRoot(Element rootElement) throws Exception {
		ScanResult sRresult = new ScanResult();
		int length = 0;
		long id;
		long cweId;
		int queryResultsCount;
		String name;
		String nodeObj;
		String group;
		String severity;
		ScanResultQuery query;
		long nodeId;
		int nodeNodeId = 0;
		//String fileName;
		String nodeFileName;
		int line;
		int column;
		String remark;
		SeverityEnum resSeverity;
		String state;
		String assignedUser;
		ScanResultPoint result;
		long pathId;
		int similarityId;
		ScanResultPath path;

		List<ScanResultQuery> queries = new ArrayList<ScanResultQuery>();
		sRresult.setResults(queries);
		if (rootElement.getChildren() == null || rootElement.getChildren().isEmpty()) {
			CxLogger.getLogger().trace("CxModel.parseRoot(): no children results in root");
			return sRresult;
		}
		Element resultsNode = /*(Element) rootElement.getChildren().get(0)*/rootElement;
		try {
			for (Object queryNodeObj : resultsNode.getChildren()) {
				id = 0; 
				cweId = 0;
				queryResultsCount = 0;
				name = ""; 
				group = ""; 
				severity = "";
				
				Element queryNode = (Element) queryNodeObj;
				List<?> queryAttribs = queryNode.getAttributes();
				for (Object attrObj : queryAttribs) {
					Attribute attr = (Attribute) attrObj;
					if (attr.getName().equalsIgnoreCase("id")) {
						id = attr.getLongValue();
					} else if (attr.getName().equalsIgnoreCase("cweId")) {
						cweId = attr.getLongValue();
					} else if (attr.getName().equalsIgnoreCase("name")) {
						name = attr.getValue();
					} else if (attr.getName().equalsIgnoreCase("group")) {
						group = attr.getValue();
					} else if (attr.getName().equalsIgnoreCase("Severity")) {
						severity = attr.getValue();
					}
				}
				
				List<ScanResultPoint> queryResults = new ArrayList<ScanResultPoint>();
				query = new ScanResultQuery();
				query.setId(id);
				query.setCweId(cweId);
				query.setName(name);
				query.setGroup(group);
				query.setSeverity(SeverityEnum.byLabel(severity).getCode());
				query.setResultsAmount(queryResultsCount);
				query.setQueryResults(new ArrayList<ScanResultPoint>());
				
				queries.add(query);
				
				// Parsing ScanResultPoint
				for (Object resultNodeObj : queryNode.getChildren()) {
					nodeId = 0;
					//fileName = "";
					remark = "";
					resSeverity = null;
					state = "";
					assignedUser = "";
					
					Element resultNode = (Element) resultNodeObj;
					List<?> resultAttribs = resultNode.getAttributes();
					for (Object attrObj : resultAttribs) {
						Attribute attr = (Attribute) attrObj;
						if (attr.getName().equalsIgnoreCase("Remark")) {
							remark = attr.getValue();
							remark = remark.replace(" ;", "\u00FF");
						} else if (attr.getName().equalsIgnoreCase("Severity")) {
							String strSeverityVal = attr.getValue();
							try {
								resSeverity = SeverityEnum.valueOf(strSeverityVal);
							} catch (Exception e) {
								resSeverity = SeverityEnum.Undefined;
							}
						} else if (attr.getName().equalsIgnoreCase("state")) {
							state = attr.getValue();
						} else if (attr.getName().equalsIgnoreCase("AssignToUser")) {
							assignedUser = attr.getValue();
						}
					}
					ArrayList<ScanResultPath> resultPaths = new ArrayList<ScanResultPath>();
					result = new ScanResultPoint();
					result.setRemark(remark);
					result.setSeverity(resSeverity);
					result.setState(state);
					result.setAssignedUser(assignedUser);
//					result.setPathId(data.getPathId());
//					result.setSrcFileName(data.getSourceFile());
//					result.setSrcDirectory(data.getSourceFolder());
//					result.setSrcLine(data.getSourceLine());
//					result.setSrcObject(data.getSourceObject());
//					
//					result.setDestFileName(data.getDestFile());
//					result.setDestDirectory(data.getDestFolder());
//					result.setDestLine(data.getDestLine());
//					result.setDestObject(data.getDestObject());
//					result.setAssignedUser(data.getAssignedUser());
					
					queryResults.add(result);
					if (resultNode.getChildren().size() > 1) {
						CxLogger.getLogger().trace("CxModel.parseRoot() nodeId=" + nodeId);
					}
					
					// ScanPath parsing
					for (Object pathNodeObj : resultNode.getChildren()) {
						Element pathNode = (Element) pathNodeObj;
						pathId = 0; 
						similarityId = 0;
						
						List<?> pathAttribs = pathNode.getAttributes();
						for (Object attrObj : pathAttribs) {
							Attribute attr = (Attribute) attrObj;
							if (attr.getName().equalsIgnoreCase("PathId")) {
								pathId = attr.getIntValue();
							} /*else if (attr.getName().equalsIgnoreCase("ResultId")) {
								resultId = attr.getIntValue();
							} */else if (attr.getName().equalsIgnoreCase("SimilarityId")) {
								similarityId = attr.getIntValue();
							}
						}
						
						ArrayList<ScanResultPathNode> pathPathNodes = new ArrayList<ScanResultPathNode>();
						result.setPathId(pathId);
						path = new ScanResultPath();
						path.setPathId(pathId);
						path.setSimilarityId(similarityId);
						resultPaths.add(path);
						result.setPath(path);
						
						for (Object pathNodeNodeObj : pathNode.getChildren()) {
							nodeObj = "";
							nodeFileName = "";
							nodeNodeId = 0;
							line = 0;
							column = 0;
							length = 0;
							
							Element pathNodeNode = (Element) pathNodeNodeObj;
							for (Object pathNodeNodeChildObj : pathNodeNode.getChildren()) {
								Element pathNodeNodeChild = (Element) pathNodeNodeChildObj;
								if (pathNodeNodeChild.getName().equals("FileName")) { //$NON-NLS-1$
									nodeFileName = pathNodeNodeChild.getValue();
								} else if (pathNodeNodeChild.getName().equals("Line")) { //$NON-NLS-1$
									line = Integer.parseInt(pathNodeNodeChild.getValue());
								} else if (pathNodeNodeChild.getName().equals("Column")) { //$NON-NLS-1$
									column = Integer.parseInt(pathNodeNodeChild.getValue());
								} else if (pathNodeNodeChild.getName().equals("NodeId")) { //$NON-NLS-1$
									nodeNodeId = Integer.parseInt(pathNodeNodeChild.getValue());
								} else if (pathNodeNodeChild.getName().equals("Name")) { //$NON-NLS-1$
									nodeObj = (pathNodeNodeChild.getValue());
								} else if (pathNodeNodeChild.getName().equals("Length")) { //$NON-NLS-1$
									length = Integer.parseInt(pathNodeNodeChild.getValue());
								}
							}
							ScanResultPathNode innerPathNode = new ScanResultPathNode();
							innerPathNode.setFileName(nodeFileName);
							innerPathNode.setName(nodeObj);
							innerPathNode.setLength(length);
							innerPathNode.setLine(line);
							innerPathNode.setColumn(column);
							innerPathNode.setNodeId(nodeNodeId);
							
							pathPathNodes.add(innerPathNode);
						}
						
						path.setPathNodes(pathPathNodes);
						
						if (!pathPathNodes.isEmpty()) {
							ScanResultPathNode firstNode = pathPathNodes.get(0);
							ScanResultPathNode lastNode = pathPathNodes.get(pathPathNodes.size() - 1);
							
							File srcFile = new File(firstNode.getFileName());
							File destFile = new File(lastNode.getFileName());
							
							
							String srcFileName = srcFile.getName();
							String srcFolder = srcFile.getParent();
							if (srcFolder == null) {
								srcFolder = "\\";
							}
							
							String destFileName = destFile.getName();
							String destFolder = destFile.getParent();
							if (destFolder == null) {
								destFolder = "\\";
							}
							
							result.setSrcFileName(srcFileName);
							
							result.setSrcDirectory(srcFolder);
							result.setSrcObject(firstNode.getName());
							result.setSrcLine(firstNode.getLine());
							result.setDestFileName(destFileName);
							result.setDestDirectory(destFolder);
							result.setDestObject(lastNode.getName());
							result.setDestLine(lastNode.getLine());
						}
					}
				}
				
				query.setQueryResults(queryResults);
				
				int resultsAmount = 0;
				for (ScanResultPoint tempResult : queryResults) {
					if (!tempResult.getState().equals("1")) {
						resultsAmount++;
					}
				}
				query.setResultsAmount(resultsAmount);
			}

		} catch (DataConversionException e) {
			CxLogger.getLogger().error("", e);
			errorMessage = e.getMessage();
			return null;
		} catch (Exception e) {
			CxLogger.getLogger().error("", e);
			errorMessage = "Error parsing results. Unsupported results xml format.";
			return null;
		}
		return sRresult;
	}
}