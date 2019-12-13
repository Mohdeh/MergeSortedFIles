import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.io.*;
import java.util.*;

public class MergeSort {
	
	public class ComparableFile <T extends Comparable<T>> implements Comparable<ComparableFile<T>> {
		
		private final Deserializer<T> deserializer;
	    private final Iterator<String> lines;
	    private T buffered;
	    
	    public ComparableFile(File file, Deserializer<T> deserializer) {
	        this.deserializer = deserializer;
	        try {
	            this.lines = Files.newBufferedReader(file.toPath()).lines().iterator();
	        } catch (IOException e) {
	            throw new UncheckedIOException(e);
	        }
	    }
	    
	    @Override
	    public int compareTo(ComparableFile<T> that) {
	        T first = peek();
	        T second = that.peek();

	        if (first == null) return second == null ? 0 : -1;
	        if (second == null) return 1;
	        return first.compareTo(second);
	    }
	    
	    public T pop() {
	        T tmp = peek();

	        if (tmp != null) {
	            buffered = null;
	            return tmp;
	        }

	        throw new NoSuchElementException();
	    }
	    
	    private T peek() {
	        if (buffered != null) return buffered;
	        if (!lines.hasNext()) return null;
	        return buffered = deserializer.deserialize(lines.next());
	    }
	    
	    public boolean isEmpty() {
	        return peek() == null;
	    }
	}

	public class MergeFiles<T extends Comparable<T>> {
	    private final PriorityQueue<ComparableFile<T>> files;

	    public MergeFiles(List<File> files, Deserializer<T> deserializer) {
	        this.files = new PriorityQueue<>(files.stream()
	                .map(file -> new ComparableFile<>(file, deserializer))
	                .filter(comparableFile -> !comparableFile.isEmpty())
	                .collect(toList()));
	    }

	    public Iterator<T> getSortedElements() {
	        return new Iterator<T>() {
	            @Override
	            public boolean hasNext() {
	                return !files.isEmpty();
	            }

	            @Override
	            public T next() {
	                if (!hasNext()) throw new NoSuchElementException();
	                ComparableFile<T> head = files.poll();
	                T next = head.pop();
	                if (!head.isEmpty()) files.add(head);
	                return next;
	            }
	        };
	    }
	}

	private static File newTempFile(List<String> words) throws IOException {
	    File tempFile = File.createTempFile("sorted-", ".txt");
	    Files.write(tempFile.toPath(), words);
	    tempFile.deleteOnExit();
	    return tempFile;
	}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		List<File> files = Arrays.asList(
	            newTempFile(Arrays.asList("hello", "world")),
	            newTempFile(Arrays.asList("professional", "java8", "programming")),
	            newTempFile(Arrays.asList("New York", "stock", "exchange"))
	    );

	    Iterator<String> sortedElements = new MergeFiles<>(files, line -> line).getSortedElements();
	    while (sortedElements.hasNext()) {
	        System.out.println(sortedElements.next());
	    }

	}

}
