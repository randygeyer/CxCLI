
package com.checkmarx.cxviewer.ws.generated;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ScheduleType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ScheduleType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="None"/>
 *     &lt;enumeration value="Now"/>
 *     &lt;enumeration value="Weekly"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "ScheduleType")
@XmlEnum
public enum ScheduleType {

    @XmlEnumValue("None")
    NONE("None"),
    @XmlEnumValue("Now")
    NOW("Now"),
    @XmlEnumValue("Weekly")
    WEEKLY("Weekly");
    private final String value;

    ScheduleType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ScheduleType fromValue(String v) {
        for (ScheduleType c: ScheduleType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
