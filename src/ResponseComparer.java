import java.util.Comparator;

public class ResponseComparer implements Comparator<Response>{
	public int compare(Response a, Response b) {
		return a.getResponse().compareTo(b.getResponse());
	}
}
