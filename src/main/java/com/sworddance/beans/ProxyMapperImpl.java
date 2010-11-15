/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the
 * License.
 */

package com.sworddance.beans;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sworddance.beans.ProxyLoader.ChildObjectNotLoadableException;
import com.sworddance.util.ApplicationIllegalStateException;
import com.sworddance.util.ApplicationNullPointerException;
import com.sworddance.util.CurrentIterator;
import com.sworddance.util.WeakProxy;

import static org.apache.commons.lang.StringUtils.*;

/**
 * enables a controlled access to an object tree that also supports caching.
 *
 * NOTE: Use {@link ProxyFactory} implementors to create instances of this class. {@link ProxyFactoryImpl} is a good default.
 * Sample use case:
 * <ul>
 * <li>a User object has a member object Role.</li>
 * <li>the Role object has its own properties</li>
 * <li>Both Role and User are stored in the database</li>
 * <li>Access to changing properties on either User or Role should be restricted on a dynamic basis</li>
 * </ul>
 *
 * <h3>Alternative rejected solutions</h3>
 * Hibernate caching rejected because:
 * <ul>
 * <li>operates on a per-entity basis not on an object tree basis</li>
 * <li>no mechanism to restrict read-only and write able properties on a per request basis.</li>
 * <li>no serialization mechanism.</l>
 * <li>no graceful way to work with flow code to preserve original state</li>
 * <li>no ability to cache only the parts of the entities needed for the request in question.
 * For example, if the request is allowing an admin to change another user's role, then the request should
 * have no ability through bug or hack attempt to access and change the user's password.</li>
 * </ul>
 *
 * Using apache bean utilities
 * <ul>
 * <li>does not seem to handle tree of objects</li>
 * <li>serialization issues</li>
 * </ul>
 *
 * @author patmoore
 * @param <I> the interface class that the Proxy implements.
 * @param <O> extends <I> the class (not interface) that is the concrete class that is wrapped by the ProxyWrapper.
 *
 */
public abstract class ProxyMapperImpl<I,O extends I> extends BeanWorker implements InvocationHandler, Serializable, ProxyMapperImplementor<I, O> {
    private I externalFacingProxy;
    /**
     * the real value may be null. This can arise if a ProxyMapper was created for a non-null object and then the object was
     * set to null.
     */
    private transient boolean realObjectSet;
    private transient WeakReference<O> realObject;
    // TODO : probably ? super O is the correct generic signature. Hibernate proxying may mean actual class is not <O>
    private Class<? extends O> realClass;
    private Class<? extends I> proxyClass;
    private String basePropertyPath;
    private transient ProxyLoader proxyLoader;
    private transient ProxyMethodHelper proxyMethodHelper;

    /**
     * {@link ConcurrentHashMap} does not allow null keys or values.
     */
    protected static final Serializable NullObject = new Serializable() {
		private static final long serialVersionUID = 1L;

		@Override
        public String toString() {
            return "(nullobject)";
        }
    };
    protected ProxyMapperImpl(String basePropertyPath, O realObject, Class<? extends O> realClass, Class<? extends I> proxyClass, ProxyLoader proxyLoader, List<String> propertyChains) {
        super(propertyChains);
        this.proxyLoader = proxyLoader;
        this.basePropertyPath = basePropertyPath;
        if (realObject != null) {
            this.setRealObject(realObject);
        }
        this.setRealClass(realClass);
        this.setProxyClass(proxyClass);
        this.setExternalFacingProxy(createExternalFacingProxy());
    }

