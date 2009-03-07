/* ------------------------------------------------------------------------
 * @source  : BufMgr.java
 * @desc    : BufMgr class
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
import global.SystemDefs;

import java.io.IOException;

import diskmgr.DiskMgrException;
import diskmgr.Page;

/**
 * The buffer manager class, it allocates new pages for the buffer pool, pins
 * and unpins the frame, frees the frame page, and uses the replacement
 * algorithm to replace the page.
 */
public class BufMgr implements GlobalConst {

    /** Total number of buffer frames in the buffer pool. */
    private final int numBuffers;

    /** physical buffer pool. */
    private byte[][] bufPool; // default = byte[NUMBUF][MAX_SPACE];

    /** The hash table, only allocated once. */
    private BufHashTbl hashTable = new BufHashTbl();

    /** An array of Descriptors one per frame. */
    private final FrameDesc[] frmeTable; // default = new FrameDesc[NUMBUF];

    /** The replacer object, which is only used in this class. */
    private Replacer replacer;

    /**
     * Factor out the common code for the two versions of Flush
     * 
     * flushes all the page if all_page flag is true
     * 
     * Remove pages from buffer.
     * 
     * @param pageid
     *            the page number of the page which needs to be flushed.
     * @param all_pages
     *            the total number of page to be flushed.
     * 
     * @exception HashOperationException
     *                if there is a hashtable error.
     * @exception PageUnpinnedException
     *                when unpinning an unpinned page
     * @exception PagePinnedException
     *                when trying to free a pinned page
     * @exception PageNotFoundException
     *                when the page could not be found
     * @exception InvalidPageNumberException
     *                when the page number is invalid
     * @exception FileIOException
     *                File I/O error
     * @exception IOException
     *                Other I/O errors
     */

    private void privFlushPages(PageId pageid, int all_pages) throws HashOperationException, PageUnpinnedException, PagePinnedException, PageNotFoundException, BufMgrException, IOException {

        int unpinnedCount = 0;

        for (int inx = 0; inx < numBuffers; inx++)

            if ((all_pages != 0) || (frmeTable[inx].getPageId().pid == pageid.pid)) {
                if (frmeTable[inx].pin_count() != 0) {
                    unpinnedCount++;
                }

                if (frmeTable[inx].isDirty()) {
                    if (frmeTable[inx].getPageId().pid == INVALID_PAGE) {
                        throw new PageNotFoundException(null, "BufMgr.privFlushPages(): PageNotFoundException");
                    }

                    final PageId targetPageId = new PageId(frmeTable[inx].getPageId().pid);
                    write_page(targetPageId, new Page(bufPool[inx]));

                    if (!hashTable.remove(targetPageId)) {
                        throw new HashOperationException(null, "BufMgr.privFlushPages(): HashOperationException");
                    }
                    frmeTable[inx].setDirty(false);
                    frmeTable[inx].getPageId().pid = INVALID_PAGE;
                }
                if (all_pages == 0) {
                    if (unpinnedCount != 0) {
                        throw new PagePinnedException(null, "BufMgr.privFlushPages(): PagePinnedException");
                    }
                }
            }

        if (all_pages != 0 && unpinnedCount != 0) {
            throw new PagePinnedException(null, "BufMgr.privFlushPages(): PagePinnedException");
        }
    }

    /**
     * Create a buffer manager object.
     * 
     * @param numbufs
     *            number of buffers in the buffer pool.
     * @param replacerArg
     *            name of the buffer replacement policy.
     */

    public BufMgr(int numbufs, String replacerArg) {

        numBuffers = numbufs;
        frmeTable = new FrameDesc[numBuffers];
        bufPool = new byte[numBuffers][MAX_SPACE];

        for (int inx = 0; inx < numBuffers; inx++) {
            frmeTable[inx] = new FrameDesc();
        }
        replacer = new Clock(this);

    }

    // Debug use only
    private void bmhashdisplay() {
        // nothing
    }

