package com.tsanikgr.whist_multiplayer.util;

import com.badlogic.gdx.audio.Sound;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.ErrorManager;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Log {
	private static final int INFO = 0;
	private static final int WARNING = 1;
	private static final int ERROR = 2;
	private static final int DISABLED = 10;
	private static final int LINE_LENGTH = 70;

	private static int LOG_LEVEL = WARNING;
	private static boolean SOUND_ON_ERROR = false;

	private static MyLogger instance;
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	private final StringBuilder stringBuilder, methodName;
	private final LogEntry logEntry;
	private final String objectName;
	private static Sound errorSound = null;


	synchronized private static MyLogger logger() {
		if (instance == null) {
			instance = new MyLogger();
		}
		return instance;
	}

	public Log(Object object) {
		if (object == null) this.objectName = "static";
		else this.objectName = object.getClass().getSimpleName();

		stringBuilder = new StringBuilder(200);
		methodName = new StringBuilder(30);
		logEntry = new LogEntry();
	}

	static public void setErrorSound(Sound errorSound) {
		Log.errorSound = errorSound;
	}

	/*******************************************************************************************************************************/

	public class LogEntry {
		private final StringBuilder helper;
		private final StringBuilder message;
		private int level;
		Exception exception;

		private LogEntry(){
			message = new StringBuilder(200);
			helper = new StringBuilder(5);
		}

		private LogEntry reset(int level){
			message.setLength(0);
			this.level = level;
			exception = null;
			return this;
		}

		public LogEntry append(String partialMessage) {
			if (LOG_LEVEL <= level) message.append(partialMessage);
			return this;
		}

		public LogEntry append(int partialMessage) {
			if (LOG_LEVEL <= level) message.append(partialMessage);
			return this;
		}

		public LogEntry append(long partialMessage){
			if (LOG_LEVEL <= level) message.append(partialMessage);
			return this;
		}

		public LogEntry append(float partialMessage, int decimalPlaces) {
			if (LOG_LEVEL <= level) {
				helper.setLength(0);
				helper.append("%.").append(decimalPlaces).append("f");
				message.append(String.format(helper.toString(), partialMessage));
			}
			return this;
		}

		public LogEntry append(double partialMessage, int decimalPlaces) {
			if (LOG_LEVEL <= level) {
				helper.setLength(0);
				helper.append("%.").append(decimalPlaces).append("f");
				message.append(String.format(helper.toString(), partialMessage));
			}
			return this;
		}

		public LogEntry append(boolean partialMessage) {
			if (LOG_LEVEL <= level) message.append(partialMessage);
			return this;
		}

		public void print() {
			if (LOG_LEVEL <= level)
				switch (level) {
					case 0:
						i(message.toString());
						break;
					case 1:
						if (exception == null) w(message.toString());
						else w(message.toString(), exception);
						break;
					case 2:
						if (exception == null) e(message.toString());
						else e(message.toString(), exception);
						break;
					default:
						break;
				}
		}
	}

	public LogEntry i(){
		return logEntry.reset(0);
	}

	public LogEntry w(){
		return logEntry.reset(1);
	}

	public LogEntry e(){
		return logEntry.reset(2);
	}

	public LogEntry w(Exception e) {
		logEntry.reset(1);
		logEntry.exception = e;
		return logEntry;
	}

	public LogEntry e(Exception e) {
		logEntry.reset(2);
		logEntry.exception = e;
		return logEntry;
	}

	synchronized private String getLine(StringBuilder prepend, String text, StringBuilder append) {
		int spaces = LINE_LENGTH - prepend.length() - text.length() /* - append.length()*/;
		prepend.append(text);
		for (int i = 0 ; i < spaces ; i++) prepend.append(" ");
		return prepend.append(append).toString();
	}

	synchronized private void i(String info) {
		updateMethodName();
		stringBuilder.setLength(0);
		logger().log(Level.INFO, getLine(stringBuilder, info, this.methodName));
	}

	synchronized private void w(String warning) {
		updateMethodName();
		stringBuilder.setLength(0);
		stringBuilder.append("[Warning] ");
		logger().log(Level.WARNING, getLine(stringBuilder, warning, this.methodName));
	}

	synchronized private void w(String warning, Throwable exception) {
		updateMethodName();
		stringBuilder.setLength(0);
		stringBuilder.append(">>>> [Warning] ");
		stringBuilder.append(this.methodName);
		stringBuilder.append("\n\t");
		stringBuilder.append(warning);
		stringBuilder.append("\n\tException   > ");
		stringBuilder.append(exception.getClass().getSimpleName());
		stringBuilder.append(" <\n");
		logger().log(Level.WARNING, stringBuilder.toString());
		exception.printStackTrace();
	}

	synchronized private void e(String error) {
		updateMethodName();
		stringBuilder.setLength(0);
		stringBuilder.append("[Error] ");
		if (errorSound != null && SOUND_ON_ERROR) errorSound.play();
		logger().log(Level.SEVERE, getLine(stringBuilder, error, this.methodName));
	}

	synchronized private void e(String error, Throwable exception) {
		updateMethodName();
		stringBuilder.setLength(0);
		stringBuilder.append(">>>> [Error] ");
		stringBuilder.append(this.methodName);
		stringBuilder.append("\n\t");
		stringBuilder.append(error);
		stringBuilder.append("\n\tException   > ");
		stringBuilder.append(exception.getClass().getSimpleName());
		stringBuilder.append(" <\n");
		logger().log(Level.SEVERE, stringBuilder.toString());
		exception.printStackTrace();
	}

	/*******************************************************************************************************************************/

	private void updateMethodName() {
		StackTraceElement[] st = Thread.currentThread().getStackTrace();
		int i;
		for (i = 0 ; i < st.length ; i++) {
			if (st[i].getFileName().compareTo("Log.java") == 0 &&
					st[i].getMethodName().compareTo("print") == 0) {
				i++;
				break;
			}
		}

		methodName.setLength(0);
		methodName.append(st[i].getClassName());
		methodName.replace(0, methodName.lastIndexOf(".") + 1, "");
		if (methodName.lastIndexOf("$") != -1)	methodName.replace(methodName.lastIndexOf("$"),methodName.length(),"");
		boolean printObject = (objectName.compareTo(methodName.toString()) != 0 &&
				objectName.compareTo("<init>") != 0);

		methodName.setLength(0);
		if (printObject)
			methodName.append(objectName);

		methodName.append(".")
				.append(st[i].getMethodName())
				.append("(")
				.append(st[i].getFileName())
				.append(":")
				.append(st[i].getLineNumber())
				.append(")");
	}

	/*******************************************************************************************************************************/

	private static class MyLogger extends Logger {

		public MyLogger() {
			super("nikos", null);
			setLevel(Level.ALL);
			addHandler(new Handler() {

				@Override
				public void publish(LogRecord record) {
					if (getFormatter() == null) {
						setFormatter(new MyFormatter());
					}
					try {
						String message = getFormatter().format(record);
						if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
							System.err.print(message);
						} else {
							System.out.print(message);
						}
					} catch (Exception exception) {
						reportError(null, exception, ErrorManager.FORMAT_FAILURE);
					}
				}

				@Override
				public void close() throws SecurityException {}

				@Override
				public void flush() {}
			});
		}
	}

	/*******************************************************************************************************************************/

	private final static class MyFormatter extends Formatter {

		final StringBuilder sb = new StringBuilder(10000);

		@Override
		public String format(LogRecord record) {
			sb.replace(0,sb.length(),formatMessage(record)).append(LINE_SEPARATOR);

			if (record.getThrown() != null) {
				try {
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					record.getThrown().printStackTrace(pw);
					pw.close();
					sb.append(sw.toString());
				} catch (Exception ex) {
					throw new RuntimeException("Problem with logger");
				}
			}
			return sb.toString();
		}
	}

	public static void setLevelToInfo() {
		LOG_LEVEL = INFO;
	}
	public static void setLevelToWarning() {
		LOG_LEVEL = WARNING;
	}
	public static void setLevelToError() {
		LOG_LEVEL = ERROR;
	}
	public static void dissableLog() {
		LOG_LEVEL = DISABLED;
	}
	public static void setSoundOnError(boolean enable) {
		SOUND_ON_ERROR = enable;
	}
}
