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

import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.MacroDef;
import org.apache.tools.ant.taskdefs.MacroInstance;

import java.util.ArrayList;
import java.util.List;

public class ForTask extends Task {
    private int from = 0;
    private int to = 0;
    private int step = 1;
    private MacroDef macroDef;
    private String param;
    private List hasIterators = new ArrayList();
    private Target owningTarget;

    public void setOwningTarget(Target owningTarget) {
        this.owningTarget = owningTarget;
    }

    public void setFrom(final String inFrom) {
        from = Integer.parseInt(inFrom);
    }

    public void setTo(final String inTo) {
        to = Integer.parseInt(inTo);
    }

    public void setStep(final String inStep) {
        step = Integer.parseInt(inStep);
    }

    public void setParam(final String inParam) {
        param = inParam;
    }

    public void add(Object value) {
        hasIterators.add(value);
    }

    public Object createSequential() {
        macroDef = new MacroDef();
        macroDef.setProject(getProject());
        return macroDef.createSequential();
    }

    public void execute() {
        MacroDef.Attribute attribute = new MacroDef.Attribute();
        attribute.setName(param);
        macroDef.addConfiguredAttribute(attribute);

        for (int i = from; i <= to; i += step) {
            doSequentialIteration(i);
        }
    }

    private void doSequentialIteration(Integer val) {
        MacroInstance instance = new MacroInstance();
        instance.setProject(getProject());
        instance.setOwningTarget(owningTarget);
        instance.setMacroDef(macroDef);
        instance.setDynamicAttribute(param.toLowerCase(), val.toString());
        instance.execute();
    }

    /*private void executeSequential(final Vector<Task> inTasks) {
        TaskContainer tc = (TaskContainer) getProject().createTask("sequential");        
        for (final Task t : inTasks) {
            tc.addTask(t);
        }

        ((Task)tc).execute();
    }*/

    /*
        private CallTarget createCallTarget() {
            CallTarget call = (CallTarget)getProject().createTask("antcall");
            call.setOwningTarget(getOwningTarget());
            call.init();
            call.setTarget(target);
            call.setInheritAll(true);
            call.setInheritRefs(true);

            for(final Property param : params) {
                Property toSet = call.createParam();
                toSet.setName(param.getName());

                if (param.getValue() != null) {
                    toSet.setValue(param.getValue());
                }
                if (param.getFile() != null) {
                    toSet.setFile(param.getFile());
                }
                if (param.getResource() != null) {
                    toSet.setResource(param.getResource());
                }
                if (param.getPrefix() != null) {
                    toSet.setPrefix(param.getPrefix());
                }
                if (param.getRefid() != null) {
                    toSet.setRefid(param.getRefid());
                }
                if (param.getEnvironment() != null) {
                    toSet.setEnvironment(param.getEnvironment());
                }
                if (param.getClasspath() != null) {
                    toSet.setClasspath(param.getClasspath());
                }
            }

            for(final Ant.Reference ref : references) {
                call.addReference(ref);
            }

            return call;
        }

    */

    /*private interface HasIterator {
        Iterator iterator();
    }

    private static class IteratorIterator implements HasIterator {
        private Iterator iterator;
        public IteratorIterator(Iterator iterator) {
            this.iterator = iterator;
        }
        public Iterator iterator() {
            return this.iterator;
        }
    }

    private static class MapIterator implements HasIterator {
        private Map map;
        public MapIterator(Map map) {
            this.map = map;
        }
        public Iterator iterator() {
            return map.values().iterator();
        }
    }

    private static class ReflectIterator implements HasIterator {
        private Object  obj;
        private Method  method;
        public ReflectIterator(Object obj) {
            this.obj = obj;
            try {
                method = obj.getClass().getMethod(
                    "iterator", new Class[] {});
            } catch (Throwable t) {
                throw new BuildException(
                    "Invalid type " + obj.getClass() + " used in For task, it does"
                    + " not have a public iterator method");
            }
        }

        public Iterator iterator() {
            try {
                return (Iterator) method.invoke(obj, new Object[] {});
            } catch (Throwable t) {
                throw new BuildException(t);
            }
        }
    }*/
}
