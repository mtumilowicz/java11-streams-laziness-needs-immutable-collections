import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by mtumilowicz on 2018-11-23.
 */
public class StreamTest {
    
    @Test
    public void mutable_list() {
//        given
        List<String> animals = new LinkedList<>();
        animals.add("cat");
        animals.add("tiger");
        animals.add("dog");

//        and
        Stream<String> animalStream = animals.stream();
        
//        when
        animals.add("elephant");
        animals.remove("tiger");
//        and
        List<String> animalsFromStream = animalStream.collect(Collectors.toUnmodifiableList());
        
//        then
        assertThat(animalsFromStream, is(List.of("cat", "dog", "elephant")));
    }
}
