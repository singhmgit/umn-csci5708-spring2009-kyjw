/* ------------------------------------------------------------------------
 * @source  : BufHashTbl.java
 * @desc    : BufHashTbl class
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
 * 
 * Class: BufHTEntry Description: This class defines a buffer frame
 * 
 * A buffer hashtable entry description class. It describes each entry for the
 * buffer hash table, the page number and frame number for that page, the
 * pointer points to the next hash table entry.
 */
class BufHTEntry {
    /** The next entry in this hashtable bucket. */
    public BufHTEntry next;

    /** This page number. */
    public PageId pageNo = new PageId();

    /** The frame we are stored in. */
    public int frameNo;
}

/**
 * Class: BufHashTbl Description: This class contains the buffer hash table and
 * its methods
 * 
 * A buffer hashtable to keep track of pages in the buffer pool. It inserts,
 * retrieves and removes pages from the h ash table.
 */
public class BufHashTbl implements GlobalConst {

    /** Hash Table size, small number for debugging. */
    private int HTSIZE;

    private static final int HTSIZE_DEFAULT = 20; // Default hash table size

    // if not specified

    // if not specified

    /**
     * Each slot holds a linked list of BufHTEntrys, NULL means none.
     */
    private final BufHTEntry[] ht;

    /**
     * Returns the number of hash bucket used, value between 0 and HTSIZE-1
     * 
     * @param pageNo
     *            the page number for the page in file.
     * @return the bucket number in the hash table.
     */
    private int hash(PageId pageNo) {
        return pageNo.pid % HTSIZE;
    }

    /**
     * Constructor for BufHashTbl() Creates a buffer hash table object.
     */
    public BufHashTbl() {
        HTSIZE = HTSIZE_DEFAULT;
        ht = new BufHTEntry[HTSIZE_DEFAULT];
    }

    /**
     * Constructor for BufHashTbl() Creates a buffer hash table object
     * 
     * @param htsizeIn
     *            Size of the hash table
     * 
     */
    public BufHashTbl(int htsizeIn) {
        if (htsizeIn > GlobalConst.MINIBASE_MAXARRSIZE) {
            System.err.println("BufHashTbl(" + htsizeIn + ") too large, reducing to" + GlobalConst.MINIBASE_MAXARRSIZE + ".");
            HTSIZE = GlobalConst.MINIBASE_MAXARRSIZE;
            ht = new BufHTEntry[GlobalConst.MINIBASE_MAXARRSIZE];
        } else {
            HTSIZE = htsizeIn;
            ht = new BufHTEntry[htsizeIn];
        }
    }

    /**
     * Insert association between page pageNo and frame frameNo into the hash
     * table.
     * 
     * @param pageNo
     *            page number in the bucket.
     * @param frameNo
     *            frame number in the bucket.
     * @return true if successful.
     */
    public boolean insert(PageId pageNo, int frameNo) {
        NeoLog.getLog().println("--------------------------------------------------");
        NeoLog.getLog().println("[INSERT] page Number : " + pageNo);
        NeoLog.getLog().println("--------------------------------------------------");
        final int bucketNumber = hash(pageNo);
        final BufHTEntry oldBufHTEntry = ht[bucketNumber];

        BufHTEntry neoBufHTEntry = new BufHTEntry();
        neoBufHTEntry.pageNo.pid = pageNo.pid;
        neoBufHTEntry.frameNo = frameNo;
        neoBufHTEntry.next = oldBufHTEntry;
        ht[bucketNumber] = neoBufHTEntry;

        NeoLog.getLog().println(getBufHTEntryDump());

        return true;
    }

    /**
     * Find a page in the hashtable, return INVALID_PAGE on failure, otherwise
     * the frame number.
     * 
     * @param pageNo
     *            page number in the bucket.
     */

