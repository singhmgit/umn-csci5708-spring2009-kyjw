/* ------------------------------------------------------------------------
 * @source  : NeoLog.java
 * @desc    : NeoLog class
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
 * this is special log to debugging the frame/page status. If you want to change
 * the directory, you should change the value of "output" variable.
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

public class NeoLog {

    private static final NeoLog log = new NeoLog();

    private FileChannel channel;

    private final Charset charset = Charset.forName("8859_1");

    private final String nameRoot = "buftest";

    private String output;

    /**
     * default constructor
     * 
     */
    private NeoLog() {
        try {

            String osType = System.getProperty("os.name");

            if ((osType.startsWith("Solaris")) || (osType.startsWith("SunOS")) || (osType.startsWith("AIX")) || (osType.startsWith("HP-UX")) || (osType.startsWith("Linux")) || (osType.startsWith("Mac OS X"))) {
                output = "/tmp/" + nameRoot + "_" + System.getProperty("user.name") + ".db_buffer.log";
            } else if (osType.startsWith("Windows")) {
                output = "c:/db_buffer.log";
            } else {
                output = null;
            }

            new File(output).getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(output);
            channel = out.getChannel();
        } catch (Exception e) {
            channel = null;
        }
    }

    /**
     * singleton class
     * 
     * @return NeoLog
     */
    public static NeoLog getLog() {
        return log;
    }

    /**
     * write log into file
     * 
     * @param str
     *            contents
     * @param newline
     *            if true, newline appended automatically.
     */
    private void print(String str, boolean newline) {

        if (channel == null) {
            System.err.println("Log.print() error: channel is null");
            return;
        }

        try {

            ByteBuffer buff = charset.encode(str + (newline ? "\n" : ""));
            channel.write(buff);
        } catch (Exception e) {
            System.err.println("Log.print() error: channel is null");
            return;
        }
    }

    /**
     * write log into file
     * 
     * @param str
     */
    public void print(String str) {
        print(str, false);
    }

    /**
     * write log into file
     * 
     * @param str
     */
    public void println(String str) {
        print(str, true);
    }
}
