/*
 Evan Gutman- erg53@pitt.edu- 3925988
 */
import java.util.*;

public class PHPArray<V> implements Iterable<V> {

    private static final int INIT_CAPACITY= 11;
    private PHPArray.Node<V> pointer;
    private PHPArray.Node<V> root;  //variable used as the starting point within the iterator
    private int N;  //number of nodes in the hash table
    private int M; //size of hash table
    private PHPArray.Node<V>[] table;   //declaring an array of PHP Nodes with generic type <V>
    private int pairCount=0;    //counter used within the pair class used to determine whether or not to instantiate the iterator
    private NodeIterator pairIterator;  //global iterator used within the pair class


    public PHPArray(){
        this(INIT_CAPACITY);
    }

    //constructor to initialize the capacity (if variable sent as argument) and the node table
    @SuppressWarnings("unchecked")
    public PHPArray(int capacity){
        M=capacity;
        table = (Node<V>[]) new Node<?>[capacity];
    }


    private int hash(String s){
        return (s.hashCode() & 0x7fffffff) % M;
    }


    //returns the number of items within the hash table
    public int length(){
        return N;
    }


    /*resize creates a new PHPArray with the new resized capacity, iterates over the original PHPArray taking the values and adding
    them to the new PHPArray*/
    private void resize(int capac){
        System.out.println("\t\tSize: " + N + " -- resizing array from " + M + " to "+ capac);
        PHPArray<V> temp = new PHPArray<V>(capac);
        NodeIterator rehashIterator = iterator();
        while(rehashIterator.hasNext()){
                temp.put(rehashIterator.current.getKey(), rehashIterator.next());
        }
        root = temp.root;
        table = temp.table;
        pointer = temp.pointer;
        M = capac;
    }




    public void put(String key, V value){

        //checks to see if the hash table is half full
        if(N >= M/2){
            resize(M * 2);
        }

        int i = hash(key);
        while(table [i] != null){ //if the index within the hash table has a node assigned already, compare keys and iterator to the next space within the table
            if(table[i].getKey().equals(key)){
                table[i].setValue(value);   //if keys are the same, update the value
                return;
            }
            i = (i + 1) % M;
        }

        table[i] = new Node<V>(key, value);   //if node doesn't exist, create a new node with the key, value
        if(root == null){   //checks if this is the first node added to the hash table

            root=pointer=table[i];

        }else {

                pointer.setNext(table[i]);  //taking previous pointer and setting it's next node value to the new value added
                table[i].setPrev(pointer);  //set the new node's previous pointer to the node added before
                pointer = table[i];   //update pointer variable to node just added

        }
        N++;

    }

    //same functionality as previous put value but will execute if the key value passed in is an integer
    public void put(int key, V value){

        if(N >= M/2){
            resize(M * 2);
        }

        String stringKey = Integer.toString(key);
        int i = hash(stringKey);
        while(table [i] != null){

            if(table[i].getKey().equals(stringKey)){
                table[i].setValue(value);
                return;
            }
            i = (i + 1) % M;
        }
        table[i] = new Node<V>(stringKey, value);

        if(root == null){
            root=pointer=table[i];
        }else {
            pointer.setNext(table[i]);//taking previous pointer and setting next node
            table[i].setPrev(pointer);
            pointer = table[i];
        }
        N++;
    }


    //will take a key and return that key's value
    public V get(String key){
        int i = hash(key);
        if(table[i] == null) {    //check to see if key exists in the first place
            return null;
        }

        /*will check to see if key argument matches the key in the specified index of the table.
        If keys do not match up, check to see if key is in the next location due to linear probing
        */
        while (!table[i].getKey().equals(key)) {
            i = (i + 1) % M;
            if(table[i] == null) {    //will break loop if next index checked is equal null
                return null;
            }
        }

        if(table[i].getKey().equals(key)){
            return table[i].getValue();
        }else{
            return null;
        }
    }


