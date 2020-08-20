package org.clas.detectors;

import org.clas.viewer.DetectorMonitor;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.H1F;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

/**
 *
 * @author devita
 */


public class HELmonitor extends DetectorMonitor {

    int syncSave = -1;
    
    public HELmonitor(String name) {
        super(name);
        this.setDetectorTabNames("Raw Signals", "Helicity");
        this.init(false);
    }

    
    @Override
    public void createHistos() {
        // create histograms
        this.setNumberOfEvents(0);
        H1F summary = new H1F("summary","helicity",20,-1.5,1.5);
        summary.setTitleX("helicity");
        summary.setTitleY("Counts");
        summary.setFillColor(3);
        DataGroup sum = new DataGroup(1,1);
        sum.addDataSet(summary, 0);
        this.setDetectorSummary(sum);
        H1F rawHel = new H1F("rawHelicity","rawHelicity", 200,0.,4000.);
        rawHel.setTitleX("helicity");
        rawHel.setTitleY("Counts");
        rawHel.setFillColor(22);
        H1F rawSync = new H1F("rawSync","rawSync", 200,0.,4000.);
        rawSync.setTitleX("sync");
        rawSync.setTitleY("Counts");
        rawSync.setFillColor(33);
        H1F rawQuartet = new H1F("rawQuartet","rawQuartet", 200,0.,4000.);
        rawQuartet.setTitleX("quartet");
        rawQuartet.setTitleY("Counts");
        rawQuartet.setFillColor(44);
        H1F rawTsettle = new H1F("rawTsettle","rawTsettle", 200,0.,4000.);
        rawTsettle.setTitleX("tsettle");
        rawTsettle.setTitleY("Counts");
        rawTsettle.setFillColor(55);
        H1F hel = new H1F("helicity","helicity", 20,-1.5,1.5);
        hel.setTitleX("helicity");
        hel.setTitleY("Counts");
        hel.setFillColor(22);
        H1F sync = new H1F("sync","sync", 20,-1.5,1.5);
        sync.setTitleX("sync");
        sync.setTitleY("Counts");
        sync.setFillColor(33);
        H1F quartet = new H1F("quartet","quartet", 20,-1.5,1.5);
        quartet.setTitleX("quartet");
        quartet.setTitleY("Counts");
        quartet.setFillColor(44);
        H1F tsettle = new H1F("tsettle","tsettle", 20,-1.5,1.5);
        tsettle.setTitleX("tsettle");
        tsettle.setTitleY("Counts");
        tsettle.setFillColor(55);
        GraphErrors  helSequence = new GraphErrors("helSequence");
        helSequence.setTitle("Helicity Sequence"); //  title
        helSequence.setTitleX("Event Number"); // X axis title
        helSequence.setTitleY("Signals");   // Y axis title
        helSequence.setMarkerColor(22); // color from 0-9 for given palette
        helSequence.setMarkerSize(5);  // size in points on the screen
        GraphErrors  syncSequence = new GraphErrors("syncSequence");
        syncSequence.setTitle("Sync Sequence"); //  title
        syncSequence.setTitleX("Event Number"); // X axis title
        syncSequence.setTitleY("Signals");   // Y axis title
        syncSequence.setMarkerColor(33); // color from 0-9 for given palette
        syncSequence.setMarkerSize(5);  // size in points on the screen
        GraphErrors  quartetSequence = new GraphErrors("quartetSequence");
        quartetSequence.setTitle("Quartet Sequence"); //  title
        quartetSequence.setTitleX("Event Number"); // X axis title
        quartetSequence.setTitleY("Signals");   // Y axis title
        quartetSequence.setMarkerColor(44); // color from 0-9 for given palette
        quartetSequence.setMarkerSize(5);  // size in points on the screen

//        helSequence.addPoint(0, 0, 1, 1);
//        helSequence.addPoint(1, 1, 1, 1);
        DataGroup dg = new DataGroup(2,4);
        dg.addDataSet(rawHel,      0);
        dg.addDataSet(rawSync,     1);
        dg.addDataSet(rawQuartet,  2);
        dg.addDataSet(rawTsettle,  3);
        dg.addDataSet(hel,         4);
        dg.addDataSet(sync,        5);
        dg.addDataSet(quartet,     6);
        dg.addDataSet(tsettle,     7);
        dg.addDataSet(helSequence, 7);
        dg.addDataSet(syncSequence, 7);
        dg.addDataSet(quartetSequence, 7);

        this.getDataGroup().add(dg, 0,0,0);
    }
        
