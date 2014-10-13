package reflang;

/**
 * Representation of a heap, which maps references to values.
 * 
 * @author hridesh
 *
 */
public interface Heap {

	Value.RefVal ref (Value value) throws HeapException;

	Value deref (Value.RefVal loc) throws HeapException;

	Value setref (Value.RefVal loc, Value value) throws HeapException;

	Value.RefVal free (Value.RefVal value) throws HeapException;

	@SuppressWarnings("serial")
	static public class HeapException extends RuntimeException {

		HeapException(String message){
			super(message);
		}

	}

	static public class Heap32Bit implements Heap {

		java.util.Hashtable<Value.RefVal, Value> _rep = new java.util.Hashtable<Value.RefVal, Value>();

		public Value.RefVal ref (Value value) {
			Value.RefVal new_loc = new Value.RefVal();
			if(_rep.size() >= Integer.MAX_VALUE)
				throw new HeapException("Fatal Error: Allowed memory size of " + Integer.MAX_VALUE + "exhausted.");
			_rep.put(new_loc, value);
			return new_loc;
		}

		public Value deref (Value.RefVal loc) {
			if(!_rep.containsKey(loc))
				throw new HeapException("Fatal Error: Segmentation fault at memory access " + loc);
			return _rep.get(loc);
		}

		public Value setref (Value.RefVal loc, Value value) {
			if(!_rep.containsKey(loc))
				throw new HeapException("Fatal Error: Segmentation fault at memory access " + loc);
			_rep.put(loc, value);
			return value;
		}

		public Value.RefVal free (Value.RefVal loc) {
			if(!_rep.containsKey(loc))
				throw new HeapException("Fatal Error: Segmentation fault at memory access " + loc);
			_rep.remove(loc);
			return loc;
		}

		public Heap32Bit(){}
	}

}
