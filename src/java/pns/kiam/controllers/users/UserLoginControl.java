/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pns.kiam.controllers.users;

import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Singleton;
import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;
import pns.kiam.controllers.app.ConfigControl;
import pns.kiam.sweb.controllers.app.SsessionControl;

/**
 *
 * @author User
 */
@SessionScoped
@Named
public class UserLoginControl implements Serializable {

    @EJB
    private ConfigControl configControl;
    @Inject
    private SsessionControl ssessionCTRL;

    private boolean loginned = false;
    private boolean isSuperUser = false;

    private String login = "";
    private String password = "";

    /**
     * Get the value of password
     *
     * @return the value of password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set the value of password
     *
     * @param password new value of password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Get the value of login
     *
     * @return the value of login
     */
    public String getLogin() {
        return login;
    }

    /**
     * Set the value of login
     *
     * @param login new value of login
     */
    public void setLogin(String login) {
        this.login = login;
    }

    /**
     * Get the value of loginned
     *
     * @return the value of loginned
     */
    public boolean isLoginned() {
        return loginned;
    }

    public boolean isIsSuperUser() {
        return isSuperUser;
    }

    /**
     * Set the value of loginned
     *
     * @param loginned new value of loginned
     */
    public void setLoginned(boolean loginned) {
        this.loginned = loginned;
    }

    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
    public void testLogin() {

        if (login.equals(configControl.getConfigADMLogin()) && password.equals(configControl.getConfigAdmPassword())) {
            loginned = true;
            isSuperUser = true;
            ssessionCTRL.init();
            ssessionCTRL.setTimeout(30);
            System.out.println(" ssessionCTRL.getSession() == null  =  "
                    + (ssessionCTRL.getSession() == null) + "  " + ""
                    + "  ssessionCTRL.getSession().getId() == null  "
                    + (ssessionCTRL.getSession().getId() == null) + "   "
                    + ssessionCTRL.getSession().getId() + "  / "
                    + ssessionCTRL.getSession().getMaxInactiveInterval() + "   "
                    + ssessionCTRL.isActive()
            );

//            return;
        }
    }

    public String deLogin() {
        login = password = "";
        loginned = false;

        configControl.sessionKill();
        return "/index.xhtml?faces-redirect=true";
    }
}
