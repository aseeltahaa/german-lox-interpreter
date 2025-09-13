package LOX;
import java.util.HashMap;
import java.util.Map;

// Chains Hashmaps to point towards one another
// Global Environment (values)
//		└──> Block Environment (HashMap, points to Global)
//				└──> Function Environment (HashMap, points to Block)
class Environment {
	// References the higher level (parent) environment
	final Environment enclosing;
	// main data structure storing variable names and their values. 
	// each environment has its own HashMap to store variables defined in that scope.
	private final Map<String, Object> values = new HashMap<>();

	// default constructor: global scope
	Environment() {
		enclosing = null;
	}

	// constructor for creating a nested environment
	Environment(Environment enclosing) {
		this.enclosing = enclosing;
	}

	// define a new variable in the current scope/ environment
	void define(String name, Object value) {
		/*
		 * if (values.containsKey(name.lexeme)) { throw new RuntimeError(name,
		 * "Variable '" + name.lexeme + "' already declared in this scope."); }
		 */
		values.put(name, value);
	}

	// Retrieve the value of a variable by its token (if found inside the current scope)
	// currentEnv → enclosing → enclosing → ... until found or null
	Object get(Token name) {
		if (values.containsKey(name.lexeme)) {
			return values.get(name.lexeme);
		}

		if (enclosing != null)
			return enclosing.get(name);

		throw new RuntimeError(name, "Undefinierte Variable '" + name.lexeme + "'.");
	}

	// Assign a new value to an existing variable (if found in the current environment)
	void assign(Token name, Object value) {
		if (values.containsKey(name.lexeme)) {
			values.put(name.lexeme, value);
			return;
		}

		if (enclosing != null) {
			enclosing.assign(name, value);
			return;
		}

		throw new RuntimeError(name, "Undefinierte Variable '" + name.lexeme + "'.");
	}
	
	// returns the environment at a specific distance up the environment chain
	// Distance 0 = current environment
    // Distance 1 = enclosing environment  
    //  Distance 2 = enclosing.enclosing environment etc.
	Environment ancestor(int distance) {
		Environment environment = this;
		for (int i = 0; i < distance; i++) {
			environment = environment.enclosing;
		}

		return environment;
	}

	// gets a variable's value at a specific distance in the environment chain.
	Object getAt(int distance, String name) {
		return ancestor(distance).values.get(name);
	}
	
	// sets a variable's value at a specific distance in the environment chain.
	void assignAt(int distance, Token name, Object value) {
	    ancestor(distance).values.put(name.lexeme, value);
	}
}