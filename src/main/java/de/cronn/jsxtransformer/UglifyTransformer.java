package de.cronn.jsxtransformer;

import java.io.IOException;
import java.net.URISyntaxException;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;

/**
 * Uglify.js transformer
 *
 * @author Hanno Fellmann, cronn GmbH
 */
public class UglifyTransformer extends JsTransformBase {
	/**
	 * Create a new Uglify.js based transformer
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public UglifyTransformer() throws IOException {
		init("uglify-js", "/transformJs/uglify/");
	}

	@Override
	protected String transformInContext(final String jsx, final Context ctx) {
		final Function transform = (Function) exports.get("convenience", topLevelScope);
		return (String) transform.call(ctx, topLevelScope, exports, new String[] { jsx });
	}
}
