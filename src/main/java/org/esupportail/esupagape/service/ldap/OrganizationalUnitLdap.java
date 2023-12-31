package org.esupportail.esupagape.service.ldap;

import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

import javax.naming.Name;
import java.util.List;

@Entry(objectClasses = {"organizationalUnit"})
public final class OrganizationalUnitLdap {

    @Id
    private Name dn;
    private List<String> supannRefId; //code UAI pour la liaison avec les personnels des scols
    private String postalAddress;
    private String supannCodeEntiteParent;
    private String supannCodeEntite;
    private String supannTypeEntite;
    private String description;

    public Name getDn() {
        return dn;
    }

    public void setDn(Name dn) {
        this.dn = dn;
    }

    public List<String> getSupannRefId() {
        return supannRefId;
    }

    public void setSupannRefId(List<String> supannRefId) {
        this.supannRefId = supannRefId;
    }

    public String getPostalAddress() {
        return postalAddress;
    }

    public void setPostalAddress(String postalAddress) {
        this.postalAddress = postalAddress;
    }

    public String getSupannCodeEntiteParent() {
        return supannCodeEntiteParent;
    }

    public void setSupannCodeEntiteParent(String supannCodeEntiteParent) {
        this.supannCodeEntiteParent = supannCodeEntiteParent;
    }

    public String getSupannCodeEntite() {
        return supannCodeEntite;
    }

    public void setSupannCodeEntite(String supannCodeEntite) {
        this.supannCodeEntite = supannCodeEntite;
    }

    public String getSupannTypeEntite() {
        return supannTypeEntite;
    }

    public void setSupannTypeEntite(String supannTypeEntite) {
        this.supannTypeEntite = supannTypeEntite;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

