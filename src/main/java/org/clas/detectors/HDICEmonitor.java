package org.clas.detectors;

import java.io.File;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.clas.viewer.DetectorMonitor;
import org.jlab.elog.LogEntry;
import org.jlab.elog.exception.AttachmentSizeException;
import org.jlab.elog.exception.LogIOException;
import org.jlab.elog.exception.LogRuntimeException;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.utils.system.ClasUtilsFile;

/**
 *
 * @author devita
 */

public class HDICEmonitor extends DetectorMonitor {
    
    private double[] gains =  new double[32];

    public HDICEmonitor(String name) {
        super(name);
        this.setDetectorTabNames("1Dhisto","2Dhisto");
        this.init(false);
    }

    
    @Override
    public void createHistos() {
        // create histograms
        this.setNumberOfEvents(0);
        H1F summary = new H1F("summary","summary",6,0.5,6.5);
        summary.setTitleX("sector");
        summary.setTitleY("DC occupancy");
        summary.setTitle("DC");
        summary.setFillColor(33);
        DataGroup sum = new DataGroup(1,1);
        sum.addDataSet(summary, 0);
        this.setDetectorSummary(sum);
                
        DataGroup dg1D = new DataGroup(8,4);
        for (int i = 0; i < 32; i++) {
            H1F h1 = null;
            if(i < 15)  h1 = new H1F("h1" + i, "", 200, 0.0, 3.0);
            else        h1 = new H1F("h1" + i, "", 200, 0.0, 10.0);
            h1.setTitleX("Energy (MeV)");
            h1.setTitleY("Counts");
            h1.setOptStat("1010");
            h1.setLineColor(2);
            dg1D.addDataSet(h1, i);
        }
        this.getDataGroup().add(dg1D, 0,0,0);
        
        DataGroup dg2D = new DataGroup(4,4);
        for (int i = 0; i < 16; i++) {
            H2F h2 = new H2F("h2" + i, "",200, 0.0, 10.0, 200, 0.0, 2.0);
            h2.setTitleX("E (EC) (MeV)");
            h2.setTitleY("E (DEC) (MeV)");
//                       h2[i].setLineColor(3);
            dg2D.addDataSet(h2, i);
        }
        this.getDataGroup().add(dg2D, 1,0,0);
        
        Scanner input = null;
        try {
            String gainDir = ClasUtilsFile.getResourceDir("HDICEDIR", "/");
            input = new Scanner(new File(gainDir+"/Gain_FADC.dat"));
        } catch (Exception ex) {
            System.out.println("Can not open file.");
            System.exit(0);
        }
        int j=0;
        while(input.hasNextDouble()) {
            double number = input.nextDouble();
            gains[j] = number;
            j = j + 1;
        }
    }
    

    @Override
    public void plotHistos() {
        // initialize canvas and plot histograms    	    
        this.getDetectorCanvas().getCanvas("1Dhisto").divide(8, 4);
        this.getDetectorCanvas().getCanvas("1Dhisto").setGridX(false);
        this.getDetectorCanvas().getCanvas("1Dhisto").setGridY(false);
        for(int i=0; i<32; i++) {
            this.getDetectorCanvas().getCanvas("1Dhisto").cd(i);
            this.getDetectorCanvas().getCanvas("1Dhisto").draw(this.getDataGroup().getItem(0,0,0).getH1F("h1"+i));
        }
        
        this.getDetectorCanvas().getCanvas("2Dhisto").divide(4, 4);
        this.getDetectorCanvas().getCanvas("2Dhisto").setGridX(false);
        this.getDetectorCanvas().getCanvas("2Dhisto").setGridY(false);
        this.getDetectorCanvas().getCanvas("2Dhisto").update();
        for(int i=0; i<16; i++) {
            this.getDetectorCanvas().getCanvas("2Dhisto").cd(i);
            this.getDetectorCanvas().getCanvas("2Dhisto").draw(this.getDataGroup().getItem(1,0,0).getH2F("h2"+i));
        }
    }


    @Override
    public void processEvent(DataEvent event) {
        
                       
        // process event info and save into data group
        double [] hADC = new double[32];
        double [] eADC = new double[32];

        int [] iacon = new int [32];
        if(event.hasBank("HDICE::adc")) {
            DataBank bank = event.getBank("HDICE::adc");
            int rows = bank.rows();
            for(int loop = 0; loop < rows; loop++){
                int sector = bank.getByte("sector", loop);
                int layer  = bank.getByte("layer", loop);
                int comp   = bank.getShort("component", loop);
                int ADC    = bank.getInt("ADC", loop);
                
                
                int iadc = -1;
                if(layer < 4)   iadc = 16*(comp - 1) + 3*(sector - 1) + (layer - 1);
                if(layer == 4)  iadc = 16*(comp - 1) + (sector - 1) + 12;

                eADC[iadc] = 1.0/(gains[iadc])*ADC;
            }

            // ---------- Anti conincidence ----------------------------
            for(int ida=0; ida<12; ida++) {
                if(eADC[ida] > 0.0 && eADC[ida + 16] > 0.0){
                    if(ida%3 == 0){
                        if(eADC[ida + 1] == 0.0 && eADC[ida + 16 + 1] == 0.0){ 
                            iacon[ida] = 1;
                            iacon[ida + 16] = 1;
                        }
                    }
                    if(ida%3 == 1){
                        if(eADC[ida - 1] == 0.0 && eADC[ida + 16 - 1] == 0.0 && eADC[ida + 1] == 0.0 && eADC[ida + 16 + 1] == 0.0){
                            iacon[ida] = 1;
                            iacon[ida + 16] = 1;
                        }
                    }
                    if(ida%3 == 2){
                        if(eADC[ida - 1] == 0.0 && eADC[ida + 16 - 1] == 0.0){
                            iacon[ida] = 1;
                            iacon[ida + 16] = 1;
                        }
                    }
                 }
            }
            for(int idb=12; idb<16; idb++) {
                if(eADC[idb] > 0.0 && eADC[idb + 16] > 0.0){
                    iacon[idb] = 1;
                    iacon[idb + 16] = 1;
                }
            }
            // fill histograms
            for(int icha=0; icha<32; icha++) {
                if(iacon[icha] == 1) {
                    this.getDataGroup().getItem(0,0,0).getH1F("h1"+icha).fill(eADC[icha]);
                    if(icha<16) this.getDataGroup().getItem(1,0,0).getH2F("h2"+icha).fill(eADC[icha + 16],eADC[icha]);
                }
            }
        }   
    }

    @Override
    public void timerUpdate() {

    }

}
