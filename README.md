# java11-streams-laziness-needs-immutable-collections
Example how using mutable collections as source for 
streams may provoke cognitive breaks during processing.

_Reference_: https://docs.oracle.com/javase/10/docs/api/java/util/stream/package-summary.html

# preface
Streams differ from collections in several ways:
* **No storage. A stream is not a data structure that stores 
elements; instead, it conveys elements from a source such 
as a data structure, an array, a generator function, or 
an I/O channel, through a pipeline of computational 
operations**.

* **Functional in nature**. An operation on a stream produces 
a result, but does not modify its source. For example, 
filtering a Stream obtained from a collection produces a 
new Stream without the filtered elements, rather than 
removing elements from the source collection.

* **Laziness-seeking**. Many stream operations, such as 
filtering, mapping, or duplicate removal, can be 
implemented lazily, exposing opportunities for optimization. 
For example, "find the first String with three consecutive 
vowels" need not examine all the input strings. Stream 
operations are divided into intermediate (Stream-producing) 
operations and terminal (value- or side-effect-producing) 
operations. Intermediate operations are always lazy.

* **Possibly unbounded**. While collections have a finite size, 
streams need not. Short-circuiting operations such as 
limit(n) or findFirst() can allow computations on infinite 
streams to complete in finite time.

* **Consumable**. The elements of a stream are only visited 
once during the life of a stream. Like an Iterator, a new 
stream must be generated to revisit the same elements of 
the source.

# project description
We provide example how mutability of source causes cognitive
break during operations on the stream.
```
//        given
        List<String> animals = new LinkedList<>();
        animals.add("cat");
        animals.add("tiger");
        animals.add("dog");

//        and
        Stream<String> animalStream = animals.stream(); // 1)
        
//        when
        animals.add("elephant");
        animals.remove("tiger");
        
//        and
        List<String> animalsFromStream = animalStream.collect(Collectors.toUnmodifiableList());
        
//        then
        assertThat(animalsFromStream, is(List.of("cat", "dog", "elephant")));
```
So the stream created at `1)` operates on different elements
at `2)` - it does not make a copy of underlying source - it 
contains a reference to the source collection.

# technical details
1. calling `stream()` on `Collection`:
    * `stream()` is a default method
       ```
       default Stream<E> stream() {
           return StreamSupport.stream(spliterator(), false);
       }
       ```
    * A stream source is described by an abstraction called 
`Spliterator`. For more info about `spliterators` please refer 
my other github projects:
        * https://github.com/mtumilowicz/java11-spliterator
        * https://github.com/mtumilowicz/java11-spliterator-forkjoin
1. `spliterator()` in `Collections` contains reference 
    to source
    ```
    default Spliterator<E> spliterator() {
        return Spliterators.spliterator(this, 0);
    }
    ```
    for example constructor of `IteratorSpliterator`
    ```
    public IteratorSpliterator(Collection<? extends T> collection, int characteristics) {
        this.collection = collection;
        this.it = null;
        this.characteristics = (characteristics & Spliterator.CONCURRENT) == 0
                               ? characteristics | Spliterator.SIZED | Spliterator.SUBSIZED
                               : characteristics;
    }
    ```