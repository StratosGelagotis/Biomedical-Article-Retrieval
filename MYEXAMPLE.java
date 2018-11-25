import gr.uoc.csd.hy463.Topic;
import gr.uoc.csd.hy463.TopicsReader;
import java.util.ArrayList;

public class MYEXAMPLE {
	public static void main(String[] args) throws Exception {
		ArrayList<Topic> topics = TopicsReader.readTopics("./CollectionIndex/topics.xml");
		for (Topic topic : topics) {
			System.out.println(topic.getNumber());
			System.out.println(topic.getType());
			System.out.println(topic.getSummary());
			System.out.println("----------------");
		}
	}
}