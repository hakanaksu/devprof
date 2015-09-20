package org.softlang.devprof.persistence.entities;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Hakan Aksu
 */
@Entity
public class Version implements Serializable, Comparable<Version>{
    
    private static final long serialVersionUID = 6671335748375701993L;
    
    private Long id;
    private Long revision;
    private String developer;
    private String comment;
    private Date date;
    private Set<ChangedFile> changedFiles;
    private Repository repository;

    public Version() {
        this.changedFiles = new LinkedHashSet<ChangedFile>();
    }
    
    public Version(Long revision, String developer, String comment, Date date) {
        this.revision = revision;
        this.developer = developer;
        this.comment = comment;
        this.date = date;
        this.changedFiles = new LinkedHashSet<ChangedFile>();
    }
    
    public Version(Long revision, String developer, String comment, Date date, Repository repository) {
        this.revision = revision;
        this.developer = developer;
        this.comment = comment;
        this.date = date;
        this.repository = repository;
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
    
    public Long getRevision() {
        return revision;
    }
    
    public void setRevision(Long revision) {
        this.revision = revision;
    }

    public String getDeveloper() {
        return developer;
    }
    
    public void setDeveloper(String developer) {
        this.developer = developer;
    }

    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }

    @Temporal(TemporalType.DATE)
    public Date getDate() {
        return date;
    }
    
    public void setDate(Date date) {
        this.date = date;
    }

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "version", cascade = CascadeType.REMOVE)
    public Set<ChangedFile> getChangedFiles() {
        return changedFiles;
    }

    public void setChangedFiles(Set<ChangedFile> changedFiles) {
        this.changedFiles = changedFiles;
    }
    
    @ManyToOne
    public Repository getRepository() {
        return repository;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + Objects.hashCode(this.revision);
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
        final Version other = (Version) obj;
        if (!Objects.equals(this.revision, other.revision)) {
            return false;
        }
        return true;
    }


    public int compareTo(Version o) {
        return this.revision.compareTo(o.revision);
    }

}