    //same functionality as previous method but will take the key parameter as an integer
    public V get(int key) {
        String stringKey = Integer.toString(key);
        int i = hash(stringKey);
        if(table[i] == null) {
            return null;
        }

        while(!table [i].getKey().equals(stringKey)){

            i = (i + 1) % M;
            if(table[i] == null) {
                return null;
            }
        }

        if(table[i].getKey().equals(stringKey)){
            return table[i].getValue();
        }else{
            return null;
        }
    }

    /*
        This will remove the data from the hash set and rearrange the node's pointers
        so the iterator will be working on the edited data. After the correct node and it's data
        is removed, the method will rehash any data in the cluster following the removed node to maintain
        our strategy of linear probing.
     */

    public void unset(String key){
        //ensures that the key that is going to be deleted is there in the first place
        if(get(key) == null){
            return;
        }

        //find the index of the key
        int i = hash(key);
        while(!table [i].getKey().equals(key)){
            i = (i + 1) % M;
        }

        //swap pointers so can delete without ruining linked list
        if(!table[i].hasPrev()){    //if the node we are deleting does not have a previous pointer, then we are deleting the root
            root=table[i].getNext();    //set the root to the next node in order
            PHPArray.Node<V> tempNext = table[i].getNext();
            tempNext.setPrev(null); //set the new root's previous pointer to null
            table[i] = null;  //delete the current node and it's data
        }else{  //case for deleting any node (besides the root) in the hash table
            PHPArray.Node<V> tempNext = table[i].getNext();
            PHPArray.Node<V> tempPrev = table[i].getPrev();
            tempPrev.setNext(tempNext);
            tempNext.setPrev(tempPrev);
            table[i] = null;
        }

        i = ( i+1 ) % M;

        //rehash data following the recently deleted node until the while loop finds an empty index in the hash table
        while(table[i] != null){
          String tempKey = table[i].getKey();
          V tempValue = table[i].getValue();

          System.out.println("\t\tKey " + tempKey + " rehashed...\n");

          PHPArray.Node<V> tempPointer = pointer;
          PHPArray.Node<V> tempNext = table[i].getNext(); //getting a temporary next pointer because get does not assign next node pointer to current node
          pointer = table[i].getPrev();
          table[i] = null;
          N--;

          put(tempKey, tempValue);
          pointer.setNext(tempNext);
          pointer = tempPointer;

          i = (i + 1) % M;
        }

        N--;

    }


    /*
        Same functionality as the previous unset method but can except an int as a key and will
        convert the int into a string.
     */

    public void unset(int key){

        String stringKey = Integer.toString(key);
        //ensures that the key that is going to be deleted is there in the first place
        if(get(stringKey) == null){
            return;
        }

        //find the index of the key
        int i = hash(stringKey);
        while(!table [i].getKey().equals(stringKey)){
            i = (i + 1) % M;
        }

        //swap pointers so can delete without ruining linked list
        if(!table[i].hasPrev()){
            root = table[i].getNext();
            PHPArray.Node<V> tempNext = table[i].getNext();
            tempNext.setPrev(null);
            table[i] = null;
        }else{
            PHPArray.Node<V> tempNext = table[i].getNext();
            PHPArray.Node<V> tempPrev = table[i].getPrev();

            tempPrev.setNext(tempNext);
            tempNext.setPrev(tempPrev);
            table[i] = null;
        }

        i = (i + 1) % M;

        while(table[i] != null){
            String tempKey = table[i].getKey();
            V tempValue = table[i].getValue();
            System.out.println("\t\tKey " + tempKey + " rehashed...\n");

            PHPArray.Node<V> tempPointer = pointer;
            PHPArray.Node<V> tempNext = table[i].getNext(); //getting a temporary next pointer because get does not assign next node pointer to current node
            pointer = table[i].getPrev();
            table[i] = null;
            N--;
            put(tempKey, tempValue);
            pointer.setNext(tempNext);
            pointer = tempPointer;

            i = (i + 1) % M;
        }

        N--;

    }


    /*
        The values method will create an arraylist of values and (using an iterator) iterate through the table of nodes
        adding the value from the node to the arraylist.
     */
    public ArrayList<V> values(){
        NodeIterator it = iterator();
        V temp;
        ArrayList<V> result = new ArrayList<V>();
        while(it.hasNext()){
            temp = it.next();
            result.add(temp);
        }
        return result;
    }


