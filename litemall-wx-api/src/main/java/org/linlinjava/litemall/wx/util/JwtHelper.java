package org.linlinjava.litemall.wx.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtHelper {
	private static String secret;
	
	@Value("${litemall.security.jwt.secret:litemall-default-secret-key-please-change-in-production}")
	public void setSecret(String secret) {
		JwtHelper.secret = secret;
	}
	
	private static final String ISSUSER = "LITEMALL";
	private static final String SUBJECT = "this is litemall token";
	private static final String AUDIENCE = "MINIAPP";
	
	
	public String createToken(Integer userId){
		try {
		    Algorithm algorithm = Algorithm.HMAC256(secret);
		    Map<String, Object> map = new HashMap<String, Object>();
		    Date nowDate = new Date();
		    Date expireDate = getAfterDate(nowDate,0,0,0,2,0,0);
	        map.put("alg", "HS256");
	        map.put("typ", "JWT");
		    String token = JWT.create()
		    	.withHeader(map)
		    	.withClaim("userId", userId)
		        .withIssuer(ISSUSER)
		        .withSubject(SUBJECT)
		        .withAudience(AUDIENCE)
		        .withIssuedAt(nowDate)
		        .withExpiresAt(expireDate)
		        .sign(algorithm);
		    return token;
		} catch (JWTCreationException exception){
			exception.printStackTrace();
		}
		return null;
	}
	
	public Integer verifyTokenAndGetUserId(String token) {
		try {
		    Algorithm algorithm = Algorithm.HMAC256(secret);
		    JWTVerifier verifier = JWT.require(algorithm)
		        .withIssuer(ISSUSER)
		        .build();
		    DecodedJWT jwt = verifier.verify(token);
		    Map<String, Claim> claims = jwt.getClaims();
		    Claim claim = claims.get("userId");
		    return claim.asInt();
		} catch (JWTVerificationException exception){
		} catch (IllegalArgumentException exception){
		}
		
		return 0;
	}
	
	public  Date getAfterDate(Date date, int year, int month, int day, int hour, int minute, int second){
		if(date == null){
			date = new Date();
		}
		
		Calendar cal = new GregorianCalendar();
		
		cal.setTime(date);
		if(year != 0){
			cal.add(Calendar.YEAR, year);
		}
		if(month != 0){
			cal.add(Calendar.MONTH, month);
		}
		if(day != 0){
			cal.add(Calendar.DATE, day);
		}
		if(hour != 0){
			cal.add(Calendar.HOUR_OF_DAY, hour);
		}
		if(minute != 0){
			cal.add(Calendar.MINUTE, minute);
		}
		if(second != 0){
			cal.add(Calendar.SECOND, second);
		}
		return cal.getTime();
	}
	
}
