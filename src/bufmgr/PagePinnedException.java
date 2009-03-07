/* ------------------------------------------------------------------------
 * @source  : PagePinnedException.java
 * @desc    : PagePinnedException class
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

public class PagePinnedException extends ChainException {

    /**
     * Constructor
     * @param arg0
     * @param arg1
     */
    public PagePinnedException(Exception arg0, String arg1) {
        super(arg0, arg1);
    }

}
