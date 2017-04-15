/**
 * 
 */
package com.vendor.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.ecarry.core.dao.IGenericDao;
import com.ecarry.core.dao.SQLUtils;
import com.ecarry.core.security.UserDetailsService;
import com.vendor.po.User;
import com.vendor.po.UserBind;
import com.vendor.util.Commons;

/**
 * @author dranson 2010-12-11
 */
public class OAuthUserService implements UserDetailsService {
	
	private IGenericDao genericDao;

	/* (non-Javadoc)
	 * @see org.springframework.security.core.userdetails.UserDetailsService#loadUserByUsername(java.lang.String)
	 */
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		StringBuffer buf = new StringBuffer("SELECT ");
		buf.append(SQLUtils.getColumnsSQL(User.class, "A")).append(",B.CODE companyCode,B.NAME companyName,B.COMPANY_ID companyId,A.ORG_ID userType ");
		buf.append("FROM SYS_USER A LEFT JOIN SYS_ORG B ON A.COMPANY_ID=B.ID WHERE A.USERNAME=?");
		User user = genericDao.findT(User.class, buf.toString(), username);
		if (user == null)
			throw new UsernameNotFoundException("user not found");
		buf.setLength(0);
		buf.append("SELECT A.USER_ID,A.THIRD_KEY,A.THIRD_TYPE FROM T_USER_BIND A WHERE USER_ID=?");
		List<UserBind> ubs = genericDao.findTs(UserBind.class,buf.toString(),user.getId());
		user.setNull();
		for (UserBind b : ubs) {
			if (Commons.THIRD_PARTY_FB.equals(b.getThirdType())) {
				user.getBound().add(Commons.THIRD_PARTY_FB);
			} else if (Commons.THIRD_PARTY_QQ.equals(b.getThirdType())) {
				user.getBound().add(Commons.THIRD_PARTY_QQ);
			} else if (Commons.THIRD_PARTY_WB.equals(b.getThirdType())) {
				user.getBound().add(Commons.THIRD_PARTY_WB);
			} else if (Commons.THIRD_PARTY_WX.equals(b.getThirdType())) {
				user.getBound().add(Commons.THIRD_PARTY_WX);
			}
		}
		if (user != null) {
			List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
			authorities.add(new GrantedAuthority() {
				@Override
				public String getAuthority() {
					return "ROLE_MOBILE";
				}
			});
			user.setAuthorities(authorities);
		}
		return user;
	}

	/* (non-Javadoc)
	 * @see com.change.core.security.UserDetailsService#loadAuthorities(org.springframework.security.core.userdetails.UserDetails)
	 */
	@Override
	public void loadAuthorities(UserDetails userDetails) {
		// TODO Auto-generated method stub
		
	}

	public void setGenericDao(IGenericDao genericDao) {
		this.genericDao = genericDao;
	}
}
