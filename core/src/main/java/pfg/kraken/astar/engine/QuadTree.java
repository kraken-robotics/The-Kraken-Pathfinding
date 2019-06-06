package pfg.kraken.astar.engine;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import pfg.kraken.display.Display;
import pfg.kraken.display.Printable;
import pfg.kraken.obstacles.Obstacle;
import pfg.kraken.obstacles.RectangularObstacle;
import pfg.kraken.struct.XY;
import pfg.kraken.struct.XY_RW;

/**
 * A quadtree for efficient collision check
 * @author pf
 *
 */

public class QuadTree implements Printable
{
	private static final long max_depth = 4;
	private static final long serialVersionUID = -8083872756981304045L;
	private XY center;
	private double x1, x2, y1, y2;
	private double width, height, squaredRobotRadius;
	private RectangularObstacle rect;
	private List<Obstacle> content = new ArrayList<Obstacle>();
	private QuadTree[] children = null;
	private boolean leaf = true;
	private int depth;
	
	/**
	 * Root constructor
	 * @param center
	 * @param width
	 * @param height
	 */
	public QuadTree(XY center, double width, double height, double squaredRobotRadius, int depth)
	{
		x1 = center.getX() - width/2;
		x2 = center.getX() + width/2;
		
		y1 = center.getY() - height/2;
		y2 = center.getY() + height/2;

		this.center = center;
		this.width = width;
		this.height = height;
		this.squaredRobotRadius = squaredRobotRadius;
		this.depth = depth;
		rect = new RectangularObstacle(center, width, height);
	}
	
	public void remove(Obstacle o)
	{
		// inside this tile ?
		if(!contains(o))
			return;
		
		if(content.remove(o))
			return;
		
		if(children == null)
		{
			assert false: "Obstacle not found";
			return;
		}
		
		for(QuadTree qt : children)
			qt.remove(o);
		
		for(QuadTree qt : children)
			if(!qt.content.isEmpty())
				return;
		
		// if they are all empty
		leaf = true;
	}
	
	public void insert(Obstacle o)
	{
		// not inside this quadrant
		if(!contains(o))
			return;

		// max depth
		if(depth == max_depth)
		{
			content.add(o);
			return;
		}
		
	    // else, subdivide
	    if(children == null)
	    	subdivide();
	    
	    // if all children would contain it, stop at this level
	    boolean allContains = true;
	    for(QuadTree qt: children)
	    {
	    	if(!qt.contains(o))
	    	{
	    		allContains = false; 
	    		break;
	    	}
	    }
	    
	    if(allContains)
	    	content.add(o);
	    else
	    {	    	
	    	// else, at least one child won't have the obstacle
	    	for(QuadTree qt : children)
	    		qt.insert(o);
	    	// there are obstacles in subtrees
	    	leaf = false;
	    }
	}
	
	public void subdivide()
	{
		XY quarterDim = new XY(width / 4, height / 4);
		XY_RW twistedQuarterDim = quarterDim.clone();
		twistedQuarterDim.setX(- twistedQuarterDim.getX());
		children = new QuadTree[4];
		children[0] = new QuadTree(center.plusNewVector(quarterDim), width / 2, height / 2, squaredRobotRadius, depth + 1);
		children[1] = new QuadTree(center.minusNewVector(quarterDim), width / 2, height / 2, squaredRobotRadius, depth + 1);
		children[2] = new QuadTree(center.plusNewVector(twistedQuarterDim), width / 2, height / 2, squaredRobotRadius, depth + 1);
		children[3] = new QuadTree(center.minusNewVector(twistedQuarterDim), width / 2, height / 2, squaredRobotRadius, depth + 1);
	}
	
	private boolean contains(Obstacle o)
	{
		return o.squaredDistanceTo(rect) <= squaredRobotRadius;
	}
	
	public boolean isThereCollision(RectangularObstacle o)
	{
		XY pos = o.getCenter();
		// inside this tile ?
		if(depth > 0 && (pos.getX() < x1 || pos.getX() > x2 || pos.getY() < y1 || pos.getY() > y2))
			return false;
		
		// check with local obstacles
		for(Obstacle obs : content)
			if(obs.isColliding(o))
				return true;
		
		// no subtrees ?
		if(leaf)
			return false;
		
		// check subtiles
		for(QuadTree qt: children)
			if(qt.isThereCollision(o))
				return true;
		
		return false;
	}
	
	@Override
	public void print(Graphics g, Display f)
	{
		g.setColor(Color.GRAY);
		rect.print(g,f);
		if(!leaf)
			for(QuadTree qt : children)
				qt.print(g, f);
	}
}
