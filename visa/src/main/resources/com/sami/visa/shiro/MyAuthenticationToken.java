/*
 * 

 * 
 */
package net.sahv.blzy.shiro;

import org.apache.log4j.Logger;
import org.apache.shiro.authc.UsernamePasswordToken;

/**
 * 登录令牌
 * 
 * 
 * @version 1.0
 */
public class MyAuthenticationToken extends UsernamePasswordToken {

	private static final long serialVersionUID = -5338569627824556813L;
	
	private final Logger log = Logger.getLogger(MyAuthenticationToken.class);
	
	/** 验证码 */
	private String checkCode;
	/**
	 *登录类型，0：密码登录，1：验证码登录 
	 */
	private String type="0";

	/**
	 * 自定义token的构错函数
	 * @param username
	 * @param password
	 * @param checkCode
	 * @param rememberMe
	 * @param host
	 */
	public MyAuthenticationToken(String username, String password,String checkCode, boolean rememberMe, String host) {
		super(username, password, rememberMe);
		this.checkCode=checkCode;
	}
	
	public MyAuthenticationToken(String type,String username, String password,String checkCode, boolean rememberMe) {
		super(username, password, rememberMe);
		this.type=type;
		this.checkCode=checkCode;
	}
	
	public MyAuthenticationToken(String username, String password,String checkCode, boolean rememberMe) {
		super(username, password, rememberMe);
		this.checkCode=checkCode;
	}

	public String getCheckCode() {
		return checkCode;
	}

	public void setCheckCode(String checkCode) {
		this.checkCode = checkCode;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}


}