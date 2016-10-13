//
// Este archivo ha sido generado por la arquitectura JavaTM para la implantaci�n de la referencia de enlace (JAXB) XML v2.2.8-b130911.1802 
// Visite <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Todas las modificaciones realizadas en este archivo se perder�n si se vuelve a compilar el esquema de origen. 
// Generado el: 2016.09.29 a las 01:22:41 PM CEST 
//


package com.oracle.schemas.bpel.audit_trail;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Clase Java para anonymous complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}message"/>
 *         &lt;sequence minOccurs="0">
 *           &lt;element ref="{}details"/>
 *           &lt;element ref="{}detailsTemplate" minOccurs="0"/>
 *         &lt;/sequence>
 *       &lt;/sequence>
 *       &lt;attribute name="wikey" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="type" use="required" type="{http://www.w3.org/2001/XMLSchema}byte" />
 *       &lt;attribute name="to" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="state" type="{http://www.w3.org/2001/XMLSchema}byte" />
 *       &lt;attribute name="sid" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="psid" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="patternName">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="bpelx:workflow"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="partnerWSDL">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value=""/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="n" use="required" type="{http://www.w3.org/2001/XMLSchema}short" />
 *       &lt;attribute name="label" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="date" use="required" type="{http://www.w3.org/2001/XMLSchema}dateTime" />
 *       &lt;attribute name="cat" use="required" type="{http://www.w3.org/2001/XMLSchema}byte" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "message",
    "details",
    "detailsTemplate"
})
@XmlRootElement(name = "event")
public class Event {

    @XmlElement(required = true)
    protected String message;
    protected Details details;
    protected String detailsTemplate;
    @XmlAttribute(name = "wikey")
    protected String wikey;
    @XmlAttribute(name = "type", required = true)
    protected byte type;
    @XmlAttribute(name = "to")
    protected String to;
    @XmlAttribute(name = "state")
    protected String state;
    @XmlAttribute(name = "sid", required = true)
    protected String sid;
    @XmlAttribute(name = "psid")
    protected String psid;
    @XmlAttribute(name = "patternName")
    protected String patternName;
    @XmlAttribute(name = "partnerWSDL")
    protected String partnerWSDL;
    @XmlAttribute(name = "n", required = true)
    protected short n;
    @XmlAttribute(name = "label")
    protected String label;
    @XmlAttribute(name = "date", required = true)
    @XmlSchemaType(name = "dateTime")
    protected String date;
    @XmlAttribute(name = "cat", required = true)
    protected byte cat;

    /**
     * Obtiene el valor de la propiedad message.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessage() {
        return message;
    }

    /**
     * Define el valor de la propiedad message.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessage(String value) {
        this.message = value;
    }

    /**
     * Obtiene el valor de la propiedad details.
     * 
     * @return
     *     possible object is
     *     {@link Details }
     *     
     */
    public Details getDetails() {
        return details;
    }

    /**
     * Define el valor de la propiedad details.
     * 
     * @param value
     *     allowed object is
     *     {@link Details }
     *     
     */
    public void setDetails(Details value) {
        this.details = value;
    }

    /**
     * Obtiene el valor de la propiedad detailsTemplate.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDetailsTemplate() {
        return detailsTemplate;
    }

    /**
     * Define el valor de la propiedad detailsTemplate.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDetailsTemplate(String value) {
        this.detailsTemplate = value;
    }

    /**
     * Obtiene el valor de la propiedad wikey.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWikey() {
        return wikey;
    }

    /**
     * Define el valor de la propiedad wikey.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWikey(String value) {
        this.wikey = value;
    }

    /**
     * Obtiene el valor de la propiedad type.
     * 
     */
    public byte getType() {
        return type;
    }

    /**
     * Define el valor de la propiedad type.
     * 
     */
    public void setType(byte value) {
        this.type = value;
    }

    /**
     * Obtiene el valor de la propiedad to.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTo() {
        return to;
    }

    /**
     * Define el valor de la propiedad to.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTo(String value) {
        this.to = value;
    }

    /**
     * Obtiene el valor de la propiedad state.
     * 
     * @return
     *     possible object is
     *     {@link Byte }
     *     
     */
    public String getState() {
        return state;
    }

    /**
     * Define el valor de la propiedad state.
     * 
     * @param value
     *     allowed object is
     *     {@link Byte }
     *     
     */
    public void setState(String value) {
        this.state = value;
    }

    /**
     * Obtiene el valor de la propiedad sid.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSid() {
        return sid;
    }

    /**
     * Define el valor de la propiedad sid.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSid(String value) {
        this.sid = value;
    }

    /**
     * Obtiene el valor de la propiedad psid.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPsid() {
        return psid;
    }

    /**
     * Define el valor de la propiedad psid.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPsid(String value) {
        this.psid = value;
    }

    /**
     * Obtiene el valor de la propiedad patternName.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPatternName() {
        return patternName;
    }

    /**
     * Define el valor de la propiedad patternName.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPatternName(String value) {
        this.patternName = value;
    }

    /**
     * Obtiene el valor de la propiedad partnerWSDL.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPartnerWSDL() {
        return partnerWSDL;
    }

    /**
     * Define el valor de la propiedad partnerWSDL.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPartnerWSDL(String value) {
        this.partnerWSDL = value;
    }

    /**
     * Obtiene el valor de la propiedad n.
     * 
     */
    public short getN() {
        return n;
    }

    /**
     * Define el valor de la propiedad n.
     * 
     */
    public void setN(short value) {
        this.n = value;
    }

    /**
     * Obtiene el valor de la propiedad label.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLabel() {
        return label;
    }

    /**
     * Define el valor de la propiedad label.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLabel(String value) {
        this.label = value;
    }

    /**
     * Obtiene el valor de la propiedad date.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public String getDate() {
        return date;
    }

    /**
     * Define el valor de la propiedad date.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDate(String value) {
        this.date = value;
    }

    /**
     * Obtiene el valor de la propiedad cat.
     * 
     */
    public byte getCat() {
        return cat;
    }

    /**
     * Define el valor de la propiedad cat.
     * 
     */
    public void setCat(byte value) {
        this.cat = value;
    }

}
