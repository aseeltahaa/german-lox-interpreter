package LOX;
import java.util.List;
import java.util.Map;

class Class implements Callable {
	final String name;
	final Class superclass;
	private final Map<String, Function> methods;

	Class(String name, Class superclass, Map<String, Function> methods) {
		this.superclass = superclass;
		this.name = name;
		this.methods = methods;
	}

	Function findMethod(String name) {
		if (methods.containsKey(name)) {
			return methods.get(name);
		}

		if (superclass != null) {
			return superclass.findMethod(name);
		}

		return null;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public Object call(Interpreter interpreter, List<Object> arguments) {
		Instance instance = new Instance(this);
		Function initializer = findMethod("init");
		if (initializer != null) {
			initializer.bind(instance).call(interpreter, arguments);
		}

		return instance;
	}

	@Override
	public int arity() {
		Function initializer = findMethod("init");
		if (initializer == null)
			return 0;
		return initializer.arity();
	}
}