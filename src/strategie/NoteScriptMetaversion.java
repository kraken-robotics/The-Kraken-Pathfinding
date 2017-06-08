package strategie;

import scripts.Script;

/**
 * Classe des couples (note, script) utilisée par Stratégie
 * @author Krissprolls
 *
 */

public class NoteScriptMetaversion {

	public float note;
	public Script script;
	public int metaversion;

	public NoteScriptMetaversion(float note, Script script, int metaversion) {
		this.note = note;
		this.script = script;
		this.metaversion = metaversion;
	}

	public NoteScriptMetaversion() {
		note = 0;
		script = null;
		metaversion = 0;
	}
	
	public NoteScriptMetaversion clone()
	{
		return new NoteScriptMetaversion(note, script, metaversion);
	}
	
	public String toString()
	{
		return "Script "+script+", métaversion "+metaversion+", note "+note;
	}
		
}
