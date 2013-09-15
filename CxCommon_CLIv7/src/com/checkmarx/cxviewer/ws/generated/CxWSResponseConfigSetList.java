
package com.checkmarx.cxviewer.ws.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CxWSResponseConfigSetList complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CxWSResponseConfigSetList">
 *   &lt;complexContent>
 *     &lt;extension base="{http://Checkmarx.com/v7}CxWSBasicRepsonse">
 *       &lt;sequence>
 *         &lt;element name="ConfigSetList" type="{http://Checkmarx.com/v7}ArrayOfConfigurationSet" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CxWSResponseConfigSetList", propOrder = {
    "configSetList"
})
public class CxWSResponseConfigSetList
    extends CxWSBasicRepsonse
{

    @XmlElement(name = "ConfigSetList")
    protected ArrayOfConfigurationSet configSetList;

    /**
     * Gets the value of the configSetList property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfConfigurationSet }
     *     
     */
    public ArrayOfConfigurationSet getConfigSetList() {
        return configSetList;
    }

    /**
     * Sets the value of the configSetList property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfConfigurationSet }
     *     
     */
    public void setConfigSetList(ArrayOfConfigurationSet value) {
        this.configSetList = value;
    }

}
