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

package org.apache.tools.ant;

import com.btobits.automator.ant.annotation.AutoParamBean;
import com.btobits.automator.ant.annotation.AutoParamBool;
import com.btobits.automator.ant.annotation.AutoParamEnum;
import com.btobits.automator.ant.annotation.AutoParamLong;
import com.btobits.automator.ant.annotation.AutoParamStr;
import com.btobits.automator.ant.annotation.AutoParamTimeValue;
import com.btobits.automator.ant.types.TimeValue;
import org.apache.log4j.Logger;
import org.apache.tools.ant.taskdefs.PreSetDef;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.resources.FileProvider;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.util.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Kirill_Mukhoiarov
 */

public final class IntrospectionHelper {
    private final static Logger log = Logger.getLogger(IntrospectionHelper.class);

    public final static String svnSignature = "$$Rev: 67588 $$ $$Date: 2014-10-28 14:02:42 +0200 (Вт, 28 окт 2014) $$ $$LastChangedBy: Alexander_Sereda $$";


    /**
     * Helper instances we've already created (Class.getName() to IntrospectionHelper).
     */
    private static final Map HELPERS = new Hashtable();

    /**
     * Map from primitive types to wrapper classes for use in
     * createAttributeSetter (Class to Class). Note that char
     * and boolean are in here even though they get special treatment
     * - this way we only need to test for the wrapper class.
     */
    private static final Map<Class, Class> PRIMITIVE_TYPE_MAP = new HashMap<Class, Class>(8);

    // Set up PRIMITIVE_TYPE_MAP

    static {
        Class[] primitives = {Boolean.TYPE, Byte.TYPE, Character.TYPE, Short.TYPE,
                Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE};
        Class[] wrappers = {Boolean.class, Byte.class, Character.class, Short.class,
                Integer.class, Long.class, Float.class, Double.class};
        for (int i = 0; i < primitives.length; i++) {
            PRIMITIVE_TYPE_MAP.put(primitives[i], wrappers[i]);
        }
    }

    private static final int MAX_REPORT_NESTED_TEXT = 20;
    private static final String ELLIPSIS = "...";

    /**
     * Map from attribute names to attribute types
     * (String to Class).
     */
    private final Hashtable attributeTypes = new Hashtable();

    /**
     * Map from attribute names to attribute setter methods
     * (String to AttributeSetter).
     */
    private final Hashtable attributeSetters = new Hashtable();

    /**
     * Map from attribute names to nested types
     * (String to Class).
     */
    private final Hashtable nestedTypes = new Hashtable();

    /**
     * Map from attribute names to methods to create nested types
     * (String to NestedCreator).
     */
    private final Hashtable nestedCreators = new Hashtable();

    /**
     * Vector of methods matching add[Configured](Class) pattern.
     */
    private final List addTypeMethods = new ArrayList();

    /**
     * The method to invoke to add PCDATA.
     */
    private final Method addText;

    /**
     * The class introspected by this instance.
     */
    private final Class bean;

    /**
     * Sole constructor, which is private to ensure that all
     * IntrospectionHelpers are created via {@link #getHelper(Class) getHelper}.
     * Introspects the given class for bean-like methods.
     * Each method is examined in turn, and the following rules are applied:
     * <p/>
     * <ul>
     * <li>If the method is <code>Task.setLocation(Location)</code>,
     * <code>Task.setTaskType(String)</code>
     * or <code>TaskContainer.addTask(Task)</code>, it is ignored. These
     * methods are handled differently elsewhere.
     * <li><code>void addText(String)</code> is recognised as the method for
     * adding PCDATA to a bean.
     * <li><code>void setFoo(Bar)</code> is recognised as a method for
     * setting the value of attribute <code>foo</code>, so long as
     * <code>Bar</code> is non-void and is not an array type.
     * As of Ant 1.8, a Resource or FileProvider parameter overrides a java.io.File parameter;
     * in practice the only effect of this is to allow objects rendered from
     * the 1.8 PropertyHelper implementation to be used as Resource parameters,
     * since Resources set from Strings are resolved as project-relative files
     * to preserve backward compatibility.  Beyond this, non-String
     * parameter types always overload String parameter types; these are
     * the only guarantees made in terms of priority.
     * <li><code>Foo createBar()</code> is recognised as a method for
     * creating a nested element called <code>bar</code> of type
     * <code>Foo</code>, so long as <code>Foo</code> is not a primitive or
     * array type.
     * <li><code>void addConfiguredFoo(Bar)</code> is recognised as a
     * method for storing a pre-configured element called
     * <code>foo</code> and of type <code>Bar</code>, so long as
     * <code>Bar</code> is not an array, primitive or String type.
     * <code>Bar</code> must have an accessible constructor taking no
     * arguments.
     * <li><code>void addFoo(Bar)</code> is recognised as a method for storing
     * an element called <code>foo</code> and of type <code>Bar</code>, so
     * long as <code>Bar</code> is not an array, primitive or String type.
     * <code>Bar</code> must have an accessible constructor taking no
     * arguments. This is distinct from the 'addConfigured' idiom in that
     * the nested element is added to the parent immediately after it is
     * constructed; in practice this means that <code>addFoo(Bar)</code> should
     * do little or nothing with its argument besides storing it for later use.
     * </ul>
     * Note that only one method is retained to create/set/addConfigured/add
     * any element or attribute.
     *
     * @param bean The bean type to introspect.
     *             Must not be <code>null</code>.
     * @see #getHelper(Class)
     */
    private IntrospectionHelper(final Class bean) {
        this.bean = bean;
        Method[] methods = bean.getMethods();
        Method addTextMethod = null;
        for (int i = 0; i < methods.length; i++) {
            final Method m = methods[i];
            final String name = m.getName();
            Class returnType = m.getReturnType();
            Class[] args = m.getParameterTypes();

            // check of add[Configured](Class) pattern
            if (args.length == 1 && java.lang.Void.TYPE.equals(returnType)
                    && ("add".equals(name) || "addConfigured".equals(name))) {
                insertAddTypeMethod(m);
                continue;
            }
            // not really user settable properties on tasks/project components
            if (org.apache.tools.ant.ProjectComponent.class.isAssignableFrom(bean)
                    && args.length == 1 && isHiddenSetMethod(name, args[0])) {
                continue;
            }
            // hide addTask for TaskContainers
            if (isContainer() && args.length == 1 && "addTask".equals(name)
                    && org.apache.tools.ant.Task.class.equals(args[0])) {
                continue;
            }
            if ("addText".equals(name) && java.lang.Void.TYPE.equals(returnType)
                    && args.length == 1 && java.lang.String.class.equals(args[0])) {
                addTextMethod = methods[i];
            } else {
                if (name.startsWith("set") && java.lang.Void.TYPE.equals(returnType)
                        && args.length == 1 && !args[0].isArray()) {
                    String propName = getPropertyName(name, "set");
                    AttributeSetter as = (AttributeSetter) attributeSetters.get(propName);
                    if (as != null) {
                        if (java.lang.String.class.equals(args[0])) {
                            /*
                                Ignore method m, as there is an overloaded
                                form of this method that takes in a
                                non-string argument, which gains higher
                                priority.
                            */
                            continue;
                        }
                        if (java.io.File.class.equals(args[0])) {
                            // Ant Resources/FileProviders override java.io.File
                            if (Resource.class.equals(as.type) || FileProvider.class.equals(as.type)) {
                                continue;
                            }
                        }
                        /*
                            In cases other than those just explicitly covered,
                            we just override that with the new one.
                            This mechanism does not guarantee any specific order
                            in which the methods will be selected: so any code
                            that depends on the order in which "set" methods have
                            been defined, is not guaranteed to be selected in any
                            particular order.
                        */
                    }
                    as = createAttributeSetter(m, args[0], propName);
                    if (as != null) {
                        attributeTypes.put(propName, args[0]);
                        attributeSetters.put(propName, as);
                    }
                } else {
                    if (name.startsWith("create") && !returnType.isArray()
                            && !returnType.isPrimitive() && args.length == 0) {

                        String propName = getPropertyName(name, "create");
                        // Check if a create of this property is already present
                        // add takes preference over create for CB purposes
                        if (nestedCreators.get(propName) == null) {
                            nestedTypes.put(propName, returnType);
                            nestedCreators.put(propName, new CreateNestedCreator(m));
                        }
                    } else {
                        if (name.startsWith("addConfigured")
                                && java.lang.Void.TYPE.equals(returnType) && args.length == 1
                                && !java.lang.String.class.equals(args[0])
                                && !args[0].isArray() && !args[0].isPrimitive()) {
                            try {
                                Constructor constructor = null;
                                try {
                                    constructor = args[0].getConstructor(new Class[]{});
                                } catch (NoSuchMethodException ex) {
                                    constructor = args[0].getConstructor(new Class[]{Project.class});
                                }
                                String propName = getPropertyName(name, "addConfigured");
                                nestedTypes.put(propName, args[0]);
                                nestedCreators.put(propName, new AddNestedCreator(m,
                                        constructor, AddNestedCreator.ADD_CONFIGURED));
                            } catch (NoSuchMethodException nse) {
                                // ignore
                            }
                        } else {
                            if (name.startsWith("add")
                                    && java.lang.Void.TYPE.equals(returnType) && args.length == 1
                                    && !java.lang.String.class.equals(args[0])
                                    && !args[0].isArray() && !args[0].isPrimitive()) {
                                try {
                                    Constructor constructor = null;
                                    try {
                                        constructor = args[0].getConstructor(new Class[]{});
                                    } catch (NoSuchMethodException ex) {
                                        constructor = args[0].getConstructor(new Class[]{Project.class});
                                    }
                                    String propName = getPropertyName(name, "add");
                                    if (nestedTypes.get(propName) != null) {
                                        /*
                                        *  Ignore this method as there is an addConfigured
                                        *  form of this method that has a higher
                                        *  priority
                                        */
                                        continue;
                                    }
                                    nestedTypes.put(propName, args[0]);
                                    nestedCreators.put(propName, new AddNestedCreator(m,
                                            constructor, AddNestedCreator.ADD));
                                } catch (NoSuchMethodException nse) {
                                    // ignore
                                }
                            }
                        }
                    }
                }
            }
        }
        addText = addTextMethod;
    }

