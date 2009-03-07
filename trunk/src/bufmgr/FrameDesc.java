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

import global.GlobalConst;
import global.PageId;

/**
 * A frame description class. It describes each page in the buffer pool, the
 * page number in the file, whether it is dirty or not, its pin count, and the
 * pin count change when pinning or unpinning a page.
 */
public class FrameDesc implements GlobalConst {

    private final PageId pageId;

    private boolean dirty;

    private int pin_cnt;

    /**
     * Creates a FrameDesc object, initialize pageNo, dirty and pin_count.
     */
    public FrameDesc() {
        pageId = new PageId();
        pageId.pid = INVALID_PAGE;
        dirty = false;
        pin_cnt = 0;
    }

    /**
     * Returns the pin count of a certain frame page.
     * 
     * @return the pin count number.
     */
    public int pin_count() {
        return pin_cnt;
    }

    /**
     * Increments the pin count of a certain frame page when the page is pinned.
     * 
     * @return the incremented pin count.
     */
    public int pin() {
        return (++pin_cnt);
    }
    
    /** 
     * Decrements the pin count of a frame when the page is
     * unpinned.  If the pin count is equal to or less than
     * zero, the pin count will be zero.
     *
     * @return the decremented pin count.
     */
    public int unpin() {
        pin_cnt = (pin_cnt > 0) ? pin_cnt - 1 : 0;
        return pin_cnt;
    }
    
    /**
     * return Frame pageId
     * 
     * @return pageId
     */

    public PageId getPageId() {
        return pageId;
    }
    
    /**
     * check dirty status
     * 
     * @return if dirty, return true
     */

    public boolean isDirty() {
        return dirty;
    }
    
    /**
     * set dirty status
     * @param dirty
     */

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
}