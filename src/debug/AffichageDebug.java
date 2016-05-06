package debug;

import java.awt.Color;
import java.util.Date;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleInsets;


/**
 * Affichage des données de debug
 * @author pf
 *
 */

public class AffichageDebug  extends ApplicationFrame
{
	private static final long serialVersionUID = 1L;
	private TimeSeries serie[];
	private final static int nbGraphe = 2;
	
	private Date temps = new Date();
	
	public void add(IncomingDataDebug data)
	{
		temps.setSeconds(temps.getSeconds()+1);
//		serie[0].add(new Millisecond(), data.PWMdroit);
//		serie[1].add(new Millisecond(), data.PWMgauche);
		serie[0].add(new Millisecond(temps), data.vitesseDroite);
		serie[1].add(new Millisecond(temps), data.vitesseGauche);
	}
	
    public AffichageDebug() {
    	
        super("Debug asser");
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        serie = new TimeSeries[nbGraphe];
        for(int i = 0; i < nbGraphe; i++)
        {
        	serie[i] = new TimeSeries("Test");
            dataset.addSeries(serie[i]);
        }
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
        		"ABWABWA",  // title
        		"Temps",             // x-axis label
        		"truc",   // y-axis label
        		dataset,            // data
        		true,               // create legend?
        		true,               // generate tooltips?
        		false               // generate URLs?
        		);
        
        chart.setBackgroundPaint(Color.white);
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        
        XYItemRenderer r = plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer)
        {
        	XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
        	renderer.setBaseShapesVisible(true);
        	renderer.setBaseShapesFilled(true);
        	renderer.setDrawSeriesLineAsPath(true);
      	}
        
        ChartPanel panel = new ChartPanel(chart);
        panel.setFillZoomRectangle(true);
        panel.setMouseWheelEnabled(true);
        panel.setPreferredSize(new java.awt.Dimension(1024, 600));
        setContentPane(panel);

    }

}
