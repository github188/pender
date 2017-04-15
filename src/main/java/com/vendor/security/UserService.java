/**
 * 
 */
package com.vendor.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.ReflectionUtils;

import com.ecarry.core.dao.IGenericDao;
import com.ecarry.core.dao.SQLUtils;
import com.ecarry.core.domain.Menu;
import com.ecarry.core.security.UserDetailsService;
import com.ecarry.core.web.core.ContextUtil;
import com.vendor.po.User;

/**
 * @author dranson 2010-12-11
 */
public class UserService implements UserDetailsService {
	
	@Value("${system.user}")
	private String admin;
	
	@Value("${system.captcha}")
	private boolean captcha;
	
	@Value("${system.type}")
	private int sysType;
	
	private IGenericDao genericDao;

	/* (non-Javadoc)
	 * @see org.springframework.security.core.userdetails.UserDetailsService#loadUserByUsername(java.lang.String)
	 */
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		List<String> args = new ArrayList<String>(Arrays.asList(username.split("_")));
		if (captcha) {
			if (!ContextUtil.validateCaptcha(args.get(args.size() - 1)))
				throw new UsernameNotFoundException("captcha error");
			args.remove(args.size() - 1);
		}		
		StringBuffer buf = new StringBuffer("SELECT ");
		buf.append(SQLUtils.getColumnsSQL(User.class, "A"));
		buf.append(",B.COMPANY_ID companyId,B.CODE orgCode,B.NAME orgName ");
		buf.append("FROM SYS_USER A LEFT JOIN SYS_ORG B ON A.ORG_ID=B.ID WHERE").append((args.size() == 1 ? "" : " B.CODE=? AND") + " A.USERNAME=?");
		User user = genericDao.findT(User.class, buf.toString(), args.toArray());
		if (user == null)
			throw new UsernameNotFoundException("user not found");
		saveRetryCount(user);
		return user;
	}
	
	private void saveRetryCount(User user) {
		if (user.isEnabled()) {
			Object bean = ContextUtil.getBeanByName(Object.class, "dictionaryService");
			int total = (int) ReflectionUtils.invokeMethod(BeanUtils.findDeclaredMethod(bean.getClass(), "findDefaultRetryPassword"), bean);
			genericDao.execute("UPDATE SYS_USER SET RETRY=? WHERE ID=?", user.getRetry() + 1, user.getId());
			if (user.getRetry() >= total - 1) {
				genericDao.execute("UPDATE SYS_USER SET ENABLE=? WHERE ID=?", false, user.getId());
				user.setEnable(false);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.change.core.security.UserDetailsService#loadAuthorities(org.springframework.security.core.userdetails.UserDetails)
	 */
	@Override
	public void loadAuthorities(UserDetails user) {
		User curUser = (User) user;
		genericDao.execute("UPDATE SYS_USER SET RETRY=? WHERE ID=?", 0, curUser.getId());
		curUser.setSysType(sysType);
		if (user instanceof User && !user.getUsername().equals(admin)) {
			String sql;
			List<Menu> menus;
			
			sql = "SELECT A.RIGHTS,B.URL FROM SYS_RIGHTS A LEFT JOIN SYS_MENU B ON A.MENU_ID=B.ID WHERE A.ROLE_ID IN(SELECT ROLE_ID FROM SYS_USER_ROLE WHERE USER_ID=?) AND B.SYS_TYPE=?";
			menus = genericDao.findTs(Menu.class, sql, curUser.getId(), sysType);
			
			List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
			authorities.addAll(menus);
			((User) user).setAuthorities(authorities);
		}
	}

	public void setGenericDao(IGenericDao genericDao) {
		this.genericDao = genericDao;
	}
}
