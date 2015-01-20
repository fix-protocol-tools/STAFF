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
import com.btobits.automator.fix.utils.MessageDifferenceException;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Volodymyr_Biloshkurs
 *
 */
public class FixSendTaskTest extends BaseXmlTest {

    @Test
    public void test1() throws Exception {
        runXml("test1.xml");
    }

    @Test(expected = MessageDifferenceException.class )
    public void test2() throws Exception {
        runXml("test2.xml");
    }

    @Ignore // TODO: fix me
    @Test(expected = MessageDifferenceException.class)
    public void test3() throws Exception {
        runXml("test3.xml");
    }

    @Test(expected = MessageDifferenceException.class)
    public void test4() throws Exception {
        runXml("test4.xml");
    }

    @Test(expected = MessageDifferenceException.class)
    public void test5() throws Exception {
        runXml("test5.xml");
    }

    @Test(expected = MessageDifferenceException.class)
    public void test6() throws Exception {
        runXml("test6.xml");
    }

    @Test
    public void test7() throws Exception {
        runXml("test7.xml");
    }

    @Ignore // TODO: fix me
    @Test
    public void test8() throws Exception {
        runXml("test8.xml");
    }

    @Ignore // TODO: fix me
    @Test
    public void test9() throws Exception {
        runXml("test9.xml");
    }

    @Test
    public void test10() throws Exception {
        runXml("test10.xml");
    }

    @Ignore // TODO: fix me
    @Test(expected = MessageDifferenceException.class)
    public void test11() throws Exception {
        runXml("test11.xml");
    }

    @Test
    public void test12() throws Exception {
        runXml("test12.xml");
    }

    @Ignore // TODO: fix me
    @Test(expected = MessageDifferenceException.class)
    public void test13() throws Exception {
        runXml("test13.xml");
    }

    @Test
    public void test14() throws Exception {
        runXml("test14.xml");
    }

    @Test
    public void test15() throws Exception {
        runXml("test15.xml");
    }

    @Test(expected = MessageDifferenceException.class)
    public void test16() throws Exception {
        runXml("test16.xml");
    }

    @Test
    public void test17() throws Exception {
        runXml("test17.xml");
    }

    @Ignore// FIXME: in this test we testing message validation? Now in one of message is absent required field
    @Test
    public void test18() throws Exception {
        try  {
            runXml("test18.xml");
            fail();
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }

    @Test
    public void test19() throws Exception {
        runXml("test19.xml");
    }

    @Test
    public void test20() throws Exception {
        runXml("test20.xml");
    }

    @Test
    public void test21() throws Exception {
        runXml("test21.xml");
    }

    @Test
    public void test22() throws Exception {
        runXml("test22.xml");
    }

    @Test(expected = MessageDifferenceException.class)
    public void test23() throws Exception {
        runXml("test23.xml");
    }

    @Test
    public void test24() throws Exception {
        runXml("test24.xml");
    }

    @Test
    public void test25() throws Exception {
        runXml("test25.xml");
    }

    @Ignore // TODO: fix me
    @Test(expected = MessageDifferenceException.class)
    public void test26() throws Exception {
        runXml("test26.xml");
    }

    @Test(expected = MessageDifferenceException.class)
    public void test27() throws Exception {
        runXml("test27.xml");
    }

    @Test
    public void test28() throws Exception {
        runXml("test28.xml");
    }

    @Test
    public void test29() throws Exception {
        runXml("test29.xml");
    }

    @Test(expected = MessageDifferenceException.class)
    public void test30() throws Exception {
        runXml("test30.xml");
    }

    @Ignore
    @Test // TODO: fix me
    public void test31() throws Exception {
        runXml("test31.xml");
    }

    @Test
    public void test32() throws Exception {
        runXml("test32.xml");
    }
    
    @Test(expected = MessageDifferenceException.class)
    public void test33() throws Exception {
        runXml("test33.xml");
    }

    @Ignore // FIXME: in this test strange sender="TROIOI"... we must test strange sender?
    @Test
    public void test34() throws Exception {
        try  {
            runXml("test34.xml");
            fail();
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }

    @Test
    public void test36() throws Exception {
        runXml("test36.xml");
    }

    @Test
    public void testFIX50() throws Exception {
        runXml("FIX50_simple_test.xml");
    }
}
