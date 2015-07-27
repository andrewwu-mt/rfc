package com.rfc.action;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.dispatcher.StrutsResultSupport;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionSupport;

public class ResultMessage extends StrutsResultSupport  {
	private static final long serialVersionUID = -5046390384916067270L;

	//private static Logger log = Logger.getLogger(ResultMessage.class);

	protected boolean prependServletContext = true;

	public ResultMessage() {
		super();
	}

	public ResultMessage(String location) {
		super(location);
	}

	public void setPrependServletContext(boolean prependServletContext) {
		this.prependServletContext = prependServletContext;
	}

	private static boolean isPathUrl(String url) {
		return (url.indexOf(':') == -1);
	}

	protected void doExecute(String finalLocation, ActionInvocation invocation)
			throws Exception {
		ActionContext ctx = invocation.getInvocationContext();
		
		HttpServletRequest request = (HttpServletRequest) ctx.get(ServletActionContext.HTTP_REQUEST);
		HttpServletResponse response = (HttpServletResponse) ctx.get(ServletActionContext.HTTP_RESPONSE);
		
		if (isPathUrl(finalLocation)) {
			if (!finalLocation.startsWith("/")) {
				String namespace = invocation.getProxy().getNamespace();
				if ((namespace != null) && (namespace.length() > 0)
						&& (!"/".equals(namespace))) {
					finalLocation = namespace + "/" + finalLocation;
				} else {
					finalLocation = "/" + finalLocation;
				}
			}
			if (prependServletContext && (request.getContextPath() != null)
					&& (request.getContextPath().length() > 0)) {
				finalLocation = request.getContextPath() + finalLocation;
			}
			finalLocation = response.encodeRedirectURL(finalLocation);
		}
		
		finalLocation = finalLocation.replace(",", "&");
		response.setContentType("text/html; charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		
		HttpSession session = request.getSession();
		
		String daField = null;
		if(request.getAttribute("DAField") != null) daField = request.getAttribute("DAField").toString();
		String daInstrument = null;
		if(request.getAttribute("DAInstrument") != null) daInstrument = request.getAttribute("DAInstrument").toString();
		
		String lastLoginTime = null;
		if(session.getAttribute("lastLoginTime") != null) {
			lastLoginTime = session.getAttribute("lastLoginTime").toString();
		}
		
		PrintWriter out = response.getWriter();
		StringBuffer buffer = new StringBuffer();
		String message = "Update success";
		boolean showalert = true;
		if(invocation.getResultCode().equalsIgnoreCase(ActionSupport.SUCCESS)) {
			message = "Completed";
		}
		buffer.append("<script type=\"text/javascript\">");
		if(showalert) {
			   buffer.append("alert('" + message + "');");
		}
		buffer.append("location='" + finalLocation + "';");
		buffer.append("</script>");
		String s = buffer.toString();
		//if (log.isDebugEnabled()) {
		//	log.debug(s);
		//}
		out.println(s);
		out.flush();
		out.close();

	}
}
