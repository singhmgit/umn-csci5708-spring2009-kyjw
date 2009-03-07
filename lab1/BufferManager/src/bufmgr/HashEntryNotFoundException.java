/* ------------------------------------------------------------------------
 * @source  : HashEntryNotFoundException.java
 * @desc    : HashEntryNotFoundException class
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

public class HashEntryNotFoundException extends ChainException {

    /**
     * Constructor
     * @param ex
     * @param name
     */
    public HashEntryNotFoundException(Exception ex, String name) {
        super(ex, name);
    }
}
