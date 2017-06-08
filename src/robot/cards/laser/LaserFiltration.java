package robot.cards.laser;

import java.util.Stack;

import smartMath.Vec2;


/**
 * @author clément
 */
import utils.Log;
import utils.Config;
import container.Service;
import smartMath.Matrn;
public class LaserFiltration implements Service {


	private double dt;
	private Kalman filtre_kalman;
	private Vec2 last_point;
	private int valeurs_rejetees;
	private Stack<Vec2> historique;

	public LaserFiltration(Config config, Log log)
	{
		this.dt = (double)0.2;
		double[][] tab_x = {{1400.}, {100.},{0.},{0.}}; 
		Matrn x = new Matrn(tab_x); //vecteur d'état au départ
		double[][] tab_p = {{30.,0.,0.,0.},{0.,30.,0.,0.},{0.,0.,10.,0.},{0.,0.,0.,10.}};
		Matrn p = new Matrn(tab_p); // incertitude initiale
		double[][] tab_f = {{1,0,this.dt,0},{0,1,0,this.dt},{0,0,1,0},{0,0,0,1}};
		Matrn f = new Matrn(tab_f); //matrice de transition
		double[][] tab_h = {{1.,0.,0.,0.},{0.,1.,0.,0.}};
		Matrn h = new Matrn(tab_h); //matrice d'observation
		double[][] tab_r = {{900.,0.},{0.,900.}};
		Matrn r = new Matrn(tab_r); // incertitude sur la mesure
		double[][] tab_q = {{Math.pow(this.dt, 3)/3., 0, Math.pow(this.dt, 2.)/2., 0},{0, Math.pow(this.dt, 3.)/3., 0, Math.pow(this.dt, 2.)/2},{Math.pow(this.dt, 2.)/2., 0, 4*this.dt, 0},{0, Math.pow(this.dt, 2.)/2, 0, 4*this.dt}};
		Matrn q = new Matrn(tab_q);
		q.multiplier_scalaire(30);
		this.filtre_kalman = new Kalman(x, p, f, h, r, q); 
		this.historique = new Stack<Vec2>();
		this.valeurs_rejetees = 0;
		this.last_point = new Vec2();
		//double acceleration = null; 
		//Je sais pas à quoi acceleration servait dans le code python, puisqu'il était inutile...
		
		
	}
	
	public void updateConfig()
	{
	}
	
	public Matrn etat_robot_adverse()
	{
		return this.filtre_kalman.x;
		//Veut-on vraiment retourner le type Matrn? Pas certain ! Mais c'est plutôt pratique
	}
	
	
	public void update_dt(float new_dt)
	{
		this.dt = new_dt;
		this.filtre_kalman.f.matrice[0][2] = new_dt; //encore visibilité !
		this.filtre_kalman.f.matrice[1][3] = new_dt; //et toujours !!
	}

	
	public Vec2 position()
	{
		return this.last_point;
		//hésitation observée dans le code python, en voici une interprétation
	}
	
	public Vec2 vitesse()
	{
		Matrn state = filtre_kalman.x;
		return new Vec2((int)state.matrice[2][0], (int)state.matrice[3][0]) ;
	}

	
	public void update(Vec2 point)
	{
		if(this.filtrage_acceleration(point))
		{
			this.last_point = new Vec2(point.x, point.y);
		}
	}
	
	
	private boolean filtrage_acceleration(Vec2 pointm0)
	{
		if(historique.size() != 3)
		{
			return true;
		}
		Vec2 pointm1 = historique.pop();
		Vec2 pointm2 = historique.pop();
//		Vec2 pointm3 = historique.pop();
		Vec2 vitesse_actuelle = pointm0.MinusNewVector(pointm1);
		Vec2 vitesse_m1 = pointm1.MinusNewVector(pointm2);
//		Vec2 vitesse_m2 = pointm2.MinusNewVector(pointm3);
		Vec2 acceleration_actuelle = vitesse_actuelle.MinusNewVector(vitesse_m1);
//		Vec2 acceleration_precedente = vitesse_m1.MinusNewVector(vitesse_m2);
		//Vec2 jerk = acceleration_actuelle.MinusNewVector(acceleration_precedente);
		//float produit = acceleration_actuelle.dot(vitesse_m1);
		//jerk et produit étaient utilisés dans du code inutilisé en Python
		//donc voilà, au cas où il y en a besoin...
		if(acceleration_actuelle.SquaredLength()/Math.pow(dt,2) >50000 & this.valeurs_rejetees < 3 )
		{
			valeurs_rejetees +=1;
			return false;
		}
		else
		{
			valeurs_rejetees = 0;
		    return true;
		}
	}

	
	
}