    /*
        The keys method will create an arraylist of values and (using an iterator) iterate through the table of nodes
        adding the value from the node to the arraylist.
     */
    public ArrayList<String> keys(){
        NodeIterator it = iterator();
        String temp;
        ArrayList<String> result = new ArrayList<String>();
        while(it.hasNext()){
            temp = it.nextKey();
            result.add(temp);
        }
        return result;
    }

    /*
        shotTable method will use a for loop and iterate through the entire hash table displaying the key and value in every index
     */
    public void showTable(){
        System.out.println("\tRaw Hash Table Contents:");
        for(int i = 0; i < M; i++){
            if(table[i] != null){
                System.out.println(i + ": Key: " + table[i].getKey() + " Value: " + table[i].getValue());
            }else{
                System.out.println(i + ": " + "null");
            }
        }
    }


    /*
        The pair class allows the user to create a pair object with the key, value pair maintaining the generic type
     */

    public static class Pair<V>{
        public String key;
        public V value;
    }

    /*
        each method will use the global iterator pairIterator to iterate through the nodes
        adding the next key and value into a Pair object.
     */
    public PHPArray.Pair<V> each(){
        PHPArray.Pair<V> p = new PHPArray.Pair<V>();    //creates the pair object that will be returned

        if(pairCount == 0){   //if pairCount is zero, then initialize the iterator
            pairIterator = iterator();
        }

        if(pairIterator.hasNext()){ //put current node's value and key into the Pair object
            p.key = pairIterator.current.getKey();
            p.value = pairIterator.next();
        }else{
            return null;
        }

        pairCount++;
        return p;
    }

    /*
        the reset method will reset the pairCounter value to zero so the next time the each method is called, the global iterator pairIterator
        will be reinitialized to a new NodeIterator
     */
    public void reset(){
        pairCount=0;
    }

    /*
        sort method will sort the nodes of the hash table by value, assign new
        keys to the nodes based on the index of the sorted nodes and will
        update the pointers within the nodes so the iterator can be called on the
        edited data
     */

    public void sort(){
        ArrayList<Node<V>> list = new ArrayList<Node<V>>();  //create an array list of the nodes
        NodeIterator sortIterator = iterator();

        while(sortIterator.hasNext()){  //use the iterator to add the nodes from the hash table to the arraylist of nodes
            list.add(sortIterator.current);
            sortIterator.next();
        }

        Collections.sort(list); //sort the nodes based on the node's value using the Iterable interface implemented on each node
        PHPArray<V> tempPHPArray = new PHPArray<V>(list.size());   //temporary PHPArray to hold sorted hash table

        int x = 0;
        PHPArray.Node<V> tempListNode;
        while(x < list.size()){   //loop through each node in the arraylist of nodes
            tempListNode = list.get(x);
            if (x==0){  //case for the root node
                tempListNode.setKey(x);
                tempListNode.setPrev(null);
                tempListNode.setNext(list.get(x+1));

                tempPHPArray.table[x]=tempListNode; //add the node to the temp PHPArray
                tempPHPArray.root=tempListNode;
                x++;
                continue;
            }

            if(x==list.size()-1){   //case for the last node in the arraylist
                tempListNode.setKey(x);
                tempListNode.setPrev(list.get(x-1));
                tempListNode.setNext(null);

                tempPHPArray.table[x]=tempListNode;   //add the node to the temp PHPArray
                x++;
                continue;
            }

            tempListNode.setKey(x);
            tempListNode.setPrev(list.get(x-1));
            tempListNode.setNext(list.get(x+1));

            tempPHPArray.table[x]=tempListNode;   //add the node to the temp PHPArray
            x++;
        }

        table = tempPHPArray.table;   //update the table within the current object to the temporary PHPArray sorted table
        pointer = tempPHPArray.pointer;
        root = tempPHPArray.root;
        M = list.size();

    }

