/*
 * Copyright (C) 2013-2018 Pierre-François Gimenez
 * Distributed under the MIT License.
 */


package pfg.kraken;

import java.awt.Color;
import pfg.graphic.printable.Layer;

/**
 * Some predefined colors used by Kraken
 * 
 * @author pf
 *
 */

public enum ColorKraken
{
	WHITE(new Color(255, 255, 255)),
	BLACK(new Color(0, 0, 0)),
	TRANSPARENT_GREY(new Color(50, 50, 50, 200)),
	BLUE(new Color(0, 0, 200)),
	YELLOW(new Color(200, 200, 0)),
	RED(new Color(200, 0, 0)),
	PURPLE(new Color(200, 0, 200)),
	GREEN(new Color(0, 200, 0)),
	TRAJECTOIRE(new Color(0x00, 0x03, 0x12), Layer.MIDDLE),
	OBSTACLES(new Color(0xFF, 0x7D, 0x3D, 150), Layer.BACKGROUND),
	ROBOT(new Color(0xE0, 0xE0, 0xE0), Layer.FOREGROUND),
//	GAME_ELEMENT(new Color(0x26, 0xCB, 0xAF, 150), Layer.FOREGROUND),
	HEURISTIQUE(new Color(0x00, 0xB0, 0x50, 150), Layer.FOREGROUND),
//	CINEMATIQUE(new Color(0xFF, 0x14, 0x93, 180), Layer.FOREGROUND),
	NAVMESH(new Color(0x00, 0xB0, 0x50, 80)),
	NAVMESH_BLOCKED(new Color(0x40, 0x40, 0x40, 80)),
	NAVMESH_TRIANGLE(new Color(NAVMESH.color.getRed(), NAVMESH.color.getGreen(), NAVMESH.color.getBlue(), 10));

	public final Color color;
	public final Layer layer;

	private ColorKraken(Color couleur)
	{
		this.color = couleur;
		this.layer = Layer.MIDDLE;
	}

	private ColorKraken(Color couleur, Layer l)
	{
		this.color = couleur;
		this.layer = l;
	}
}
