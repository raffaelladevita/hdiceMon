package org.clas.viewer;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSplitPane;
import org.jlab.detector.base.DetectorOccupancy;
import org.jlab.detector.calib.utils.ConstantsManager;
import org.jlab.detector.view.DetectorPane2D;
import org.jlab.elog.LogEntry;
import org.jlab.groot.base.GStyle;
import org.jlab.groot.data.IDataSet;
import org.jlab.groot.data.TDirectory;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.groot.graphics.EmbeddedCanvasTabbed;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataEventType;
import org.jlab.io.task.IDataEventListener;
import org.jlab.utils.groups.IndexedList;

/**
 *
 * @author devita
 */


public class DetectorMonitor implements IDataEventListener {    
    
    private final String           detectorName;
    private ConstantsManager                    ccdb = new ConstantsManager(); 
    private ArrayList<String>      detectorTabNames  = new ArrayList();
    private IndexedList<DataGroup> detectorData      = new IndexedList<DataGroup>(3);
    private DataGroup              detectorSummary   = null;
    private DetectorOccupancy      detectorOccupancy = new DetectorOccupancy();
    private JPanel                 detectorPanel     = null;
    private EmbeddedCanvasTabbed   detectorCanvas    = null;
    private DetectorPane2D         detectorView      = null;
    private int                    numberOfEvents;
    private Boolean                     detectorLogZ = true;
    private Boolean                     detectorLogY = false;
    private String                           workDir = null;
    
    public IndexedList<List<Float>>         ttdcs = new IndexedList<List<Float>>(4);
    public IndexedList<List<Float>>         fadcs = new IndexedList<List<Float>>(4);
    public IndexedList<List<Float>>         ftdcs = new IndexedList<List<Float>>(4);
    public IndexedList<List<Integer>>       fapmt = new IndexedList<List<Integer>>(3); 
    public IndexedList<List<Integer>>       ftpmt = new IndexedList<List<Integer>>(3); 
        
    
    public DetectorMonitor(String name){
        GStyle.getAxisAttributesX().setTitleFontSize(18); //24
        GStyle.getAxisAttributesX().setLabelFontSize(18); //18
        GStyle.getAxisAttributesY().setTitleFontSize(18); //24
        GStyle.getAxisAttributesY().setLabelFontSize(18); //18
        GStyle.getAxisAttributesZ().setLabelFontSize(14); //14
        GStyle.setPalette("kDefault");
        GStyle.getAxisAttributesX().setLabelFontName("Avenir");
        GStyle.getAxisAttributesY().setLabelFontName("Avenir");
        GStyle.getAxisAttributesZ().setLabelFontName("Avenir");
        GStyle.getAxisAttributesX().setTitleFontName("Avenir");
        GStyle.getAxisAttributesY().setTitleFontName("Avenir");
        GStyle.getAxisAttributesZ().setTitleFontName("Avenir");
        GStyle.setGraphicsFrameLineWidth(1);
        GStyle.getH1FAttributes().setLineWidth(1);
//        GStyle.getH1FAttributes().setOptStat("1111111");

        this.detectorName = name;
        this.detectorPanel  = new JPanel();
        this.detectorCanvas = new EmbeddedCanvasTabbed();
        this.detectorView   = new DetectorPane2D();
        this.numberOfEvents = 0;   
        
    }

    
    public LogEntry addLogEntry(LogEntry entry, String dir, String timestamp) {        
        try {
            for(int tab=0; tab<this.detectorTabNames.size(); tab++) {
                String fileName = dir + "/" + this.detectorName + "_canvas" + tab + "_" + timestamp + ".png";
                System.out.println(fileName);
                this.detectorCanvas.getCanvas(this.detectorTabNames.get(tab)).save(fileName);
                entry.addAttachment(fileName, "1D histograms");
            }
            System.out.println(this.detectorName + " plots uploaded");
        } catch (Exception exc) {
                exc.printStackTrace(); 
                System.out.println( exc.getMessage());
        }
        return entry;
     }

    public void analyze() {
        // analyze detector data at the end of data processing
    }

