/* ------------------------------------------------------------------------
 * @source  : BufferPoolExceededException.java
 * @desc    : BufferPoolExceededException class
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

public class BufferPoolExceededException extends ChainException {
    
    /**
     * Constructor
     * @param e
     * @param name
     */
    
    public BufferPoolExceededException(Exception e, String name) {
        super(e, name);
    }
}
