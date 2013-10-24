
package com.checkmarx.cxviewer.ws.resolver;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CxClientType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="CxClientType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="None"/>
 *     &lt;enumeration value="WebPortal"/>
 *     &lt;enumeration value="CLI"/>
 *     &lt;enumeration value="Eclipse"/>
 *     &lt;enumeration value="VS"/>
 *     &lt;enumeration value="InteliJ"/>
 *     &lt;enumeration value="Audit"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "CxClientType")
@XmlEnum
public enum CxClientType {

    @XmlEnumValue("None")
    NONE("None"),
    @XmlEnumValue("WebPortal")
    WEB_PORTAL("WebPortal"),
    CLI("CLI"),
    @XmlEnumValue("Eclipse")
    ECLIPSE("Eclipse"),
    VS("VS"),
    @XmlEnumValue("InteliJ")
    INTELI_J("InteliJ"),
    @XmlEnumValue("Audit")
    AUDIT("Audit");
    private final String value;

    CxClientType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static CxClientType fromValue(String v) {
        for (CxClientType c: CxClientType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
