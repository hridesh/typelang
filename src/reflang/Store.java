package reflang;

/**
 * Representation of a Store, which maps locations to values.
 * 
 * @author hridesh
 *
 */
public interface Store {

	Value.Loc ref (Value value) throws StoreException;

	Value deref (Value.Loc loc) throws StoreException;

	Value setref (Value.Loc loc, Value value) throws StoreException;

	@SuppressWarnings("serial")
	static public class StoreException extends RuntimeException {

		StoreException(String message){
			super(message);
		}

	}

	static public class Store32Bit implements Store {

		java.util.Hashtable<Value.Loc, Value> _rep = new java.util.Hashtable<Value.Loc, Value>();

		public Value.Loc ref (Value value) {
			Value.Loc new_loc = new Value.Loc();
			if(_rep.size() >= Integer.MAX_VALUE)
				throw new StoreException("Fatal Error: Allowed memory size of " + Integer.MAX_VALUE + "exhausted.");
			_rep.put(new_loc, value);
			return new_loc;
		}

		public Value deref (Value.Loc loc) {
			if(!_rep.containsKey(loc))
				throw new StoreException("Fatal Error: Segmentation fault at memory access " + loc);
			return _rep.get(loc);
		}

		public Value setref (Value.Loc loc, Value value) {
			if(!_rep.containsKey(loc))
				throw new StoreException("Fatal Error: Segmentation fault at memory access " + loc);
			_rep.put(loc, value);
			return value;
		}

		public Store32Bit(){}
	}

}
