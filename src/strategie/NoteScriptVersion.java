package strategie;

import scripts.Script;

/**
 * Classe des couples (note, script) utilisée par Stratégie
 * @author pf
 *
 */

public class NoteScriptVersion 
{

	public float note;
	public Script script;
	public int version;

	public NoteScriptVersion(float note, Script script, int version) {
		this.note = note;
		this.script = script;
		this.version = version;
	}

	public NoteScriptVersion() {
		note = 0;
		script = null;
		version = 0;
	}
	
	public NoteScriptVersion clone()
	{
		return new NoteScriptVersion(note, script, version);
	}
	
	public String toString()
	{
		return "Script "+script+", version "+version+", note "+note;
	}
		
}
