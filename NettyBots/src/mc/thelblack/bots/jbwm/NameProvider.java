package mc.thelblack.bots.jbwm;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public final class NameProvider {

	private static volatile List<String> NAMES = new ArrayList<>();
	
	private static synchronized List<String> getNames() {
		return NameProvider.NAMES;
	}
	
	public static String getName() {
		if (NameProvider.getNames().isEmpty()) return null;
		
		return NameProvider.getNames().remove(0);
	}
	
	public static void returnName(String name) {
		NameProvider.getNames().add(name);
	}
	
	public static void loadNames(File f) throws IOException {
		FileReader reader = new FileReader(f);
		Scanner sc = new Scanner(reader);
		List<String> n = new ArrayList<>();
		
		while (sc.hasNext()) {
			n.add(sc.nextLine());
		}
		
		NameProvider.NAMES.addAll(n);
		Collections.shuffle(NameProvider.NAMES);
		
		sc.close();
		reader.close();
	}
}
