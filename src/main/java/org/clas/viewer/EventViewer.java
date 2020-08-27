package org.clas.viewer;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.imageio.ImageIO;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.clas.detectors.*;
import org.jlab.detector.decode.CLASDecoder4;
import org.jlab.detector.view.DetectorListener;
import org.jlab.detector.view.DetectorPane2D;
import org.jlab.detector.view.DetectorShape2D;
import org.jlab.groot.base.GStyle;
import org.jlab.groot.data.TDirectory;
import org.jlab.groot.graphics.EmbeddedCanvasTabbed;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.hipo.HipoDataEvent;
import org.jlab.jnp.hipo4.data.Bank;
import org.jlab.io.task.DataSourceProcessorPane;
import org.jlab.io.task.IDataEventListener;
import org.jlab.jnp.hipo4.data.Event;
import org.jlab.jnp.hipo4.data.SchemaFactory;
import org.jlab.utils.system.ClasUtilsFile;
import org.jlab.elog.LogEntry; 

        
/**
 *
 * @author devita
 */

public class EventViewer implements IDataEventListener, DetectorListener, ActionListener, ChangeListener {
    
    List<DetectorPane2D> DetectorPanels     = new ArrayList<DetectorPane2D>();
    JTabbedPane tabbedpane           	    = null;
    JPanel mainPanel 			            = null;
    JMenuBar menuBar                        = null;
    JTextPane clas12Textinfo                = new JTextPane();
    DataSourceProcessorPane processorPane   = null;
    EmbeddedCanvasTabbed CLAS12Canvas       = null;
    private SchemaFactory     schemaFactory = new SchemaFactory();
    
    CLASDecoder4                clasDecoder = null; 
           
    private int canvasUpdateTime   = 2000;
    private int analysisUpdateTime = 1000;
    private int runNumber     = 2284;
    private int ccdbRunNumber = 0;
    private int eventNumber = 0;
    
    public String workDir = null; 
    
    // detector monitors
    DetectorMonitor[] monitors = {        
                new HDICEmonitor("HDICE"),
                new HELmonitor("HEL")
    };
    
        
    public EventViewer() {    
        
    	String dir = ClasUtilsFile.getResourceDir("CLAS12DIR", "etc/bankdefs/hipo4");
        schemaFactory.initFromDirectory(dir);
        System.out.println("Bank schema read from " + dir);
        clasDecoder = new CLASDecoder4(true);

        workDir = ClasUtilsFile.getResourceDir("HDICEDIR", "/");
        File wd = new File(workDir);
        if (!wd.exists() || !wd.isDirectory()) {
            System.out.println("Work directory " + workDir + " set incorrectly, exiting");
            System.exit(1);
        }
        else {
            System.out.println("Work directory set to " + workDir);
            String outDir = workDir + "/output";
            File wo = new File(outDir);
            if (!wo.exists()) {
                System.out.println("Creating histogram output directory " + outDir);
                boolean result = false;
                try{
                    wo.mkdir();
                    System.out.println("Histogram output directory created");  
                } 
                catch(SecurityException se){
                    System.out.println("Error creating histogram output directory, exiting");
                    System.exit(1);
                }
            }
        
        }
        
        
        // create menu bar
        menuBar = new JMenuBar();
        JMenuItem menuItem;
        JMenu file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_A);
        file.getAccessibleContext().setAccessibleDescription("File options");
        menuItem = new JMenuItem("Open histograms file", KeyEvent.VK_O);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Open histograms file");
        menuItem.addActionListener(this);
        file.add(menuItem);
        menuItem = new JMenuItem("Save histograms to file", KeyEvent.VK_S);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Save histograms to file");
        menuItem.addActionListener(this);
        file.add(menuItem);
        menuItem = new JMenuItem("Print histograms as png", KeyEvent.VK_B);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Print histograms as png");
        menuItem.addActionListener(this);
        file.add(menuItem);
        //menuItem = new JMenuItem("Create histogram PDF", KeyEvent.VK_P);
        //menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
        //menuItem.getAccessibleContext().setAccessibleDescription("Create historgram PDF");
        //menuItem.addActionListener(this);
        //file.add(menuItem);
        
        menuBar.add(file);
        JMenu settings = new JMenu("Settings");
        settings.setMnemonic(KeyEvent.VK_A);
        settings.getAccessibleContext().setAccessibleDescription("Choose monitoring parameters");
        menuItem = new JMenuItem("Set GUI update interval", KeyEvent.VK_T);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Set GUI update interval");
        menuItem.addActionListener(this);
        settings.add(menuItem);
        menuItem = new JMenuItem("Set global z-axis log scale", KeyEvent.VK_L);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Set global z-axis log scale");
        menuItem.addActionListener(this);
        settings.add(menuItem);
        menuItem = new JMenuItem("Set global z-axis lin scale", KeyEvent.VK_R);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Set global z-axis lin scale");
        menuItem.addActionListener(this);
        settings.add(menuItem);
        menuItem = new JMenuItem("Set DC occupancy scale max");
        menuItem.getAccessibleContext().setAccessibleDescription("Set DC occupancy scale max");
        menuItem.addActionListener(this);
        settings.add(menuItem);
        menuItem = new JMenuItem("Set run number");
        menuItem.getAccessibleContext().setAccessibleDescription("Set run number");
        menuItem.addActionListener(this);
        settings.add(menuItem);
        menuBar.add(settings);
         
