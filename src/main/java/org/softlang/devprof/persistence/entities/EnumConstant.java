package org.softlang.devprof.persistence.entities;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
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
public class EnumConstant implements Serializable{
    
    private static final long serialVersionUID = 6541384726332445651L;
    
    private Long id;
    private String name;
    private Package pack;
    private List<ChangedLine> changedLines;

    public EnumConstant() {
        changedLines = new LinkedList<ChangedLine>();
    }
    
    public EnumConstant(String name){
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
}
