/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pns.kiam.controllers.app;

import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import pns.kiam.sweb.controllers.app.XXParserSWEB;

/**
 *
 * @author User
 */
@Named
@RequestScoped
public class MenuControl {

    /**
     * Creates a new instance of MenuControl
     */
    @EJB
    private XXParserSWEB xxparser;

    public MenuControl() {
    }

    public String navigste(String gotoURL) {
        System.out.println("      (xxparser.getSsessionControl().getSession() finished)  " + (xxparser.getSsessionControl().isFinished()));

        if (xxparser.getSsessionControl().getSession() == null) {
            return "/index";
        }
        return "/index";
        //return gotoURL;
    }
}
