package mas.action;

public class ActionFailedException extends Exception {

	private static final long serialVersionUID = 1L;

	public ActionFailedException() {
		super();
	}

	public ActionFailedException(String message, Throwable cause) {
		super(message, cause);
	}

	public ActionFailedException(String message) {
		super(message);
	}

	public ActionFailedException(Throwable cause) {
		super(cause);
	}

}
