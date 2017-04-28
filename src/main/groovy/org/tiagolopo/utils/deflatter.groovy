package org.tiagolopo.utils


class Deflatter {
    String input
    Map hash = [:]
    Map result = [:]
    
    Deflatter(String _input){ 
	input = _input
	createHash() 
    }

    void createHash () {    
        input.split("\n").each { line ->
            def a = line.split(':')
            def k = a[0]
            def v = a[1]
            v = v.trim()
            def stack = []
            k = k.replaceAll('\\[','.')
            k = k.replaceAll('\\]','')
            k.split('\\.').each { subkey ->
               stack.push subkey 
            }
            hash[k] = [stack: stack, value: v]
        }    
    }

    def deflat(){ 
	hash.each{k,v ->
		unstack(v.stack,v.value)
        }
        return result
    } 

    private unstack (stack,value,obj=null) {
        stack = stack as Queue
        def subkey = stack.poll()

        obj = (obj != null) ? obj : result         

        subkey = isObjectArray(obj) ? Integer.parseInt(subkey) : subkey

        def nextValue = isNextArray(stack) ? [] : [:]
        nextValue = stack ? nextValue : value 

        if ( !isObjectArray(obj[subkey])  ) {
            obj[subkey] = obj[subkey] ?: nextValue
        }
  
        obj = obj[subkey]
        if (!stack) {  obj = value; return} 
        unstack(stack,value,obj)      
    }

    private isObjectArray(obj){
        obj.getClass() == [].getClass()
    }

    private isNextArray(stack) {
        if (stack && stack[0] ==~ /^\d+$/ ) {return true} 
        false 
    }
}

//def result  = new Deflatter(System.in.text).deflat()
//println(JsonOutput.prettyPrint(JsonOutput.toJson(result)))
