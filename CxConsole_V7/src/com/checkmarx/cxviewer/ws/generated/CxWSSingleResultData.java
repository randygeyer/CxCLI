
package com.checkmarx.cxviewer.ws.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CxWSSingleResultData complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CxWSSingleResultData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="QueryId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="PathId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="SourceFolder" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SourceFile" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SourceLine" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="SourceObject" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DestFolder" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DestFile" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="DestLine" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="DestObject" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Comment" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="State" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="Severity" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="AssignedUser" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CxWSSingleResultData", propOrder = {
    "queryId",
    "pathId",
    "sourceFolder",
    "sourceFile",
    "sourceLine",
    "sourceObject",
    "destFolder",
    "destFile",
    "destLine",
    "destObject",
    "comment",
    "state",
    "severity",
    "assignedUser"
})
@XmlSeeAlso({
    CxWSSingleResultCompareData.class
})
public class CxWSSingleResultData {

    @XmlElement(name = "QueryId")
    protected long queryId;
    @XmlElement(name = "PathId")
    protected long pathId;
    @XmlElement(name = "SourceFolder")
    protected String sourceFolder;
    @XmlElement(name = "SourceFile")
    protected String sourceFile;
    @XmlElement(name = "SourceLine")
    protected long sourceLine;
    @XmlElement(name = "SourceObject")
    protected String sourceObject;
    @XmlElement(name = "DestFolder")
    protected String destFolder;
    @XmlElement(name = "DestFile")
    protected String destFile;
    @XmlElement(name = "DestLine")
    protected long destLine;
    @XmlElement(name = "DestObject")
    protected String destObject;
    @XmlElement(name = "Comment")
    protected String comment;
    @XmlElement(name = "State")
    protected int state;
    @XmlElement(name = "Severity")
    protected int severity;
    @XmlElement(name = "AssignedUser")
    protected String assignedUser;

    /**
     * Gets the value of the queryId property.
     * 
     */
    public long getQueryId() {
        return queryId;
    }

    /**
     * Sets the value of the queryId property.
     * 
     */
    public void setQueryId(long value) {
        this.queryId = value;
    }

    /**
     * Gets the value of the pathId property.
     * 
     */
    public long getPathId() {
        return pathId;
    }

    /**
     * Sets the value of the pathId property.
     * 
     */
    public void setPathId(long value) {
        this.pathId = value;
    }

    /**
     * Gets the value of the sourceFolder property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSourceFolder() {
        return sourceFolder;
    }

    /**
     * Sets the value of the sourceFolder property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSourceFolder(String value) {
        this.sourceFolder = value;
    }

    /**
     * Gets the value of the sourceFile property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSourceFile() {
        return sourceFile;
    }

    /**
     * Sets the value of the sourceFile property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSourceFile(String value) {
        this.sourceFile = value;
    }

    /**
     * Gets the value of the sourceLine property.
     * 
     */
    public long getSourceLine() {
        return sourceLine;
    }

    /**
     * Sets the value of the sourceLine property.
     * 
     */
    public void setSourceLine(long value) {
        this.sourceLine = value;
    }

    /**
     * Gets the value of the sourceObject property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSourceObject() {
        return sourceObject;
    }

    /**
     * Sets the value of the sourceObject property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSourceObject(String value) {
        this.sourceObject = value;
    }

    /**
     * Gets the value of the destFolder property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDestFolder() {
        return destFolder;
    }

    /**
     * Sets the value of the destFolder property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDestFolder(String value) {
        this.destFolder = value;
    }

    /**
     * Gets the value of the destFile property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDestFile() {
        return destFile;
    }

    /**
     * Sets the value of the destFile property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDestFile(String value) {
        this.destFile = value;
    }

    /**
     * Gets the value of the destLine property.
     * 
     */
    public long getDestLine() {
        return destLine;
    }

    /**
     * Sets the value of the destLine property.
     * 
     */
    public void setDestLine(long value) {
        this.destLine = value;
    }

    /**
     * Gets the value of the destObject property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDestObject() {
        return destObject;
    }

    /**
     * Sets the value of the destObject property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDestObject(String value) {
        this.destObject = value;
    }

    /**
     * Gets the value of the comment property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the value of the comment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComment(String value) {
        this.comment = value;
    }

    /**
     * Gets the value of the state property.
     * 
     */
    public int getState() {
        return state;
    }

    /**
     * Sets the value of the state property.
     * 
     */
    public void setState(int value) {
        this.state = value;
    }

    /**
     * Gets the value of the severity property.
     * 
     */
    public int getSeverity() {
        return severity;
    }

    /**
     * Sets the value of the severity property.
     * 
     */
    public void setSeverity(int value) {
        this.severity = value;
    }

    /**
     * Gets the value of the assignedUser property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAssignedUser() {
        return assignedUser;
    }

    /**
     * Sets the value of the assignedUser property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAssignedUser(String value) {
        this.assignedUser = value;
    }

}
