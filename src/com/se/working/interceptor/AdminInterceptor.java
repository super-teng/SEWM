package com.se.working.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.se.working.entity.User;
import com.se.working.entity.UserAuthority.UserAuthorityType;
/**
 * 管理员操作验证
 * @author BO
 *
 */
public class AdminInterceptor implements HandlerInterceptor {

	public AdminInterceptor() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void afterCompletion(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, Exception arg3)
			throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void postHandle(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, ModelAndView arg3)
			throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object object) throws Exception {
		// TODO Auto-generated method stub
		User user = (User) request.getSession().getAttribute("user");
		if (user.getUserAuthority().getLevel() >= UserAuthorityType.ADAMIN) {
			return true;
		}
		return false;
	}

}
