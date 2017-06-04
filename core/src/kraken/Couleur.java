/*
 * Copyright (C) 2013-2017 Pierre-François Gimenez
 * Distributed under the MIT License.
 */


package kraken;

import java.awt.Color;
import graphic.printable.Layer;

/**
 * Quelques couleurs prédéfinies
 * 
 * @author pf
 *
 */

public enum Couleur
{
	BLANC(new Color(255, 255, 255)),
	NOIR(new Color(0, 0, 0)),
	GRIS(new Color(50, 50, 50, 200)),
	BLEU(new Color(0, 0, 200)),
	JAUNE(new Color(200, 200, 0)),
	ROUGE(new Color(200, 0, 0)),
	VIOLET(new Color(200, 0, 200)),
	ROBOT_BOF(new Color(200, 0, 200), Layer.BACKGROUND),
	VERT(new Color(0, 200, 0)),
	ToF_COURT(new Color(0x00, 0xB0, 0x50)),
	ToF_LONG(new Color(0x92, 0xD0, 0x50)),
	IR(new Color(0x2E, 0x75, 0xB6)),
	TRAJECTOIRE(new Color(0x00, 0x03, 0x12), Layer.MIDDLE),
	TRAJECTOIRE_MAUVAIS_SENS(new Color(0xC0, 0x00, 0x00), Layer.MIDDLE),
	OBSTACLES(new Color(0xFF, 0x7D, 0x3D, 150), Layer.BACKGROUND),
	ROBOT(new Color(0x94, 0xEB, 0x2A), Layer.FOREGROUND),
	GAME_ELEMENT(new Color(0x26, 0xCB, 0xAF, 150), Layer.FOREGROUND),
	HEURISTIQUE(new Color(0xFF, 0xD7, 0x00, 180), Layer.FOREGROUND),
	CINEMATIQUE(new Color(0xFF, 0x14, 0x93, 180), Layer.FOREGROUND),
	SCAN(new Color(0x80, 0x00, 0x80));

	public final Color couleur;
	public final Layer l;

	private Couleur(Color couleur)
	{
		this.couleur = couleur;
		this.l = Layer.MIDDLE;
	}

	private Couleur(Color couleur, Layer l)
	{
		this.couleur = couleur;
		this.l = l;
	}
}
