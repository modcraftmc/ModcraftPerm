package fr.goldor.ModcraftPerm.System;

import java.util.ArrayList;

public class PrettyListing{
    public stringTreeElement rootElement;

    public String list(String list,String splitRegex){
        return list(list.split(splitRegex));
    }

    public String list(String[] elements){
        rootElement = new stringTreeElement("tree");

        for (String element: elements) {
            rootElement.newElement(element);
        }

        rootElement.sort();

        ArrayList<String> tree = rootElement.generateStringTree();
        tree.remove(0);

        String returnValue = "";
        for (String line: tree) {
            returnValue = String.format("%s%s%s",returnValue,"\n",line);
        }

        return returnValue;
    }

    public String listPermission(String[] perms,String splitRegex){
        //make a tree of the permission
        rootElement = new stringTreeElement("tree");

        for (String perm: perms) {
            String[] parts = perm.split(splitRegex);
            stringTreeElement lastElement = rootElement;
            for (int i = 0;i<parts.length;i++) {
                lastElement = lastElement.getElement(parts[i]);
            }
        }

        rootElement.sort();

        ArrayList<String> tree = rootElement.generateStringTree();
        tree.remove(0); //remove element named "tree"

        String returnValue = "";
        for (String line: tree) {
            returnValue = String.format("%s%s%s",returnValue,"\n",line);
        }

        return returnValue;
    }

    public class stringTreeElement{
        public stringTreeElement _parent;
        public int _generation;
        public String _element;
        public ArrayList<stringTreeElement> _branch;
        public stringTreeElement[] _sorted;

        public stringTreeElement(String element){
            _generation = 0;
            _element = element;
            _branch = new ArrayList<stringTreeElement>();
            _parent = null;
            _sorted = new stringTreeElement[0];
        }

        public stringTreeElement(String element,stringTreeElement parent){
            _generation = parent._generation + 1;
            _element = element;
            _branch = new ArrayList<stringTreeElement>();
            _sorted = new stringTreeElement[0];
            _parent = parent;
            parent._branch.add(this);
        }

        public stringTreeElement newElement(String element){
            return new stringTreeElement(element,this);
        }

        public stringTreeElement getElement(String element){
            for (stringTreeElement child: _branch) {
                if(child._element.contentEquals(element)){
                    return child;
                }
            }
            return newElement(element);
        }

        public void sort(){
            _sorted = _branch.toArray(new stringTreeElement[0]);

            for(int i = 0; i< _sorted.length-1; i++) {
                for (int j = i+1; j< _sorted.length; j++) {
                    if(_sorted[i]._element.compareTo(_sorted[j]._element)>0) {
                        stringTreeElement temp = _sorted[i];
                        _sorted[i] = _sorted[j];
                        _sorted[j] = temp;
                    }
                }
            }

            for (stringTreeElement element: _sorted) {
                element.sort();
            }
        }

        public ArrayList<String> generateStringTree(){
            String line = "";
            for (int i = 0;i<_generation;i++) {
                line = String.format("%s   ",line);
            }
            line = String.format("%s-%s",line,_element);

            ArrayList<String> returnValue = new ArrayList<String>();
            returnValue.add(line);

            for (stringTreeElement element: _branch) {
                returnValue.addAll(element.generateStringTree());
            }

            return returnValue;
        }
    }
}