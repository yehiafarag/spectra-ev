/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package no.probe.uib.mgfevaluator.processes;

import java.awt.Color;
import java.io.Serializable;
import java.util.List;
import no.uib.jexpress_modularized.core.dataset.Dataset;
import no.uib.jexpress_modularized.core.visualization.colors.colorcomponents.ColorFactory;
import no.uib.jexpress_modularized.core.visualization.colors.colorcomponents.ControlPoint;

/**
 *
 * @author Yehia Farag
 */
public class HeatmapColorFactory implements Serializable{
//    private ColorFactory ColorFactory = new ColorFactory();
    private ColorFactory colorFactory;

    

    public HeatmapColorFactory() {
        initDefault();
    }

    public ColorFactory getActiveFactory() {
        return colorFactory;
    }

    public ColorFactory getActiveColorFactory(Dataset data) {
        ColorFactory ret = colorFactory.getDerivedFactory();
        ret.getValueRange().setRangeFromData(data.getMinMeasurement(), data.getMaxMeasurement());
        return ret;
    }

    public ColorFactory getActiveColorFactory() {
        ColorFactory ret = colorFactory.getDerivedFactory();
        return ret;
    }

    public ColorFactory getColorFactoryByID(int ID) {
       
        return colorFactory;
    }

//    public void setActiveFactory(ColorFactory fac) {
//        colorFactory = fac;
////        this.firePropertyChange("ActiveFactoryChanged", 0, 1);
//    }
//
//    public void FireColorFactoryChanged() {
////        this.firePropertyChange("ActiveFactoryChangedColors", 0, 1);
//    }

//    public int selectedIndex() {
//        return ColorFactories.indexOf(colorFactory);
//    }

//    public void addColorFactory(ColorFactory factory) {
////        ColorFactories.add(factory);
////        this.firePropertyChange("ColorListChanged", 0, 1);
//    }

//    public void removeColorFactory(ColorFactory factory) {
//        ColorFactories.remove(factory);
//        this.firePropertyChange("ColorListChanged", 0, 1);
//    }

//    public List<ColorFactory> getAllFactories() {
//        return ColorFactories;
//    }

//    public void storeInHash(Hashtable hash) {
//        List<Hashtable> factories = new ArrayList<Hashtable>();
//        for (ColorFactory cf : ColorFactories) {
//            factories.add(PersistenceFactory.toHash(cf));
//        }
//        hash.put("ColorFactories", factories);
//        hash.put("SelectedColorFactoryIndex", selectedIndex());
//    }
//
//    public void restoreFromHash(Hashtable hash) {
//        if (hash.containsKey("ColorFactories")) {
//            ColorFactories.clear();
//            List<Hashtable> fc = (List<Hashtable>) hash.get("ColorFactories");
//            for (Hashtable h : fc) {
//                ColorFactories.add(PersistenceFactory.fromHash(h));
//            }
//
//            int sel = (Integer) hash.get("SelectedColorFactoryIndex");
//            if (sel < 0) {
//                sel = 0;
//            }
//            colorFactory = ColorFactories.get(sel);
//        }
//    }

    public void resetAll() {
//        ColorFactories.clear();
        initDefault();
    }

    public final void initDefault() {

        ColorFactory cf = new ColorFactory();
        cf.setMirror(true);
        cf.initControlPoints(true);
        List<ControlPoint> cp = cf.getControlPoints();
        cp.clear();

        ControlPoint c1 = new ControlPoint();
        c1.setLocation(-1.0);
        c1.setColor(Color.CYAN);
        c1.setFixed(true);

        ControlPoint c2 = new ControlPoint();
        c2.setLocation(1.0);
        c2.setColor(Color.YELLOW);
        c2.setFixed(true);

        c1.setPartner(c2);
        c2.setPartner(c1);

        ControlPoint c3 = new ControlPoint();
        c3.setLocation(-0.70);
        c3.setColor(Color.BLUE);
        c3.setFixed(false);

        ControlPoint c4 = new ControlPoint();
        c4.setLocation(0.70);
        c4.setColor(Color.RED);
        c4.setFixed(false);

        c3.setPartner(c4);
        c4.setPartner(c3);

        ControlPoint c5 = new ControlPoint();
        c5.setLocation(-0.1);
        c5.setColor(new Color(170, 170, 170));
        c5.setFixed(false);

        ControlPoint c6 = new ControlPoint();
        c6.setLocation(0.1);
        c6.setColor(new Color(170, 170, 170));
        c6.setFixed(false);

        ControlPoint c7 = new ControlPoint();
        c7.setLocation(0.0);
        c7.setColor(Color.lightGray);
        c7.setFixed(true);

        c5.setPartner(c6);
        c6.setPartner(c5);

        cp.add(c1);
        cp.add(c2);
        cp.add(c3);
        cp.add(c4);
        cp.add(c5);
        cp.add(c6);
        cp.add(c7);

        cf.setMissing(Color.GREEN);
//        cf.setID(550);

//        ColorFactories.add(cf);
        colorFactory = cf;

//
//        cf = new ColorFactory();
//        cf.setMirror(true);
//        cf.initControlPoints(true);
//        cp = cf.getControlPoints();
//        cp.get(0).setColor(Color.BLUE);
//        cp.get(1).setColor(Color.white);
//        cp.get(2).setColor(Color.GREEN);
//        cf.setMissing(Color.GRAY);

//        ColorFactories.add(cf);

        ColorFactory cf2 = new ColorFactory();
        cf2.initControlPoints(true);
        cf2.setMirror(true);
        List<ControlPoint> cp2 = cf2.getControlPoints();
        cp2.get(0).setColor(Color.GREEN);
        cp2.get(1).setColor(Color.BLACK);
        cp2.get(2).setColor(Color.RED);
        cf2.setControlPoints(cp2);
        cf2.setMissing(Color.GREEN);
//        ColorFactories.add(cf2);

//        cf = new ColorFactory();
//        cf.setMirror(false);
//        cf.initControlPoints(false);
//        cp = cf.getControlPoints();
//        cp.get(0).setColor(Color.green);
//        cp.get(1).setColor(Color.CYAN);
//        cf.setMissing(Color.blue);
          colorFactory = cf2;

    }
    
}
