package LOX;

// exit from deep code execution and carry a value back
class Return extends RuntimeException {
	final Object value;
	
	/*
	 * This disables Java’s usual exception stuff:
	 * null: No error message
	 * null: No cause
	 * false: No stack trace generated
	 * false: No suppression
	 */
	Return(Object value) {
		super(null, null, false, false);
		this.value = value;
	}
}