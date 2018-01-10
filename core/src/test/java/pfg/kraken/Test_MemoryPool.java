package pfg.kraken;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import pfg.kraken.astar.AStarNode;
import pfg.kraken.memory.NodePool;

/**
 * Memory pool test
 * @author Pierre-François Gimenez
 *
 */

public class Test_MemoryPool extends JUnit_Test
{
	protected NodePool pool;
	
	@Before
	public void setUp() throws Exception
	{
		setUpWith(null, "default");
		pool = injector.getService(NodePool.class);
	}

	@Test
	public void test_multithreading() throws Exception
	{
		int nbThread = 10;
		ThreadPool[] thread = new ThreadPool[nbThread];
		for(int i = 0; i < nbThread; i++)
		{
			thread[i] = new ThreadPool(pool);
			thread[i].start();
		}

		for(int i = 0; i < nbThread; i++)
		{
			synchronized(thread[i])
			{
				if(!thread[i].done)
					thread[i].wait();
				Assert.assertTrue(thread[i].isAlive());
				thread[i].notify();
			}
		}
	}
	
	private class ThreadPool extends Thread
	{
		protected NodePool pool;
		public volatile boolean done = false;
		
		public ThreadPool(NodePool pool)
		{
			this.pool = pool;
		}
		
		@Override
		public void run()
		{
			try {
				Random r = new Random();
				int nbObjets = 1000;
				List<AStarNode> liste = new ArrayList<AStarNode>();
				for(int i = 0; i < nbObjets; i++)
				{
					Thread.sleep(1);
					liste.add(pool.getNewNode());
				}
				for(int i = 0; i < nbObjets; i++)
				{
					Thread.sleep(1);
					pool.destroyNode(liste.remove(r.nextInt(liste.size())));
				}
				for(int i = 0; i < nbObjets; i++)
				{
					Thread.sleep(1);
					liste.add(pool.getNewNode());
				}
				Thread.sleep(1);
				pool.destroy(liste);
				synchronized(this)
				{
					done = true;
					notify();
					wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}