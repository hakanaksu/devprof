package org.softlang.devprof.persistence.entities;

import java.io.Serializable;
import java.util.LinkedHashSet;
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
public class API implements Serializable{
    
    private static final long serialVersionUID = 1294683952218077199L;
    
    private Long id;
    private String jarName;
    private String jarPath;
    private Set<Package> packages;


    public API() {
        this.packages = new LinkedHashSet<Package>();
    }

    public API(String jarName, String jarPath, String jarVersion) {
        this.jarName = jarName;
        this.jarPath = jarPath;
        this.packages = new LinkedHashSet<Package>();
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
    public String getJarName() {
        return jarName;
    }

    public void setJarName(String jarName) {
        this.jarName = jarName;
    }

    public String getJarPath() {
        return jarPath;
    }

    public void setJarPath(String jarPath) {
        this.jarPath = jarPath;
    }

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "api")
    public Set<Package> getPackages() {
        return packages;
    }

    public void setPackages(Set<Package> packages) {
        this.packages = packages;
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + Objects.hashCode(this.id);
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
        final API other = (API) obj;
        if (!(other.getJarName().equals(getJarName()) && other.getJarPath().equals(getJarPath()) )){
            return false;
        }
        return true;
    }
    
}