    /**
     * Certain set methods are part of the Ant core interface to tasks and
     * therefore not to be considered for introspection
     *
     * @param name the name of the set method
     * @param type the type of the set method's parameter
     * @return true if the given set method is to be hidden.
     */
    private boolean isHiddenSetMethod(String name, Class type) {
        if ("setLocation".equals(name) && org.apache.tools.ant.Location.class.equals(type)) {
            return true;
        }
        if ("setTaskType".equals(name) && java.lang.String.class.equals(type)) {
            return true;
        }
        return false;
    }

    /**
     * Returns a helper for the given class, either from the cache
     * or by creating a new instance.
     *
     * @param c The class for which a helper is required.
     *          Must not be <code>null</code>.
     * @return a helper for the specified class
     */
    public static synchronized IntrospectionHelper getHelper(Class c) {
        return getHelper(null, c);
    }

    /**
     * Returns a helper for the given class, either from the cache
     * or by creating a new instance.
     * <p/>
     * The method will make sure the helper will be cleaned up at the end of
     * the project, and only one instance will be created for each class.
     *
     * @param p the project instance. Can be null, in which case the helper is not cached.
     * @param c The class for which a helper is required.
     *          Must not be <code>null</code>.
     * @return a helper for the specified class
     */
    public static IntrospectionHelper getHelper(Project p, Class c) {
        IntrospectionHelper ih = (IntrospectionHelper) HELPERS.get(c.getName());
        // If a helper cannot be found, or if the helper is for another
        // classloader, create a new IH
        if (ih == null || ih.bean != c) {
            ih = new IntrospectionHelper(c);
            if (p != null) {
                // #30162: do *not* cache this if there is no project, as we
                // cannot guarantee that the cache will be cleared.
                HELPERS.put(c.getName(), ih);
            }
        }
        return ih;
    }

    /**
     * Sets the named attribute in the given element, which is part of the
     * given project.
     *
     * @param p             The project containing the element. This is used when files
     *                      need to be resolved. Must not be <code>null</code>.
     * @param element       The element to set the attribute in. Must not be
     *                      <code>null</code>.
     * @param attributeName The name of the attribute to set. Must not be
     *                      <code>null</code>.
     * @param value         The value to set the attribute to. This may be interpreted
     *                      or converted to the necessary type if the setter method
     *                      doesn't accept an object of the supplied type.
     * @throws BuildException if the introspected class doesn't support
     *                        the given attribute, or if the setting
     *                        method fails.
     */
    public void setAttribute(Project p, Object element, String attributeName, Object value) throws BuildException {
        if (!parseAnnotation(element, attributeName, value)) {
            AttributeSetter as = (AttributeSetter) attributeSetters.get(attributeName.toLowerCase(Locale.ENGLISH));
            if (as == null && value != null) {
                if (element instanceof DynamicAttributeNS) {
                    DynamicAttributeNS dc = (DynamicAttributeNS) element;
                    String uriPlusPrefix = ProjectHelper.extractUriFromComponentName(attributeName);
                    String uri = ProjectHelper.extractUriFromComponentName(uriPlusPrefix);
                    String localName = ProjectHelper.extractNameFromComponentName(attributeName);
                    String qName = "".equals(uri) ? localName : uri + ":" + localName;
                    dc.setDynamicAttribute(uri, localName, qName, value.toString());
                    return;
                }
                if (element instanceof DynamicAttribute) {
                    DynamicAttribute dc = (DynamicAttribute) element;
                    dc.setDynamicAttribute(attributeName.toLowerCase(Locale.ENGLISH), value.toString());
                    return;
                }
                if (attributeName.indexOf(':') >= 0) {
                    return; // Ignore attribute from unknown uri's
                }
                String msg = getElementName(p, element)
                        + " doesn't support the \"" + attributeName + "\" attribute. !!!";
                throw new UnsupportedAttributeException(msg, attributeName);
            }
            try {
                as.setObject(p, element, value);
            } catch (IllegalAccessException ie) {
                // impossible as getMethods should only return public methods
                throw new BuildException(ie);
            } catch (InvocationTargetException ite) {
                throw extractBuildException(ite);
            }
        }
    }

    private enum FieldType {
        STR, LONG, BOOL, DOUBLE, TIME, ENUM
    }

    private class FieldInfo {
        boolean isRequired;
        FieldType type;
        Field field;
        Class<?> enumClass;
        Object defaultValue;
    }

    private final Map<Class, Map<String, FieldInfo>> annotatedFieldsMap = new HashMap<Class, Map<String, FieldInfo>>();