    /*
        asort method is the same as sort method but will leave the key value the same as it was before the data was sorted
        instead of updating the key value to the index within the hash table. Also the asort method hashes the sorted data to
        the temporary arrayList instead of placing the data directly in the table like in the sort method above
     */
    public void asort(){
        ArrayList<Node<V>> list = new ArrayList<Node<V>>();
        NodeIterator sortIterator = iterator();

        while(sortIterator.hasNext()){
            list.add(sortIterator.current);
            sortIterator.next();
        }

        Collections.sort(list);

        PHPArray<V> tempPHPArray = new PHPArray<V>(list.size()*2);

        int x = 0;
        PHPArray.Node<V> tempListNode;
        while(x < list.size()){
            tempListNode = list.get(x);
            if (x == 0){  //case for root node
                tempListNode.setPrev(null);
                tempListNode.setNext(list.get(x+1));
                tempPHPArray.put(tempListNode.getKey(), tempListNode.getValue());
                x++;
                continue;
            }

            if(x==list.size()-1){   //case for last node in the list
                tempListNode.setPrev(list.get(x-1));
                tempListNode.setNext(null);
                tempPHPArray.put(tempListNode.getKey(), tempListNode.getValue());
                x++;
                continue;
            }

            tempListNode.setPrev(list.get(x-1));
            tempListNode.setNext(list.get(x+1));
            tempPHPArray.put(tempListNode.getKey(), tempListNode.getValue());

            x++;
        }

        table=tempPHPArray.table;
        pointer=tempPHPArray.pointer;
        root=tempPHPArray.root;
        M=tempPHPArray.M;
    }

    /*
        array_flip method uses the iterator to move through the nodes and will take each node's key and value and put them
        in reverse order into the PHPArray of type String.
     */

    public PHPArray<String> array_flip(){
        PHPArray<String> stringNodeArray = new PHPArray<String>(M);
        NodeIterator arrayFlipIterator = iterator();

        while(arrayFlipIterator.hasNext()){
            PHPArray.Node<V> tempNode = arrayFlipIterator.current;
            stringNodeArray.put((String)tempNode.getValue(), tempNode.getKey());
            arrayFlipIterator.next();
        }

        return stringNodeArray;

    }


    /*
       Creates an instance of the NodeIterator class.
     */
    public NodeIterator iterator() {
        return new NodeIterator();
    }


    //Iterates through the nodes in the table in the order they were added.
    private class NodeIterator implements Iterator<V>{

        private Node<V> current;

        public NodeIterator(){
            current = root;
        }


        public boolean hasNext(){
            return current != null;
        }

        public V next(){
            V result = current.getValue();
            current = current.getNext();
            return result;
        }

        public String nextKey(){
            String keyResult=current.getKey();
            current=current.getNext();
            return keyResult;
        }

    }


    private static class Node<V> implements Comparable<Node<V>>{
        private V value;
        private String key;
        private PHPArray.Node<V> prev;
        private PHPArray.Node<V> next;

        public Node(String key, V value){
            this.key=key;
            this.value=value;
            prev=null;
            next=null;
        }

        public V getValue() { return this.value; }

        public void setValue(V val) { this.value = val; }

        public boolean hasValue() { return this.value != null; }

        public String getKey() { return this.key; }

        public void setKey(String k) { this.key = k; }

        public void setKey(int k) {  //can set key as a string when argument sent is an integer
            String stringKey = Integer.toString(k);
            this.key = stringKey;
        }

        public boolean hasKey() { return this.key != null; }

        public boolean hasPrev() { return this.prev!=null; }

        public void setPrev(PHPArray.Node<V> p) { this.prev = p; }

        public PHPArray.Node<V> getPrev() { return this.prev; }

        public boolean hasNext() { return this.next != null; }

        public void setNext(PHPArray.Node<V> n) { this.next = n; }

        public PHPArray.Node<V> getNext() { return this.next; }

		@SuppressWarnings("unchecked")
        public int compareTo(Node<V> other){        //compareTo method which is specified in the iterable interface
            Comparable val1 = (Comparable)(this.getValue());  //converts the value of the node to a comparable value
            Comparable val2 = (Comparable)(other.getValue()); //converts the value of the node comparing into a comparable value
            return val1.compareTo(val2);
        }
    }
}