    public int lookup(PageId pageNo) {

        NeoLog.getLog().println("--------------------------------------------------");
        NeoLog.getLog().println("[LOOK UP] page Number : " + pageNo);
        NeoLog.getLog().println("--------------------------------------------------");
        NeoLog.getLog().println(getBufHTEntryDump());

        if (pageNo.pid == INVALID_PAGE) {
            NeoLog.getLog().println("Find: Nothing : INVALID_PAGE");
            NeoLog.getLog().println("--------------------------------------------------");
            return INVALID_PAGE;
        }
        final int bucketNumber = hash(pageNo);
        BufHTEntry bufHTEntry = ht[bucketNumber];

        while (bufHTEntry != null) {
            if (bufHTEntry.pageNo.pid == pageNo.pid) {
                NeoLog.getLog().println("Find: Frame Number : " + bufHTEntry.frameNo);
                NeoLog.getLog().println("--------------------------------------------------");
                return bufHTEntry.frameNo;
            }
            bufHTEntry = bufHTEntry.next;
        }
        NeoLog.getLog().println("Find: Nothing : INVALID_PAGE");
        NeoLog.getLog().println("--------------------------------------------------");
        return INVALID_PAGE;
    }

    /**
     * Remove the page from the hashtable.
     * 
     * @param pageNo
     *            page number of the bucket.
     */

    public boolean remove(PageId pageNo) {

        NeoLog.getLog().println("--------------------------------------------------");
        NeoLog.getLog().println("[REMOVE] page Number : " + pageNo);
        NeoLog.getLog().println("--------------------------------------------------");

        if (pageNo.pid == INVALID_PAGE) {
            NeoLog.getLog().println(getBufHTEntryDump());
            NeoLog.getLog().println("Reomve: Nothing");
            NeoLog.getLog().println("--------------------------------------------------");
            return true;
        }

        final int bucketNumber = hash(pageNo);

        BufHTEntry bufHTEntry = ht[bucketNumber];
        BufHTEntry beforeBufHTEntry = null;

        while (bufHTEntry != null) {
            if (bufHTEntry.pageNo.pid == pageNo.pid) {
                if (beforeBufHTEntry != null) {
                    beforeBufHTEntry.next = bufHTEntry.next;
                } else {
                    ht[bucketNumber] = bufHTEntry.next;
                }
                NeoLog.getLog().println(getBufHTEntryDump());
                NeoLog.getLog().println("Remove:\tFrame number: " + bufHTEntry.frameNo + " \t" + "Page Number: " + bufHTEntry.pageNo.pid + "\n");
                NeoLog.getLog().println("--------------------------------------------------");
                return true;
            }
            beforeBufHTEntry = bufHTEntry;
            bufHTEntry = bufHTEntry.next;
        }
        NeoLog.getLog().println(getBufHTEntryDump());
        NeoLog.getLog().println("Reomve: Nothing");
        NeoLog.getLog().println("--------------------------------------------------");
        return false;
    }

    /**
     * Show hashtable contents.
     */
    public void display() {
        System.out.println(getBufHTEntryDump());
    }

    /**
     * make hashtable contents.
     * 
     * @return hashtable contents.
     */

    private String getBufHTEntryDump() {
        StringBuffer strbuf = new StringBuffer();
        strbuf.append("***************** BufHTEntry Dump*****************\n");
        final int size = ht.length;
        for (int inx = 0; inx < size; inx++) {
            BufHTEntry bufHTEntry = ht[inx];
            // strbuf.append("******************************************************************\n");
            strbuf.append("BufHTEntry[" + inx + "]\n");
            // strbuf.append("******************************************************************\n");
            while (bufHTEntry != null) {
                strbuf.append("\tFrame number: " + bufHTEntry.frameNo + " \t" + "Page Number: " + bufHTEntry.pageNo.pid + "\n");
                bufHTEntry = bufHTEntry.next;
            }
        }
        return strbuf.toString();
    }

    /**
     * Returns the size of the hash table.
     * 
     * @return size of hash table.
     */
    public int getHTsize() {
        return (HTSIZE);
    }
}