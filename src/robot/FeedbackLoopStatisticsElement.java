package robot;

/**
 * Utilisé par LocomotionCardWrapper
 * @author pf
 *
 */

public enum FeedbackLoopStatisticsElement {
	PWMmoteurGauche,
	PWMmoteurDroit,
	erreur_rotation,
	erreur_translation,
	derivee_erreur_rotation,
	derivee_erreur_translation;
//	inverse_erreur_translation_integrale;
	
}
