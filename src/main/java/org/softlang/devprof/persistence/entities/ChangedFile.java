package org.softlang.devprof.persistence.entities;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
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
import javax.persistence.OneToMany;

/**
 *
 * @author Hakan Aksu
 */
@Entity
public class ChangedFile implements Serializable{
    
    private static final long serialVersionUID = 6194741713343757093L;
    
    private Long id;
    private String packageName;
    private String fileName;
    private Character type;
    private Version version;
    private Set<Package> packages;
    private List<ChangedLine> changedLines;

    public ChangedFile() {
        this.packages = new LinkedHashSet<Package>();
        this.changedLines = new LinkedList<ChangedLine>();
    }

    public ChangedFile(String filePath, String fileName, Character type) {
        this.packageName = filePath;
        this.fileName = fileName;
        this.type = type;
        this.packages = new LinkedHashSet<Package>();
        this.changedLines = new LinkedList<ChangedLine>();
    }

    public ChangedFile(String filePath, String fileName, Character type, Version version) {
        this.packageName = filePath;
        this.fileName = fileName;
        this.type = type;
        this.version = version;
        this.packages = new LinkedHashSet<Package>();
        this.changedLines = new LinkedList<ChangedLine>();
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
            return id;
    }

    public void setId(Long id) {
            this.id = id;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Character getType() {
        return type;
    }

    public void setType(Character type) {
        this.type = type;
    }

    @ManyToOne
    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "changedFiles")
    public Set<Package> getPackages() {
        return packages;
    }

    public void setPackages(Set<Package> packages) {
        this.packages = packages;
    }
    
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "changedFile", cascade = CascadeType.REMOVE)
    public List<ChangedLine> getChangedLines() {
        return changedLines;
    }

    public void setChangedLines(List<ChangedLine> changedLines) {
        this.changedLines = changedLines;
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + Objects.hashCode(this.packageName);
        hash = 37 * hash + Objects.hashCode(this.fileName);
        hash = 37 * hash + Objects.hashCode(this.type);
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
        final ChangedFile other = (ChangedFile) obj;
        if (!Objects.equals(this.packageName, other.packageName)) {
            return false;
        }
        if (!Objects.equals(this.fileName, other.fileName)) {
            return false;
        }
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }
    
}
