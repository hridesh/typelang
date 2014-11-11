package typelang;

public interface Type {
	public String tostring();
	static class ErrorT implements Type {
	    public String tostring() { return "error"; }
	}
	static class UnitT implements Type {
	    public String tostring() { return "unit"; }
	}
	static class BoolT implements Type {
	    public String tostring() { return "bool"; }
	}
	static class StringT implements Type {
	    public String tostring() { return "string"; }
	}
	static class NumT implements Type {
	    public String tostring() { return "number"; }
	}
	static class PairT implements Type {
		protected Type _fst;
		protected Type _snd;
	    public PairT(Type fst, Type snd) { _fst = fst; _snd = snd; } 
		public Type fst() { return _fst; }
		public Type snd() { return _snd; }
	    public java.lang.String tostring() { 
	    	return "(" + _fst.tostring() + " " + _snd.tostring() + ")"; 
	    }
	}
}
