package org.softlang.devprof.persistence.entities;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

/**
 *
 * @author Hakan Aksu
 */
@Entity
public class Package implements Serializable{
    
    private static final long serialVersionUID = -7952307665507859322L;
    
    private Long id;
    private String packageName;
    private Set<ChangedFile> changedFiles;
    private API api;
    private Set<Classifier> classifier;
    private Set<MethodEntity> methods;
    private Set<EnumConstant> enums;
    private Domain domain;

    public Package() {
        this.changedFiles = new LinkedHashSet<ChangedFile>();
    }

    public Package(String packageName) {
        this.packageName = packageName;
        this.changedFiles = new LinkedHashSet<ChangedFile>();
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
    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    
    @ManyToOne (fetch = FetchType.LAZY)
    public API getApi() {
        return api;
    }

    public void setApi(API api) {
        this.api = api;
    }

    //(fetch = FetchType.EAGER)
    @ManyToMany 
    public Set<ChangedFile> getChangedFiles() {
        return changedFiles;
    }

    public void setChangedFiles(Set<ChangedFile> changedFiles) {
        this.changedFiles = changedFiles;
    }

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "pack")
    public Set<Classifier> getClassifier() {
        return classifier;
    }

    public void setClassifier(Set<Classifier> classifier) {
        this.classifier = classifier;
    }

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "pack")
    public Set<MethodEntity> getMethods() {
        return methods;
    }

    public void setMethods(Set<MethodEntity> methods) {
        this.methods = methods;
    }

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "pack")
    public Set<EnumConstant> getEnums() {
        return enums;
    }

    public void setEnums(Set<EnumConstant> enums) {
        this.enums = enums;
    }
    
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    public Domain getDomain() {
        return domain;
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + Objects.hashCode(this.packageName);
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
        Package other = (Package) obj;
        if (!Objects.equals(this.packageName, other.packageName)) {
            return false;
        }
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }
}
