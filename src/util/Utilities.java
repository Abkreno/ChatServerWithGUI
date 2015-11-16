package util;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by kady on 07/11/14.
 */
public class Utilities {
	/**
	 * Closes all the closeable objects passed in ,this is used to clean the
	 * resources after using it instead of the usage if try with resources as it
	 * causes problems with older JDKs (Which could be used in the uni)
	 * 
	 * @param items
	 *            a number of closeable objects
	 */
	public static void cleanResources(Closeable... items) {
		for (Closeable item : items) {
			if (item != null) {
				try {
					item.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
