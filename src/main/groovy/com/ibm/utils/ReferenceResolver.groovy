package com.ibm.utils

import org.apache.commons.beanutils.PropertyUtils

import java.util.regex.Matcher

import static groovy.json.JsonOutput.*

class ReferenceResolver {

    public static resolve(Object obj) {
        if (!Map.isAssignableFrom(obj.getClass())) {
            throw new IllegalArgumentException("ReferenceResolver only supports resolving references within nested Maps. Please provide a Map.")
        }
        resolveRecursive(obj, obj)
    }
    public static resolveRecursive(Object obj, Object base) {
        def result = obj
        if ([Collection, Object[]].any { it.isAssignableFrom(obj.getClass()) }) {
            def l = []
            obj.each { v -> l << resolveRecursive(v, base) }
            result = l
        } else if (Map.isAssignableFrom(obj.getClass())) {
            def m = [:]
            obj.each { k, v -> m[k] = resolveRecursive(v, base) }
            result = m
        } else if (String.isAssignableFrom(obj.getClass())) {
            Matcher matcher = obj =~ /%\{([^\}]*)\}/
            if (matcher.size() > 0) {
                if (matcher[0][0] == obj.toString().trim()) {
                    // Full object reference back to related object
                    def ref = matcher[0][1]
                    def resolvedRef = PropertyUtils.getNestedProperty(base, ref)
                    result = resolvedRef ? resolvedRef : result
                } else {
                    // String reference(s) to resolve inline
                    matcher.each {g->
                        def match = g[0] // i.e. #{foo.bar.baz}
                        def ref = g[1] // i.e. foo.bar.baz
                        def resolvedRef = PropertyUtils.getNestedProperty(base, ref)
                        result = resolvedRef ? result.toString().replace(match, resolvedRef) : result
                    }
                }
            }
        }
        result
    }

    public static void main(String[] args) {
        def map = [
                "tree_1": [
                        "str1": "value1",
                        "str2": "value2",
                        "arr": [1,2,3],
                        "map": [
                                "k": "v"
                        ]
                ],
                "tree_2": [
                        "str": "%{tree_1.str1} and %{tree_1.str2}",
                        "arr": "%{tree_1.arr}",
                        "map": "%{tree_1.map}"
                ]
        ]
        println "SOURCE MAP"
        println prettyPrint(toJson(map))

        println "\n\n RESULT MAP"
        println prettyPrint(toJson(resolve(map)))
    }
}
