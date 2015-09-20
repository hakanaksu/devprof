package org.softlang.devprof.persistence.entities;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

/**
 *
 * @author Hakan Aksu
 */
@Entity
public class ChangedLine implements Serializable {
    
    private static final long serialVersionUID = 5152247631807517199L;
    
    private Long id;
    
    private String change;
    private Character type;
    
    private ChangedFile changedFile;
    private Set<MethodEntity> methods;
    private Set<Classifier> classifiers;
    private Set<EnumConstant> enums;
    

    public ChangedLine() {
        this.methods = new LinkedHashSet<MethodEntity>();
        this.classifiers = new LinkedHashSet<Classifier>();
        this.enums = new LinkedHashSet<EnumConstant>();
    }
    
    public ChangedLine(String change, Character type) {
        this.change = change;
        this.type = type;
        this.methods = new LinkedHashSet<MethodEntity>();
        this.classifiers = new LinkedHashSet<Classifier>();
        this.enums = new LinkedHashSet<EnumConstant>();
    }

    public ChangedLine(String change, Character type, ChangedFile changedFile) {
        this.change = change;
        this.type = type;
        this.changedFile = changedFile;
        this.methods = new LinkedHashSet<MethodEntity>();
        this.classifiers = new LinkedHashSet<Classifier>();
        this.enums = new LinkedHashSet<EnumConstant>();
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
            return id;
    }

    public void setId(Long id) {
            this.id = id;
    }

    public String getChange() {
        return change;
    }

    public void setChange(String change) {
        this.change = change;
    }

    public Character getType() {
        return type;
    }

    public void setType(Character type) {
        this.type = type;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    public ChangedFile getChangedFile() {
        return changedFile;
    }

    public void setChangedFile(ChangedFile changedFile) {
        this.changedFile = changedFile;
    }

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "changedLines", cascade = CascadeType.ALL)
    public Set<MethodEntity> getMethods() {
        return methods;
    }

    public void setMethods(Set<MethodEntity> methods) {
        this.methods = methods;
    }

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "changedLines", cascade = CascadeType.ALL)
    public Set<Classifier> getClassifier() {
        return classifiers;
    }

    public void setClassifier(Set<Classifier> classifiers) {
        this.classifiers = classifiers;
    }

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "changedLines", cascade = CascadeType.ALL)
    public Set<EnumConstant> getEnums() {
        return enums;
    }

    public void setEnums(Set<EnumConstant> enums) {
        this.enums = enums;
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + Objects.hashCode(this.change);
        hash = 37 * hash + Objects.hashCode(this.type);
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
        final ChangedLine other = (ChangedLine) obj;
        if (!Objects.equals(this.change, other.change)) {
            return false;
        }
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        return true;
    }
    
}