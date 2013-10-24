
package com.checkmarx.cxviewer.ws.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ScheduleSettings complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ScheduleSettings">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Schedule" type="{http://Checkmarx.com/v7}ScheduleType"/>
 *         &lt;element name="ScheduledDays" type="{http://Checkmarx.com/v7}ArrayOfDayOfWeek" minOccurs="0"/>
 *         &lt;element name="Time" type="{http://Checkmarx.com/v7}CxDateTime" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ScheduleSettings", propOrder = {
    "schedule",
    "scheduledDays",
    "time"
})
public class ScheduleSettings {

    @XmlElement(name = "Schedule", required = true)
    protected ScheduleType schedule;
    @XmlElement(name = "ScheduledDays")
    protected ArrayOfDayOfWeek scheduledDays;
    @XmlElement(name = "Time")
    protected CxDateTime time;

    /**
     * Gets the value of the schedule property.
     * 
     * @return
     *     possible object is
     *     {@link ScheduleType }
     *     
     */
    public ScheduleType getSchedule() {
        return schedule;
    }

    /**
     * Sets the value of the schedule property.
     * 
     * @param value
     *     allowed object is
     *     {@link ScheduleType }
     *     
     */
    public void setSchedule(ScheduleType value) {
        this.schedule = value;
    }

    /**
     * Gets the value of the scheduledDays property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfDayOfWeek }
     *     
     */
    public ArrayOfDayOfWeek getScheduledDays() {
        return scheduledDays;
    }

    /**
     * Sets the value of the scheduledDays property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfDayOfWeek }
     *     
     */
    public void setScheduledDays(ArrayOfDayOfWeek value) {
        this.scheduledDays = value;
    }

    /**
     * Gets the value of the time property.
     * 
     * @return
     *     possible object is
     *     {@link CxDateTime }
     *     
     */
    public CxDateTime getTime() {
        return time;
    }

    /**
     * Sets the value of the time property.
     * 
     * @param value
     *     allowed object is
     *     {@link CxDateTime }
     *     
     */
    public void setTime(CxDateTime value) {
        this.time = value;
    }

}
