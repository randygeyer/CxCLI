
package com.checkmarx.cxviewer.ws.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CxWSResponseRunID complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CxWSResponseRunID">
 *   &lt;complexContent>
 *     &lt;extension base="{http://Checkmarx.com/v7}CxWSBasicRepsonse">
 *       &lt;sequence>
 *         &lt;element name="ProjectID" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="RunId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CxWSResponseRunID", propOrder = {
    "projectID",
    "runId"
})
public class CxWSResponseRunID
    extends CxWSBasicRepsonse
{

    @XmlElement(name = "ProjectID")
    protected long projectID;
    @XmlElement(name = "RunId")
    protected String runId;

    /**
     * Gets the value of the projectID property.
     * 
     */
    public long getProjectID() {
        return projectID;
    }

    /**
     * Sets the value of the projectID property.
     * 
     */
    public void setProjectID(long value) {
        this.projectID = value;
    }

    /**
     * Gets the value of the runId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRunId() {
        return runId;
    }

    /**
     * Sets the value of the runId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRunId(String value) {
        this.runId = value;
    }

}
