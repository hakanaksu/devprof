package org.softlang.devprof.persistence.entities;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.TreeSet;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Hakan Aksu
 */
@Entity
public class Repository implements Serializable {
    
    private static final long serialVersionUID = 8373652947837210974L;
    
    private Long id;
    private String url;
    private Date date;
    private TreeSet<Version> versions;
    
    public Repository() {
        this.date = new Date();
        this.versions = new TreeSet<Version>();
    }

    public Repository(String url) {
        this.url = url;
        this.date = new Date();
        this.versions = new TreeSet<Version>();
    }

    public Repository(String url, TreeSet<Version> versions) {
        this.url = url;
        this.date = new Date();
        this.versions = versions;
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    
    @Temporal(TemporalType.DATE)
    public Date getDate() {
        return date;
    }
    
    public void setDate(Date date) {
        this.date = date;
    }
    
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "repository", cascade = CascadeType.REMOVE)
    public TreeSet<Version> getVersions() {
        return versions;
    }

    public void setVersions(TreeSet<Version> versions) {
        this.versions = versions;
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + Objects.hashCode(this.url);
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
        final Repository other = (Repository) obj;
        if (!Objects.equals(this.url, other.url)) {
            return false;
        }
        return true;
    }
    
}
