/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dhenton9000.dependency;

import javax.xml.bind.annotation.*;

/**
 *
 * @author dhenton
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "IvyDependency", propOrder = {
    "org",
    "name",
    "rev",
    "conf"
})
@XmlRootElement(name = "IvyDependency", namespace = "http://com.dhenton9000/xsd/ivy")
public class IvyDependency {

    @XmlElement(name = "org", namespace = "http://com.dhenton9000/xsd/ivy")
    private String org = null;
    @XmlElement(name = "name", namespace = "http://com.dhenton9000/xsd/ivy")
    private String name = null;
    @XmlElement(name = "rev", namespace = "http://com.dhenton9000/xsd/ivy")
    private String rev = null;
    @XmlElement(name = "conf", namespace = "http://com.dhenton9000/xsd/ivy")
    private String conf = null;

    /**
     * @return the org
     */
    public String getOrg() {
        return org;
    }

    /**
     * @param org the org to set
     */
    public void setOrg(String org) {
        this.org = org;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the rev
     */
    public String getRev() {
        return rev;
    }

    /**
     * @param rev the rev to set
     */
    public void setRev(String rev) {
        this.rev = rev;
    }

    /**
     * @return the conf
     */
    public String getConf() {
        return conf;
    }

    /**
     * @param conf the conf to set
     */
    public void setConf(String conf) {
        this.conf = conf;
    }

    @Override
    public String toString() {
        return "IvyDependency{" + "org=" + org + ", name=" + name + ", rev=" + rev + ", conf=" + conf + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IvyDependency other = (IvyDependency) obj;
        if ((this.org == null) ? (other.org != null) : !this.org.equals(other.org)) {
            return false;
        }
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if ((this.rev == null) ? (other.rev != null) : !this.rev.equals(other.rev)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + (this.org != null ? this.org.hashCode() : 0);
        hash = 71 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 71 * hash + (this.rev != null ? this.rev.hashCode() : 0);
        return hash;
    }
}
