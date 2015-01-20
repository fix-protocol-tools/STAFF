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

package com.btobits.automator.ant.utils.task;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.text.NumberFormat;
import java.util.Random;

public class RandomTask extends Task {
    private String min = "0";
    private String max = "1";
    private String property;
    private String type = "int";
    private int fraction = 8;

    public void setFraction(String inFrcation) {
        this.fraction = Integer.parseInt(inFrcation);
    }

    public String getType() {
        return type;
    }

    public void setType(String inType) {
        if (inType.equalsIgnoreCase("int") || inType.equalsIgnoreCase("double")) {
            this.type = inType;
        } else {
            throw new BuildException("Invalid type, must be [int,double].");
        }
    }

    @Override
    public void execute() throws BuildException {
        if (min == null || min.equals(""))
            throw new BuildException("Min property not specified");

        if (max == null || max.equals(""))
            throw new BuildException("Max property not specified");

        if (type.equalsIgnoreCase("int") || StringUtils.isBlank(type)) {

            int minInt = Integer.parseInt(min);
            int maxInt = Integer.parseInt(max);

            if (minInt > maxInt)
                throw new BuildException("Min is bigger than max");

            int randomInt = calculateRandom(minInt, maxInt);
            getProject().setNewProperty(property, String.valueOf(randomInt));
        } else {

            double minD = Double.parseDouble(min);
            double maxD = Double.parseDouble(max);


            if (minD > maxD)
                throw new BuildException("Min is bigger than max");


            double randomDouble = calculateRandom2(minD, maxD);
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(fraction);
            getProject().setNewProperty(property, nf.format(randomDouble));
        }

        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
        }
    }

    protected int calculateRandom(int minInt, int maxInt) {
        return minInt + (int) (Math.random() * (maxInt - minInt + 1));
    }

    protected double calculateRandom2(double minD, double maxD) {
        return minD + Math.random() * (maxD - minD) ;
    }

    public void setMin(String min) {
        this.min = min;
    }

    public void setMax(String max) {
        this.max = max;
    }

    public void setProperty(String property) {
        this.property = property;
    }
}