    /**
     * Check if this page is in buffer pool, otherwise find a frame for this
     * page, read in and pin it. Also write out the old page if it's dirty
     * before reading if emptyPage==TRUE, then actually no read is done to bring
     * the page in.
     * 
     * @param pin_pgid
     *            page number in the minibase.
     * @param page
     *            the pointer poit to the page.
     * @param emptyPage
     *            true (empty page); false (non-empty page)
     * 
     * @exception ReplacerException
     *                if there is a replacer error.
     * @exception HashOperationException
     *                if there is a hashtable error.
     * @exception PageUnpinnedException
     *                if there is a page that is already unpinned.
     * @exception InvalidFrameNumberException
     *                if there is an invalid frame number .
     * @exception PageNotReadException
     *                if a page cannot be read.
     * @exception BufferPoolExceededException
     *                if the buffer pool is full.
     * @exception PagePinnedException
     *                if a page is left pinned .
     * @exception BufMgrException
     *                other error occured in bufmgr layer
     * @exception IOException
     *                if there is other kinds of I/O error.
     */

    public void pinPage(PageId pin_pgid, Page page, boolean emptyPage) throws ReplacerException, HashOperationException, PageUnpinnedException, InvalidFrameNumberException, PageNotReadException, BufferPoolExceededException, PagePinnedException, BufMgrException, IOException {

        PageId writablePageId = new PageId(INVALID_PAGE);
        boolean isWritable = false;

        int frameNumber = hashTable.lookup(pin_pgid);
        if (frameNumber < 0) {
            frameNumber = replacer.pick_victim();
            if (frameNumber < 0) {
                throw new ReplacerException(null, "BufMgr.pinPage(): ReplacerException");
            }

            if ((frmeTable[frameNumber].getPageId().pid != INVALID_PAGE) && (frmeTable[frameNumber].isDirty() == true)) {
                isWritable = true;
                writablePageId.pid = frmeTable[frameNumber].getPageId().pid;
            }

            if (!hashTable.remove(frmeTable[frameNumber].getPageId())) {
                throw new HashOperationException(null, "BufMgr.pinPage(): HashOperationException");
            }

            frmeTable[frameNumber].getPageId().pid = pin_pgid.pid;
            frmeTable[frameNumber].setDirty(false);

            if (!hashTable.insert(pin_pgid, frameNumber)) {
                throw new HashOperationException(null, "BufMgr.pinPage(): HashOperationException");
            }

            Page writablePage = new Page(bufPool[frameNumber]);
            if (isWritable == true) {
                write_page(writablePageId, writablePage);
            }

            if (emptyPage == false) {
                try {
                    writablePage.setpage(bufPool[frameNumber]);
                    read_page(pin_pgid, writablePage);
                } catch (Exception e) {

                    final FrameDesc frameDesc = frmeTable[frameNumber];
                    final PageId pageId = frameDesc.getPageId();
                    if (!hashTable.remove(pageId)) {
                        throw new HashOperationException(e, "BufMgr.pinPage(): HashOperationException");
                    }
                    pageId.pid = INVALID_PAGE;
                    frameDesc.setDirty(false);
                    if (!replacer.unpin(frameNumber)) {
                        throw new ReplacerException(e, "BufMgr.pinPage(): ReplacerException");
                    }
                    throw new PageNotReadException(e, "BufMgr.pinPage(): PageNotReadException");
                }

            }
            page.setpage(bufPool[frameNumber]);
        } else {
            page.setpage(bufPool[frameNumber]);
            replacer.pin(frameNumber);
        }
    }

    /**
     * To unpin a page specified by a pageId. If pincount>0, decrement it and if
     * it becomes zero, put it in a group of replacement candidates. if
     * pincount=0 before this call, return error.
     * 
     * @param globalPageId_in_a_DB
     *            page number in the minibase.
     * @param dirty
     *            the dirty bit of the frame
     * 
     * @exception ReplacerException
     *                if there is a replacer error.
     * @exception PageUnpinnedException
     *                if there is a page that is already unpinned.
     * @exception InvalidFrameNumberException
     *                if there is an invalid frame number .
     * @exception HashEntryNotFoundException
     *                if there is no entry of page in the hash table.
     */

    public void unpinPage(PageId PageId_in_a_DB, boolean dirty) throws ReplacerException, PageUnpinnedException, HashEntryNotFoundException, InvalidFrameNumberException {

        final int frameNumber = hashTable.lookup(PageId_in_a_DB);

        if (frameNumber == INVALID_PAGE) {
            throw new HashEntryNotFoundException(null, "BufMgr.unpinPage(): HashEntryNotFoundException");
        }

        final FrameDesc frameDesc = frmeTable[frameNumber];

        if (frameDesc.getPageId().pid == INVALID_PAGE) {
            throw new InvalidFrameNumberException(null, "BufMgr.unpinPage(): InvalidFrameNumberException");
        }

        if ((replacer.unpin(frameNumber)) != true) {
            throw new ReplacerException(null, "BufMgr.unpinPage(): ReplacerException");
        }

        if (dirty == true) {
            frameDesc.setDirty(dirty);
        }

    }

