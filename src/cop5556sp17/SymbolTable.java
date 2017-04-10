package cop5556sp17;


import cop5556sp17.AST.Dec;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;


public class SymbolTable {
	
	
	//TODO  add fields
	Stack<Integer> scopeStack;
    Map<String, LinkedList<SymTabEntry>> symTab;
    Integer currentScope;
    Integer nextScope;

	/** 
	 * to be called when block entered
	 */
	public void enterScope(){
		currentScope = nextScope++;
        scopeStack.push(currentScope);
	}
	
	
	/**
	 * leaves scope
	 */
	public void leaveScope(){
		scopeStack.pop();
        currentScope = scopeStack.peek();
	}
	
	public boolean insert(String ident, Dec dec){
        SymTabEntry symTabEntry = new SymTabEntry(dec, currentScope);
        if(!symTab.containsKey(ident)) {
            LinkedList<SymTabEntry> list = new LinkedList<>();
            list.add(symTabEntry);
            symTab.put(ident, list);
        } else {
            LinkedList<SymTabEntry> list = symTab.get(ident);
            for(SymTabEntry s : list){
                if(s.scopeNumber.equals(symTabEntry.scopeNumber))
                    return false;
            }
            list.add(symTabEntry);
        }
        return true;
	}

    public Dec lookup(String ident){
        LinkedList<SymTabEntry> list = symTab.get(ident);
        Dec dec = null;
        if(list !=null && !list.isEmpty()) {
            int maxScope = -1;
            for (SymTabEntry symTabEntry : list) {
                if(symTabEntry.scopeNumber > maxScope && scopeStack.contains(symTabEntry.scopeNumber)) {
                    maxScope = symTabEntry.scopeNumber;
                    dec = symTabEntry.dec;
                }
            }
        }
        return dec;
    }
		
	public SymbolTable() {
        currentScope = 0;
        nextScope = 1;
		scopeStack = new Stack<>();
        symTab =  new HashMap<>();
        // Push 0 for global scope
        scopeStack.push(0);
    }


	@Override
	public String toString() {
		//TODO:  IMPLEMENT THIS
		return "";
	}

    class SymTabEntry {
        Dec dec;
        Integer scopeNumber;

        public SymTabEntry(Dec dec, Integer scopeNumber) {
            this.dec = dec;
            this.scopeNumber = scopeNumber;
        }
    }

}
