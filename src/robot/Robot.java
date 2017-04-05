/*
Copyright (C) 2013-2017 Pierre-François Gimenez

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>
*/

package robot;

import config.Config;
import config.DynamicConfigurable;
import exceptions.UnableToMoveException;
import utils.Log;
import utils.Vec2RO;

/**
 * Classe abstraite du robot, dont héritent RobotVrai et RobotChrono
 * @author pf
 */

public abstract class Robot implements DynamicConfigurable
{
	/*
	 * DÉPLACEMENT HAUT NIVEAU
	 */
	
    public abstract long getTempsDepuisDebutMatch();

    protected Cinematique cinematique;
    protected volatile boolean symetrie;
    protected boolean deploye = false;
	protected Log log;
	protected boolean filetBaisse = false;
	protected boolean filetPlein = false;

	protected abstract void bloque(String nom, Object... param) throws InterruptedException;
	public abstract void avance(double distance, Speed speed) throws UnableToMoveException, InterruptedException;
	public abstract void avanceVersCentre(double distance, Speed speed, Vec2RO centre) throws UnableToMoveException, InterruptedException;
	public abstract void followTrajectory(Speed vitesse) throws InterruptedException, UnableToMoveException;
	
	public Robot(Log log)
	{
		this.log = log;
		cinematique = new Cinematique();
	}

	public int codeForPFCache()
	{
		return cinematique.codeForPFCache();
	}
	
	/**
	 * Copy this dans rc. this reste inchangé.
	 * 
	 * @param rc
	 */
    public final void copy(RobotChrono rc)
    {
    	cinematique.copy(rc.cinematique);
    	// pas besoin de copier symétrie car elle ne change pas en cours de match
    	rc.date = getTempsDepuisDebutMatch();
    }
	
    @Override
	public synchronized void updateConfig(Config config)
	{
		symetrie = config.getSymmetry();
	}
    
    @Override
    public String toString()
    {
    	return cinematique.toString();
    }
    	
	public void setCinematique(Cinematique cinematique)
	{
		cinematique.copy(this.cinematique);
	}
	

	/**
	 * Méthode bloquante qui baisse le filet
	 * @throws InterruptedException 
	 */
	public void baisseFilet() throws InterruptedException
	{
		bloque("baisseFilet");
		filetBaisse = true;
	}
	
	public void bougeFiletMiChemin() throws InterruptedException
	{
		bloque("bougeFiletMiChemin");
		filetBaisse = true;
	}
	
	public void leveFilet() throws InterruptedException
	{
		bloque("leveFilet");
		filetBaisse = false;
	}
	
	public boolean isFiletBaisse()
	{
		return filetBaisse;
	}

	public void ouvreFilet() throws InterruptedException
	{
		bloque("ouvreFilet");
		filetPlein = false;
	}

	public void fermeFilet() throws InterruptedException
	{
		bloque("fermeFilet");
//		filetPlein = false; // TODO
	}
	
	public void ejecteBalles() throws InterruptedException
	{
		bloque("ejecteBalles", !symetrie);
		filetPlein = false;
	}

	public void rearme() throws InterruptedException
	{
		bloque("rearme", !symetrie);
	}
	
	public void traverseBascule() throws InterruptedException
	{
		bloque("traverseBascule");
	}
	
	public void funnyAction() throws InterruptedException
	{
		bloque("funnyAction");
	}
	
	/**
	 * Géré par le capteur de jauge
	 */
	public void filetVuVide()
	{
		filetPlein = false;
	}
	
	/**
	 * Géré par le capteur de jauge
	 */
	public void filetVuPlein()
	{
		filetPlein = true;
	}
	
	public boolean isFiletPlein()
	{
		return filetPlein;
	}
	
}
