
package com.checkmarx.cxviewer.ws.generated;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CompareStatusType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="CompareStatusType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Fixed"/>
 *     &lt;enumeration value="Reoccured"/>
 *     &lt;enumeration value="New"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "CompareStatusType", namespace = "CxDataTypes.xsd")
@XmlEnum
public enum CompareStatusType {

    @XmlEnumValue("Fixed")
    FIXED("Fixed"),
    @XmlEnumValue("Reoccured")
    REOCCURED("Reoccured"),
    @XmlEnumValue("New")
    NEW("New");
    private final String value;

    CompareStatusType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static CompareStatusType fromValue(String v) {
        for (CompareStatusType c: CompareStatusType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