    private void initAnnotatedFieldsMap(final Class inAntTaskContainer) {
        if (!annotatedFieldsMap.containsKey(inAntTaskContainer)) {
            final Map<String, FieldInfo> nextFieldMap = new HashMap<String, FieldInfo>();
            for (final Field field : Arrays.asList(inAntTaskContainer.getFields())) {
                if (Modifier.isStatic(field.getModifiers())
                        || Modifier.isFinal(field.getModifiers())
                        || !Modifier.isPublic(field.getModifiers())) {
                    continue;
                }
                String parameterName = null;
                final FieldInfo fieldInfo = new FieldInfo();
                if (field.isAnnotationPresent(AutoParamStr.class)) {
                    fieldInfo.type = FieldType.STR;
                    final AutoParamStr annotation = field.getAnnotation(AutoParamStr.class);
                    if (annotation.xmlAttr() != null && annotation.xmlAttr().trim().length() > 0) {
                        parameterName = annotation.xmlAttr();
                    } else {
                        parameterName = field.getName();
                    }
                    fieldInfo.defaultValue = annotation.defaultValue();
                    fieldInfo.isRequired = annotation.required();
                    fieldInfo.field = field;
                } else {
                    if (field.isAnnotationPresent(AutoParamLong.class)) {
                        fieldInfo.type = FieldType.LONG;
                        final AutoParamLong annotation = field.getAnnotation(AutoParamLong.class);
                        if (annotation.xmlAttr() != null &&
                                annotation.xmlAttr().trim().length() > 0) {
                            parameterName = annotation.xmlAttr();
                        } else {
                            parameterName = field.getName();
                        }
                        fieldInfo.defaultValue = annotation.defaultValue();
                        fieldInfo.isRequired = annotation.required();
                        fieldInfo.field = field;
                    } else {
                        if (field.isAnnotationPresent(AutoParamBool.class)) {
                            fieldInfo.type = FieldType.BOOL;
                            final AutoParamBool annotation = field.getAnnotation(AutoParamBool.class);
                            if (annotation.xmlAttr() != null &&
                                    annotation.xmlAttr().trim().length() > 0) {
                                parameterName = annotation.xmlAttr();
                            } else {
                                parameterName = field.getName();
                            }
                            fieldInfo.defaultValue = annotation.defaultValue();
                            fieldInfo.isRequired = annotation.required();
                            fieldInfo.field = field;
                        } else {
                            if (field.isAnnotationPresent(AutoParamTimeValue.class)) {
                                fieldInfo.type = FieldType.TIME;
                                final AutoParamTimeValue annotation =
                                        field.getAnnotation(AutoParamTimeValue.class);
                                if (annotation.xmlAttr() != null &&
                                        annotation.xmlAttr().trim().length() > 0) {
                                    parameterName = annotation.xmlAttr();
                                } else {
                                    parameterName = field.getName();
                                }
                                fieldInfo.defaultValue = new TimeValue();
                                fieldInfo.isRequired = annotation.required();
                                fieldInfo.field = field;
                            } else {
                                if (field.isAnnotationPresent(AutoParamEnum.class)) {
                                    fieldInfo.type = FieldType.ENUM;
                                    final AutoParamEnum annotation =
                                            field.getAnnotation(AutoParamEnum.class);
                                    if (annotation.xmlAttr() != null &&
                                            annotation.xmlAttr().trim().length() > 0) {
                                        parameterName = annotation.xmlAttr();
                                    } else {
                                        parameterName = field.getName();
                                    }
                                    fieldInfo.defaultValue = annotation.defaultItemName();
                                    fieldInfo.isRequired = annotation.required();
                                    fieldInfo.field = field;
                                    fieldInfo.enumClass = annotation.enumClass();
                                }
                            }
                        }
                    }
                }

                if (parameterName != null) {
                    nextFieldMap.put(parameterName, fieldInfo);
                }
            }
            if (nextFieldMap.size() > 0) {
                annotatedFieldsMap.put(inAntTaskContainer, nextFieldMap);
            }
        }
    }

    private boolean parseAnnotation(final Object inElement, final String inFieldName, final Object inFieldValue) {
        boolean verdict = false;
        if (inElement != null) {
            final Class elementClass = inElement.getClass();
            verdict = (elementClass.getAnnotation(AutoParamBean.class) != null);
            if (verdict) {
                initAnnotatedFieldsMap(elementClass);
                if (annotatedFieldsMap.containsKey(elementClass)) {
                    final Map<String, FieldInfo> fieldInfoMap = annotatedFieldsMap.get(elementClass);
                    if (fieldInfoMap.containsKey(inFieldName)) {
                        final FieldInfo fieldInfo = fieldInfoMap.get(inFieldName);
                        try {
                            switch (fieldInfo.type) {
                                case STR:
                                    fieldInfo.field.set(inElement, inFieldValue);
                                    break;
                                case LONG:
                                    try {
                                        long valueLong;
                                        valueLong = Long.parseLong((String) inFieldValue);
                                        fieldInfo.field.setLong(inElement, valueLong);
                                    } catch (final Exception e) {
                                        log.warn("AutoParamLong[" + inFieldValue + "] parse error: "
                                                + e.getClass().getSimpleName() + " - " + e.getMessage());
                                        fieldInfo.field.setLong(inElement, (Long) fieldInfo.defaultValue);
                                    }
                                    break;
                                case BOOL:
                                    try {
                                        boolean valueBool = false;
                                        final String inValueStr = ((String) inFieldValue).toLowerCase().trim();
                                        if ("true".equalsIgnoreCase(inValueStr)
                                                || inValueStr.charAt(0) == 'y'
                                                || Boolean.parseBoolean(inValueStr)) {
                                            valueBool = true;
                                        }
                                        fieldInfo.field.set(inElement, valueBool);
                                    } catch (final Exception e) {
                                        log.warn("AutoParamBool[" + inFieldValue + "] parse error: "
                                                + e.getClass().getSimpleName() + " - " + e.getMessage());
                                        fieldInfo.field.setBoolean(inElement, (Boolean) fieldInfo.defaultValue);
                                    }
                                    break;
                                case DOUBLE:
                                    try {
                                        double valueDouble;
                                        valueDouble = Double.parseDouble((String) inFieldValue);
                                        fieldInfo.field.setDouble(inElement, valueDouble);
                                    } catch (final Exception e) {
                                        log.warn("AutoParamDouble[" + inFieldValue + "] parse error: "
                                                + e.getClass().getSimpleName() + " - " + e.getMessage());
                                        fieldInfo.field.setDouble(inElement, (Double) fieldInfo.defaultValue);
                                    }
                                    break;
                                case TIME:
                                    try {
                                        final TimeValue value = new TimeValue((String) inFieldValue);
                                        fieldInfo.field.set(inElement, value);
                                    } catch (final Exception e) {
                                        log.warn("AutoParamTimeValue[" + inFieldValue + "] parse error: "
                                                + e.getClass().getSimpleName() + " - " + e.getMessage());
                                        fieldInfo.field.set(inElement, fieldInfo.defaultValue);
                                    }
                                    break;
                                case ENUM:
                                    final Class<?> enumClass = fieldInfo.enumClass;
                                    if (enumClass.isEnum()) {
                                        Method valueOf = null;

                                        try {
                                            valueOf = enumClass.getMethod("valueOf", String.class);
                                            final Object enumValue =
                                                    valueOf.invoke(enumClass, ((String) inFieldValue).toUpperCase());
                                            fieldInfo.field.set(inElement, enumValue);
                                        } catch (final Exception e) {
                                            log.warn("AutoParamEnum[" + enumClass.getSimpleName() + ":"
                                                    + inFieldValue + "] parse error: "
                                                    + e.getClass().getSimpleName() + " - " + e.getMessage());
                                            if (valueOf != null) {
                                                try {
                                                    fieldInfo.field.set(inElement,
                                                            valueOf.invoke(enumClass,
                                                                    ((String) fieldInfo.defaultValue).toUpperCase()));
                                                } catch (final Exception e1) {
                                                    fieldInfo.field.set(inElement, null);
                                                }
                                            } else {
                                                fieldInfo.field.set(inElement, null);
                                            }
                                        }
                                    }
                                    break;
                            }
                        } catch (final IllegalAccessException e) {
                            log.error("catch generic exception: " + e);
//                            log.error(StackTraceUtil.getStackTrace(e));
                        }
                    }
                }
            }
        }
        return verdict;
    }

    /**
     * Sets the named attribute in the given element, which is part of the
     * given project.
     *
     * @param p             The project containing the element. This is used when files
     *                      need to be resolved. Must not be <code>null</code>.
     * @param element       The element to set the attribute in. Must not be
     *                      <code>null</code>.
     * @param attributeName The name of the attribute to set. Must not be
     *                      <code>null</code>.
     * @param value         The value to set the attribute to. This may be interpreted
     *                      or converted to the necessary type if the setter method
     *                      doesn't just take a string. Must not be <code>null</code>.
     * @throws BuildException if the introspected class doesn't support
     *                        the given attribute, or if the setting
     *                        method fails.
     */
    public void setAttribute(Project p, Object element, String attributeName,
                             String value) throws BuildException {
        setAttribute(p, element, attributeName, (Object) value);
    }

