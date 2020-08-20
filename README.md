# KPP-monitoring
CLAS12 monitoring plots GUI

## Using sub-tabs
Each detector monitor has 1 sub-tab by default.
To add additional sub-tabs, see DCmonitor.java as an example.
In particular, notice that the sub-tabs are added in the constructor:
```java
    public DCmonitor(String name) {
        super(name);
        
        this.getDetectorCanvas().addCanvas("canvas2");
        this.init();
    }
```
And properties are set in the createHistos() method:
```java
    @Override
    public void createHistos() {
        // initialize canvas and create histograms
        this.setNumberOfEvents(0);
        this.getDetectorCanvas().getCanvas("canvas1").divide(2, 3);
        this.getDetectorCanvas().getCanvas("canvas1").setGridX(false);
        this.getDetectorCanvas().getCanvas("canvas1").setGridY(false);
        this.getDetectorCanvas().getCanvas("canvas2").divide(2, 3);
        this.getDetectorCanvas().getCanvas("canvas2").setGridX(false);
        this.getDetectorCanvas().getCanvas("canvas2").setGridY(false);
        ...
        ...
        for(int sector=1; sector <=6; sector++) {
            this.getDetectorCanvas().getCanvas("canvas1").cd(sector-1);
            this.getDetectorCanvas().getCanvas("canvas1").draw(this.getDataGroup().getItem(sector,0,0).getH2F("occ"));
            this.getDetectorCanvas().getCanvas("canvas2").cd(sector-1);
            this.getDetectorCanvas().getCanvas("canvas2").draw(this.getDataGroup().getItem(sector,0,0).getH2F("raw"));
        }
        this.getDetectorCanvas().getCanvas("canvas1").update();
        this.getDetectorCanvas().getCanvas("canvas2").update();
```
and an @Override setCanvasUpdate(int time) method as been added:
```java
    @Override
    public void setCanvasUpdate(int time) {
        this.getDetectorCanvas().getCanvas("canvas1").initTimer(time);
        this.getDetectorCanvas().getCanvas("canvas2").initTimer(time);
    }
```
# hdiceMon
