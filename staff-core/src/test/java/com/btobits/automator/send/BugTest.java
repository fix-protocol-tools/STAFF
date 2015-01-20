/*
  * Copyright © 2011-2014 EPAM Systems/B2BITS® (http://www.b2bits.com).
 *
 * This file is part of STAFF.
 *
 * STAFF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * STAFF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with STAFF. If not, see <http://www.gnu.org/licenses/>.
 */

package com.btobits.automator.send;

import com.btobits.automator.BaseXmlTest;
import com.btobits.automator.fix.utils.fix.FixAcceptor;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Mykhailo_Sereda
 */
public class BugTest extends BaseXmlTest {

    @Ignore
    // bug not fixed. When we use custom dictionary
    // and call it standard version name(FIX.4.0 || FIX.4.1 e.c.),
    // MessageFactory use standard dictionary instead of custom
    @Test
    public void testBug15533() throws Exception {
        runXml("Bug_15533.xml");
    }

    @Test
    public void testBug15642() throws Exception {
        runXml("Bug_15642.xml");
    }

    @Test
    public void testBug16361() throws Exception {
        runXml("Bug_16361.xml");
    }

    /**
     * Test bug 17173.
     * In this bug some problem with synchronization.
     * Problem arises when initiator send few messages and in this time initiator must send reject message.
     * Requires a fixFilter task
     * @throws Exception
     */
    @Test
    public void testBug17173() throws Exception {
        runXml("Bug_17173.xml");
    }

    @Test
    @Ignore
    public void testBug17048() throws Exception {
        runXml("Bug_17048.xml");
    }

}
