package catchthepattern.com.afilters;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity(name = "filterbs")
public class FilterB {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name="afilterSetId", nullable=false)
    private AfilterSet afilterSet;
    
    EnumFilterVar var1;
    double var2;
    EnumOp op;
    double coef = 1.0;
    
    boolean include;
    
    public FilterB() {}
    
    public FilterB(AfilterSet afilterSet, EnumFilterVar var1, double var2, EnumOp op, double coef, boolean include) {        
        this.afilterSet = afilterSet;
        this.var1 = var1;
        this.var2 = var2;
        this.op = op;
        this.coef = coef;
        this.include = include;
    }
    public EnumFilterVar getVar1() {
        return var1;
    }
    public void setVar1(EnumFilterVar var1) {
        this.var1 = var1;
    }
    public double getVar2() {
        return var2;
    }
    public void setVar2(double var2) {
        this.var2 = var2;
    }
    public EnumOp getOp() {
        return op;
    }
    public void setOp(EnumOp op) {
        this.op = op;
    }
    public double getCoef() {
        return coef;
    }
    public void setCoef(double coef) {
        this.coef = coef;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public AfilterSet getAfilterSet() {
        return afilterSet;
    }
    public void setAfilterSet(AfilterSet afilterSet) {
        this.afilterSet = afilterSet;
    }

    public boolean isInclude() {
        return include;
    }

    public void setInclude(boolean include) {
        this.include = include;
    }
    
    
    
}
