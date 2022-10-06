/*
 * Copyright (c) 2011 - Georgios Gousios <gousiosg@gmail.com>
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package gr.gousiosg.javacg.stat;

import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.EmptyVisitor;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.MethodGen;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The simplest of class visitors, invokes the method visitor class for each
 * method found.
 */
public class ClassVisitor extends EmptyVisitor {

    private JavaClass clazz;
    private ConstantPoolGen constants;
    private String classReferenceFormat;
    private final DynamicCallManager DCManager = new DynamicCallManager();
    private List<GousiousCall> methodCalls = new ArrayList<>();
    private List<GousiousCall> inheritanceCalls = new ArrayList<>();

    private Map<String,JavaClass> classes = new HashMap<>();

    public ClassVisitor(JavaClass jc) {
        clazz = jc;
        constants = new ConstantPoolGen(clazz.getConstantPool());
        classReferenceFormat = "C:" + clazz.getClassName() + " %s";
    }

    public void visitJavaClass(JavaClass jc) {
        jc.getConstantPool().accept(this);
        classes.put(jc.getClassName(),jc);
        try {
            if(jc.getInterfaceNames()!= null && jc.getInterfaceNames().length>0) {
                String interfaceString = Arrays.stream(jc.getInterfaceNames()).parallel().collect(Collectors.joining(","));

                String[] interfaces = interfaceString.split(",");
                String middleSide = jc.isInterface() ? "(INHSUP)" : "(INHINT)"; // if jc is interface it all used interfaces are effectively extended superclasses
                for (String anInterface : interfaces) {
                    inheritanceCalls.add(new GousiousCall(anInterface,middleSide,clazz.getClassName(),null,null));
                }


            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        try {
            String superclassName = jc.getSuperclassName();
            if(superclassName!=null && !"".equals(superclassName)) {
//                inheritanceCalls.add(superclassName + " (INHSUP)" + clazz.getClassName());
                inheritanceCalls.add(new GousiousCall(superclassName,"(INHSUP)",clazz.getClassName(),null,null));
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        Method[] methods = jc.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            DCManager.retrieveCalls(method, jc);
            DCManager.linkCalls(method);
            method.accept(this);

        }
    }

    public void visitConstantPool(ConstantPool constantPool) {
        for (int i = 0; i < constantPool.getLength(); i++) {
            Constant constant = constantPool.getConstant(i);
            if (constant == null)
                continue;
            if (constant.getTag() == 7) {
                String referencedClass = 
                    constantPool.constantToString(constant);
//                System.out.println(String.format(classReferenceFormat, referencedClass));
            }
        }
    }

    public void visitMethod(Method method) {
        MethodGen mg = new MethodGen(method, clazz.getClassName(), constants);
        MethodVisitor visitor = new MethodVisitor(mg, clazz);
        List<String> start = visitor.start();
        for (String mCall : start) {
            String patternString1 = "M:(.*):(.*) \\((M|I|O|S|D|INHSUP|INHINT)\\)(.*):(.*)";

            Pattern pattern = Pattern.compile(patternString1);
            Matcher matcher = pattern.matcher(mCall);
            boolean found = matcher.find();
            if(found) {
                GousiousCall gc = new GousiousCall(getFQNForSide(matcher.group(1).trim()),"("+matcher.group(3).trim()+")",getFQNForSide(matcher.group(4).trim()),matcher.group(2),matcher.group(5));
                methodCalls.add(gc);
            } else {
                System.out.println("ERROR!!!");
            }
        }
    }


    private String getFQNForSide(String callSide) {
        String[] callParts = callSide.split("\\(");
        try {
            String paramString = callParts[1].split("\\)")[0];
            String[] params = paramString.split(",");
            int paramCount = params.length;
            return callParts[0]+"("+paramCount+")";
        } catch (IndexOutOfBoundsException e){
            return callSide;
        }
    }

    public ClassVisitor start() {
        visitJavaClass(clazz);
        return this;
    }

    public List<GousiousCall> inheritanceCalls() {
        return this.inheritanceCalls;
    }

    public List<GousiousCall> methodCalls() {
        return this.methodCalls;
    }

    public Map<String, JavaClass> classes() {
        return classes;
    }
}