    /**
     * Adds PCDATA to an element, using the element's
     * <code>void addText(String)</code> method, if it has one. If no
     * such method is present, a BuildException is thrown if the
     * given text contains non-whitespace.
     *
     * @param project The project which the element is part of.
     *                Must not be <code>null</code>.
     * @param element The element to add the text to.
     *                Must not be <code>null</code>.
     * @param text    The text to add.
     *                Must not be <code>null</code>.
     * @throws BuildException if non-whitespace text is provided and no
     *                        method is available to handle it, or if
     *                        the handling method fails.
     */
    public void addText(Project project, Object element, String text)
            throws BuildException {
        if (addText == null) {
            text = text.trim();
            // Element doesn't handle text content
            if (text.length() == 0) {
                // Only whitespace - ignore
                return;
            }
            // Not whitespace - fail
            throw new BuildException(project.getElementName(element)
                    + " doesn't support nested text data (\"" + condenseText(text) + "\").");
        }
        try {
            addText.invoke(element, new Object[]{text});
        } catch (IllegalAccessException ie) {
            // impossible as getMethods should only return public methods
            throw new BuildException(ie);
        } catch (InvocationTargetException ite) {
            throw extractBuildException(ite);
        }
    }

    /**
     * part of the error message created by {@link #throwNotSupported
     * throwNotSupported}.
     *
     * @since Ant 1.8.0
     */
    protected static final String NOT_SUPPORTED_CHILD_PREFIX =
            " doesn't support the nested \"";

    /**
     * part of the error message created by {@link #throwNotSupported
     * throwNotSupported}.
     *
     * @since Ant 1.8.0
     */
    protected static final String NOT_SUPPORTED_CHILD_POSTFIX = "\" element.";

    /**
     * Utility method to throw a NotSupported exception
     *
     * @param project     the Project instance.
     * @param parent      the object which doesn't support a requested element
     * @param elementName the name of the Element which is trying to be created.
     */
    public void throwNotSupported(Project project, Object parent, String elementName) {
        String msg = project.getElementName(parent)
                + NOT_SUPPORTED_CHILD_PREFIX + elementName
                + NOT_SUPPORTED_CHILD_POSTFIX;
        throw new UnsupportedElementException(msg, elementName);
    }

    /**
     * Get the specific NestedCreator for a given project/parent/element combination
     *
     * @param project     ant project
     * @param parentUri   URI of the parent.
     * @param parent      the parent class
     * @param elementName element to work with. This can contain
     *                    a URI,localname tuple of of the form uri:localname
     * @param child       the bit of XML to work with
     * @return a nested creator that can handle the child elements.
     * @throws BuildException if the parent does not support child elements of that name
     */
    private NestedCreator getNestedCreator(
            Project project, String parentUri, Object parent,
            String elementName, UnknownElement child) throws BuildException {

        String uri = ProjectHelper.extractUriFromComponentName(elementName);
        String name = ProjectHelper.extractNameFromComponentName(elementName);

        if (uri.equals(ProjectHelper.ANT_CORE_URI)) {
            uri = "";
        }
        if (parentUri.equals(ProjectHelper.ANT_CORE_URI)) {
            parentUri = "";
        }
        NestedCreator nc = null;
        if (uri.equals(parentUri) || uri.length() == 0) {
            nc = (NestedCreator) nestedCreators.get(name.toLowerCase(Locale.ENGLISH));
        }
        if (nc == null) {
            nc = createAddTypeCreator(project, parent, elementName);
        }
        if (nc == null &&
                (parent instanceof DynamicElementNS
                        || parent instanceof DynamicElement)
                ) {
            String qName = child == null ? name : child.getQName();
            final Object nestedElement =
                    createDynamicElement(parent,
                            child == null ? "" : child.getNamespace(),
                            name, qName);
            if (nestedElement != null) {
                nc = new NestedCreator(null) {
                    Object create(Project project, Object parent, Object ignore) {
                        return nestedElement;
                    }
                };
            }
        }
        if (nc == null) {
            throwNotSupported(project, parent, elementName);
        }
        return nc;
    }

    /**
     * Invokes the "correct" createDynamicElement method on parent in
     * order to obtain a child element by name.
     *
     * @since Ant 1.8.0.
     */
    private Object createDynamicElement(Object parent, String ns,
                                        String localName, String qName) {
        Object nestedElement = null;
        if (parent instanceof DynamicElementNS) {
            DynamicElementNS dc = (DynamicElementNS) parent;
            nestedElement = dc.createDynamicElement(ns, localName, qName);
        }
        if (nestedElement == null && parent instanceof DynamicElement) {
            DynamicElement dc = (DynamicElement) parent;
            nestedElement =
                    dc.createDynamicElement(localName.toLowerCase(Locale.ENGLISH));
        }
        return nestedElement;
    }

    /**
     * Creates a named nested element. Depending on the results of the
     * initial introspection, either a method in the given parent instance
     * or a simple no-arg constructor is used to create an instance of the
     * specified element type.
     *
     * @param project     Project to which the parent object belongs.
     *                    Must not be <code>null</code>. If the resulting
     *                    object is an instance of ProjectComponent, its
     *                    Project reference is set to this parameter value.
     * @param parent      Parent object used to create the instance.
     *                    Must not be <code>null</code>.
     * @param elementName Name of the element to create an instance of.
     *                    Must not be <code>null</code>.
     * @return an instance of the specified element type
     * @throws BuildException if no method is available to create the
     *                        element instance, or if the creating method fails.
     * @deprecated since 1.6.x.
     *             This is not a namespace aware method.
     */
    public Object createElement(Project project, Object parent, String elementName)
            throws BuildException {
        NestedCreator nc = getNestedCreator(project, "", parent, elementName, null);
        try {
            Object nestedElement = nc.create(project, parent, null);
            if (project != null) {
                project.setProjectReference(nestedElement);
            }
            return nestedElement;
        } catch (IllegalAccessException ie) {
            // impossible as getMethods should only return public methods
            throw new BuildException(ie);
        } catch (InstantiationException ine) {
            // impossible as getMethods should only return public methods
            throw new BuildException(ine);
        } catch (InvocationTargetException ite) {
            throw extractBuildException(ite);
        }
    }

    /**
     * returns an object that creates and stores an object
     * for an element of a parent.
     *
     * @param project     Project to which the parent object belongs.
     * @param parentUri   The namespace uri of the parent object.
     * @param parent      Parent object used to create the creator object to
     *                    create and store and instance of a subelement.
     * @param elementName Name of the element to create an instance of.
     * @param ue          The unknown element associated with the element.
     * @return a creator object to create and store the element instance.
     */
    public Creator getElementCreator(
            Project project, String parentUri, Object parent, String elementName, UnknownElement ue) {
        NestedCreator nc = getNestedCreator(project, parentUri, parent, elementName, ue);
        return new Creator(project, parent, nc);
    }

    /**
     * Indicates whether the introspected class is a dynamic one,
     * supporting arbitrary nested elements and/or attributes.
     *
     * @return <code>true<code> if the introspected class is dynamic;
     *         <code>false<code> otherwise.
     * @see DynamicElement
     * @see DynamicElementNS
     * @since Ant 1.6.3
     */
    public boolean isDynamic() {
        return DynamicElement.class.isAssignableFrom(bean)
                || DynamicElementNS.class.isAssignableFrom(bean);
    }

    /**
     * Indicates whether the introspected class is a task container,
     * supporting arbitrary nested tasks/types.
     *
     * @return <code>true<code> if the introspected class is a container;
     *         <code>false<code> otherwise.
     * @see TaskContainer
     * @since Ant 1.6.3
     */
    public boolean isContainer() {
        return TaskContainer.class.isAssignableFrom(bean);
    }

    /**
     * Indicates if this element supports a nested element of the
     * given name.
     *
     * @param elementName the name of the nested element being checked
     * @return true if the given nested element is supported
     */
    public boolean supportsNestedElement(String elementName) {
        return supportsNestedElement("", elementName);
    }

    /**
     * Indicate if this element supports a nested element of the
     * given name.
     * <p/>
     * <p>Note that this method will always return true if the
     * introspected class is {@link #isDynamic dynamic} or contains a
     * method named "add" with void return type and a single argument.
     * To ge a more thorough answer, use the four-arg version of this
     * method instead.</p>
     *
     * @param parentUri   the uri of the parent
     * @param elementName the name of the nested element being checked
     * @return true if the given nested element is supported
     */
    public boolean supportsNestedElement(String parentUri, String elementName) {
        if (isDynamic() || addTypeMethods.size() > 0) {
            return true;
        }
        return supportsReflectElement(parentUri, elementName);
    }