    /**
     * Call DB object to allocate a run of new pages and find a frame in the
     * buffer pool for the first page and pin it. If buffer is full, ask DB to
     * deallocate all these pages and return error (null if error).
     * 
     * @param firstpage
     *            the address of the first page.
     * @param howmany
     *            total number of allocated new pages.
     * @return the first page id of the new pages.
     * 
     * @exception BufferPoolExceededException
     *                if the buffer pool is full.
     * @exception HashOperationException
     *                if there is a hashtable error.
     * @exception ReplacerException
     *                if there is a replacer error.
     * @exception HashEntryNotFoundException
     *                if there is no entry of page in the hash table.
     * @exception InvalidFrameNumberException
     *                if there is an invalid frame number.
     * @exception PageUnpinnedException
     *                if there is a page that is already unpinned.
     * @exception PagePinnedException
     *                if a page is left pinned.
     * @exception PageNotReadException
     *                if a page cannot be read.
     * @exception IOException
     *                if there is other kinds of I/O error.
     * @exception BufMgrException
     *                other error occured in bufmgr layer
     * @exception DiskMgrException
     *                other error occured in diskmgr layer
     */

    public PageId newPage(Page firstpage, int howmany) throws BufferPoolExceededException, HashOperationException, ReplacerException, HashEntryNotFoundException, InvalidFrameNumberException, PagePinnedException, PageUnpinnedException, PageNotReadException, BufMgrException, DiskMgrException, IOException {

        final PageId firstPageId = new PageId();
        allocate_page(firstPageId, howmany);
        try {
            pinPage(firstPageId, firstpage, true);
            return firstPageId;
        } catch (Exception e) {
            deallocate_page(firstPageId, howmany);
            return null;
        }
    }

    /**
     * User should call this method if she needs to delete a page. this routine
     * will call DB to deallocate the page.
     * 
     * @param globalPageId
     *            the page number in the data base.
     * @exception InvalidBufferException
     *                if buffer pool corrupted.
     * @exception ReplacerException
     *                if there is a replacer error.
     * @exception HashOperationException
     *                if there is a hash table error.
     * @exception InvalidFrameNumberException
     *                if there is an invalid frame number.
     * @exception PageNotReadException
     *                if a page cannot be read.
     * @exception BufferPoolExceededException
     *                if the buffer pool is already full.
     * @exception PagePinnedException
     *                if a page is left pinned.
     * @exception PageUnpinnedException
     *                if there is a page that is already unpinned.
     * @exception HashEntryNotFoundException
     *                if there is no entry of page in the hash table.
     * @exception IOException
     *                if there is other kinds of I/O error.
     * @exception BufMgrException
     *                other error occured in bufmgr layer
     * @exception DiskMgrException
     *                other error occured in diskmgr layer
     */

    public void freePage(PageId globalPageId) throws InvalidBufferException, ReplacerException, HashOperationException, InvalidFrameNumberException, PageNotReadException, BufferPoolExceededException, PagePinnedException, PageUnpinnedException, HashEntryNotFoundException, BufMgrException, DiskMgrException, IOException {

        final int frameNumber = hashTable.lookup(globalPageId);

        if (frameNumber >= numBuffers) {
            throw new InvalidBufferException(null, "BufMgr.freePage(): InvalidBufferException");
        }

        if (frameNumber < 0) {
            deallocate_page(globalPageId);
            return;
        }

        try {
            replacer.free(frameNumber);
        } catch (PagePinnedException ppe) {
            throw new ReplacerException(ppe, "BufMgr.freePage(): ReplacerException");
        }

        final FrameDesc frameDesc = frmeTable[frameNumber];
        final PageId pageId = frameDesc.getPageId();

        if (!hashTable.remove(pageId)) {
            throw new HashOperationException(null, "BufMgr.freePage(): HashOperationException");
        }

        pageId.pid = INVALID_PAGE;
        frameDesc.setDirty(false);
        deallocate_page(globalPageId);
    }

