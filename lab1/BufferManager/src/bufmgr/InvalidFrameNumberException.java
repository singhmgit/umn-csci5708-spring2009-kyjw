/* ------------------------------------------------------------------------
 * @source  : InvalidFrameNumberException.java
 * @desc    : InvalidFrameNumberException class
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

public class InvalidFrameNumberException extends ChainException {

    /**
     * Constructor
     * @param arg0
     * @param arg1
     */
    public InvalidFrameNumberException(Exception arg0, String arg1) {
        super(arg0, arg1);
        // TODO Auto-generated constructor stub
    }

}
