package typelang;

public interface Type {
	public String tostring();
	static class ErrorT implements Type {
		private static final ErrorT _instance = new ErrorT();
		public static ErrorT getInstance() { return _instance; }
	    public String tostring() { return "error"; }
	}
	static class UnitT implements Type {
		private static final UnitT _instance = new UnitT();
		public static UnitT getInstance() { return _instance; }
	    public String tostring() { return "unit"; }
	}
	static class BoolT implements Type {
		private static final BoolT _instance = new BoolT();
		public static BoolT getInstance() { return _instance; }
	    public String tostring() { return "bool"; }
	}
	static class StringT implements Type {
		private static final StringT _instance = new StringT();
		public static StringT getInstance() { return _instance; }
	    public String tostring() { return "string"; }
	}
	static class NumT implements Type {
		private static final NumT _instance = new NumT();
		public static NumT getInstance() { return _instance; }
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
