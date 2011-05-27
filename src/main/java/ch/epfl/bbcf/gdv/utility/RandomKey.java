package ch.epfl.bbcf.gdv.utility;

import java.math.BigInteger;
import java.security.SecureRandom;

public class RandomKey {

	private SecureRandom random;
	
	public RandomKey(){
		this.random = new SecureRandom();
	}
	public String getRandom(){
		 return new BigInteger(130, random).toString(32);
	}
}
