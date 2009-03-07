/* ------------------------------------------------------------------------
 * @source  : Clock.java
 * @desc    : Clock class
 * ------------------------------------------------------------------------
 *
 * ------------------------------------------------------------------------
 * VER  DATE         AUTHOR                           DESCRIPTION
 * ---  -----------  -------------------------------  ---------------------
 * 1.0  03.04. 2009  Wolff Jeffrey, KwangSoo Yang     Initialization
 *
 * ------------------------------------------------------------------------ */

package bufmgr;

/**
 * A clock algorithm for buffer pool replacement policy. It picks up the frame
 * in the buffer pool to be replaced. This is the default replacement policy.
 */
public class Clock extends Replacer {

    private int target;

    private final int noProblemStatus = 0;

    private final int referencedSatus = 1;

    private final int pinnedStatus = 2;

    private final int limitLoop = 2;

    private final int numberOfBuffer;

    private final int limitCount;

    private final int frameStatus[];

    private final FrameDesc[] frameDescArray;

    /**
     * Creates a clock object.
     * 
     * @param javamgr
     */
    public Clock(BufMgr javamgr) {
        target = 0;
        numberOfBuffer = javamgr.getNumBuffers();
        frameStatus = new int[numberOfBuffer];
        limitCount = numberOfBuffer * limitLoop;  // One loop is enough. yet I give one more chance.
        frameDescArray = javamgr.frameTable();
    }

    /**
     * Picks up the victim frame to be replaced according to the clock
     * algorithm. Pin the victim so that other process can not pick it as a
     * victim.
     * 
     * @return -1 if no frame is available. head of the list otherwise.
     * @throws BufferPoolExceededException.
     */

    public int pick_victim() throws BufferPoolExceededException, PagePinnedException {

        for (int inx = 0; inx < limitCount; inx++) {
            if (frameStatus[target] == noProblemStatus) {
                break;
            } else if (frameStatus[target] == referencedSatus) {
                frameStatus[target] = noProblemStatus;
            }
            target = (target + 1) % numberOfBuffer;
        }

        if (frameStatus[target] != noProblemStatus) {
            throw new BufferPoolExceededException(null, "Clock.pick_victim(): BufferPoolExceededException");
        }

        if (frameDescArray[target].pin_count() != 0) {
            throw new PagePinnedException(null, "Clock.pick_victim: PagePinnedException");
        }
        frameStatus[target] = pinnedStatus;
        frameDescArray[target].pin();

        return target;
    }

    /**
     * Returns the name of the clock algorithm as a string.
     * 
     * @return "Clock", the name of the algorithm.
     */

    public final String name() {
        return "Clock";
    }

    /**
     * check frame number bound. if the bound is more than the number of buffer
     * or less than 0, throw exception.
     * 
     * @param frameNumber
     * @throws InvalidFrameNumberException
     */
    public void checkFrameNumberBound(int frameNumber) throws InvalidFrameNumberException {
        if (frameNumber < 0 || frameNumber >= numberOfBuffer) {
            throw new InvalidFrameNumberException(null, "Clock.checkFrameNumberRange: InvalidFrameNumberException");
        }
    }

    /**
     * pin frame
     * 
     * @param frameNumber
     */
    public void pin(int frameNumber) throws InvalidFrameNumberException {
        checkFrameNumberBound(frameNumber);
        frameDescArray[frameNumber].pin();
        frameStatus[frameNumber] = pinnedStatus;
    }

    /**
     * unpin frame
     * 
     * @param frameNumber
     */

    public boolean unpin(int frameNumber) throws PageUnpinnedException, InvalidFrameNumberException {
        checkFrameNumberBound(frameNumber);
        if (frameDescArray[frameNumber].pin_count() == 0) {
            throw new PageUnpinnedException(null, "Clock.unpin(): PageUnpinnedException.");
        }
        frameDescArray[frameNumber].unpin();
        if (frameDescArray[frameNumber].pin_count() == 0) {
            frameStatus[frameNumber] = referencedSatus;
        }
        return true;
    }

    /**
     * free frame
     * 
     * @param frameNumber
     */

    public void free(int frameNumber) throws PagePinnedException {
        if (frameDescArray[frameNumber].pin_count() > 1) {
            throw new PagePinnedException(null, "Clock.free(): PagePinnedException.");
        } else {
            frameDescArray[frameNumber].unpin();
            frameStatus[frameNumber] = noProblemStatus;
        }
    }

    /**
     * Displays information from clock replacement algorithm.
     */

    public void info() {
    }

    /**
     * return the number of buffers.
     */
    public int getNumberOfBuffer() {
        return numberOfBuffer;
    }

}