    @Override
    public void plotHistos() {
        // initialize canvas and plot histograms
        this.getDetectorCanvas().getCanvas("Raw Signals").divide(2, 2);
        this.getDetectorCanvas().getCanvas("Raw Signals").setGridX(false);
        this.getDetectorCanvas().getCanvas("Raw Signals").setGridY(false);
        this.getDetectorCanvas().getCanvas("Raw Signals").cd(0);
        this.getDetectorCanvas().getCanvas("Raw Signals").getPad(0).getAxisY().setLog(true);
        this.getDetectorCanvas().getCanvas("Raw Signals").draw(this.getDataGroup().getItem(0,0,0).getH1F("rawHelicity"));
        this.getDetectorCanvas().getCanvas("Raw Signals").cd(1);
        this.getDetectorCanvas().getCanvas("Raw Signals").getPad(1).getAxisY().setLog(true);
        this.getDetectorCanvas().getCanvas("Raw Signals").draw(this.getDataGroup().getItem(0,0,0).getH1F("rawSync"));
        this.getDetectorCanvas().getCanvas("Raw Signals").cd(2);
        this.getDetectorCanvas().getCanvas("Raw Signals").getPad(2).getAxisY().setLog(true);
        this.getDetectorCanvas().getCanvas("Raw Signals").draw(this.getDataGroup().getItem(0,0,0).getH1F("rawQuartet"));
        this.getDetectorCanvas().getCanvas("Raw Signals").cd(3);
        this.getDetectorCanvas().getCanvas("Raw Signals").getPad(2).getAxisY().setLog(true);
        this.getDetectorCanvas().getCanvas("Raw Signals").draw(this.getDataGroup().getItem(0,0,0).getH1F("rawTsettle"));
        this.getDetectorCanvas().getCanvas("Helicity").divide(2, 2);
        this.getDetectorCanvas().getCanvas("Helicity").setGridX(false);
        this.getDetectorCanvas().getCanvas("Helicity").setGridY(false);
        this.getDetectorCanvas().getCanvas("Helicity").cd(0);
        this.getDetectorCanvas().getCanvas("Helicity").draw(this.getDataGroup().getItem(0,0,0).getH1F("helicity"));
        this.getDetectorCanvas().getCanvas("Helicity").cd(1);
        this.getDetectorCanvas().getCanvas("Helicity").draw(this.getDataGroup().getItem(0,0,0).getH1F("sync"));
        this.getDetectorCanvas().getCanvas("Helicity").cd(2);
        this.getDetectorCanvas().getCanvas("Helicity").draw(this.getDataGroup().getItem(0,0,0).getH1F("quartet"));
        this.getDetectorCanvas().getCanvas("Helicity").cd(3);
        this.getDetectorCanvas().getCanvas("Helicity").draw(this.getDataGroup().getItem(0,0,0).getH1F("tsettle"));
//        if(this.getDataGroup().getItem(0,0,0).getGraph("helSequence").getVectorX().size()>1) {
//            this.getDetectorCanvas().getCanvas("Helicity").draw(this.getDataGroup().getItem(0,0,0).getGraph("helSequence"));
//            this.getDetectorCanvas().getCanvas("Helicity").draw(this.getDataGroup().getItem(0,0,0).getGraph("syncSequence"),"same");
//            this.getDetectorCanvas().getCanvas("Helicity").draw(this.getDataGroup().getItem(0,0,0).getGraph("quartetSequence"),"same");
//        }
        this.getDetectorCanvas().getCanvas("Helicity").update();
        
    }

