package catchthepattern.com.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import catchthepattern.com.models.PatternDef;

import org.assertj.core.api.*;

public class SearchPatternServiceTest {
    
    private SearchPatternService sps;
    PatternDef pdf1;
    PatternDef pdf2;
    PatternDef pdf3;
    
    @BeforeEach
    public void setup() {
        sps = new SearchPatternService();
        
        pdf1 = new PatternDef();
        pdf1.setOpenClose(true);
        pdf1.setHighLow(true);
        pdf1.setVol(true);
        pdf1.setOhlc(new double[][]{
            new double[] { 65.0, 65.0, 18.0, 18.0 },
            new double[] { 0.0, 100.0, 0.0, 100.0 }
        });
        pdf1.setVols(new double[]{0.0,100.0});
        
        pdf2 = new PatternDef();
        pdf2.setOpenClose(true);
        pdf2.setHighLow(true);
        pdf2.setVol(true);
        pdf2.setOhlc(new double[][]{
            new double[] { 30.0, 87.0, 11.0, 25.0 },
            new double[] { 12.0, 100, 0, 93.0 },
           
        });
        pdf2.setVols(new double[]{0.0,100.0});
        
        
        pdf3 = new PatternDef();
        pdf3.setOpenClose(true);
        pdf3.setHighLow(true);
        pdf3.setVol(false);
        pdf3.setOhlc(new double[][]{
            new double[] { 0, 94, 0, 94 },
            new double[] { 83, 94, 83, 94 },
            new double[] { 88, 131, 88, 111 },
            new double[] { 88, 100, 88, 100 }           
        });
        pdf3.setVols(new double[]{50, 20, 30, 100});
    }
    /*
     * @Test
     * void getPatternScoreTest() {
     * Assertions.assertThat(sps.getPatternScore(pdf1, pdf2)).isEqualTo(9.0);
     * }
     * 
     * @Test
     * void getNormalizedPatternDefTest() {
     * Assertions.assertThat(sps.getNormalizedPatternDef(pdf3).getOhlc()[2][2]).
     * isEqualTo(67.17557251908397);
     * }
     */


}
