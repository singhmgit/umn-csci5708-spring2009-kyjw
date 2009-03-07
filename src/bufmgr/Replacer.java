/* ------------------------------------------------------------------------
 * @source  : Replacer.java
 * @desc    : Replacer class
 * ------------------------------------------------------------------------
 *
 * ------------------------------------------------------------------------
 * VER  DATE         AUTHOR                           DESCRIPTION
 * ---  -----------  -------------------------------  ---------------------
 * 1.0  03.04. 2009  Wolff Jeffrey, KwangSoo Yang     Initialization
 *
 * ------------------------------------------------------------------------ */
package bufmgr;

import global.GlobalConst;

/**
 * Repalcer class It is a abstract class to implement Replacement algorithm.(eg,
 * Clock)
 * 
 * @author Wolff Jeffrey, KwangSoo Yang
 * 
 */
abstract class Replacer implements GlobalConst {

    /**
     * Picks up the victim frame to be replaced according to the clock
     * algorithm. Pin the victim so that other process can not pick it as a
     * victim.
     * 
     * @return -1 if no frame is available. head of the list otherwise.
     * @throws BufferPoolExceededException.
     */
    public abstract int pick_victim() throws PagePinnedException, BufferPoolExceededException;

    /**
     * pin frame
     * 
     * @param frameNumber
     */
    abstract public void pin(int frameNo) throws InvalidFrameNumberException;

    /**
     * unpin frame
     * 
     * @param frameNumber
     */

    abstract public boolean unpin(int frameNo) throws PageUnpinnedException, InvalidFrameNumberException;

    /**
     * free frame
     * 
     * @param frameNumber
     */
    abstract public void free(int frameNo) throws PagePinnedException;

    /**
     * return the number of buffers.
     */
    abstract public int getNumberOfBuffer();

}
