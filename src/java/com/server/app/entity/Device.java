package com.server.app.entity;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Diogo Oliveira
 * @date 13/11/2015 10:24:22
 */
@Entity
@Table(name = "DEVICE")
@XmlRootElement
@NamedQueries(
        {
            @NamedQuery(name = "Device.findAll", query = "SELECT d FROM Device d"),
            @NamedQuery(name = "Device.findById", query = "SELECT d FROM Device d WHERE d.id = :id"),
            @NamedQuery(name = "Device.gerateId", query = "SELECT (COALESCE(MAX(d.id), 2000) + 1) AS id FROM Device d"),
            @NamedQuery(name = "Device.findByRegistrationId", query = "SELECT d FROM Device d WHERE d.registrationId = :registrationId"),
            @NamedQuery(name = "Device.findByRegistrationDate", query = "SELECT d FROM Device d WHERE d.registrationDate = :registrationDate")
        })
public class Device extends JsonMessage implements Serializable
{
    @Id
    @NotNull
    @Basic(optional = false)
    @Column(name = "ID")
    private Integer id;

    @NotNull
    @Basic(optional = false)
    @Size(min = 1, max = 500)
    @Column(name = "REGISTRATION_ID")
    private String registrationId;

    @NotNull
    @Basic(optional = false)
    @Column(name = "REGISTRATION_DATE")
    private long registrationDate;

    public Device()
    {
    }

    public Device(Integer id)
    {
        this.id = id;
    }

    public Device(Integer id, String registrationId, long registrationDate)
    {
        this.id = id;
        this.registrationId = registrationId;
        this.registrationDate = registrationDate;
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public String getRegistrationId()
    {
        return registrationId;
    }

    public void setRegistrationId(String registrationId)
    {
        this.registrationId = registrationId;
    }

    public long getRegistrationDate()
    {
        return registrationDate;
    }

    public void setRegistrationDate(long registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    @Override
    public int hashCode()
    {
        return (id != null ? id.hashCode() : 0);
    }

    @Override
    public boolean equals(Object object)
    {
        if(!(object instanceof Device))
        {
            return false;
        }

        Device other = (Device)object;
        return (this.id.equals(other.id));
    }

    @Override
    public String toString()
    {
        return "com.server.app.entity.Device[ id=" + id + " ]";
    }
}
