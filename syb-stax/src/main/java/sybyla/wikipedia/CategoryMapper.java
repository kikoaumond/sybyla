package sybyla.wikipedia;

import java.io.IOException;

public class CategoryMapper {
	
	private static final StAXParser PARSER=new StAXParser();
	private static PageCategoryProcessor processor;
	
	public static void main(String[] args){
		
		String input = args[0];
		String categoryMap = args[1];
		String synonyms = args[2];

		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					processor.close();
				} catch (IOException e) {
					System.out.println("Error closing processor "+e);
				}
			}
		});
		
		try {
			processor = new PageCategoryProcessor(categoryMap, synonyms);
			PARSER.read(input, processor);
			processor.close();
		} catch (IOException e) {
			System.out.println(e);
		}
	}
}