    /*
     * Crash note:
     * when ProxyMapperImplementor<?, ?> wildcard generics are used
     *     ProxyMapperImplementor<?, ?> childProxy = this;
     * An exception has occurred in the compiler (1.6.0_17). Please file a bug at the Java Developer Connection (http://java.sun.com/webapps/bugreport)  after checking the Bug Parade for duplicates. Include your program and the following diagnostic in your report.  Thank you.
java.lang.AssertionError: isSubtype 15
    at com.sun.tools.javac.code.Types$5.visitType(Types.java:347)
    at com.sun.tools.javac.code.Types$5.visitType(Types.java:328)
    at com.sun.tools.javac.code.Types$DefaultTypeVisitor.visitWildcardType(Types.java:3163)
    at com.sun.tools.javac.code.Type$WildcardType.accept(Type.java:416)
    at com.sun.tools.javac.code.Types$DefaultTypeVisitor.visit(Types.java:3161)
    at com.sun.tools.javac.code.Types.isSubtype(Types.java:324)
    at com.sun.tools.javac.code.Types.isSubtype(Types.java:308)
    at com.sun.tools.javac.code.Types.isSubtypeUnchecked(Types.java:288)
    at com.sun.tools.javac.code.Types.isSubtypeUnchecked(Types.java:460)
    at com.sun.tools.javac.comp.Infer.checkWithinBounds(Infer.java:388)
    at com.sun.tools.javac.comp.Infer.instantiateExpr(Infer.java:241)
    at com.sun.tools.javac.comp.Check.instantiatePoly(Check.java:356)
    at com.sun.tools.javac.comp.Check.checkType(Check.java:324)
    at com.sun.tools.javac.comp.Attr.check(Attr.java:160)
    at com.sun.tools.javac.comp.Attr.visitApply(Attr.java:1276)
    at com.sun.tools.javac.tree.JCTree$JCMethodInvocation.accept(JCTree.java:1210)
    at com.sun.tools.javac.comp.Attr.attribTree(Attr.java:360)
    at com.sun.tools.javac.comp.Attr.attribExpr(Attr.java:377)
    at com.sun.tools.javac.comp.Attr.visitAssign(Attr.java:1550)
    at com.sun.tools.javac.tree.JCTree$JCAssign.accept(JCTree.java:1342)
    at com.sun.tools.javac.comp.Attr.attribTree(Attr.java:360)
    at com.sun.tools.javac.comp.Attr.attribExpr(Attr.java:384)
    at com.sun.tools.javac.comp.Attr.visitExec(Attr.java:1017)
    at com.sun.tools.javac.tree.JCTree$JCExpressionStatement.accept(JCTree.java:1074)
    at com.sun.tools.javac.comp.Attr.attribTree(Attr.java:360)
    at com.sun.tools.javac.comp.Attr.attribStat(Attr.java:397)
    at com.sun.tools.javac.comp.Attr.attribStats(Attr.java:413)
    at com.sun.tools.javac.comp.Attr.visitBlock(Attr.java:715)
    at com.sun.tools.javac.tree.JCTree$JCBlock.accept(JCTree.java:739)
    at com.sun.tools.javac.comp.Attr.attribTree(Attr.java:360)
    at com.sun.tools.javac.comp.Attr.attribStat(Attr.java:397)
    at com.sun.tools.javac.comp.Attr.visitIf(Attr.java:1009)
    at com.sun.tools.javac.tree.JCTree$JCIf.accept(JCTree.java:1050)
    at com.sun.tools.javac.comp.Attr.attribTree(Attr.java:360)
    at com.sun.tools.javac.comp.Attr.attribStat(Attr.java:397)
    at com.sun.tools.javac.comp.Attr.attribStats(Attr.java:413)
    at com.sun.tools.javac.comp.Attr.visitBlock(Attr.java:715)
    at com.sun.tools.javac.tree.JCTree$JCBlock.accept(JCTree.java:739)
    at com.sun.tools.javac.comp.Attr.attribTree(Attr.java:360)
    at com.sun.tools.javac.comp.Attr.attribStat(Attr.java:397)
    at com.sun.tools.javac.comp.Attr.visitForLoop(Attr.java:740)
    at com.sun.tools.javac.tree.JCTree$JCForLoop.accept(JCTree.java:818)
    at com.sun.tools.javac.comp.Attr.attribTree(Attr.java:360)
    at com.sun.tools.javac.comp.Attr.attribStat(Attr.java:397)
    at com.sun.tools.javac.comp.Attr.attribStats(Attr.java:413)
    at com.sun.tools.javac.comp.Attr.visitBlock(Attr.java:715)
    at com.sun.tools.javac.tree.JCTree$JCBlock.accept(JCTree.java:739)
    at com.sun.tools.javac.comp.Attr.attribTree(Attr.java:360)
    at com.sun.tools.javac.comp.Attr.attribStat(Attr.java:397)
    at com.sun.tools.javac.comp.Attr.visitIf(Attr.java:1009)
    at com.sun.tools.javac.tree.JCTree$JCIf.accept(JCTree.java:1050)
    at com.sun.tools.javac.comp.Attr.attribTree(Attr.java:360)
    at com.sun.tools.javac.comp.Attr.attribStat(Attr.java:397)
    at com.sun.tools.javac.comp.Attr.attribStats(Attr.java:413)
    at com.sun.tools.javac.comp.Attr.visitBlock(Attr.java:715)
    at com.sun.tools.javac.tree.JCTree$JCBlock.accept(JCTree.java:739)
    at com.sun.tools.javac.comp.Attr.attribTree(Attr.java:360)
    at com.sun.tools.javac.comp.Attr.attribStat(Attr.java:397)
    at com.sun.tools.javac.comp.Attr.visitIf(Attr.java:1009)
    at com.sun.tools.javac.tree.JCTree$JCIf.accept(JCTree.java:1050)
    at com.sun.tools.javac.comp.Attr.attribTree(Attr.java:360)
    at com.sun.tools.javac.comp.Attr.attribStat(Attr.java:397)
    at com.sun.tools.javac.comp.Attr.attribStats(Attr.java:413)
    at com.sun.tools.javac.comp.Attr.visitBlock(Attr.java:715)
    at com.sun.tools.javac.tree.JCTree$JCBlock.accept(JCTree.java:739)
    at com.sun.tools.javac.comp.Attr.attribTree(Attr.java:360)
    at com.sun.tools.javac.comp.Attr.attribStat(Attr.java:397)
    at com.sun.tools.javac.comp.Attr.visitMethodDef(Attr.java:634)
    at com.sun.tools.javac.tree.JCTree$JCMethodDecl.accept(JCTree.java:639)
    at com.sun.tools.javac.comp.Attr.attribTree(Attr.java:360)
    at com.sun.tools.javac.comp.Attr.attribStat(Attr.java:397)
    at com.sun.tools.javac.comp.Attr.attribClassBody(Attr.java:2697)
    at com.sun.tools.javac.comp.Attr.attribClass(Attr.java:2628)
    at com.sun.tools.javac.comp.Attr.attribClass(Attr.java:2564)
    at com.sun.tools.javac.main.JavaCompiler.attribute(JavaCompiler.java:1036)
    at com.sun.tools.javac.main.JavaCompiler.compile2(JavaCompiler.java:765)
    at com.sun.tools.javac.main.JavaCompiler.compile(JavaCompiler.java:730)
    at com.sun.tools.javac.main.Main.compile(Main.java:353)
    at com.sun.tools.javac.main.Main.compile(Main.java:279)
    at com.sun.tools.javac.main.Main.compile(Main.java:270)
    at com.sun.tools.javac.Main.compile(Main.java:69)
    at com.sun.tools.javac.Main.main(Main.java:54)
     */
    /**
     * used to initialize the ProxyMapper with the cached values
     * @param property a dot-separated chain of properties ( for example, "grandparent.parent.child" )
     */
    protected Object initValue(String property) {
        Object result = getRealObject();

        if ( result != null && property != null ) {
            StringBuilder builder = new StringBuilder();
            // expecting a chain of methods to get the final value,
            PropertyMethodChain methodChain = getPropertyMethodChainAddIfAbsent(getProxyClass(), property, true);
            if ( methodChain != null ) {
                CurrentIterator<PropertyAdaptor> iterator = methodChain.iterator();
                PropertyAdaptor propertyAdaptor = null;
                /* see note above */
                ProxyMapperImplementor/*<?, ?>*/ childProxy = this;
                for (;iterator.hasNext() && result != null;) {
                    propertyAdaptor = iterator.next();
                    // construct the intermediate propertyPath to the passed property name.
                    if ( builder.length() > 0 ) {
                        builder.append(".");
                    }
                    builder.append(propertyAdaptor.getPropertyName());

                    if (propertyAdaptor.getReturnType().isInterface()) {
                        // only interfaces get child proxies
                        // need to do leaf nodes as well because one propertyChain's leaf is another's parent
                        // example: "foo" and "foo.uri"
                        childProxy = getChildProxyMapper(builder.toString(), propertyAdaptor, result, childProxy);
                        if ( childProxy == null) {
                            result = null;
                        } else if ( iterator.hasNext()) {
                            // we are still walking the property chain.
                            // result will be the real object for the next iteration through the loop.
                            result = childProxy.getRealObject();
                        } else {
                            // we are going to be done. return the child proxy.
                            result = childProxy.getExternalFacingProxy();
                        }
                    } else {
                        // there will not be any more proxies
                        // now finish out the retrieval of the result.
                        result = methodChain.getValue(result, iterator);
                    }
                }
            }
        } else {
            result = null;
        }
        putOriginalValues(property, result);
        return result;
    }