    /**
     * Indicate if this element supports a nested element of the
     * given name.
     * <p/>
     * <p>Note that this method will always return true if the
     * introspected class is {@link #isDynamic dynamic}, so be
     * prepared to catch an exception about unsupported children when
     * calling {@link #getElementCreator getElementCreator}.</p>
     *
     * @param parentUri   the uri of the parent
     * @param elementName the name of the nested element being checked
     * @param project     currently executing project instance
     * @param parent      the parent element
     * @return true if the given nested element is supported
     * @since Ant 1.8.0.
     */
    public boolean supportsNestedElement(String parentUri, String elementName,
                                         Project project, Object parent) {
        if (addTypeMethods.size() > 0
                && createAddTypeCreator(project, parent, elementName) != null) {
            return true;
        }
        return isDynamic() || supportsReflectElement(parentUri, elementName);
    }

    /**
     * Check if this element supports a nested element from reflection.
     *
     * @param parentUri   the uri of the parent
     * @param elementName the name of the nested element being checked
     * @return true if the given nested element is supported
     * @since Ant 1.8.0
     */
    public boolean supportsReflectElement(
            String parentUri, String elementName) {
        String name = ProjectHelper.extractNameFromComponentName(elementName);
        if (!nestedCreators.containsKey(name.toLowerCase(Locale.ENGLISH))) {
            return false;
        }
        String uri = ProjectHelper.extractUriFromComponentName(elementName);
        if (uri.equals(ProjectHelper.ANT_CORE_URI)) {
            uri = "";
        }
        if ("".equals(uri)) {
            return true;
        }
        if (parentUri.equals(ProjectHelper.ANT_CORE_URI)) {
            parentUri = "";
        }
        return uri.equals(parentUri);
    }

    /**
     * Stores a named nested element using a storage method determined
     * by the initial introspection. If no appropriate storage method
     * is available, this method returns immediately.
     *
     * @param project     Ignored in this implementation.
     *                    May be <code>null</code>.
     * @param parent      Parent instance to store the child in.
     *                    Must not be <code>null</code>.
     * @param child       Child instance to store in the parent.
     *                    Should not be <code>null</code>.
     * @param elementName Name of the child element to store.
     *                    May be <code>null</code>, in which case
     *                    this method returns immediately.
     * @throws BuildException if the storage method fails.
     */
    public void storeElement(Project project, Object parent, Object child,
                             String elementName) throws BuildException {
        if (elementName == null) {
            return;
        }
        NestedCreator ns = (NestedCreator) nestedCreators.get(elementName.toLowerCase(Locale.ENGLISH));
        if (ns == null) {
            return;
        }
        try {
            ns.store(parent, child);
        } catch (IllegalAccessException ie) {
            // impossible as getMethods should only return public methods
            throw new BuildException(ie);
        } catch (InstantiationException ine) {
            // impossible as getMethods should only return public methods
            throw new BuildException(ine);
        } catch (InvocationTargetException ite) {
            throw extractBuildException(ite);
        }
    }

    /**
     * Helper method to extract the inner fault from an {@link InvocationTargetException}, and turn
     * it into a BuildException. If it is already a BuildException, it is type cast and returned; if
     * not a new BuildException is created containing the child as nested text.
     *
     * @param ite
     * @return the nested exception
     */
    private static BuildException extractBuildException(InvocationTargetException ite) {
        Throwable t = ite.getTargetException();
        if (t instanceof BuildException) {
            return (BuildException) t;
        }
        return new BuildException(t);
    }

    /**
     * Returns the type of a named nested element.
     *
     * @param elementName The name of the element to find the type of.
     *                    Must not be <code>null</code>.
     * @return the type of the nested element with the specified name.
     *         This will never be <code>null</code>.
     * @throws BuildException if the introspected class does not
     *                        support the named nested element.
     */
    public Class getElementType(String elementName) throws BuildException {
        Class nt = (Class) nestedTypes.get(elementName);
        if (nt == null) {
            throw new UnsupportedElementException("Class "
                    + bean.getName() + " doesn't support the nested \""
                    + elementName + "\" element.", elementName);
        }
        return nt;
    }

    /**
     * Returns the type of a named attribute.
     *
     * @param attributeName The name of the attribute to find the type of.
     *                      Must not be <code>null</code>.
     * @return the type of the attribute with the specified name.
     *         This will never be <code>null</code>.
     * @throws BuildException if the introspected class does not
     *                        support the named attribute.
     */
    public Class getAttributeType(String attributeName) throws BuildException {
        Class at = (Class) attributeTypes.get(attributeName);
        if (at == null) {
            throw new UnsupportedAttributeException("Class "
                    + bean.getName() + " doesn't support the \""
                    + attributeName + "\" attribute.", attributeName);
        }
        return at;
    }

    /**
     * Returns the addText method when the introspected
     * class supports nested text.
     *
     * @return the method on this introspected class that adds nested text.
     *         Cannot be <code>null</code>.
     * @throws BuildException if the introspected class does not
     *                        support the nested text.
     * @since Ant 1.6.3
     */
    public Method getAddTextMethod() throws BuildException {
        if (!supportsCharacters()) {
            throw new BuildException("Class " + bean.getName()
                    + " doesn't support nested text data.");
        }
        return addText;
    }

    /**
     * Returns the adder or creator method of a named nested element.
     *
     * @param elementName The name of the attribute to find the setter
     *                    method of. Must not be <code>null</code>.
     * @return the method on this introspected class that adds or creates this
     *         nested element. Can be <code>null</code> when the introspected
     *         class is a dynamic configurator!
     * @throws BuildException if the introspected class does not
     *                        support the named nested element.
     * @since Ant 1.6.3
     */
    public Method getElementMethod(String elementName) throws BuildException {
        Object creator = nestedCreators.get(elementName);
        if (creator == null) {
            throw new UnsupportedElementException("Class "
                    + bean.getName() + " doesn't support the nested \""
                    + elementName + "\" element.", elementName);
        }
        return ((NestedCreator) creator).method;
    }

    /**
     * Returns the setter method of a named attribute.
     *
     * @param attributeName The name of the attribute to find the setter
     *                      method of. Must not be <code>null</code>.
     * @return the method on this introspected class that sets this attribute.
     *         This will never be <code>null</code>.
     * @throws BuildException if the introspected class does not
     *                        support the named attribute.
     * @since Ant 1.6.3
     */
    public Method getAttributeMethod(String attributeName) throws BuildException {
        Object setter = attributeSetters.get(attributeName);
        if (setter == null) {
            throw new UnsupportedAttributeException("Class "
                    + bean.getName() + " doesn't support the \""
                    + attributeName + "\" attribute.", attributeName);
        }
        return ((AttributeSetter) setter).method;
    }

    /**
     * Returns whether or not the introspected class supports PCDATA.
     *
     * @return whether or not the introspected class supports PCDATA.
     */
    public boolean supportsCharacters() {
        return addText != null;
    }

    /**
     * Returns an enumeration of the names of the attributes supported by the introspected class.
     *
     * @return an enumeration of the names of the attributes supported by the introspected class.
     * @see #getAttributeMap
     */
    public Enumeration getAttributes() {
        return attributeSetters.keys();
    }

    /**
     * Returns a read-only map of attributes supported by the introspected class.
     *
     * @return an attribute name to attribute <code>Class</code>
     *         unmodifiable map. Can be empty, but never <code>null</code>.
     * @since Ant 1.6.3
     */
    public Map getAttributeMap() {
        return attributeTypes.isEmpty()
                ? Collections.EMPTY_MAP : Collections.unmodifiableMap(attributeTypes);
    }

    /**
     * Returns an enumeration of the names of the nested elements supported
     * by the introspected class.
     *
     * @return an enumeration of the names of the nested elements supported
     *         by the introspected class.
     * @see #getNestedElementMap
     */
    public Enumeration getNestedElements() {
        return nestedTypes.keys();
    }

