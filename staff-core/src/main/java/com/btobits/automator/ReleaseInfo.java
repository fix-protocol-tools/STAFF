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

import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.net.URL;

public class ReleaseInfo {

    public static String getShortVersion() {
        String version = getVersion();
        return shortenVersion(version);
    }

    static String shortenVersion(String version) {
        int index1 = version.indexOf('.', 1);
        if (index1 >= 0) {
            int index2 = version.indexOf('.', index1 + 1);
            if (index2 >= 0) {
                int index3 = version.indexOf('.', index2 + 1);
                if (index3 >= 0) {
                    return version.substring(0, index3);
                } else {
                    return version.substring(0, index2);
                }
            } else {
                return version.substring(0, index1);
            }
        } else {
            return version;
        }
    }

    public static String getVersion() {
        String version = "";
        String classContainer = ReleaseInfo.class.getProtectionDomain().getCodeSource().getLocation().toString();
        try {
            URL manifestUrl = new URL("jar:" + classContainer + "!/META-INF/MANIFEST.MF");
            Manifest manifest = new Manifest(manifestUrl.openStream());
            Attributes attr = manifest.getMainAttributes();
            version = attr.getValue(Attributes.Name.IMPLEMENTATION_VERSION);
        } catch (Exception ex) {
            // ignore to avoid log record with ERROR priority when running unit tests at default & site lifecycle
        }
        return version;
    }
    
    public static void main(String[] args) {
    	System.out.println("Version=" + getVersion());
    	System.out.println("Short version=" + getShortVersion());
	}
}

