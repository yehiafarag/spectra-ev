/*--------------------------------------------------------------------------
 *
 * Copyright (c) 2001 MolMine AS.  All rights reserved.
 *
 * All paper, computer, digital, graphical, or other representations of the source code remain
 * the property of MolMine AS. All patents, ideas, inventions or novelties contained within the
 * source code are the exclusive intellectual property of MolMine AS. Surce code is provided 
 * for reference and support purposes only. Copies of the source code in any form, whether this
 * be digital, graphical or any other media, may not be distributed, discussed, or revealed to 
 * any person, computer or organisation not directly involved in support of a related product 
 * provided by the licensee or organisation not authorzed by MolMine AS to be directly involved 
 * in source code level support of J-Express.
 
 * The source code may not be modified except where specifically authorized by MolMine AS. No 
 * part of the source code may be used  within any product other than J-Express.
 *
 * You undertake that:
 *  The source code will not be distributed except where specifical authorized by MolMine AS.
 *  That you will ensure that all copies and representations of the source code can be identified.
 *
 * DISCLAIMER:
 * THIS SOFTWARE IS PROVIDED BY MOLMINE AS "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, 
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE  ARE DISCLAIMED.  IN NO EVENT SHALL MOLMINE OR ITS CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
 * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *---------------------------------------------------------------------------
 */
package web.diva.server.model.SomClustering;

/**
 *
 * @author - updated by Yehia Farag
 */
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Rectangle;
import java.io.Serializable;
import no.uib.jexpress_modularized.core.visualization.DoublePoint;
import no.uib.jexpress_modularized.somclust.model.Node;

public class TreeView extends java.awt.Canvas implements Cloneable, Serializable {

    public Node root, marker;
    java.awt.Graphics offGraphics;
    java.awt.Image offImage;
    BufferedImage bim;
    java.awt.Image doneIm;
    public boolean drawAll = false;
    javax.swing.JFrame ms;
    int count = 0;
    int outcount = 0;
//-------Values that can be set ------------
    public int treewidth = 200;	//Is treedept when vertical...
    double step;
    // @TODO: the variables should not be public but rather accessed via methods
    public boolean valuedistances = true;
    public boolean drawrects = true;
    public int leafdist = 20;
    private boolean horizontal = true;

    public boolean isHorizontal() {
        return horizontal;
    }

    public void setHorizontal(boolean horizontal) {
        this.horizontal = horizontal;
    }
    public boolean drawframe = true;
    public int leftmargin = 10;	//Distances from the start of the frame to the start of the tree..
    public int rightmargin = 10;
    public int topmargin = 10;
    public int bottommargin = 10;
    public Color bg = Color.white;
    Color fg = Color.black;
    //mainclust mc;
//-----------------------------------------
    Rectangle bounds = null;
    double temp = 0.0;
    double max = -999999;
    double min = 999999;
    public int leaves, lvs;
    public int actualLength = 0;
    public int[] arrangement;
    double deepestval;
    int deepeststep;

    public void setBnds(Rectangle bounds) {
        this.bounds = bounds;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();

    }

    @Override
    public int getHeight() {
        if (!horizontal) {
            return treewidth + bottommargin + topmargin;
        } else {
            return (leafdist * lvs) + bottommargin + topmargin;
        }
    }

    @Override
    public int getWidth() {
        if (horizontal) {
            return treewidth + leftmargin + rightmargin;
        } else {
            return (leafdist * lvs) + leftmargin + rightmargin;
        }
    }

    public TreeView() {
        java.awt.Frame f = new java.awt.Frame();

        deepeststep = getdeepSteps(root);
        deepestval = root.longestspanval;
        step = stepnorm();

        preparetreeY(root);
//if(horizontal) preparetree(root,leftmargin,topmargin);
//else preparetree(root,topmargin,leftmargin);

        if (horizontal) {
            preparetree(root, topmargin);
        } else {
            preparetree(root, leftmargin);
        }

        if (horizontal) {
            setSize(treewidth + (leftmargin * 2), (count - 1) * leafdist + (topmargin * 2));
        } else {
            setSize((count - 1) * leafdist + (topmargin * 2), treewidth + (leftmargin * 2));
        }

        f.add("Center", TreeView.this);
        f.pack();
        repaint();

        f.setVisible(true);
    }

