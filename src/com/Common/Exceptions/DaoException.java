package com.Common.Exceptions;

public class DaoException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected  int code;

    public static final DaoException DB_SESSION_NULL = new DaoException(90040001, "session为空");
    public static final DaoException DB_INSERT_EXCEPTION = new DaoException(90040002, "数据库操作，hibernate异常");
    public static final DaoException DB_UPDATE_EXCEPTION = new DaoException(90040002, "数据库操作，hibernate异常");
    public static final DaoException DB_DELETE_EXCEPTION = new DaoException(90040002, "数据库操作，hibernate异常");
    public static final DaoException DB_QUERY_EXCEPTION = new DaoException(90040002, "数据库操作，hibernate异常");
    public static final DaoException DB_INPUT_EXCEPTION = new DaoException(90040002, "数据库操作，Entity为空");
    public static final DaoException DB_BUILDSQL_EXCEPTION = new DaoException(90040002, "数据库操作，Entity为空");

    public DaoException() {
        super();
    }

    public DaoException(String message) {
        super(message);
    }

    public DaoException(Throwable cause) {
        super(cause);
    }

    public DaoException(String message, Throwable cause) {
        super(message, cause);
    }

    public DaoException(int code, String msgFormat, Object... args) {
        super(String.format(msgFormat, args));
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public DaoException newInstance(String msgFormat, Object... args){
        return new DaoException(this.code, msgFormat, args);
    }
}
