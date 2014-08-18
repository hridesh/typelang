package reflang;

import java.util.List;

import reflang.AST.Exp;

public interface Value {
	public String toString();
	static class Loc implements Value { //New in the reflang
	    public Loc() {}
	    public String toString() {
	    	return "loc:" + this.hashCode();
	    }
	}
	static class Fun implements Value { //New in the funclang
		private Env _env;
		private List<String> _formals;
		private Exp _body;
		public Fun(Env env, List<String> formals, Exp body) {
			_env = env;
			_formals = formals;
			_body = body;
		}
		public Env env() { return _env; }
		public List<String> formals() { return _formals; }
		public Exp body() { return _body; }
	    public String toString() { 
			String result = "(lambda ( ";
			for(String formal : _formals) 
				result += formal + " ";
			result += ") ";
			result += _body.accept(new Printer.Formatter(), _env);
			return result + ")";
	    }
	}
	static class Int implements Value {
		private int _val;
	    public Int(int v) { _val = v; } 
	    public int v() { return _val; }
	    public String toString() { return "" + _val; }
	}
	static class Bool implements Value {
		private boolean _val;
	    public Bool(boolean v) { _val = v; } 
	    public boolean v() { return _val; }
	    public String toString() { return "" + _val; }
	}
	static class Unit implements Value {
		public static final Unit v = new Unit();
	    public String toString() { return "unit"; }
	}
	static class DynamicError implements Value { 
		String message = "Unknown dynamic error.";
		public DynamicError() { }
		public DynamicError(String message) { this.message = message; }
	    public String toString() { return "" + message; }
	}
}