    public TreeView(Node root, int leaves, Color bg, Color fg) {
        this.bg = bg;
        this.fg = fg;

        setBackground(bg);

        this.root = root;
        this.leaves = leaves;
        arrangement = new int[leaves];
        for (int i = 0; i < arrangement.length; i++) {
            arrangement[i] = 0;
        }

    }

    public void generatecoords() {
        count = 0;
        deepeststep = getdeepSteps(root);
        deepestval = getdeepValue(root);//-root.value;//+root.value;

        step = stepnorm();
        preparetreeY(root);
//preparetree(root,leftmargin,topmargin);
        preparetree(root, topmargin);

//if(horizontal) setSize(treewidth+(leftmargin*2),(count-1)*leafdist+(topmargin*2));
//if(horizontal) setSize(treewidth,(count-1)*leafdist);
//else setSize((count-1)*leafdist+(topmargin*2),treewidth+(leftmargin*2));
    }

    public final void preparetreeY(Node trunk) {
//finds the overall max and min.
        if (trunk.value < min) {
            min = trunk.value;
        }
        if (trunk.value > max) {
            max = trunk.value;
        }

        if (trunk.merged) {
            preparetreeY(trunk.right);
            preparetreeY(trunk.left);
        } else {
            arrangement[count] = trunk.nme;//intvalue(trunk.name);
            //arrangement[count]=intvalue(trunk.name);

            if (horizontal) {
                trunk.xcor = treewidth;
                trunk.ycor = leftmargin + count * leafdist;
            } else {
                trunk.xcor = treewidth;
                trunk.ycor = count * leafdist + leftmargin;
            }

            count++;
        }
    }


    /*
     * public DoublePoint preparetree(Gen trunk,double x,double y){ DoublePoint
     * rightret=null; DoublePoint leftret=null;
     *
     * if(trunk.merged){
     *
     *
     * if(valuedistances){ rightret=
     * preparetree(trunk.right,x+norm(trunk.right.value),y+leafdist); leftret=
     * preparetree(trunk.left,x+norm(trunk.left.value),y-leafdist); } else{
     * rightret= preparetree(trunk.right,x+step,y+leafdist); leftret=
     * preparetree(trunk.left,x+step,y-leafdist); }
     *
     * lvs++; trunk.ycor= (trunk.left.ycor+trunk.right.ycor)/2; trunk.xcor= x; }
     * return new DoublePoint(trunk.xcor,trunk.ycor); }
     */
    public final DoublePoint preparetree(Node trunk, double y) {
        if (trunk == root) {
            lvs = 0;
        }

        DoublePoint rightret = null;
        DoublePoint leftret = null;

        if (trunk.merged) {

            rightret = preparetree(trunk.right, y + leafdist);
            leftret = preparetree(trunk.left, y - leafdist);

            lvs++;

            if (valuedistances) {
                trunk.ycor = (trunk.left.ycor + trunk.right.ycor) / 2;
                trunk.xcor = Math.min(rightret.x, leftret.x) - Math.max(norm(trunk.value), 0);
            } else {
                trunk.ycor = (trunk.left.ycor + trunk.right.ycor) / 2;
                trunk.xcor = Math.min(rightret.x, leftret.x) - Math.max(step, 0);
            }

        } else {
            trunk.xcor = treewidth;
        }

        return new DoublePoint(trunk.xcor, trunk.ycor);
    }

    public double norm(double val) {

        double rel = ((double) (treewidth)) / (double) deepestval;
//temp=temp+val;

        return (rel * val * 0.90);

    }

    public final double stepnorm() {
        double rel = ((double) treewidth) / (double) (deepeststep);
        return rel;
    }

