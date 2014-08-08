package reflang;

import java.util.List;

/**
 * Representation of an environment, which maps variables to values.
 * 
 * @author hridesh
 *
 */
public interface Env {
	Value get (String search_var);

	@SuppressWarnings("serial")
	static public class LookupException extends RuntimeException {
		LookupException(String message){
			super(message);
		}
	}
	
	static public class EmptyEnv implements Env {
		public Value get (String search_var) {
			throw new LookupException("No binding found for name: " + search_var);
		}
	}
	
	static public class ExtendEnv implements Env {
		Env _saved_env; 
		String _var; 
		Value _val; 
		public ExtendEnv(Env saved_env, String var, Value val){
			_saved_env = saved_env;
			_var = var;
			_val = val;
		}
		public Value get (String search_var) {
			if (search_var.equals(_var))
				return _val;
			return _saved_env.get(search_var);
		}
	}
	
	static public class ExtendEnvRec implements Env {
		private Env _saved_env;
		private List<String> _names;
		private List<Value.Fun> _funs;
		public ExtendEnvRec(Env saved_env, List<String> names, List<Value.Fun> funs){
			_saved_env = saved_env;
			_names = names;
			_funs = funs;
		}
		public Value get (String search_var) {
			int size = _names.size();
			for(int index = 0; index < size; index++) {
				if (search_var.equals(_names.get(index))) {
					Value.Fun f = _funs.get(index);
					return new Value.Fun(this, f.formals(), f.body());				
				}
			}
			return _saved_env.get(search_var);
		}
	}
	
}
