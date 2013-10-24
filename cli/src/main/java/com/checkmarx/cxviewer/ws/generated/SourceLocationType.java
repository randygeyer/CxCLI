
package com.checkmarx.cxviewer.ws.generated;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SourceLocationType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="SourceLocationType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Local"/>
 *     &lt;enumeration value="Shared"/>
 *     &lt;enumeration value="SourceControl"/>
 *     &lt;enumeration value="SourcePulling"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "SourceLocationType")
@XmlEnum
public enum SourceLocationType {

    @XmlEnumValue("Local")
    LOCAL("Local"),
    @XmlEnumValue("Shared")
    SHARED("Shared"),
    @XmlEnumValue("SourceControl")
    SOURCE_CONTROL("SourceControl"),
    @XmlEnumValue("SourcePulling")
    SOURCE_PULLING("SourcePulling");
    private final String value;

    SourceLocationType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static SourceLocationType fromValue(String v) {
        for (SourceLocationType c: SourceLocationType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
