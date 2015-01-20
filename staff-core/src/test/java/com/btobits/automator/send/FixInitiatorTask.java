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

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.junit.Test;
import static org.junit.Assert.*;

/** 
 * @author Yaroslav_Yaremych
 */
public class FixInitiatorTask extends BaseXmlTest {

	@Test
	public void test_Bug_15844() throws Exception {
		try {
            runXml("Bug_15642.xml", new BuildEventPrint());
			fail();
		} catch (Exception ex) {
			System.err.println(ex.getMessage());
		}
    }


    class BuildEventPrint implements BuildListener {

        @Override
        public void taskStarted(BuildEvent arg0) {
        }
        @Override
        public void taskFinished(BuildEvent arg0) {
        }
        @Override
        public void targetStarted(BuildEvent arg0) {
        }
        @Override
        public void targetFinished(BuildEvent arg0) {
        }
        @Override
        public void messageLogged(BuildEvent arg0) {
            System.out.println(arg0.getMessage());
        }
        @Override
        public void buildStarted(BuildEvent arg0) {
        }
        @Override
        public void buildFinished(BuildEvent arg0) {
        }
    }

}