    public double getdeepValue(Node trunk) {
        double ret = 0.0;
        double lret = 0.0;
        double rret = 0.0;

        if (trunk.merged) {
            rret = getdeepValue(trunk.right);
            lret = getdeepValue(trunk.left);

            ret = Math.max(rret, lret) + (trunk.value);
            //System.out.print("+"+Math.max(rret,lret));        
        } else {
            ret = 0;
        }

        return ret;
    }

    public final int getdeepSteps(Node trunk) {
        int ret = 0;
        int lret = 0;
        int rret = 0;

        if (trunk.merged) {
            rret = getdeepSteps(trunk.right);
            lret = getdeepSteps(trunk.left);

            ret = Math.max(rret, lret) + 1;

            if (ret == lret + 1) {
                trunk.longestspanval = trunk.left.value + trunk.longestspanval;
            } else {
                trunk.longestspanval = trunk.right.value + trunk.longestspanval;
            }
        } else {
            ret = 0;
        }

        return ret;
    }

    public BufferedImage getImage() {
        count = 0;
        generatecoords();
        if (treewidth != 0) {

            if (horizontal) {
                bim = new BufferedImage(treewidth + (topmargin * bottommargin), (count) * leafdist + (leftmargin + rightmargin), BufferedImage.TYPE_INT_RGB);
            } else {
                bim = new BufferedImage((count) * leafdist + (leftmargin + rightmargin), treewidth + (topmargin * bottommargin), BufferedImage.TYPE_INT_RGB);
            }

            offGraphics = bim.getGraphics();
            offGraphics.setColor(bg);
            offGraphics.fillRect(0, 0, bim.getWidth(), bim.getHeight());
//offGraphics.clearRect(0,0,bim.getWidth(),bim.getHeight());

            offGraphics.setColor(fg);

            painttree(root, offGraphics, fg);

            if (drawframe) {
                if (horizontal) {
                    offGraphics.drawRect(leftmargin - 2, topmargin - 2, treewidth + 2, leftmargin + ((count - 1) * leafdist) + 2);
                } else {
                    offGraphics.drawRect(topmargin - 2, leftmargin - 2, ((count - 1) * leafdist) + 4, treewidth + 2);
                }
            }
        }
        return bim;
    }

    public BufferedImage getImage(Node marker, Color c) {
        this.marker = marker;
//generatecoords();

//if(horizontal) bim = new BufferedImage(treewidth+(leftmargin*2),(count)*leafdist+(topmargin*2)+10,BufferedImage.TYPE_INT_ARGB );
//else bim = new BufferedImage( (count)*leafdist+(topmargin*2)+10,treewidth+(leftmargin*2),BufferedImage.TYPE_INT_ARGB);
        offGraphics = bim.getGraphics();
//offGraphics.setColor(bg);
//offGraphics.fillRect(0,0,bim.getWidth(),bim.getHeight());
//offGraphics.setColor(fg);
        if (marker.getColor() == null) {
            // offGraphics.setColor(c);
            painttree(marker, offGraphics, c);
        } else {
            // offGraphics.setColor(marker.);
            painttree(marker, offGraphics, marker.getColor());

        }

//if(drawframe){
//	if(horizontal) offGraphics.drawRect(leftmargin-2,topmargin-2,treewidth+2,leftmargin+((count-1)*leafdist)+2);
//	else offGraphics.drawRect(topmargin-2,leftmargin-2,((count-1)*leafdist)+4,treewidth+2);
//}
        return bim;
    }

    public java.awt.Point lightUpCluster(Node target) {
        //offGraphics.setColor(Color.red);

        //offGraphics.drawRect(target.getx()-1,target.gety()-1,2,2);
        return new java.awt.Point(target.getx() - 1, target.gety() - 1);
    }

