
package com.checkmarx.cxviewer.ws.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CliScanArgs complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CliScanArgs">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="PrjSettings" type="{http://Checkmarx.com/v7}ProjectSettings" minOccurs="0"/>
 *         &lt;element name="SrcCodeSettings" type="{http://Checkmarx.com/v7}SourceCodeSettings" minOccurs="0"/>
 *         &lt;element name="IsPrivateScan" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="IsIncremental" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CliScanArgs", propOrder = {
    "prjSettings",
    "srcCodeSettings",
    "isPrivateScan",
    "isIncremental"
})
public class CliScanArgs {

    @XmlElement(name = "PrjSettings")
    protected ProjectSettings prjSettings;
    @XmlElement(name = "SrcCodeSettings")
    protected SourceCodeSettings srcCodeSettings;
    @XmlElement(name = "IsPrivateScan")
    protected boolean isPrivateScan;
    @XmlElement(name = "IsIncremental")
    protected boolean isIncremental;

    /**
     * Gets the value of the prjSettings property.
     * 
     * @return
     *     possible object is
     *     {@link ProjectSettings }
     *     
     */
    public ProjectSettings getPrjSettings() {
        return prjSettings;
    }

    /**
     * Sets the value of the prjSettings property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProjectSettings }
     *     
     */
    public void setPrjSettings(ProjectSettings value) {
        this.prjSettings = value;
    }

    /**
     * Gets the value of the srcCodeSettings property.
     * 
     * @return
     *     possible object is
     *     {@link SourceCodeSettings }
     *     
     */
    public SourceCodeSettings getSrcCodeSettings() {
        return srcCodeSettings;
    }

    /**
     * Sets the value of the srcCodeSettings property.
     * 
     * @param value
     *     allowed object is
     *     {@link SourceCodeSettings }
     *     
     */
    public void setSrcCodeSettings(SourceCodeSettings value) {
        this.srcCodeSettings = value;
    }

    /**
     * Gets the value of the isPrivateScan property.
     * 
     */
    public boolean isIsPrivateScan() {
        return isPrivateScan;
    }

    /**
     * Sets the value of the isPrivateScan property.
     * 
     */
    public void setIsPrivateScan(boolean value) {
        this.isPrivateScan = value;
    }

    /**
     * Gets the value of the isIncremental property.
     * 
     */
    public boolean isIsIncremental() {
        return isIncremental;
    }

    /**
     * Sets the value of the isIncremental property.
     * 
     */
    public void setIsIncremental(boolean value) {
        this.isIncremental = value;
    }

}