    /**
     * @param propertyName
     * @param result
     */
    protected abstract void putOriginalValues(String propertyName, Object result);
    protected abstract void putNewValues(String propertyName, Object result);

    /**
     * @see com.sworddance.beans.ProxyMapper#clear()
     */
    public void clear() {
        this.realObject = null;
        this.realObjectSet = false;
    }
    /**
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        String propertyName;
        ProxyMethodHelper methodHelper = getProxyMethodHelper();
        if ( methodHelper != null && methodHelper.isHandling(this, proxy, method, args)) {
            return methodHelper.invoke(this, proxy, method, args);
        } else if ( method.getGenericParameterTypes().length == 0 && (propertyName = this.getGetterPropertyName(methodName)) != null) {
            if ( this.containsKey(propertyName)) {
                return this.getCachedValue(propertyName);
            } else {
                switch(this.getProxyBehavior()) {
                case nullValue:
                    return null;
                case readThrough:
                case leafStrict:
                    if ( method.getReturnType() == Void.class || args != null && args.length > 1) {
                        O actualObject = getRealObject();
                        // or more than 1 argument (therefore not java bean property )
                        return doInvoke(method, args);
                    } else {
                        return initValue(propertyName);
                    }
                case strict:
                    throw new ApplicationIllegalStateException("no cached value with strict proxy behavior. proxy=", this, " method=", method, "(", join(args), ")");
                }
            }
            return null;
        } else if (method.getGenericParameterTypes().length == 1 &&(propertyName = this.getSetterPropertyName(methodName)) != null) {
            this.putNewValues(propertyName, args[0]);
            return null;
        } else {
            // HACK: how to handle sideeffects? (can't )
            switch(this.getProxyBehavior()) {
            case strict:
                throw new ApplicationIllegalStateException(this, " method=", method, "(", join(args), ")");
            default:
                return doInvoke(method, args);
            }
        }
    }

    /**
     * @param method
     * @param args
     * @param actualObject
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private Object doInvoke(Method method, Object[] args) throws IllegalAccessException, InvocationTargetException {
        O actualObject;
        if ( (method.getModifiers() & Modifier.STATIC) != Modifier.STATIC) {
            // not a static method
            actualObject = getRealObject();
            ApplicationNullPointerException.notNull(actualObject, "need object to call a non-static method ",this, " method=", method, "(", join(args), ")");
        } else {
            actualObject = null;
        }
        try {
            return method.invoke(actualObject, args);
        } catch(RuntimeException e) {
            // would like to log or annotate somehow ..
            // changing type of exception is bad so we will throw as an exception that will normally be unwrapped.
            throw new InvocationTargetException(e, this+ " method="+ method + "(" +join(args)+ ")");
        }
    }
    public String getKeyProperty() {
        return this.getPropertyName(0);
    }

    /**
     * @param propertyName
     * @return "{@link #basePropertyPath}.propertyName"
     */
    protected String getTruePropertyName(Object propertyName) {
        if ( propertyName == null) {
            return null;
        }
        String propertyNameString = propertyName.toString();
        if ( isBlank(this.getBasePropertyPath())) {
            return propertyNameString;
        } else {
            return this.getBasePropertyPath()+"."+propertyNameString;
        }
    }

