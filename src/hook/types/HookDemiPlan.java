package hook.types;

import permissions.ReadOnly;
import permissions.ReadWrite;
import utils.Log;
import utils.Vec2;

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
	public HookDemiPlan(Log log, Vec2<ReadOnly> point, Vec2<ReadOnly> direction)
	{
		super(log, true);
		this.point = point.clone();
		this.direction = direction.clone();
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
		ArrayList<Byte> out = new ArrayList<Byte>();
		out.add(SerialProtocol.OUT_HOOK_DEMI_PLAN.code);
		out.add((byte) ((point.x+1500) >> 4));
		out.add((byte) ((point.x+1500) << 4 + point.y >> 8));
		out.add((byte) (point.y));
		out.add((byte) ((direction.x+1500) >> 4));
		out.add((byte) ((direction.x+1500) << 4 + direction.y >> 8));
		out.add((byte) (direction.y));
		out.addAll(super.toSerial());
		return out;
	}	
}
