package org.softlang.devprof.persistence.entities;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

/**
 *
 * @author Hakan Aksu
 */
@Entity
public class Domain implements Serializable{
    
    private static final long serialVersionUID = -3434083667248419777L;
    
    private Long id;
    private String domain;
    private Set<Package> packages;

    public Domain() {
        packages = new HashSet<Package>();
    }

    public Domain(String domain) {
        this.domain = domain;
        packages = new HashSet<Package>();
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
            return id;
    }

    public void setId(Long id) {
            this.id = id;
    }

    @Column(unique = true)
    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "domain")
    public Set<Package> getPackages() {
        return packages;
    }

    public void setPackages(Set<Package> packages) {
        this.packages = packages;
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + Objects.hashCode(this.domain);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Domain other = (Domain) obj;
        if (!other.getDomain().equals(getDomain())){
            return false;
        }
        return true;
    }
}
