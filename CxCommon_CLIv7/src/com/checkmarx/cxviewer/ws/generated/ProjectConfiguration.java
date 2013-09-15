
package com.checkmarx.cxviewer.ws.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ProjectConfiguration complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ProjectConfiguration">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ProjectSettings" type="{http://Checkmarx.com/v7}ProjectSettings" minOccurs="0"/>
 *         &lt;element name="SourceCodeSettings" type="{http://Checkmarx.com/v7}SourceCodeSettings" minOccurs="0"/>
 *         &lt;element name="ScheduleSettings" type="{http://Checkmarx.com/v7}ScheduleSettings" minOccurs="0"/>
 *         &lt;element name="ScanActionSettings" type="{http://Checkmarx.com/v7}ScanActionSettings" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProjectConfiguration", propOrder = {
    "projectSettings",
    "sourceCodeSettings",
    "scheduleSettings",
    "scanActionSettings"
})
public class ProjectConfiguration {

    @XmlElement(name = "ProjectSettings")
    protected ProjectSettings projectSettings;
    @XmlElement(name = "SourceCodeSettings")
    protected SourceCodeSettings sourceCodeSettings;
    @XmlElement(name = "ScheduleSettings")
    protected ScheduleSettings scheduleSettings;
    @XmlElement(name = "ScanActionSettings")
    protected ScanActionSettings scanActionSettings;

    /**
     * Gets the value of the projectSettings property.
     * 
     * @return
     *     possible object is
     *     {@link ProjectSettings }
     *     
     */
    public ProjectSettings getProjectSettings() {
        return projectSettings;
    }

    /**
     * Sets the value of the projectSettings property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProjectSettings }
     *     
     */
    public void setProjectSettings(ProjectSettings value) {
        this.projectSettings = value;
    }

    /**
     * Gets the value of the sourceCodeSettings property.
     * 
     * @return
     *     possible object is
     *     {@link SourceCodeSettings }
     *     
     */
    public SourceCodeSettings getSourceCodeSettings() {
        return sourceCodeSettings;
    }

    /**
     * Sets the value of the sourceCodeSettings property.
     * 
     * @param value
     *     allowed object is
     *     {@link SourceCodeSettings }
     *     
     */
    public void setSourceCodeSettings(SourceCodeSettings value) {
        this.sourceCodeSettings = value;
    }

    /**
     * Gets the value of the scheduleSettings property.
     * 
     * @return
     *     possible object is
     *     {@link ScheduleSettings }
     *     
     */
    public ScheduleSettings getScheduleSettings() {
        return scheduleSettings;
    }

    /**
     * Sets the value of the scheduleSettings property.
     * 
     * @param value
     *     allowed object is
     *     {@link ScheduleSettings }
     *     
     */
    public void setScheduleSettings(ScheduleSettings value) {
        this.scheduleSettings = value;
    }

    /**
     * Gets the value of the scanActionSettings property.
     * 
     * @return
     *     possible object is
     *     {@link ScanActionSettings }
     *     
     */
    public ScanActionSettings getScanActionSettings() {
        return scanActionSettings;
    }

    /**
     * Sets the value of the scanActionSettings property.
     * 
     * @param value
     *     allowed object is
     *     {@link ScanActionSettings }
     *     
     */
    public void setScanActionSettings(ScanActionSettings value) {
        this.scanActionSettings = value;
    }

}
