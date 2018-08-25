package com.jackiepenghe.blelibrary;

/**
 * 错误的大型数据异常(当使用大型数据写入远端设备时，数据长度小于等于20字节时抛出此异常)
 *
 * @author jackie
 */
class WrongLargeDataArrayException extends RuntimeException {
    /**
     * Constructs a new runtime exception with {@code null} as its
     * detail message.  The cause is not initialized, and may subsequently be
     * initialized by a call to {@link #initCause}.
     */
    WrongLargeDataArrayException() {
        super("large data array length must be Greater than 20");
    }
}
