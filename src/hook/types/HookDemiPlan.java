package hook.types;

import utils.Log;
import utils.Vec2;
import utils.permissions.ReadOnly;
import utils.permissions.ReadWrite;

import java.util.ArrayList;

import enums.SerialProtocol;
import hook.Hook;

/**
 * Hook qui se déclenche lorsqu'on traverse un demi-plan
 * @author pf
 *
 */

public class HookDemiPlan extends Hook
{

	private final Vec2<ReadWrite> point, direction;

	/**
	 * Constructeur sans paramètre, qui crée un hook qui n'est jamais appelable.
	 * Les paramètres sont donnés dans update.
	 * @param config
	 * @param log
	 * @param state
	 */
	public HookDemiPlan(Log log)
	{
		super(log, true);
		point = new Vec2<ReadWrite>();
		direction = new Vec2<ReadWrite>();
	}
	
	/**
	 * Constructeur
	 * @param config
	 * @param log
	 * @param state
	 */
	public HookDemiPlan(Log log, Vec2<ReadOnly> point, Vec2<ReadOnly> direction, boolean symetrie)
	{
		super(log, true);
		this.point = point.clone();
		if(symetrie)
			this.point.x = -this.point.x;
		this.direction = direction.clone();
		if(symetrie)
			this.direction.x = -this.direction.x;
	}
	
	/**
	 * Mise à jour des paramètres du hooks
	 */
	public void update(Vec2<ReadOnly> direction, Vec2<ReadOnly> point)
	{
		Vec2.copy(direction, this.direction);
		Vec2.copy(point, this.point);
	}

	/**
	 * Pour vérifier si le hook peut être déclenché sur un segment [A,B], il
	 * faut juste vérifier s'il peut l'être en A ou en B.
	 */
	@Override
	public boolean simulated_evaluate(Vec2<ReadOnly> pointA, Vec2<ReadOnly> pointB, long date)
	{
		return pointA.minusNewVector(point).dot(direction) > 0 ||
				pointB.minusNewVector(point).dot(direction) > 0;
	}

	@Override
	public ArrayList<Byte> toSerial()
	{
		int x = (int)point.x;
		int y = (int)point.y;
		int dx = (int)direction.x;
		int dy = (int)direction.y;
		ArrayList<Byte> out = new ArrayList<Byte>();
		out.add(SerialProtocol.OUT_HOOK_DEMI_PLAN.code);
		out.add((byte) ((x+1500) >> 4));
		out.add((byte) (((x+1500) << 4) + (y >> 8)));
		out.add((byte) (y));
		out.add((byte) ((dx+1500) >> 4));
		out.add((byte) (((dx+1500) << 4) + ((dy+1500) >> 8)));
		out.add((byte) (dy+1500));
		out.addAll(super.toSerial());
		return out;
	}	
}