    /**
     * @see com.sworddance.beans.ProxyMapper#getBasePropertyPath()
     */
    public String getBasePropertyPath() {
        return basePropertyPath;
    }

    /**
     * @see com.sworddance.beans.ProxyMapper#getRealObject()
     */
    @SuppressWarnings("unchecked")
    public O getRealObject() throws ChildObjectNotLoadableException {

        if ( !this.isRealObjectSet()) {
            ProxyLoader loader = getProxyLoader();
            if ( loader != null ) {
                O actualObject = loader.getRealObject(this);

                this.setRealObject(actualObject);
            }
        }
        return (O) WeakProxy.getActual(this.realObject);
    }

    public void setRealObject(O realObject) {
        this.realObject = WeakProxy.getWeakReference(realObject);
        this.realObjectSet = true;
    }

    /**
     * @see com.sworddance.beans.ProxyMapper#isRealObjectSet()
     */
    public boolean isRealObjectSet() {
        // null may be the real value so can not just check realObject being null
        return this.realObjectSet;
    }
    /**
     * @see com.sworddance.beans.ProxyMapper#getKeyExpression()
     */
    public Object getKeyExpression() {
        return this.getCachedValue(getKeyProperty());
    }

    /**
     * @see com.sworddance.beans.ProxyMapper#applyToRealObject()
     */
    public O applyToRealObject() {
        O base = getRealObject();
        for(Map.Entry<String, Object> entry : this.getNewValues().entrySet()) {
            this.setValue(base, entry.getKey(), entry.getValue());
        }
        return base;
    }
    public void setExternalFacingProxy(I externalFacingProxy) {
        this.externalFacingProxy = externalFacingProxy;
    }
    /**
     * @see com.sworddance.beans.ProxyMapper#getExternalFacingProxy()
     */
    public I getExternalFacingProxy() {
        return externalFacingProxy;
    }
    @SuppressWarnings("unchecked")
    protected I createExternalFacingProxy() {
        Class<?>[] interfaces;
        if ( getRealClass().isInterface()) {
            interfaces = new Class<?>[] { getRealClass() };
        } else {
            interfaces = getRealClass().getInterfaces();
        }
        if (interfaces.length == 0) {
            throw new IllegalArgumentException(this.getRealClass()+" is not an interface or does not have any interfaces.");
        }
        return (I) Proxy.newProxyInstance(getRealClass().getClassLoader(), interfaces, this);
    }
    /**
     * @see com.sworddance.beans.ProxyMapper#setProxyLoader(com.sworddance.beans.ProxyLoader)
     */
    public void setProxyLoader(ProxyLoader proxyLoader) {
        this.proxyLoader = proxyLoader;
    }
    /**
     * @see com.sworddance.beans.ProxyMapper#getProxyLoader()
     */
    public ProxyLoader getProxyLoader() {
        return proxyLoader;
    }