    public void createHistos() {
        // initialize canvas and create histograms
    }
    
    @Override
    public void dataEventAction(DataEvent event) {
    	
        this.setNumberOfEvents(this.getNumberOfEvents()+1);
        
        if (event.getType() == DataEventType.EVENT_START) {
//            resetEventListener();
            processEvent(event);
	} else if (event.getType() == DataEventType.EVENT_SINGLE) {		   
            processEvent(event);
            plotEvent(event);
	} else if (event.getType() == DataEventType.EVENT_ACCUMULATE) {
            processEvent(event);
	} else if (event.getType() == DataEventType.EVENT_STOP) {
            analyze();
	}
    }

    public void drawDetector() {
    
    }
    
    public ConstantsManager getCcdb() {
        return ccdb;
    }
   
    public EmbeddedCanvasTabbed getDetectorCanvas() {
        return detectorCanvas;
    }
    
    public ArrayList<String> getDetectorTabNames() {
        return detectorTabNames;
    }
    
    public IndexedList<DataGroup>  getDataGroup(){
        return detectorData;
    }

    public String getDetectorName() {
        return detectorName;
    }
    
    public DetectorOccupancy getDetectorOccupancy() {
        return detectorOccupancy;
    }
    
    public JPanel getDetectorPanel() {
        return detectorPanel;
    }
    
    public DataGroup getDetectorSummary() {
        return detectorSummary;
    }
    
    public DetectorPane2D getDetectorView() {
        return detectorView;
    }
    
    public int getNumberOfEvents() {
        return numberOfEvents;
    }

    public String getWorkDir() {
        return workDir;
    }
    
    public void setLogZ(boolean flag) {
	    this.detectorLogZ = flag;
    }
    
    public Boolean getLogZ() {
	    return this.detectorLogZ;
    }
    
    public void setLogY(boolean flag) {
	    this.detectorLogY = flag;
    }
    
    public Boolean getLogY() {
	    return this.detectorLogY;
    }   

    public void initPanel(boolean flagDetectorView) {
        // initialize monitoring application
        // detector view is shown if flag is true
        getDetectorPanel().setLayout(new BorderLayout());
        drawDetector();
        JSplitPane   splitPane = new JSplitPane();
        splitPane.setLeftComponent(getDetectorView());
        splitPane.setRightComponent(getDetectorCanvas());
        if(flagDetectorView) {
            getDetectorPanel().add(splitPane,BorderLayout.CENTER);  
        }
        else {
            getDetectorPanel().add(getDetectorCanvas(),BorderLayout.CENTER); 
        }
    }
    
    public void initHistos(String workdir)  {
        setWorkDir(workdir);
        createHistos();
        plotHistos(); 
    }
    
//    public void actionPerformed(ActionEvent e) {
//        // TODO Auto-generated method stub
//        plotHistos();
//    } 
    
    public void processEvent(DataEvent event) {
        // process event
    }
    
    public void plotEvent(DataEvent event) {
        // process event
    }
    
    public void plotDetectorSummary(EmbeddedCanvas c, String hname) {
    }  
    
    public void plotHistos() {
    }
    
    public void printCanvas(String dir, String timestamp) {
        // print canvas to files
        for(int tab=0; tab<this.detectorTabNames.size(); tab++) {
            String fileName = dir + "/" + this.detectorName + "_canvas" + tab + "_" + timestamp + ".png";
            System.out.println(fileName);
            this.detectorCanvas.getCanvas(this.detectorTabNames.get(tab)).save(fileName);
        }
    }
    
    @Override
    public void resetEventListener() {
        System.out.println("Resetting " + this.getDetectorName() + " histogram");
        this.createHistos();
        this.plotHistos();
    }

    public void setCanvasUpdate(int time) {
        for(int tab=0; tab<this.detectorTabNames.size(); tab++) {
            this.detectorCanvas.getCanvas(this.detectorTabNames.get(tab)).initTimer(time);
        }
    }
    
    public void setDetectorCanvas(EmbeddedCanvasTabbed canvas) {
        this.detectorCanvas = canvas;
    }
    