    /*
     * public void paint(Graphics g){
     *
     * if(horizontal) offImage =
     * createImage(treewidth+(leftmargin*2),(count-1)*leafdist+(topmargin*2));
     * else offImage = createImage(
     * (count-1)*leafdist+(topmargin*2),treewidth+(leftmargin*2));
     *
     *
     * offGraphics = offImage.getGraphics(); offGraphics.setColor(bg);
     * offGraphics.fillRect(0,0,offImage.getWidth(this),offImage.getHeight(this));
     * offGraphics.setColor(fg);
     *
     *
     * painttree(root,offGraphics,fg);
     *
     * if(drawframe){ if(horizontal)
     * offGraphics.drawRect(leftmargin-2,topmargin-2,treewidth+2,leftmargin+((count-1)*leafdist)+2);
     * else
     * offGraphics.drawRect(topmargin-2,leftmargin-2,((count-1)*leafdist)+4,treewidth+2);
     * }
     *
     * g.drawImage(offImage,0 , 0,this);
     *
     * }
     */
    public void painttree(Node trunk, java.awt.Graphics g, Color color) {

        if (trunk.merged) {

//    if(trunk==marker){selected=1;}
//    if(selected==1)g.setColor(Color.red);        
//    else g.setColor(Color.black);    
            if (trunk.getColor() != null) {
                painttree(trunk.right, g, trunk.getColor());
                painttree(trunk.left, g, trunk.getColor());
                g.setColor(trunk.getColor());
            } else {
                painttree(trunk.right, g, color);
                painttree(trunk.left, g, color);
                g.setColor(color);
            }

            if (horizontal) {
                if (drawrects || trunk.mark) {
                    if (drawAll || bounds == null || bounds.intersects(trunk.getx() - 1, trunk.gety() - 1, 2, 2)) {
                        g.drawRect(trunk.getx() - 3, trunk.gety() - 3, 6, 6);
                    }

                }
//g.setColor(new Color(220,250,220));
//g.fillRect(trunk.getx()-1,trunk.gety()-1,2,2);
//g.setColor(Color.black);
                if (drawAll || bounds == null || bounds.intersectsLine(trunk.left.getx(), trunk.left.gety(), trunk.getx(), trunk.left.gety())) {
                    g.drawLine(trunk.left.getx(), trunk.left.gety(), trunk.getx(), trunk.left.gety());
                }

                if (drawAll || bounds == null || bounds.intersectsLine(trunk.right.getx(), trunk.right.gety(), trunk.getx(), trunk.right.gety())) {
                    g.drawLine(trunk.right.getx(), trunk.right.gety(), trunk.getx(), trunk.right.gety());
                }

                if (drawAll || bounds == null || bounds.intersectsLine(trunk.getx(), trunk.right.gety(), trunk.getx(), trunk.left.gety())) {
                    g.drawLine(trunk.getx(), trunk.right.gety(), trunk.getx(), trunk.left.gety());
                }
            } else {
                if (drawrects || trunk.mark) {
                    g.drawRect(trunk.gety() - 3, trunk.getx() - 3, 6, 6);
                }
//g.setColor(new Color(220,250,220));
//g.fillRect(trunk.gety()-1,trunk.getx()-1,2,2);
//g.setColor(Color.black);
                if (drawAll || bounds == null || bounds.intersectsLine(trunk.left.gety(), trunk.left.getx(), trunk.left.gety(), trunk.getx())) {
                    g.drawLine(trunk.left.gety(), trunk.left.getx(), trunk.left.gety(), trunk.getx());
                }

                if (drawAll || bounds == null || bounds.intersectsLine(trunk.right.gety(), trunk.right.getx(), trunk.right.gety(), trunk.getx())) {
                    g.drawLine(trunk.right.gety(), trunk.right.getx(), trunk.right.gety(), trunk.getx());
                }

                if (drawAll || bounds == null || bounds.intersectsLine(trunk.right.gety(), trunk.getx(), trunk.left.gety(), trunk.getx())) {
                    g.drawLine(trunk.right.gety(), trunk.getx(), trunk.left.gety(), trunk.getx());
                }

            }

        }

    }

    public int intvalue(String s) {
        Integer i = new Integer(s);
        return i;
    }
}