    /**
     * Returns a read-only map of nested elements supported
     * by the introspected class.
     *
     * @return a nested-element name to nested-element <code>Class</code>
     *         unmodifiable map. Can be empty, but never <code>null</code>.
     * @since Ant 1.6.3
     */
    public Map getNestedElementMap() {
        return nestedTypes.isEmpty()
                ? Collections.EMPTY_MAP : Collections.unmodifiableMap(nestedTypes);
    }

    /**
     * Returns a read-only list of extension points supported
     * by the introspected class.
     * <p/>
     * A task/type or nested element with void methods named <code>add()<code>
     * or <code>addConfigured()</code>, taking a single class or interface
     * argument, supports extensions point. This method returns the list of
     * all these <em>void add[Configured](type)</em> methods.
     *
     * @return a list of void, single argument add() or addConfigured()
     *         <code>Method<code>s of all supported extension points.
     *         These methods are sorted such that if the argument type of a
     *         method derives from another type also an argument of a method
     *         of this list, the method with the most derived argument will
     *         always appear first. Can be empty, but never <code>null</code>.
     * @since Ant 1.6.3
     */
    public List getExtensionPoints() {
        return addTypeMethods.isEmpty()
                ? Collections.EMPTY_LIST : Collections.unmodifiableList(addTypeMethods);
    }

