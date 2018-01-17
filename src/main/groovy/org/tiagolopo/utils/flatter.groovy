package org.tiagolopo.utils

class Flatter {

    def stack = []
    def hash = [:]

    def flat (e, String name='', _stack=[]) {
        stack = _stack
        if (e.getClass().toString().toLowerCase().contains('map') && e.size() > 0 ){
            def counter = 0
            e.each { k,v ->
                def str = stack ? ".${k}" : k 
                stack.push(str)
                flat(v,k,stack)
                counter == e.size() - 1 && stack && stack.pop() 
                counter++
            }
        }else if (e.getClass() == ArrayList && e.size() > 0 ) {
            def counter = 0
            e.each { v -> 
                stack.push("[${counter}]")
                flat(v,"${counter}",stack)
                counter == e.size() - 1 && stack &&  stack.pop()
                counter ++  
            }
		}else {
	            hash[stack.join('')]  = e
	            stack && stack.pop()
        }

        def result = ''
        hash.each {k,v ->
            result += "${k}: ${v}\n"
        }
        result
    }
}

//fromJson = new JsonSlurper().parseText(System.in.text)
//println (new Flatter().flat(fromJson))