    /**
     * Added to flush a particular page of the buffer pool to disk
     * 
     * @param pageid
     *            the page number in the database.
     * 
     * @exception HashOperationException
     *                if there is a hashtable error.
     * @exception PageUnpinnedException
     *                if there is a page that is already unpinned.
     * @exception PagePinnedException
     *                if a page is left pinned.
     * @exception PageNotFoundException
     *                if a page is not found.
     * @exception BufMgrException
     *                other error occured in bufmgr layer
     * @exception IOException
     *                if there is other kinds of I/O error.
     */

    public void flushPage(PageId pageid) throws HashOperationException, PageUnpinnedException, PagePinnedException, PageNotFoundException, BufMgrException, IOException {
        privFlushPages(pageid, 0);
    }

    /**
     * Flushes all pages of the buffer pool to disk
     * 
     * @exception HashOperationException
     *                if there is a hashtable error.
     * @exception PageUnpinnedException
     *                if there is a page that is already unpinned.
     * @exception PagePinnedException
     *                if a page is left pinned.
     * @exception PageNotFoundException
     *                if a page is not found.
     * @exception BufMgrException
     *                other error occured in bufmgr layer
     * @exception IOException
     *                if there is other kinds of I/O error.
     */

    public void flushAllPages() throws HashOperationException, PageUnpinnedException, PagePinnedException, PageNotFoundException, BufMgrException, IOException {
        privFlushPages(new PageId(INVALID_PAGE), 1);
    }

    /**
     * Gets the total number of buffers.
     * 
     * @return total number of buffer frames.
     */

    public int getNumBuffers() {
        return numBuffers;
    }

    /**
     * Gets the total number of unpinned buffer frames.
     * 
     * @return total number of unpinned buffer frames.
     */

    public int getNumUnpinnedBuffers() {

        int count = 0;
        final int size = replacer.getNumberOfBuffer();
        for (int inx = 0; inx < size; inx++) {
            if (frmeTable[inx].pin_count() == 0) {
                count++;
            }
        }
        return count;
    }

    /**
     * A few routines currently need direct access to the FrameTable.
     */
    public FrameDesc[] frameTable() {
        return frmeTable;
    }

    /**
     * write page
     * 
     * @param pageno
     *            pageId will be wrote to disk
     * @param page
     *            the page object will be wrote to disk
     * @throws BufMgrException
     */

    private void write_page(PageId pageno, Page page) throws BufMgrException {

        try {
            SystemDefs.JavabaseDB.write_page(pageno, page);
        } catch (Exception e) {
            throw new BufMgrException(e, "BufMgr.write_page(): BufMgrException");
        }
    }

    /**
     * read page
     * 
     * @param pageno
     *            pageId which will be read
     * @param page
     *            the page object which holds the contents of page
     * @throws BufMgrException
     */

    private void read_page(PageId pageno, Page page) throws BufMgrException {

        try {
            SystemDefs.JavabaseDB.read_page(pageno, page);
        } catch (Exception e) {
            throw new BufMgrException(e, "BufMgr.read_page(): BufMgrException");
        }

    }

    /**
     * allocate page
     * 
     * @param pageno
     *            the starting page id of the run of pages
     * @param num
     *            the number of page need allocated
     * @throws BufMgrException
     */

    private void allocate_page(PageId pageno, int num) throws BufMgrException {

        try {
            SystemDefs.JavabaseDB.allocate_page(pageno, num);
        } catch (Exception e) {
            throw new BufMgrException(e, "BufMgr.allocate_page(): BufMgrException");
        }
    }

    /**
     * deallocate page
     * 
     * @param pageno
     *            the start pageId to be deallocate
     * @throws BufMgrException
     */

    private void deallocate_page(PageId pageno) throws BufMgrException {

        try {
            SystemDefs.JavabaseDB.deallocate_page(pageno);
        } catch (Exception e) {
            throw new BufMgrException(e, "BufMgr.deallocate_page() BufMgrException");
        }
    }

    /**
     * deallocate page
     * 
     * @param pageno
     *            the start pageId to be deallocate
     * @param num
     *            the number of pages to be deallocated
     * @throws BufMgrException
     */

    private void deallocate_page(PageId pageno, int num) throws BufMgrException {

        try {
            SystemDefs.JavabaseDB.deallocate_page(pageno, num);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BufMgrException(e, "BufMgr.deallocate_page(): BufMgrException");
        }
    }

}