        JMenu upload = new JMenu("Upload");
        upload.setMnemonic(KeyEvent.VK_A);
        upload.getAccessibleContext().setAccessibleDescription("Upload all histograms to the Logbook");
        menuItem = new JMenuItem("Upload all histos to the logbook");
        menuItem.getAccessibleContext().setAccessibleDescription("Upload all histos to the logbook");
        menuItem.addActionListener(this);
        upload.add(menuItem);
        upload.add(menuItem);
        menuBar.add(upload);
                


        // create main panel
        mainPanel = new JPanel();	
        mainPanel.setLayout(new BorderLayout());
        
      	tabbedpane 	= new JTabbedPane();

        processorPane = new DataSourceProcessorPane();
        processorPane.setUpdateRate(analysisUpdateTime);

        mainPanel.add(tabbedpane);
        mainPanel.add(processorPane,BorderLayout.PAGE_END);
        
    
        GStyle.getAxisAttributesX().setTitleFontSize(18);
        GStyle.getAxisAttributesX().setLabelFontSize(14);
        GStyle.getAxisAttributesY().setTitleFontSize(18);
        GStyle.getAxisAttributesY().setLabelFontSize(14);
        
        JPanel    CLAS12View = new JPanel(new BorderLayout());
        JSplitPane splitPanel = new JSplitPane();
        splitPanel.setLeftComponent(CLAS12View);
        splitPanel.setRightComponent(CLAS12Canvas);
        JTextPane clas12Text   = new JTextPane();
        clas12Text.setText("CLAS12\n monitoring plots\n V3.0\n");
        clas12Text.setEditable(false);       
        this.clas12Textinfo.setEditable(false);
        this.clas12Textinfo.setFont(new Font("Avenir",Font.PLAIN,16));
        this.clas12Textinfo.setBackground(CLAS12View.getBackground());
        StyledDocument styledDoc = clas12Text.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        styledDoc.setParagraphAttributes(0, styledDoc.getLength(), center, false);
        clas12Text.setBackground(CLAS12View.getBackground());
        clas12Text.setFont(new Font("Avenir",Font.PLAIN,20));
        JLabel clas12Design = this.getImage("https://www.jlab.org/Hall-B/clas12-web/sidebar/clas12-design.jpg",0.08);
        JLabel clas12Logo   = this.getImage("https://www.jlab.org/Hall-B/pubs-web/logo/CLAS-frame-low.jpg", 0.3);
//        CLAS12View.add(clas12Name,BorderLayout.PAGE_START);
        CLAS12View.add(clas12Textinfo,BorderLayout.BEFORE_FIRST_LINE );
        CLAS12View.add(clas12Design);
        CLAS12View.add(clas12Text,BorderLayout.PAGE_END);

//        tabbedpane.add(splitPanel,"Summary");
//        tabbedpane.addChangeListener(this);
        
        for(int k =0; k<this.monitors.length; k++) {
            this.monitors[k].initHistos(workDir);
            this.tabbedpane.add(this.monitors[k].getDetectorPanel(), this.monitors[k].getDetectorName());
            this.monitors[k].getDetectorView().getView().addDetectorListener(this);                        
        }
        
        this.processorPane.addEventListener(this);
        