    @Override
    public String toString() {
        return this.getClass()+ " for " + this.getRealClass()+" new values="+this.getNewValues()+ " original="+this.getOriginalValues();
    }

    /**
     * returns existing or creates a new ProxyMapper and returns it for the property.
     * @param propertyFullPath full path to property from {@link RootProxyMapper}
     * @param propertyAdaptor --  can't have full property name because PropertyAdaptor can be reused.
     * @param base TODO
     * @param baseProxyMapper TODO
     * @return null if base's value for the property is null otherwise returns a ProxyMapper
     */
    protected abstract <CI, CO extends CI> ProxyMapperImplementor<CI, CO> getChildProxyMapper(String propertyFullPath, PropertyAdaptor propertyAdaptor, Object base, ProxyMapperImplementor<?, ?> baseProxyMapper);

    /**
     * @see com.sworddance.beans.ProxyMapper#setRealClass(java.lang.Class)
     */
    public void setRealClass(Class<? extends O> realClass) {
        this.realClass = realClass;
    }

    /**
     * @see com.sworddance.beans.ProxyMapper#getRealClass()
     */
    public Class<? extends O> getRealClass() {
        return realClass;
    }

    /**
     * @param proxyClass the proxyClass to set
     */
    public void setProxyClass(Class<? extends I> proxyClass) {
        this.proxyClass = proxyClass;
    }

    /**
     * @return the proxyClass
     */
    public Class<? extends I> getProxyClass() {
        return proxyClass;
    }

    /**
     * @see com.sworddance.beans.ProxyMapper#getValue(java.lang.Object, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue(Object base, String property) {
        return (T) super.getValue(base, property);
    }

    public void setProxyMethodHelper(ProxyMethodHelper proxyMethodHelper) {
        this.proxyMethodHelper = proxyMethodHelper;
    }

    /**
     * @see com.sworddance.beans.ProxyMapper#getProxyMethodHelper()
     */
    public ProxyMethodHelper getProxyMethodHelper() {
        return proxyMethodHelper;
    }
}