    @Override
    public void processEvent(DataEvent event) {
        

   
    // process event info and save into data group
        
        if(event.hasBank("RUN::config") && event.hasBank("HEL::adc")){
	    DataBank head = event.getBank("RUN::config");
	    DataBank bank = event.getBank("HEL::adc");
	    int eventNumber = head.getInt("event", 0);
	    int unixTime = head.getInt("unixtime", 0);
            int rows = bank.rows();
            int hel     = -1;
            int sync    = -1;
            int quartet = -1;
            int tsettle = -1;
            hel=-1; sync=-1; quartet=-1; tsettle=-1;
            boolean good = true; 
	    for(int loop = 0; loop < rows; loop++){
                int component = bank.getShort("component", loop);
                int rawValue  = bank.getShort("ped", loop);
                int value = (int) rawValue/2000;
                if(component      == 1) {
                    this.getDataGroup().getItem(0,0,0).getH1F("rawHelicity").fill(rawValue);
                    this.getDataGroup().getItem(0,0,0).getH1F("helicity").fill(value);
                    hel=value;
                }
                else if(component == 2) {
                    this.getDataGroup().getItem(0,0,0).getH1F("rawSync").fill(rawValue);
                    this.getDataGroup().getItem(0,0,0).getH1F("sync").fill(value);
                    sync=value;
                }
                else if(component == 3) {
                    this.getDataGroup().getItem(0,0,0).getH1F("rawQuartet").fill(rawValue);
                    this.getDataGroup().getItem(0,0,0).getH1F("quartet").fill(value);
                    quartet=value;
                }
                else if(component == 4) {
                    this.getDataGroup().getItem(0,0,0).getH1F("rawTsettle").fill(rawValue);
                    this.getDataGroup().getItem(0,0,0).getH1F("tsettle").fill(value);
                    tsettle=value;
                }
            }
            if(hel==-1 || sync == -1 || quartet == -1 || tsettle == -1) {
                System.out.println("Helicity info not set for event number " + eventNumber);
                bank.show();
                good = false;
            }
            if(syncSave != sync && false) {
                syncSave = sync;
//                System.out.println(eventNumber + " " + unixTime + " " + hel + " " + sync + " " + quartet);
                this.getDataGroup().getItem(0,0,0).getGraph("helSequence").addPoint(eventNumber, hel, 0., 0.);
                this.getDataGroup().getItem(0,0,0).getGraph("syncSequence").addPoint(eventNumber, sync+0.1, 0., 0.);
                this.getDataGroup().getItem(0,0,0).getGraph("quartetSequence").addPoint(eventNumber, quartet-0.1, 0., 0.);
                if(this.getDataGroup().getItem(0,0,0).getGraph("helSequence").getVectorX().size()==2) {
                    this.getDetectorCanvas().getCanvas("Helicity").cd(3);
                    this.getDetectorCanvas().getCanvas("Helicity").draw(this.getDataGroup().getItem(0,0,0).getGraph("helSequence"));
                    this.getDetectorCanvas().getCanvas("Helicity").draw(this.getDataGroup().getItem(0,0,0).getGraph("syncSequence"),"same");
                    this.getDetectorCanvas().getCanvas("Helicity").draw(this.getDataGroup().getItem(0,0,0).getGraph("quartetSequence"),"same");
                    this.getDetectorCanvas().getCanvas("Raw Signals").getPad(3).getAxisY().setRange(-1, 2);
                }

            }
            this.getDetectorSummary().getH1F("summary").fill(hel);
        }       
        
    }

    @Override
    public void timerUpdate() {
    }
    
}
