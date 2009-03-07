/* ------------------------------------------------------------------------
 * @source  : InvalidBufferException.java
 * @desc    : InvalidBufferException class
 * ------------------------------------------------------------------------
 *
 * ------------------------------------------------------------------------
 * VER  DATE         AUTHOR                           DESCRIPTION
 * ---  -----------  -------------------------------  ---------------------
 * 1.0  03.04. 2009  Wolff Jeffrey, KwangSoo Yang     Initialization
 *
 * ------------------------------------------------------------------------ */
package bufmgr;

import chainexception.ChainException;

public class InvalidBufferException extends ChainException {

    /**
     * Constructor
     * @param e
     * @param name
     */
    public InvalidBufferException(Exception e, String name) {
        super(e, name);
    }
}
