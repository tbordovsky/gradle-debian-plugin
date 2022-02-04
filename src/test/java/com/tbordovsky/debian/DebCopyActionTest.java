package com.tbordovsky.debian;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class DebCopyActionTest {

    /**
     * All we really want this method to do is resolve the difference between relative paths on our machine and absolute
     * paths inside of the package, i.e.,
     *  debian/prerm -> /debian/prerm
     *  debian/postinst -> /debian/postinst
     */
    @Test
    public void givenRelativePath_matchAbsolute() {
        assertTrue(DebCopyAction.isControlFile("debian/prerm", "/debian/prerm"));
        assertTrue(DebCopyAction.isControlFile("debian/postinst", "/debian/postinst"));
    }
}
