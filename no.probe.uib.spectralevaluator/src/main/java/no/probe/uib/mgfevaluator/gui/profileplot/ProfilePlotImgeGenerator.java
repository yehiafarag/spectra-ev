/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package no.probe.uib.mgfevaluator.gui.profileplot;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Base64;
import no.uib.jexpress_modularized.core.dataset.Dataset;
import no.uib.jexpress_modularized.core.dataset.Group;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.fop.svg.PDFTranscoder;
import org.jfree.chart.encoders.ImageEncoder;
import org.jfree.chart.encoders.ImageEncoderFactory;
import org.jfree.chart.encoders.ImageFormat;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.svg.SVGDocument;

/**
 *
 * @author Yehia Farag
 */
public class ProfilePlotImgeGenerator extends ProfilePlotComponent {

    private final Dataset dataset;
    private boolean aalias = false;
    private final boolean[] members;
    private int width;
    private int height;

    public ProfilePlotImgeGenerator(final Dataset dataset, boolean[] members) {
        this.dataset = dataset;
        this.members = members;
        updateDataset(dataset);

    }

    public final void updateDataset(Dataset dataset) {

        int cnt = 0;
        if (members == null) {
            cnt = dataset.getDataLength();
        } else {
            for (int i = 0; i < members.length; i++) {
                if (members[i]) {
                    cnt++;
                }
            }
        }
        int autoalias = 6000;
        if (cnt < autoalias) {
            aalias = true;
        }
        setData(dataset, members);
        if (aalias) {
            this.setAntialias(aalias);
        }
        int over = 0;
        for (String str : dataset.getColumnIds()) {
            if (str.length() > over) {
                over = str.length();
            }
        }
        getXaxis().minimumSize = 900 - (over*4);

        width = getXaxis().predictLength() + getYaxis().predictWidth() + getXaxis().endLength() + (over * 4);
        height = 900;
        super.setDsize(new Dimension(width, height));
        setSize(new Dimension(getDsize().width, getDsize().height));
        setDraw(getDataSelection(new int[]{}));
        setForeground(Color.GRAY);//new Color(0, 51, 153));
        forceFullRepaint();

    }
    private BufferedImage image;

    @Override
    public BufferedImage getImage() {
        return image;
    }

    @Override
    public int getWidth() {
        return super.Width();
    }

    @Override
    public int getHeight() {
        return super.Height();
    }

    private final ImageEncoder in = ImageEncoderFactory.newInstance(ImageFormat.PNG, new Float(0.084666f));

    public String toImage() {
        image = new BufferedImage(900, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setPaint(Color.WHITE);
        super.forceFullRepaint(graphics);
//        super.paint(graphics);
        byte[] imageData = null;     

        try {

            imageData = in.encode(image);
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }

        String base64 = "";//Base64.Encoder(imageData);
//        base64 = "data:image/png;base64," + base64;
        return base64;

    }

    @SuppressWarnings("CallToPrintStackTrace")
    public String toPdfFile(File userFolder, String url) {
        try {
            BufferedImage pdfImage = image;

              DOMImplementation domImpl = new SVGDOMImplementation();
            String svgNS = "http://www.w3.org/2000/svg";
            SVGDocument svgDocument = (SVGDocument) domImpl.createDocument(svgNS, "svg", null);
            SVGGraphics2D svgGenerator = new SVGGraphics2D(svgDocument);
            svgGenerator.setSVGCanvasSize(new Dimension(pdfImage.getWidth(), pdfImage.getHeight()));
            svgGenerator.setPaint(Color.WHITE);
            svgGenerator.drawImage(pdfImage,0,0,null);
            File pdfFile = new File(userFolder,dataset.getName()+ "_Profile_Plot" + ".pdf");
            if (!pdfFile.exists()) {
                pdfFile.createNewFile();
            } else {
                pdfFile.delete();
                pdfFile.createNewFile();
            }
            // write the svg file
            File svgFile = new File(pdfFile.getAbsolutePath() + ".temp");
            OutputStream outputStream = new FileOutputStream(svgFile);
            BufferedOutputStream bos = new BufferedOutputStream(outputStream);
            Writer out = new OutputStreamWriter(bos, "UTF-8");
            
            svgGenerator.stream(out, true /* use css */);
            outputStream.flush();
            outputStream.close();
            bos.close();
            System.gc();
            String svgURI = svgFile.toURI().toString();
            TranscoderInput svgInputFile = new TranscoderInput(svgURI);

            OutputStream outstream = new FileOutputStream(pdfFile);
            bos = new BufferedOutputStream(outstream);
            TranscoderOutput output = new TranscoderOutput(bos);
            Transcoder pdfTranscoder = new PDFTranscoder();
            pdfTranscoder.addTranscodingHint(PDFTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER, 0.084666f);
            pdfTranscoder.transcode(svgInputFile, output);
            outstream.flush();
            outstream.close();
            bos.close();
            System.gc();
            return url + userFolder.getName() + "/" + pdfFile.getName();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";

    }

    public void updateColumnGroupColors() {
        if (dataset.getColumnGroups().size() > 1) {
            Color[] columnLabelColor = new Color[dataset.getColumnIds().length];
            for (int x = 0; x < columnLabelColor.length; x++) {
                columnLabelColor[x] = Color.BLACK;
            }

            for (int x = 0; x < columnLabelColor.length; x++) {
                for (Group g : dataset.getColumnGroups()) {
                    if (!g.getName().equalsIgnoreCase("All") && g.hasMember(x)) {
                        columnLabelColor[x] = g.getColor();
                        break;
                    }

                }
            }
            getXaxis().setGroupColors(columnLabelColor);
        }

    }

    public int[] getProfilePlotSelection(int x, int y) {
    return new int[]{1,3,5};
    }

}