    /**
     * Creates an implementation of AttributeSetter for the given
     * attribute type. Conversions (where necessary) are automatically
     * made for the following types:
     * <ul>
     * <li>String (left as it is)
     * <li>Character/char (first character is used)
     * <li>Boolean/boolean
     * ({@link Project#toBoolean(String) Project.toBoolean(String)} is used)
     * <li>Class (Class.forName is used)
     * <li>File (resolved relative to the appropriate project)
     * <li>Path (resolve relative to the appropriate project)
     * <li>Resource (resolved as a FileResource relative to the appropriate project)
     * <li>FileProvider (resolved as a FileResource relative to the appropriate project)
     * <li>EnumeratedAttribute (uses its own
     * {@link EnumeratedAttribute#setValue(String) setValue} method)
     * <li>Other primitive types (wrapper classes are used with constructors
     * taking String)
     * </ul>
     * <p/>
     * If none of the above covers the given parameters, a constructor for the
     * appropriate class taking a String parameter is used if it is available.
     *
     * @param m        The method to invoke on the bean when the setter is invoked.
     *                 Must not be <code>null</code>.
     * @param arg      The type of the single argument of the bean's method.
     *                 Must not be <code>null</code>.
     * @param attrName the name of the attribute for which the setter is being
     *                 created.
     * @return an appropriate AttributeSetter instance, or <code>null</code>
     *         if no appropriate conversion is available.
     */
    private AttributeSetter createAttributeSetter(final Method m,
                                                  Class arg,
                                                  final String attrName) {
        // use wrappers for primitive classes, e.g. int and
        // Integer are treated identically
        final Class reflectedArg = PRIMITIVE_TYPE_MAP.containsKey(arg)
                ? (Class) PRIMITIVE_TYPE_MAP.get(arg) : arg;

        // Object.class - it gets handled differently by AttributeSetter
        if (java.lang.Object.class == reflectedArg) {
            return new AttributeSetter(m, arg) {
                public void set(Project p, Object parent, String value)
                        throws InvocationTargetException,
                        IllegalAccessException {
                    throw new BuildException(
                            "Internal ant problem - this should not get called");
                }
            };
        }
        // simplest case - setAttribute expects String
        if (java.lang.String.class.equals(reflectedArg)) {
            return new AttributeSetter(m, arg) {
                public void set(Project p, Object parent, String value)
                        throws InvocationTargetException, IllegalAccessException {
                    m.invoke(parent, (Object[]) new String[]{value});
                }
            };
        }
        // char and Character get special treatment - take the first character
        if (java.lang.Character.class.equals(reflectedArg)) {
            return new AttributeSetter(m, arg) {
                public void set(Project p, Object parent, String value)
                        throws InvocationTargetException, IllegalAccessException {
                    if (value.length() == 0) {
                        throw new BuildException("The value \"\" is not a "
                                + "legal value for attribute \"" + attrName + "\"");
                    }
                    m.invoke(parent, (Object[]) new Character[]{new Character(value.charAt(0))});
                }
            };
        }
        // boolean and Boolean get special treatment because we have a nice method in Project
        if (java.lang.Boolean.class.equals(reflectedArg)) {
            return new AttributeSetter(m, arg) {
                public void set(Project p, Object parent, String value)
                        throws InvocationTargetException, IllegalAccessException {
                    m.invoke(parent, (Object[]) new Boolean[]{
                            Project.toBoolean(value) ? Boolean.TRUE : Boolean.FALSE});
                }
            };
        }
        // Class doesn't have a String constructor but a decent factory method
        if (java.lang.Class.class.equals(reflectedArg)) {
            return new AttributeSetter(m, arg) {
                public void set(Project p, Object parent, String value)
                        throws InvocationTargetException, IllegalAccessException, BuildException {
                    try {
                        m.invoke(parent, new Object[]{Class.forName(value)});
                    } catch (ClassNotFoundException ce) {
                        throw new BuildException(ce);
                    }
                }
            };
        }
        // resolve relative paths through Project
        if (java.io.File.class.equals(reflectedArg)) {
            return new AttributeSetter(m, arg) {
                public void set(Project p, Object parent, String value)
                        throws InvocationTargetException, IllegalAccessException {
                    m.invoke(parent, new Object[]{p.resolveFile(value)});
                }
            };
        }
        // resolve Resources/FileProviders as FileResources relative to Project:
        if (Resource.class.equals(reflectedArg) || FileProvider.class.equals(reflectedArg)) {
            return new AttributeSetter(m, arg) {
                void set(Project p, Object parent, String value) throws InvocationTargetException,
                        IllegalAccessException, BuildException {
                    m.invoke(parent, new Object[]{new FileResource(p, p.resolveFile(value))});
                }

                ;
            };
        }
        // EnumeratedAttributes have their own helper class
        if (EnumeratedAttribute.class.isAssignableFrom(reflectedArg)) {
            return new AttributeSetter(m, arg) {
                public void set(Project p, Object parent, String value)
                        throws InvocationTargetException, IllegalAccessException, BuildException {
                    try {
                        EnumeratedAttribute ea = (EnumeratedAttribute) reflectedArg.newInstance();
                        ea.setValue(value);
                        m.invoke(parent, new Object[]{ea});
                    } catch (InstantiationException ie) {
                        throw new BuildException(ie);
                    }
                }
            };
        }

        AttributeSetter setter = getEnumSetter(reflectedArg, m, arg);
        if (setter != null) {
            return setter;
        }

        if (java.lang.Long.class.equals(reflectedArg)) {
            return new AttributeSetter(m, arg) {
                public void set(Project p, Object parent, String value)
                        throws InvocationTargetException, IllegalAccessException, BuildException {
                    try {
                        m.invoke(parent, new Object[]{
                                new Long(StringUtils.parseHumanSizes(value))});
                    } catch (NumberFormatException e) {
                        throw new BuildException("Can't assign non-numeric"
                                + " value '" + value + "' to"
                                + " attribute " + attrName);
                    } catch (InvocationTargetException e) {
                        throw e;
                    } catch (IllegalAccessException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new BuildException(e);
                    }
                }
            };
        }
        // worst case. look for a public String constructor and use it
        // also supports new Whatever(Project, String) as for Path or Reference
        // This is used (deliberately) for all primitives/wrappers other than
        // char, boolean, and long.
        boolean includeProject;
        Constructor c;
        try {
            // First try with Project.
            c = reflectedArg.getConstructor(new Class[]{Project.class, String.class});
            includeProject = true;
        } catch (NoSuchMethodException nme) {
            // OK, try without.
            try {
                c = reflectedArg.getConstructor(new Class[]{String.class});
                includeProject = false;
            } catch (NoSuchMethodException nme2) {
                // Well, no matching constructor.
                return null;
            }
        }
        final boolean finalIncludeProject = includeProject;
        final Constructor finalConstructor = c;

        return new AttributeSetter(m, arg) {
            public void set(Project p, Object parent, String value)
                    throws InvocationTargetException, IllegalAccessException, BuildException {
                try {
                    Object[] args = finalIncludeProject
                            ? new Object[]{p, value} : new Object[]{value};

                    Object attribute = finalConstructor.newInstance(args);
                    if (p != null) {
                        p.setProjectReference(attribute);
                    }
                    m.invoke(parent, new Object[]{attribute});
                } catch (InvocationTargetException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof IllegalArgumentException) {
                        throw new BuildException("Can't assign value '" + value
                                + "' to attribute " + attrName
                                + ", reason: "
                                + cause.getClass()
                                + " with message '"
                                + cause.getMessage() + "'");
                    }
                    throw e;
                } catch (InstantiationException ie) {
                    throw new BuildException(ie);
                }
            }
        };
    }

    private AttributeSetter getEnumSetter(
            final Class reflectedArg, final Method m, Class arg) {
        Class enumClass = null;
        try {
            enumClass = Class.forName("java.lang.Enum");
        } catch (ClassNotFoundException e) {
            //ignore
        }
        if (enumClass != null && enumClass.isAssignableFrom(reflectedArg)) {
            return new AttributeSetter(m, arg) {
                public void set(Project p, Object parent, String value)
                        throws InvocationTargetException, IllegalAccessException,
                        BuildException {
                    try {
                        m.invoke(
                                parent, new Object[]{
                                        reflectedArg.getMethod(
                                                "valueOf", new Class[]{String.class}).
                                                invoke(null, new Object[]{value})});
                    } catch (InvocationTargetException x) {
                        //there is specific logic here for the value
                        // being out of the allowed set of enumerations.
                        if (x.getTargetException() instanceof IllegalArgumentException) {
                            throw new BuildException(
                                    "'" + value + "' is not a permitted value for "
                                            + reflectedArg.getName());
                        }
                        //only if the exception is not an IllegalArgument do we
                        // request the
                        //BuildException via extractBuildException():
                        throw extractBuildException(x);
                    } catch (Exception x) {
                        //any other failure of invoke() to work.
                        throw new BuildException(x);
                    }
                }
            };
        }
        return null;
    }

    /**
     * Returns a description of the type of the given element in
     * relation to a given project. This is used for logging purposes
     * when the element is asked to cope with some data it has no way of handling.
     *
     * @param project The project the element is defined in. Must not be <code>null</code>.
     * @param element The element to describe. Must not be <code>null</code>.
     * @return a description of the element type
     */
    private String getElementName(Project project, Object element) {
        return project.getElementName(element);
    }

    /**
     * Extracts the name of a property from a method name by subtracting
     * a given prefix and converting into lower case. It is up to calling
     * code to make sure the method name does actually begin with the
     * specified prefix - no checking is done in this method.
     *
     * @param methodName The name of the method in question. Must not be <code>null</code>.
     * @param prefix     The prefix to remove. Must not be <code>null</code>.
     * @return the lower-cased method name with the prefix removed.
     */
    private static String getPropertyName(String methodName, String prefix) {
        return methodName.substring(prefix.length()).toLowerCase(Locale.ENGLISH);
    }

    /**
     * creator - allows use of create/store external
     * to IntrospectionHelper.
     * The class is final as it has a private constructor.
     */
    public static final class Creator {
        private NestedCreator nestedCreator;
        private Object parent;
        private Project project;
        private Object nestedObject;
        private String polyType;

        /**
         * Creates a new Creator instance.
         * This object is given to the UnknownElement to create
         * objects for sub-elements. UnknownElement calls
         * create to create an object, the object then gets
         * configured and then UnknownElement calls store.
         * SetPolyType may be used to override the type used
         * to create the object with. SetPolyType gets called before create.
         *
         * @param project       the current project
         * @param parent        the parent object to create the object in
         * @param nestedCreator the nested creator object to use
         */
        private Creator(Project project, Object parent, NestedCreator nestedCreator) {
            this.project = project;
            this.parent = parent;
            this.nestedCreator = nestedCreator;
        }

        /**
         * Used to override the class used to create the object.
         *
         * @param polyType a ant component type name
         */
        public void setPolyType(String polyType) {
            this.polyType = polyType;
        }

        /**
         * Create an object using this creator, which is determined by introspection.
         *
         * @return the created object
         */
        public Object create() {
            if (polyType != null) {
                if (!nestedCreator.isPolyMorphic()) {
                    throw new BuildException(
                            "Not allowed to use the polymorphic form for this element");
                }
                ComponentHelper helper = ComponentHelper.getComponentHelper(project);
                nestedObject = helper.createComponent(polyType);
                if (nestedObject == null) {
                    throw new BuildException("Unable to create object of type " + polyType);
                }
            }
            try {
                nestedObject = nestedCreator.create(project, parent, nestedObject);
                if (project != null) {
                    project.setProjectReference(nestedObject);
                }
                return nestedObject;
            } catch (IllegalAccessException ex) {
                throw new BuildException(ex);
            } catch (InstantiationException ex) {
                throw new BuildException(ex);
            } catch (IllegalArgumentException ex) {
                if (polyType == null) {
                    throw ex;
                }
                throw new BuildException("Invalid type used " + polyType);
            } catch (InvocationTargetException ex) {
                throw extractBuildException(ex);
            }
        }

        /**
         * @return the real object (used currently only for presetdef).
         */
        public Object getRealObject() {
            return nestedCreator.getRealObject();
        }

        /**
         * Stores the nested element object using a storage method determined by introspection.
         */
        public void store() {
            try {
                nestedCreator.store(parent, nestedObject);
            } catch (IllegalAccessException ex) {
                throw new BuildException(ex);
            } catch (InstantiationException ex) {
                throw new BuildException(ex);
            } catch (IllegalArgumentException ex) {
                if (polyType == null) {
                    throw ex;
                }
                throw new BuildException("Invalid type used " + polyType);
            } catch (InvocationTargetException ex) {
                throw extractBuildException(ex);
            }
        }
    }

    /**
     * Internal interface used to create nested elements. Not documented
     * in detail for reasons of source code readability.
     */
    private abstract static class NestedCreator {
        private Method method; // the method called to add/create the nested element

        protected NestedCreator(Method m) {
            method = m;
        }

        Method getMethod() {
            return method;
        }

        boolean isPolyMorphic() {
            return false;
        }

        Object getRealObject() {
            return null;
        }

        abstract Object create(Project project, Object parent, Object child)
                throws InvocationTargetException, IllegalAccessException, InstantiationException;

        void store(Object parent, Object child)
                throws InvocationTargetException, IllegalAccessException, InstantiationException {
            // DO NOTHING
        }
    }

    private static class CreateNestedCreator extends NestedCreator {
        CreateNestedCreator(Method m) {
            super(m);
        }

        Object create(Project project, Object parent, Object ignore)
                throws InvocationTargetException, IllegalAccessException {
            return getMethod().invoke(parent, new Object[]{});
        }
    }

    /**
     * Version to use for addXXX and addConfiguredXXX
     */
    private static class AddNestedCreator extends NestedCreator {

        static final int ADD = 1;
        static final int ADD_CONFIGURED = 2;

        private Constructor constructor;
        private int behavior; // ADD or ADD_CONFIGURED

        AddNestedCreator(Method m, Constructor c, int behavior) {
            super(m);
            this.constructor = c;
            this.behavior = behavior;
        }

        boolean isPolyMorphic() {
            return true;
        }

        Object create(Project project, Object parent, Object child)
                throws InvocationTargetException, IllegalAccessException, InstantiationException {
            if (child == null) {
                child = constructor.newInstance(
                        constructor.getParameterTypes().length == 0
                                ? new Object[]{} : new Object[]{project});
            }
            if (child instanceof PreSetDef.PreSetDefinition) {
                child = ((PreSetDef.PreSetDefinition) child).createObject(project);
            }
            if (behavior == ADD) {
                istore(parent, child);
            }
            return child;
        }

        void store(Object parent, Object child)
                throws InvocationTargetException, IllegalAccessException, InstantiationException {
            if (behavior == ADD_CONFIGURED) {
                istore(parent, child);
            }
        }

        private void istore(Object parent, Object child)
                throws InvocationTargetException, IllegalAccessException, InstantiationException {
            getMethod().invoke(parent, new Object[]{child});
        }
    }

    /**
     * Internal interface used to setting element attributes. Not documented
     * in detail for reasons of source code readability.
     */
    private abstract static class AttributeSetter {
        private Method method; // the method called to set the attribute
        private Class type;

        protected AttributeSetter(Method m, Class type) {
            method = m;
            this.type = type;
        }

        void setObject(Project p, Object parent, Object value)
                throws InvocationTargetException, IllegalAccessException, BuildException {
            if (type != null) {
                Class useType = type;
                if (type.isPrimitive()) {
                    if (value == null) {
                        throw new BuildException(
                                "Attempt to set primitive "
                                        + getPropertyName(method.getName(), "set")
                                        + " to null on " + parent);
                    }
                    useType = (Class) PRIMITIVE_TYPE_MAP.get(type);
                }
                if (value == null || useType.isInstance(value)) {
                    method.invoke(parent, new Object[]{value});
                    return;
                }
            }
            set(p, parent, value.toString());
        }

        abstract void set(Project p, Object parent, String value)
                throws InvocationTargetException, IllegalAccessException, BuildException;
    }

    /**
     * Clears the static cache of on build finished.
     */
    public static void clearCache() {
        HELPERS.clear();
    }

    /**
     * Create a NestedCreator for the given element.
     *
     * @param project     owning project
     * @param parent      Parent object used to create the instance.
     * @param elementName name of the element
     * @return a nested creator, or null if there is no component of the given name, or it
     *         has no matching add type methods
     * @throws BuildException
     */
    private NestedCreator createAddTypeCreator(
            Project project, Object parent, String elementName) throws BuildException {
        if (addTypeMethods.size() == 0) {
            return null;
        }
        ComponentHelper helper = ComponentHelper.getComponentHelper(project);

        MethodAndObject restricted = createRestricted(
                helper, elementName, addTypeMethods);
        MethodAndObject topLevel = createTopLevel(
                helper, elementName, addTypeMethods);

        if (restricted == null && topLevel == null) {
            return null;
        }

        if (restricted != null && topLevel != null) {
            throw new BuildException(
                    "ambiguous: type and component definitions for "
                            + elementName);
        }

        MethodAndObject methodAndObject
                = restricted != null ? restricted : topLevel;

        Object rObject = methodAndObject.object;
        if (methodAndObject.object instanceof PreSetDef.PreSetDefinition) {
            rObject = ((PreSetDef.PreSetDefinition) methodAndObject.object)
                    .createObject(project);
        }
        final Object nestedObject = methodAndObject.object;
        final Object realObject = rObject;

        return new NestedCreator(methodAndObject.method) {
            Object create(Project project, Object parent, Object ignore)
                    throws InvocationTargetException, IllegalAccessException {
                if (!getMethod().getName().endsWith("Configured")) {
                    getMethod().invoke(parent, new Object[]{realObject});
                }
                return nestedObject;
            }

            Object getRealObject() {
                return realObject;
            }

            void store(Object parent, Object child) throws InvocationTargetException,
                    IllegalAccessException, InstantiationException {
                if (getMethod().getName().endsWith("Configured")) {
                    getMethod().invoke(parent, new Object[]{realObject});
                }
            }
        };
    }

    /**
     * Inserts an add or addConfigured method into
     * the addTypeMethods array. The array is
     * ordered so that the more derived classes are first.
     * If both add and addConfigured are present, the addConfigured will take priority.
     *
     * @param method the <code>Method</code> to insert.
     */
    private void insertAddTypeMethod(Method method) {
        Class argClass = method.getParameterTypes()[0];
        for (int c = 0; c < addTypeMethods.size(); ++c) {
            Method current = (Method) addTypeMethods.get(c);
            if (current.getParameterTypes()[0].equals(argClass)) {
                if (method.getName().equals("addConfigured")) {
                    // add configured replaces the add method
                    addTypeMethods.set(c, method);
                }
                return; // Already present
            }
            if (current.getParameterTypes()[0].isAssignableFrom(argClass)) {
                addTypeMethods.add(c, method);
                return; // higher derived
            }
        }
        addTypeMethods.add(method);
    }

    /**
     * Search the list of methods to find the first method
     * that has a parameter that accepts the nested element object.
     *
     * @param paramClass the <code>Class</code> type to search for.
     * @param methods    the <code>List</code> of methods to search.
     * @return a matching <code>Method</code>; null if none found.
     */
    private Method findMatchingMethod(Class paramClass, List methods) {
        if (paramClass == null) {
            return null;
        }
        Class matchedClass = null;
        Method matchedMethod = null;

        for (int i = 0; i < methods.size(); ++i) {
            Method method = (Method) methods.get(i);
            Class methodClass = method.getParameterTypes()[0];
            if (methodClass.isAssignableFrom(paramClass)) {
                if (matchedClass == null) {
                    matchedClass = methodClass;
                    matchedMethod = method;
                } else {
                    if (!methodClass.isAssignableFrom(matchedClass)) {
                        throw new BuildException("ambiguous: types " + matchedClass.getName() + " and "
                                + methodClass.getName() + " match " + paramClass.getName());
                    }
                }
            }
        }
        return matchedMethod;
    }

    private String condenseText(final String text) {
        if (text.length() <= MAX_REPORT_NESTED_TEXT) {
            return text;
        }
        int ends = (MAX_REPORT_NESTED_TEXT - ELLIPSIS.length()) / 2;
        return new StringBuffer(text).replace(ends, text.length() - ends, ELLIPSIS).toString();
    }


    private class MethodAndObject {
        private Method method;
        private Object object;

        public MethodAndObject(Method method, Object object) {
            this.method = method;
            this.object = object;
        }
    }

    /**
     *
     */
    private AntTypeDefinition findRestrictedDefinition(
            ComponentHelper helper, String componentName, List methods) {
        AntTypeDefinition definition = null;
        Class matchedDefinitionClass = null;

        List definitions = helper.getRestrictedDefinitions(componentName);
        if (definitions == null) {
            return null;
        }
        synchronized (definitions) {
            for (int i = 0; i < definitions.size(); ++i) {
                AntTypeDefinition d = (AntTypeDefinition) definitions.get(i);
                Class exposedClass = d.getExposedClass(helper.getProject());
                if (exposedClass == null) {
                    continue;
                }
                Method method = findMatchingMethod(exposedClass, methods);
                if (method == null) {
                    continue;
                }
                if (matchedDefinitionClass != null) {
                    throw new BuildException(
                            "ambiguous: restricted definitions for "
                                    + componentName + " "
                                    + matchedDefinitionClass + " and " + exposedClass);
                }
                matchedDefinitionClass = exposedClass;
                definition = d;
            }
        }
        return definition;
    }

    private MethodAndObject createRestricted(
            ComponentHelper helper, String elementName, List addTypeMethods) {

        Project project = helper.getProject();

        AntTypeDefinition restrictedDefinition =
                findRestrictedDefinition(helper, elementName, addTypeMethods);

        if (restrictedDefinition == null) {
            return null;
        }

        Method addMethod = findMatchingMethod(
                restrictedDefinition.getExposedClass(project), addTypeMethods);
        if (addMethod == null) {
            throw new BuildException(
                    "Ant Internal Error - contract mismatch for "
                            + elementName);
        }
        Object addedObject = restrictedDefinition.create(project);
        if (addedObject == null) {
            throw new BuildException(
                    "Failed to create object " + elementName
                            + " of type " + restrictedDefinition.getTypeClass(project));
        }
        return new MethodAndObject(addMethod, addedObject);
    }

    private MethodAndObject createTopLevel(ComponentHelper helper, String elementName, List methods) {
        Class clazz = helper.getComponentClass(elementName);
        if (clazz == null) {
            return null;
        }
        Method addMethod = findMatchingMethod(clazz, addTypeMethods);
        if (addMethod == null) {
            return null;
        }
        Object addedObject = helper.createComponent(elementName);
        return new MethodAndObject(addMethod, addedObject);
    }

}
