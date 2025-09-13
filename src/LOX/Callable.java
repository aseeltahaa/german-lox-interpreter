package LOX;

import java.util.List;

// can be called using a function
interface Callable {
	int arity(); // number of parameters of a function

	Object call(Interpreter interpreter, List<Object> arguments); // function call
	
}