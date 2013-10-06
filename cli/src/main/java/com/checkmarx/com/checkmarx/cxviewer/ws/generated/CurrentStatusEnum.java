
package com.checkmarx.cxviewer.ws.generated;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for CurrentStatusEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="CurrentStatusEnum">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Queued"/>
 *     &lt;enumeration value="Working"/>
 *     &lt;enumeration value="Finished"/>
 *     &lt;enumeration value="Failed"/>
 *     &lt;enumeration value="Canceled"/>
 *     &lt;enumeration value="Deleted"/>
 *     &lt;enumeration value="Unknown"/>
 *     &lt;enumeration value="Unzipping"/>
 *     &lt;enumeration value="WaitingToProcess"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "CurrentStatusEnum")
@XmlEnum
public enum CurrentStatusEnum {

    @XmlEnumValue("Queued")
    QUEUED("Queued"),
    @XmlEnumValue("Working")
    WORKING("Working"),
    @XmlEnumValue("Finished")
    FINISHED("Finished"),
    @XmlEnumValue("Failed")
    FAILED("Failed"),
    @XmlEnumValue("Canceled")
    CANCELED("Canceled"),
    @XmlEnumValue("Deleted")
    DELETED("Deleted"),
    @XmlEnumValue("Unknown")
    UNKNOWN("Unknown"),
    @XmlEnumValue("Unzipping")
    UNZIPPING("Unzipping"),
    @XmlEnumValue("WaitingToProcess")
    WAITING_TO_PROCESS("WaitingToProcess");
    private final String value;

    CurrentStatusEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static CurrentStatusEnum fromValue(String v) {
        for (CurrentStatusEnum c: CurrentStatusEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
