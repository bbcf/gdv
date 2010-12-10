package ch.epfl.authentication;

import ch.epfl.bbcf.gdv.config.Application;
import ch.epfl.tequila.client.model.ClientConfig;
import ch.epfl.tequila.client.model.TequilaPrincipal;
import ch.epfl.tequila.client.service.TequilaService;

public class TequilaAuthentication {

	public static final ClientConfig _clientConfig = init();
	public static final TequilaService _tequilaService = TequilaService.instance();
	/**
	 * The name of the session attribute holding the tequila key.
	 */
	public static final String TEQUILA_KEY = "TEQUILA_KEY";

	/**
	 * The name of the session attribute holding the TequilaPrincipal after successful authentification.
	 */
	public static final String TEQUILA_PRINCIPAL = "TEQUILA_PRINCIPAL";

	private static ClientConfig init() {
		ClientConfig config = new ClientConfig();
		config.setHost("tequila.epfl.ch");
		config.setLanguage("english");
		config.setService("GDV authentication service");
		config.setRequest("name firstname email title unit office phone user");
		config.setAllows("categorie=SHIBBOLETH");
		//config.setAllows("categorie=Guest");
		return config;
	}
	public static TequilaPrincipal validateKey(String key) throws Exception{
		_clientConfig.setHost("tequila.epfl.ch");
		return _tequilaService.validateKey(_clientConfig, key);
	}
}
