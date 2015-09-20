package org.softlang.devprof.persistence.entities;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
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
public class Classifier implements Serializable {
    
    private static final long serialVersionUID = -4192714981374517199L;
    
    private Long id;
    private String name;
    private Package pack;
    private List<ChangedLine> changedLines;

    public Classifier() {
        this.changedLines = new LinkedList<ChangedLine>();
    }

    public Classifier(String name) {
        this.name = name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ManyToOne
    public Package getPack() {
        return pack;
    }

    public void setPack(Package pack) {
        this.pack = pack;
    }
    
    @ManyToMany(fetch = FetchType.LAZY)
    public List<ChangedLine> getChangedLines() {
        return changedLines;
    }

    public void setChangedLines(List<ChangedLine> changedLines) {
        this.changedLines = changedLines;
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + Objects.hashCode(this.name);
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
        final Classifier other = (Classifier) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }
}
