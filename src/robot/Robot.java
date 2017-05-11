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
import exceptions.ActionneurException;
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

	protected abstract void bloque(String nom, Object... param) throws InterruptedException, ActionneurException;
	public abstract void avance(double distance, Speed speed) throws UnableToMoveException, InterruptedException;
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
	 * @throws ActionneurException 
	 */
	public void baisseFilet() throws InterruptedException, ActionneurException
	{
		filetBaisse = true;
		bloque("baisseFilet");
	}
	
	public void bougeFiletMiChemin() throws InterruptedException, ActionneurException
	{
		filetBaisse = true;
		bloque("bougeFiletMiChemin");
	}
	
	public void leveFilet() throws InterruptedException, ActionneurException
	{
		bloque("leveFilet");
		filetBaisse = false;
	}

	public void verrouilleFilet() throws InterruptedException
	{
		try {
			bloque("verrouilleFilet");
		} catch (ActionneurException e) {
			log.critical(e);
			// impossible
		}
		filetBaisse = false;
	}
	
	public boolean isFiletBaisse()
	{
		return filetBaisse;
	}

	public void ouvreFilet() throws InterruptedException
	{
		try {
			bloque("ouvreFilet");
		} catch (ActionneurException e) {
			log.critical(e);
			// impossible
		}
		filetPlein = false;
	}

	public void fermeFilet() throws InterruptedException
	{
		try {
			bloque("fermeFilet");
		} catch (ActionneurException e) {
			log.critical(e);
			// impossible
		}
//		filetPlein = false; // TODO
	}
	
	public void ejecteBalles() throws InterruptedException, ActionneurException
	{
		bloque("ejecteBalles", !symetrie);
		filetPlein = false;
	}
	public void ejecteBallesAutreCote() throws InterruptedException, ActionneurException
	{
		bloque("ejecteBalles", symetrie);
		filetPlein = false;
	}
	
	public void rearme() throws InterruptedException, ActionneurException
	{
		bloque("rearme", !symetrie);
	}
	
	public void rearmeAutreCote() throws InterruptedException, ActionneurException
	{
		bloque("rearme", symetrie);
	}
	
	public void traverseBascule() throws InterruptedException, ActionneurException
	{
		bloque("traverseBascule");
	}
	
	public void funnyAction() throws InterruptedException
	{
		try {
			bloque("funnyAction");
		} catch (ActionneurException e) {
			log.critical(e);
			// impossible
		}
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
