package preprocessutils;

public class TwitterPreprocessing {
	public static void main(String[] args)
	{
		
	}

//	@humarakausar hey humara! #smxchat
	public static String filterTweet(String origTweet)
	{
		String filtered = "";
		
		origTweet = origTweet.replaceAll("", "'");
//		filtered = origTweet.replaceAll("@[\\w|\\d]+", "targetuser").replaceAll("#", "").replaceAll("\"", "").replaceAll("\\*", "").replaceAll("�", "");
		filtered = origTweet.replaceAll("@\\s?[\\w|\\d]+", "@ username").replaceAll("\"", "").replaceAll("\\*", "").replaceAll("�", "");
		
		filtered = filtered.replaceAll("#", " # ").replaceAll("^\"", "").replaceAll("\"$", "").replaceAll(" +", " ");
		return filtered;
	}

}