    public void setDetectorTabNames(String... names) {
        for(String name : names) {
            this.detectorTabNames.add(name);
        }
        EmbeddedCanvasTabbed canvas = new EmbeddedCanvasTabbed(names);
        this.setDetectorCanvas(canvas);
    }
 
    public void setDetectorSummary(DataGroup group) {
        this.detectorSummary = group;
    }
    
    public void setNumberOfEvents(int numberOfEvents) {
        this.numberOfEvents = numberOfEvents;
    }

    public void setWorkDir(String workDir) {
        this.workDir = workDir;
    }
    
    @Override
    public void timerUpdate() {
        
    }
 
    public void readDataGroup(TDirectory dir) {
        String folder = this.getDetectorName() + "/";
        System.out.println("Reading from: " + folder);
        DataGroup sum = this.getDetectorSummary();
        if (sum!=null) {
        int nrows = sum.getRows();
        int ncols = sum.getColumns();
        int nds   = nrows*ncols;
        DataGroup newSum = new DataGroup(ncols,nrows);
        for(int i = 0; i < nds; i++){
            List<IDataSet> dsList = sum.getData(i);
            for(IDataSet ds : dsList){
                System.out.println("\t --> " + ds.getName());
                if(dir.getObject(folder, ds.getName())!=null) newSum.addDataSet(dir.getObject(folder, ds.getName()),i);
            }
        }            
        this.setDetectorSummary(newSum);
        
        }
        
        Map<Long, DataGroup> map = this.getDataGroup().getMap();
        for( Map.Entry<Long, DataGroup> entry : map.entrySet()) {
            Long key = entry.getKey();
            DataGroup group = entry.getValue();
            int nrows = group.getRows();
            int ncols = group.getColumns();
            int nds   = nrows*ncols;
            DataGroup newGroup = new DataGroup(ncols,nrows);
            for(int i = 0; i < nds; i++){
                List<IDataSet> dsList = group.getData(i);
                for(IDataSet ds : dsList){
                    System.out.println("\t --> " + ds.getName());
                    if(dir.getObject(folder, ds.getName())!=null) newGroup.addDataSet(dir.getObject(folder, ds.getName()),i);
                }
            }
            map.replace(key, newGroup);
        }
        this.plotHistos();
        
    }
    
    public void writeDataGroup(TDirectory dir) {
        System.out.println(this.getDetectorName());
        String folder = "/" + this.getDetectorName();
        dir.mkdir(folder);
        dir.cd(folder);
        DataGroup sum = this.getDetectorSummary();
        int nrows = sum.getRows();
        int ncols = sum.getColumns();
        int nds   = nrows*ncols;
        for(int i = 0; i < nds; i++){
            List<IDataSet> dsList = sum.getData(i);
            for(IDataSet ds : dsList){
                System.out.println("\t --> " + ds.getName());
                dir.addDataSet(ds);
            }
        }            
        Map<Long, DataGroup> map = this.getDataGroup().getMap();
        for( Map.Entry<Long, DataGroup> entry : map.entrySet()) {
            DataGroup group = entry.getValue();
            nrows = group.getRows();
            ncols = group.getColumns();
            nds   = nrows*ncols;
            for(int i = 0; i < nds; i++){
                List<IDataSet> dsList = group.getData(i);
                for(IDataSet ds : dsList){
                    System.out.println("\t --> " + ds.getName());
                    dir.addDataSet(ds);
                }
            }
        }
    }
    
    public void drawGroup(EmbeddedCanvas c, DataGroup group) {
        int nrows = group.getRows();
        int ncols = group.getColumns();
        c.divide(ncols, nrows); 	    
        int nds = nrows * ncols;
        for (int i = 0; i < nds; i++) {
            List<IDataSet> dsList = group.getData(i);
            c.cd(i);  String opt = " ";
            c.getPad().getAxisZ().setLog(getLogZ());
            c.getPad().getAxisY().setLog(getLogY());
            for (IDataSet ds : dsList) {
               c.draw(ds,opt); opt="same";
            }
        } 	
        c.update();
    }     
        
}
