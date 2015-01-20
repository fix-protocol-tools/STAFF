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

package com.btobits.automator;

import com.btobits.automator.send.FixSendTaskTest;
import org.apache.tools.ant.*;
import org.junit.Before;

import java.io.File;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;

/**
 * @author Mykhailo_Sereda
 */
public abstract class BaseXmlTest {

    private String baseDir = null;
    private String xmlDir = null;
    private String logDir = null;

    @Before
    public void setUp() throws Exception {
        if(baseDir == null || xmlDir == null || logDir == null){
            Properties properties = new Properties();
            properties.load(FixSendTaskTest.class.getResourceAsStream("location.properties"));
            baseDir = properties.getProperty("test.baseDir");
            xmlDir = properties.getProperty("test.scenario.xml.folder");
            logDir = properties.getProperty("test.log.dir");
            assertNotNull(baseDir);
            assertNotNull(xmlDir);
            assertNotNull(logDir);
        }
    }

    public void runXml(String source) throws Exception {
        runXml(source, null);
    }

    public void runXml(String source, BuildListener buildListener) throws Exception {
        new TestCmd(buildListener).run(source);
    }

    protected class TestCmd {
        private final BuildListener buildListener;

        public TestCmd() {
            this(null);
        }

        public TestCmd(BuildListener buildListener) {
            this.buildListener = buildListener;
        }

        public void run(String inBuildFile) throws Exception {
            inBuildFile = xmlDir + inBuildFile;
            Project p = new Project();
            p.setBasedir(baseDir);
            if (buildListener!=null) {
                p.addBuildListener(buildListener);
            }

            BuildLogger logger = new DefaultLogger();
            logger.setMessageOutputLevel(Project.MSG_INFO);
            logger.setOutputPrintStream(System.out);
            logger.setErrorPrintStream(System.out);
            logger.setEmacsMode(true);

            File buildFile = new File(inBuildFile);
            p.addBuildListener(logger);
            p.setUserProperty("ant.file", buildFile.getAbsolutePath());
            p.setUserProperty("test.log.dir", logDir);
            p.setUserProperty("test.base.dir", baseDir);

            p.init();
            ProjectHelper helper = ProjectHelper.getProjectHelper();
            p.addReference("ant.projectHelper", helper);
            helper.parse(p, buildFile);
            try {
                p.executeTarget(p.getDefaultTarget());
            } finally {
                p.executeTarget("clean_all");
            }
        }
    }

}