        this.setCanvasUpdate(canvasUpdateTime);
        this.plotSummaries();
        
    }
      
    public void actionPerformed(ActionEvent e) {
        System.out.println(e.getActionCommand());
        if(e.getActionCommand()=="Set GUI update interval") {
            this.chooseUpdateInterval();
        }
        if(e.getActionCommand()=="Set global z-axis log scale") {
        	   for(int k=0; k<this.monitors.length; k++) {this.monitors[k].setLogZ(true);this.monitors[k].plotHistos();}
        }
        if(e.getActionCommand()=="Set global z-axis lin scale") {
           for(int k=0; k<this.monitors.length; k++) {this.monitors[k].setLogZ(false);this.monitors[k].plotHistos();}
        }
        if(e.getActionCommand()=="Set run number") {
           setRunNumber(e.getActionCommand());
        }

        if(e.getActionCommand()=="Open histograms file") {
            String fileName = null;
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            File workingDirectory = new File(System.getProperty("user.dir"));  
            fc.setCurrentDirectory(workingDirectory);
            int option = fc.showOpenDialog(null);
            if (option == JFileChooser.APPROVE_OPTION) {
                fileName = fc.getSelectedFile().getAbsolutePath();            
            }
            if(fileName != null) this.loadHistosFromFile(fileName);
        }        
        if(e.getActionCommand()=="Print histograms as png") {
            this.printHistosToFile();
        }
        if(e.getActionCommand()=="Save histograms to file") {
            DateFormat df = new SimpleDateFormat("MM-dd-yyyy_hh.mm.ss_aa");
            String fileName = "CLAS12Mon_run_" + this.runNumber + "_" + df.format(new Date()) + ".hipo";
            JFileChooser fc = new JFileChooser();
            File workingDirectory = new File(System.getProperty("user.dir"));   
            fc.setCurrentDirectory(workingDirectory);
            File file = new File(fileName);
            fc.setSelectedFile(file);
            int returnValue = fc.showSaveDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
               fileName = fc.getSelectedFile().getAbsolutePath();            
            }
            this.saveHistosToFile(fileName);
        }
        
        if(e.getActionCommand()=="Upload all histos to the logbook") {   
            
            DateFormat df = new SimpleDateFormat("MM-dd-yyyy_hh.mm.ss_aa");
            String tstamp = df.format(new Date());
            String data = ClasUtilsFile.getResourceDir("HDICEDIR", "/output" + "/hdicemon_" + this.runNumber + "_" + tstamp);        
            File theDir = new File(data);
            // if the directory does not exist, create it
            if (!theDir.exists()) {
                boolean result = false;
                try{theDir.mkdir();result = true;} 
                catch(SecurityException se){}        
                if(result){ System.out.println("Created directory: " + data);}
            }
            
            String fileName = data + "/hdicemon_histos_" + this.runNumber + "_" + tstamp + ".hipo"; 
            this.saveHistosToFile(fileName);
            
            LogEntry entry = new LogEntry("All online monitoring histograms for run number " + this.runNumber, "TLOG");
            
            System.out.println("Starting to upload all monitoring plots");
            
            try{
                for(int k=0; k<this.monitors.length; k++) {
                    entry = this.monitors[k].addLogEntry(entry, data, tstamp);
                }
                long lognumber = entry.submitNow();
                System.out.println("Successfully submitted log entry number: " + lognumber); 
            } catch(Exception exc){
                exc.printStackTrace(); 
                System.out.println( exc.getMessage());
            }
              
        }
                
    }

    public void chooseUpdateInterval() {
        String s = (String)JOptionPane.showInputDialog(
                    null,
                    "GUI update interval (ms)",
                    " ",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    "1000");
        if(s!=null){
            int time = 1000;
            try { 
                time= Integer.parseInt(s);
            } catch(NumberFormatException e) { 
                JOptionPane.showMessageDialog(null, "Value must be a positive integer!");
            }
            if(time>0) {
                this.setCanvasUpdate(time);
            }
            else {
                JOptionPane.showMessageDialog(null, "Value must be a positive integer!");
            }
        }
    }
        
    private JLabel getImage(String path,double scale) {
        JLabel label = null;
        Image image = null;
        try {
            URL url = new URL(path);
            image = ImageIO.read(url);
        } catch (IOException e) {
        	e.printStackTrace();
                System.out.println("Picture upload from " + path + " failed");
        }
        ImageIcon imageIcon = new ImageIcon(image);
        double width  = imageIcon.getIconWidth()*scale;
        double height = imageIcon.getIconHeight()*scale;
        imageIcon = new ImageIcon(image.getScaledInstance((int) width,(int) height, Image.SCALE_SMOOTH));
        label = new JLabel(imageIcon);
        return label;
    }
    
    public JPanel  getPanel(){
        return mainPanel;
    }
    
    
    private int getRunNumber(DataEvent event) {
        int rNum = this.runNumber;
        DataBank bank = event.getBank("RUN::config");
        if(bank!=null) {
            rNum      = bank.getInt("run", 0);
        }
        return rNum;
    }
    
    private int getEventNumber(DataEvent event) {
        DataBank bank = event.getBank("RUN::config");
        return (bank!=null) ? bank.getInt("event", 0): this.eventNumber;
    }
    
    @Override
    public void dataEventAction(DataEvent event) {
    	
    	DataEvent hipo = null;
   	
	    if(event!=null ){
            if(event instanceof EvioDataEvent){
             	Event    dump = clasDecoder.getDataEvent(event);    
                Bank   header = clasDecoder.createHeaderBank(this.ccdbRunNumber, getEventNumber(event), (float) 0, (float) 0);
                Bank  trigger = clasDecoder.createTriggerBank();
                if(header!=null)  dump.write(header);
                if(trigger!=null) dump.write(trigger);
                hipo = new HipoDataEvent(dump,schemaFactory);
            }   
            else {            	
            	hipo = event; 
            }
        
            if(this.runNumber != this.getRunNumber(hipo)) {
                this.runNumber = this.getRunNumber(hipo);
                System.out.println("Setting run number to: " +this.runNumber);
                resetEventListener();
                this.clas12Textinfo.setText("\nrun number: "+this.runNumber + "\n");
            }     
            
            for(int k=0; k<this.monitors.length; k++) {
                this.monitors[k].dataEventAction(hipo);
            }      
	    }
    }

    public void loadHistosFromFile(String fileName) {
        // TXT table summary FILE //
        System.out.println("Opening file: " + fileName);
        TDirectory dir = new TDirectory();
        dir.readFile(fileName);
        System.out.println(dir.getDirectoryList());
        dir.cd();
        dir.pwd();
        
        for(int k=0; k<this.monitors.length; k++) {
            this.monitors[k].readDataGroup(dir);
        }
        this.plotSummaries();
    }

    public void plotSummaries() {
        
        /////////////////////////////////////////////////
        /// FD:
        
      
        
    }
    
    public void printHistosToFile() {
        DateFormat df = new SimpleDateFormat("MM-dd-yyyy_hh.mm.ss_aa");
        String tstamp = df.format(new Date());
        String data = workDir + "/output" + "/clas12mon_" + this.runNumber + "_" + tstamp;        
        File theDir = new File(data);
        // if the directory does not exist, create it
        if (!theDir.exists()) {
            boolean result = false;
            try{
                theDir.mkdir();
                result = true;
            } 
            catch(SecurityException se){
                //handle it
            }        
            if(result) {    
            System.out.println("Created directory: " + data);
            }
        }
        
        for(int k=0; k<this.monitors.length; k++) {
            this.monitors[k].printCanvas(data,tstamp);
        }
        
        System.out.println("Histogram pngs succesfully saved in: " + data);
    }
       

    @Override
    public void processShape(DetectorShape2D shape) {
        System.out.println("SHAPE SELECTED = " + shape.getDescriptor());
    }
    
    @Override
    public void resetEventListener() {
        for(int k=0; k<this.monitors.length; k++) {
            this.monitors[k].resetEventListener();
            this.monitors[k].timerUpdate();
        }      
        this.plotSummaries();
    }
    
    public void saveHistosToFile(String fileName) {
        // TXT table summary FILE //
        TDirectory dir = new TDirectory();
        for(int k=0; k<this.monitors.length; k++) {
            this.monitors[k].writeDataGroup(dir);
        }
        System.out.println("Saving histograms to file " + fileName);
        dir.writeFile(fileName);
    }
        
    public void setCanvasUpdate(int time) {
        System.out.println("Setting " + time + " ms update interval");
        this.canvasUpdateTime = time;
        for(int k=0; k<this.monitors.length; k++) {
            this.monitors[k].setCanvasUpdate(time);
        }
    }

    public void stateChanged(ChangeEvent e) {
        this.timerUpdate();
    }
    
    @Override
    public void timerUpdate() {
//        System.out.println("Time to update ...");
        for(int k=0; k<this.monitors.length; k++) {
            this.monitors[k].timerUpdate();
        }
        this.plotSummaries();
   }

    public static void main(String[] args){
        int xSize = 1600;
        int ySize = 1000;
        
        if(args.length>0){
            xSize = Integer.parseInt(args[0]);
            if(args.length>1){
                ySize = Integer.parseInt(args[1]);
            } else {
                ySize = (int) (xSize/1.6);
            }
        }
        JFrame frame = new JFrame("CLAS12Mon");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        EventViewer viewer = new EventViewer();
        //frame.add(viewer.getPanel());
        frame.add(viewer.mainPanel);
        frame.setJMenuBar(viewer.menuBar);
        frame.setSize(xSize, ySize);
        frame.setVisible(true);
    }
    
    
    
    private void setRunNumber(String actionCommand) {
    
        System.out.println("Set run number for CCDB access");
        String  RUN_number = (String) JOptionPane.showInputDialog(null, "Set run number to ", " ", JOptionPane.PLAIN_MESSAGE, null, null, "2284");
        
        if (RUN_number != null) { 
            int cur_runNumber= this.runNumber;
            try {
                cur_runNumber = Integer.parseInt(RUN_number);
            } 
            catch (
                NumberFormatException f) {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");
            }
            if (cur_runNumber > 0){ 
                this.ccdbRunNumber = cur_runNumber;               
                clasDecoder.setRunNumber(cur_runNumber,true);
            } 
            else {JOptionPane.showMessageDialog(null, "Value must be a positive integer!");}   
        }
        
    }


   
}
