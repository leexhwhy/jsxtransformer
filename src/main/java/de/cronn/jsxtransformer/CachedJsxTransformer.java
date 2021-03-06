package de.cronn.jsxtransformer;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import org.mozilla.javascript.JavaScriptException;

/**
 * Class for caching javascript assets in jsx transformed and minified form
 *
 * @author Hanno Fellmann, cronn GmbH
 */
public class CachedJsxTransformer {
	private BabelTransformer jsxTransformer;

	private UglifyTransformer uglifier;

	private final HashMap<String, AssetEntry> jsCache = new HashMap<String, AssetEntry>();

	private boolean recompile;

	public CachedJsxTransformer(boolean uglify, boolean recompile) throws IOException {
		jsxTransformer = new BabelTransformer();
		if (uglify) {
			uglifier = new UglifyTransformer();
		} else {
			uglifier = null;
		}
		this.recompile = recompile;
	}

	/**
	 * Entry in asset Cache.
	 */
	public static class AssetEntry {
		/**
		 * The compiled cache content.
		 */
		public final String content;

		/**
		 * Etag based on the uncompiled input content
		 */
		public final String etag;

		private AssetEntry(final String content, final String etag) {
			super();
			this.content = content;

			this.etag = etag;
		}
	}

	/**
	 * Get compiled JSX file from cache
	 * 
	 * Gets the specified JSX entry from cache or recompiles it if it is
	 * non-existent or has changed
	 * 
	 * @param key
	 *            The key for the entry
	 * @param contentProvider Provider for the file content.
	 * 
	 * @return The compiled content as JavaScript.
	 * 
	 * @throws IOException
	 *             on compile or file read/write error.
	 * @throws JavaScriptException
	 *             on compilation error
	 */
	public synchronized AssetEntry get(String key, ContentProvider contentProvider) throws JavaScriptException, IOException {
		final AssetEntry entry = jsCache.get(key);

		if (entry != null && !recompile) {
			return entry;
		}

		String content;
		content = contentProvider.get(key);
		final String etag = UUID.nameUUIDFromBytes(content.getBytes()).toString();

		if (entry == null || !entry.etag.equals(etag)) {
			return getNewEntry(key, content, etag);
		}

		return entry;
	}

	private AssetEntry getNewEntry(final String fileName, String content, String etag) throws IOException {
		String newContent = null;

		if (newContent == null) {
			newContent = jsxTransformer.transform(content);
			if (uglifier != null) {
				newContent = uglifier.transform(newContent);
			}
		}

		final AssetEntry entry = new AssetEntry(newContent, etag);
		jsCache.put(fileName, entry);
		return entry;
	}

	/**
	 * Interface for supplying JSX file content
	 * 
	 * @author Hanno Fellmann, cronn GmbH
	 */
	public interface ContentProvider {
		public String get(String key) throws IOException;
	}

}
