package catchthepattern.com.afilters;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

@Entity(name = "filteras")
public class FilterA {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;    
    
    
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name="afilterSetId", nullable=false)
    private AfilterSet afilterSet;
    
    EnumFilterVar var1;// = FILTER_VAR.DAY0_C;
    EnumFilterVar var2;
    EnumOp op;// = OP.GREATER_THAN;
    double coef;// = COEF.C_1;
    
    boolean include;
   
    public FilterA() {}
    
    public FilterA(EnumFilterVar var1, EnumFilterVar var2, EnumOp op, double coef, boolean include) {
        super();
        this.var1 = var1;
        this.var2 = var2;
        this.op = op;
        this.coef = coef;
        this.include = include;
    }
    
    
    public boolean isInclude() {
        return include;
    }

    public void setInclude(boolean include) {
        this.include = include;
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


    public EnumFilterVar getVar1() {
        return var1;
    }
    public void setVar1(EnumFilterVar var1) {
        this.var1 = var1;
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
    public EnumFilterVar getVar2() {
        return var2;
    }
    public void setVar2(EnumFilterVar var2) {
        this.var2 = var2;
    }
    
    
    
}
