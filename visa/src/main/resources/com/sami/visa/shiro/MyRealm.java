package net.sahv.blzy.shiro;

import java.security.Principal;
import java.util.Calendar;
import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.ExpiredCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.pam.UnsupportedTokenException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.subject.WebSubject;
import org.springframework.beans.factory.annotation.Autowired;

import net.sahv.blzy.model.Employee;
import net.sahv.blzy.service.EmployeeService;
import net.sahv.blzy.util.Security;

public class MyRealm extends AuthorizingRealm {

	private final Logger log = Logger.getLogger(MyRealm.class);
	
	@Autowired
	private EmployeeService employeeService;
	 /** 
     * 为当前登录的Subject授予角色和权限 
     * @see  经测试:本例中该方法的调用时机为需授权资源被访问时 
     * @see  经测试:并且每次访问需授权资源时都会执行该方法中的逻辑,这表明本例中默认并未启用AuthorizationCache 
     * @see  个人感觉若使用了Spring3.1开始提供的ConcurrentMapCache支持,则可灵活决定是否启用AuthorizationCache 
     * @see  比如说这里从数据库获取权限信息时,先去访问Spring3.1提供的缓存,而不使用Shior提供的AuthorizationCache 
     */ 
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		log.debug("doGetAuthorizationInfo");
		//获取当前登录的用户名,等价于(String)principals.fromRealm(this.getName()).iterator().next()  
        String currentUsername = (String)super.getAvailablePrincipal(principals);
        //Principal principal = (Principal) principals.fromRealm(getName()).iterator().next();
        Subject currentUser = SecurityUtils.getSubject(); 
        Object id=currentUser.getSession().getAttribute("id");
		if (id!=null&&currentUsername != null) {
			//List<String> authorities = employeeService.findEmployeeById(Integer.parseInt(id.toString()));
			//if (authorities != null) {
				SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
				authorizationInfo.addStringPermission("all");
				return authorizationInfo;
			///}
		}
		return null;  
	}

	/** 
     * 验证当前登录的Subject 
     * @see  经测试:本例中该方法的调用时机为LoginController.login()方法中执行Subject.login()时 
     */ 
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken) throws AuthenticationException {
		log.debug("doGetAuthenticationInfo");
		//获取基于用户名和密码的令牌  
        //实际上这个authcToken是从LoginController里面currentUser.login(token)传过来的  
        //两个token的引用都是一样的
		MyAuthenticationToken token = (MyAuthenticationToken)authcToken;  
        String userName=token.getUsername();  
        String checkCode=token.getCheckCode();
        String password=new String(token.getPassword());
        String type=token.getType();
        ServletRequest request = ((WebSubject)SecurityUtils.getSubject()).getServletRequest();   
        HttpSession httpSession = ((HttpServletRequest)request).getSession(); 
        Object code2=httpSession.getAttribute("validateCode");
        Object passwordCode=httpSession.getAttribute("passwordCode");
        if(checkCode==null||"".equals(checkCode)||code2==null||!checkCode.equalsIgnoreCase(code2.toString())){
        	throw new UnsupportedTokenException();
        }
        Employee emp=employeeService.findEmpByLoginName(userName);
        if (emp == null) {
			//throw new UnknownAccountException();
        	return null;
		}
        //更新登录信息
        Calendar cal=Calendar.getInstance();
        emp.seteLoginIp(token.getHost());
        emp.seteLoginTime(cal.getTime());
        employeeService.updateEmp(emp);
        //判断用户状态
		if (emp.geteLogin()==null||emp.geteLogin()!=1) {
			if(emp.geteLogin()==null||emp.geteLogin()==0){
				throw new DisabledAccountException();
			}
			else{
				throw new LockedAccountException();
			}
		}
		//判断密码过期
		if(emp.getePasswordDate()==null||cal.getTime().getTime()>=emp.getePasswordDate().getTime()){
			//密码己过期
			throw new ExpiredCredentialsException();
		}
		String passWordStr=password;
		if(!"1".equals(type)){
			passWordStr=(passwordCode==null?"":passwordCode.toString())+emp.getePassword();
			passWordStr=Security.getSHA1(passWordStr);
		}
		AuthenticationInfo authcInfo = new SimpleAuthenticationInfo(userName,passWordStr, this.getName());
		this.setSession("userName", emp.geteLoginName()); 
		this.setSession("name", emp.geteName()); 
		this.setSession("phone", emp.getePhone()); 
		this.setSession("job", emp.geteJobTitle()); 
		this.setSession("id", emp.geteId());
		if(emp.geteSex() != null){
			if(emp.geteSex() == 1){
				this.setSession("sex", "男");
			}else{
				this.setSession("sex", "女");
			}
		}
		else{
			this.setSession("sex", " ");
		}
		return authcInfo;  
//      User user = userService.getByUsername(token.getUsername());  
//      if(null != user){  
//          AuthenticationInfo authcInfo = new SimpleAuthenticationInfo(user.getUsername(), user.getPassword(), user.getNickname());  
//          this.setSession("currentUser", user);  
//          return authcInfo;  
//      }else{  
//          return null;  
//      }  
        //此处无需比对,比对的逻辑Shiro会做,我们只需返回一个和令牌相关的正确的验证信息  
        //说白了就是第一个参数填登录用户名,第二个参数填合法的登录密码(可以是从数据库中取到的,本例中为了演示就硬编码了)  
        //这样一来,在随后的登录页面上就只有这里指定的用户和密码才能通过验证  
//        if("admin".equals(token.getUsername())){  
//            AuthenticationInfo authcInfo = new SimpleAuthenticationInfo("admin",token.getPassword(), this.getName());  
//            this.setSession("currentUser", "mym");  
//            
//            return authcInfo;  
//        }
//        //没有返回登录用户名对应的SimpleAuthenticationInfo对象时,就会在LoginController中抛出UnknownAccountException异常  
//        return null;  
	}
	/** 
     * 将一些数据放到ShiroSession中,以便于其它地方使用 
     * @see  比如Controller,使用时直接用HttpSession.getAttribute(key)就可以取到 
     */  
    private void setSession(Object key, Object value){  
        Subject currentUser = SecurityUtils.getSubject();  
        if(null != currentUser){  
            Session session = currentUser.getSession();  
            //System.out.println("Session默认超时时间为[" + session.getTimeout() + "]毫秒");  
            if(null != session){  
                session.setAttribute(key, value);  
            }  
        }  
    }  
}
