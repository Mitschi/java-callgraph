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

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;

/**
 * Constructs a callgraph out of a JAR archive. Can combine multiple archives
 * into a single call graph.
 *
 * @author Georgios Gousios <gousiosg@gmail.com>
 */
public class JCallGraph {

    private Map<String, List<GousiousCall> > calls;
    private Map<String, Map<String, JavaClass>> classes;

    public static void main(String[] args) {
        JCallGraph jCallGraph = new JCallGraph();
        jCallGraph.generateCalls(args);
        Map<String, List<GousiousCall>> stringStringMap = jCallGraph.getCalls();
//        System.out.println(stringStringMap);
        for (String s : stringStringMap.keySet()) {
//            System.out.println(s+": "+stringStringMap.get(s));
            List<GousiousCall> gousiousCalls = stringStringMap.get(s);
            gousiousCalls.forEach(System.out::println);
            System.out.println(s+": "+stringStringMap.get(s).size());
        }
    }



    public  void generateCalls(String[] jars) {

        Map<String, List<GousiousCall>> callsPerJar = new HashMap<>();
        Map<String, Map<String, JavaClass>> classesPerJar = new HashMap<>();

        Function<ClassParser, ClassVisitor> getClassVisitor =
                (ClassParser cp) -> {
                    try {
                        return new ClassVisitor(cp.parse());
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                };

        try {
            for (String arg : jars) {

                File f = new File(arg);

                Map<String,JavaClass> currentClasses = new HashMap<>();
                classesPerJar.put(f.getAbsolutePath(),currentClasses);

                if (!f.exists()) {
                    System.err.println("Jar file " + arg + " does not exist");
                }

                try (JarFile jar = new JarFile(f)) {
                    Stream<JarEntry> entries = enumerationAsStream(jar.entries());

                    List<GousiousCall> methodCalls = entries.
                            flatMap(e -> {
                                if (e.isDirectory() || !e.getName().endsWith(".class"))
                                    return (new ArrayList<String>()).stream();

                                ClassParser cp = new ClassParser(arg, e.getName());
                                ClassVisitor cv = getClassVisitor.apply(cp).start();
                                List<GousiousCall> localMethodCalls = cv.methodCalls();
                                List<GousiousCall> localInheritanceCalls = cv.inheritanceCalls();
                                List<GousiousCall> allCalls = new ArrayList<>();
                                allCalls.addAll(localMethodCalls);
                                allCalls.addAll(localInheritanceCalls);

                                currentClasses.putAll(cv.classes());

//                                for (GousiousCall call : localInheritanceCalls) {
//                                    System.out.println("");
//                                    if(cv.classes().containsKey(call.getLeftSide())) {
//                                        call.setLeftSide(f.getAbsolutePath()+"::"+call.getLeftSide());
//                                    }
//                                    if(cv.classes().containsKey(call.getRightSide())) {
//                                        call.setRightSide(f.getAbsolutePath()+"::"+call.getRightSide());
//                                    }
//                                    //add to allcalls
//                                    allCalls.add(call.toString());
//                                }
                                Stream<GousiousCall> stream = allCalls.stream();
//                                getClassVisitor.apply(cp).start().methodCalls().stream().collect(Collectors.toList());
                                return stream;
                            }).map(x->(GousiousCall)x).collect(Collectors.toList());
//                            map(s -> s + "\n").
//                            reduce(new StringBuilder(),
//                                    StringBuilder::append,
//                                    StringBuilder::append).toString();

//                    BufferedWriter log = new BufferedWriter(new OutputStreamWriter(System.out));
//                    log.write(methodCalls);
//                    log.close();

                    callsPerJar.put(arg,methodCalls);

                    //evtl. Postprocessing for FQN
//                    for (GousiousCall gc : methodCalls) {
//                        if(currentClasses.containsKey(gc.getLeftSide())) {
//                            gc.setLeftSide(f.getAbsolutePath()+"::"+gc.getLeftSide());
//                        }
//                        if(currentClasses.containsKey(gc.getRightSide())) {
//                            gc.setRightSide(f.getAbsolutePath()+"::"+gc.getRightSide());
//                        }
////                        System.out.println();
//                    }
                }


            }
        } catch (IOException e) {
            System.err.println("Error while processing jar: " + e.getMessage());
            e.printStackTrace();
        }

        this.calls= callsPerJar;
        this.classes = classesPerJar;
    }
    public <T> Stream<T> enumerationAsStream(Enumeration<T> e) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                        new Iterator<T>() {
                            public T next() {
                                return e.nextElement();
                            }

                            public boolean hasNext() {
                                return e.hasMoreElements();
                            }
                        },
                        Spliterator.ORDERED), false);
    }

    public Map<String, List<GousiousCall>> getCalls() {
        return calls;
    }

    public Map<String, Map<String, JavaClass>> getClasses() {
        return classes;
    }
}
