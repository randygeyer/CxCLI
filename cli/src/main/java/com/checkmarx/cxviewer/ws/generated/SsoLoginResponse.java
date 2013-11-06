
package com.checkmarx.cxviewer.ws.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="SsoLoginResult" type="{http://Checkmarx.com/v7}CxWSResponseLoginData" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "ssoLoginResult"
})
@XmlRootElement(name = "SsoLoginResponse")
public class SsoLoginResponse {

    @XmlElement(name = "SsoLoginResult")
    protected CxWSResponseLoginData ssoLoginResult;

    /**
     * Gets the value of the ssoLoginResult property.
     * 
     * @return
     *     possible object is
     *     {@link CxWSResponseLoginData }
     *     
     */
    public CxWSResponseLoginData getSsoLoginResult() {
        return ssoLoginResult;
    }

    /**
     * Sets the value of the ssoLoginResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link CxWSResponseLoginData }
     *     
     */
    public void setSsoLoginResult(CxWSResponseLoginData value) {
        this.ssoLoginResult = value;
    }

}
